package com.controlstation.communication

import com.controlstation.mavlink.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * Unified Communication Manager that orchestrates multiple communication protocols.
 * Supports protocol detection, automatic failover, and concurrent multi-protocol operation.
 */
class UnifiedCommunicationManager(
    private val webSocketProtocol: WebSocketCommunicationModule,
    private val mavlinkProtocol: MAVLinkCommunicationModule? = null,
    private val protocolAdapter: ProtocolAdapter
) : CommunicationProtocol {
    
    private val logger = LoggerFactory.getLogger(UnifiedCommunicationManager::class.java)
    
    // State management
    private val isConnected = AtomicBoolean(false)
    private val primaryProtocol = AtomicReference<CommunicationProtocol?>(null)
    private val activeProtocols = mutableSetOf<CommunicationProtocol>()
    private var managementJob: Job? = null
    
    // Communication channels
    private val unifiedTelemetryChannel = Channel<Telemetry>(Channel.UNLIMITED)
    private val unifiedHealthChannel = Channel<ConnectionHealth>(Channel.UNLIMITED)
    private val commandChannel = Channel<Command>(Channel.UNLIMITED)
    
    // Configuration
    private var connectionConfig: UnifiedConnectionConfig? = null
    
    /**
     * Auto-detect available protocols and establish connections
     */
    override suspend fun connect(connectionString: String) {
        logger.info("Starting unified connection to: $connectionString")
        
        connectionConfig = parseConnectionString(connectionString)
        
        managementJob = CoroutineScope(Dispatchers.Default).launch {
            launch { connectProtocols() }
            launch { commandDistributor() }
            launch { telemetryAggregator() }
            launch { healthMonitor() }
            launch { protocolFailoverManager() }
        }
        
        // Start protocol adapter
        protocolAdapter.startBridge()
    }
    
    private suspend fun connectProtocols() {
        val config = connectionConfig ?: return
        
        logger.info("Connecting protocols: ${config.enabledProtocols}")
        
        // Connect WebSocket if enabled
        if (ProtocolType.WEBSOCKET in config.enabledProtocols) {
            try {
                webSocketProtocol.connect(config.webSocketUrl)
                if (webSocketProtocol.isConnected()) {
                    activeProtocols.add(webSocketProtocol)
                    logger.info("WebSocket protocol connected successfully")
                    
                    if (primaryProtocol.get() == null) {
                        primaryProtocol.set(webSocketProtocol)
                        logger.info("WebSocket set as primary protocol")
                    }
                }
            } catch (e: Exception) {
                logger.error("Failed to connect WebSocket protocol: ${e.message}")
            }
        }
        
        // Connect MAVLink if enabled and available
        if (ProtocolType.MAVLINK in config.enabledProtocols && mavlinkProtocol != null) {
            try {
                mavlinkProtocol.connect(config.mavlinkConnection)
                if (mavlinkProtocol.isConnected()) {
                    activeProtocols.add(mavlinkProtocol)
                    logger.info("MAVLink protocol connected successfully")
                    
                    // Prefer MAVLink as primary for real hardware
                    if (config.preferMAVLink || primaryProtocol.get() == null) {
                        primaryProtocol.set(mavlinkProtocol)
                        logger.info("MAVLink set as primary protocol")
                    }
                }
            } catch (e: Exception) {
                logger.error("Failed to connect MAVLink protocol: ${e.message}")
            }
        }
        
        // Update connection status
        isConnected.set(activeProtocols.isNotEmpty())
        
        if (isConnected.get()) {
            logger.info("Unified communication manager connected with ${activeProtocols.size} active protocols")
            publishHealth(ConnectionHealth.CONNECTED)
        } else {
            logger.error("Failed to establish any protocol connections")
            publishHealth(ConnectionHealth.DISCONNECTED)
        }
    }
    
    private suspend fun commandDistributor() {
        logger.info("Starting unified command distributor")
        
        try {
            for (command in commandChannel) {
                distributeCommand(command)
            }
        } catch (e: Exception) {
            logger.error("Error in command distributor: ${e.message}", e)
        }
        
        logger.info("Unified command distributor stopped")
    }
    
    private suspend fun distributeCommand(command: Command) {
        val config = connectionConfig ?: return
        
        try {
            when (config.commandDistributionMode) {
                CommandDistributionMode.PRIMARY_ONLY -> {
                    // Send command only to primary protocol
                    primaryProtocol.get()?.sendCommand(command)
                        ?: logger.warn("No primary protocol available for command: ${command.type}")
                }
                
                CommandDistributionMode.ALL_PROTOCOLS -> {
                    // Send command to all active protocols
                    activeProtocols.forEach { protocol ->
                        try {
                            protocol.sendCommand(command)
                        } catch (e: Exception) {
                            logger.error("Failed to send command via ${protocol.getProtocolType()}: ${e.message}")
                        }
                    }
                }
                
                CommandDistributionMode.PROTOCOL_SPECIFIC -> {
                    // Send command to specific protocol based on command type
                    val targetProtocol = selectProtocolForCommand(command)
                    targetProtocol?.sendCommand(command)
                        ?: logger.warn("No suitable protocol for command: ${command.type}")
                }
            }
        } catch (e: Exception) {
            logger.error("Error distributing command ${command.type}: ${e.message}", e)
        }
    }
    
    private fun selectProtocolForCommand(command: Command): CommunicationProtocol? {
        return when (command.type) {
            CommandType.EMERGENCY -> {
                // Emergency commands should go to the most reliable protocol
                activeProtocols.firstOrNull { it.getProtocolType() == ProtocolType.MAVLINK }
                    ?: primaryProtocol.get()
            }
            CommandType.SET_MODE -> {
                // Mode changes are better handled by MAVLink
                activeProtocols.firstOrNull { it.getProtocolType() == ProtocolType.MAVLINK }
                    ?: primaryProtocol.get()
            }
            else -> {
                // Default to primary protocol
                primaryProtocol.get()
            }
        }
    }
    
    private suspend fun telemetryAggregator() {
        logger.info("Starting unified telemetry aggregator")
        
        try {
            // Combine telemetry streams from all active protocols
            val telemetryFlows = activeProtocols.map { protocol ->
                protocol.getTelemetryFlow().map { telemetry ->
                    telemetry to protocol.getProtocolType()
                }
            }
            
            if (telemetryFlows.isNotEmpty()) {
                merge(*telemetryFlows.toTypedArray())
                    .collect { (telemetry, protocolType) ->
                        processTelemetry(telemetry, protocolType)
                    }
            }
        } catch (e: Exception) {
            logger.error("Error in telemetry aggregator: ${e.message}", e)
        }
        
        logger.info("Unified telemetry aggregator stopped")
    }
    
    private suspend fun processTelemetry(telemetry: Telemetry, source: ProtocolType) {
        try {
            // Add source information to telemetry
            val enhancedTelemetry = telemetry.copy(
                timestamp = System.currentTimeMillis()
            )
            
            unifiedTelemetryChannel.trySend(enhancedTelemetry)
            
            logger.debug("Processed telemetry from $source: $enhancedTelemetry")
        } catch (e: Exception) {
            logger.error("Error processing telemetry from $source: ${e.message}", e)
        }
    }
    
    private suspend fun healthMonitor() {
        logger.info("Starting unified health monitor")
        
        try {
            while (isConnected.get() && !currentCoroutineContext().job.isCancelled) {
                monitorProtocolHealth()
                delay(2000) // Check every 2 seconds
            }
        } catch (e: Exception) {
            logger.error("Error in health monitor: ${e.message}", e)
        }
        
        logger.info("Unified health monitor stopped")
    }
    
    private suspend fun monitorProtocolHealth() {
        val healthyProtocols = mutableSetOf<CommunicationProtocol>()
        val unhealthyProtocols = mutableSetOf<CommunicationProtocol>()
        
        activeProtocols.forEach { protocol ->
            if (protocol.isConnected()) {
                healthyProtocols.add(protocol)
            } else {
                unhealthyProtocols.add(protocol)
                logger.warn("Protocol ${protocol.getProtocolType()} is not connected")
            }
        }
        
        // Remove disconnected protocols
        activeProtocols.removeAll(unhealthyProtocols)
        
        // Update overall health status
        val overallHealth = when {
            healthyProtocols.isEmpty() -> ConnectionHealth.DISCONNECTED
            healthyProtocols.size < activeProtocols.size -> ConnectionHealth.DEGRADED
            else -> ConnectionHealth.CONNECTED
        }
        
        publishHealth(overallHealth)
        
        // Update connection status
        isConnected.set(healthyProtocols.isNotEmpty())
    }
    
    private suspend fun protocolFailoverManager() {
        logger.info("Starting protocol failover manager")
        
        try {
            while (isConnected.get() && !currentCoroutineContext().job.isCancelled) {
                checkAndHandleFailover()
                delay(5000) // Check every 5 seconds
            }
        } catch (e: Exception) {
            logger.error("Error in failover manager: ${e.message}", e)
        }
        
        logger.info("Protocol failover manager stopped")
    }
    
    private suspend fun checkAndHandleFailover() {
        val currentPrimary = primaryProtocol.get()
        
        if (currentPrimary == null || !currentPrimary.isConnected()) {
            logger.warn("Primary protocol is not available, attempting failover")
            
            // Find a healthy protocol to use as primary
            val newPrimary = activeProtocols.firstOrNull { it.isConnected() }
            
            if (newPrimary != null) {
                primaryProtocol.set(newPrimary)
                logger.info("Failover completed: ${newPrimary.getProtocolType()} is now primary")
            } else {
                logger.error("No healthy protocols available for failover")
                primaryProtocol.set(null)
            }
        }
    }
    
    private suspend fun publishHealth(health: ConnectionHealth) {
        unifiedHealthChannel.trySend(health)
    }
    
    private fun parseConnectionString(connectionString: String): UnifiedConnectionConfig {
        // Parse connection string format: "unified://websocket=ws://localhost:8080;mavlink=/dev/ttyUSB0:57600"
        val config = UnifiedConnectionConfig()
        
        if (connectionString.startsWith("unified://")) {
            val params = connectionString.removePrefix("unified://").split(";")
            
            params.forEach { param ->
                val (key, value) = param.split("=", limit = 2)
                when (key.lowercase()) {
                    "websocket" -> {
                        config.webSocketUrl = value
                        config.enabledProtocols.add(ProtocolType.WEBSOCKET)
                    }
                    "mavlink" -> {
                        config.mavlinkConnection = value
                        config.enabledProtocols.add(ProtocolType.MAVLINK)
                    }
                    "primary" -> {
                        config.preferMAVLink = value.lowercase() == "mavlink"
                    }
                    "distribution" -> {
                        config.commandDistributionMode = CommandDistributionMode.valueOf(value.uppercase())
                    }
                }
            }
        } else {
            // Single protocol connection
            if (connectionString.startsWith("ws://") || connectionString.startsWith("wss://")) {
                config.webSocketUrl = connectionString
                config.enabledProtocols.add(ProtocolType.WEBSOCKET)
            } else {
                config.mavlinkConnection = connectionString
                config.enabledProtocols.add(ProtocolType.MAVLINK)
            }
        }
        
        return config
    }
    
    override suspend fun sendCommand(command: Command) {
        if (!isConnected.get()) {
            throw IllegalStateException("Unified communication manager not connected")
        }
        commandChannel.trySend(command)
    }
    
    override fun getTelemetryFlow(): Flow<Telemetry> {
        return unifiedTelemetryChannel.receiveAsFlow()
    }
    
    override fun getHealthFlow(): Flow<ConnectionHealth> {
        return unifiedHealthChannel.receiveAsFlow()
    }
    
    override fun isConnected(): Boolean = isConnected.get()
    
    override suspend fun disconnect() {
        logger.info("Disconnecting unified communication manager")
        
        isConnected.set(false)
        managementJob?.cancel()
        protocolAdapter.stopBridge()
        
        // Disconnect all protocols
        activeProtocols.forEach { protocol ->
            try {
                protocol.disconnect()
            } catch (e: Exception) {
                logger.error("Error disconnecting ${protocol.getProtocolType()}: ${e.message}")
            }
        }
        
        activeProtocols.clear()
        primaryProtocol.set(null)
        publishHealth(ConnectionHealth.DISCONNECTED)
    }
    
    override fun getProtocolType(): ProtocolType = ProtocolType.UNIFIED
    
    override fun getConnectionInfo(): String {
        val activeInfo = activeProtocols.joinToString(", ") { "${it.getProtocolType()}: ${it.getConnectionInfo()}" }
        return "Unified Communication Manager - Active Protocols: [$activeInfo]"
    }
    
    /**
     * Get detailed status of all protocols
     */
    fun getProtocolStatus(): Map<String, Any> {
        return mapOf(
            "unified_connected" to isConnected.get(),
            "primary_protocol" to (primaryProtocol.get()?.getProtocolType()?.name ?: "NONE"),
            "active_protocols" to activeProtocols.map { it.getProtocolType().name },
            "websocket_status" to mapOf(
                "connected" to webSocketProtocol.isConnected(),
                "info" to webSocketProtocol.getConnectionInfo()
            ),
            "mavlink_status" to if (mavlinkProtocol != null) mapOf(
                "connected" to mavlinkProtocol.isConnected(),
                "info" to mavlinkProtocol.getConnectionInfo(),
                "stats" to mavlinkProtocol.getStats(),
                "system_info" to mavlinkProtocol.getSystemInfo()
            ) else null,
            "adapter_stats" to protocolAdapter.getTranslationStats(),
            "timestamp" to System.currentTimeMillis()
        )
    }
    
    /**
     * Force protocol switch
     */
    suspend fun switchPrimaryProtocol(protocolType: ProtocolType) {
        val targetProtocol = activeProtocols.firstOrNull { it.getProtocolType() == protocolType }
        
        if (targetProtocol != null && targetProtocol.isConnected()) {
            primaryProtocol.set(targetProtocol)
            logger.info("Manually switched primary protocol to: $protocolType")
        } else {
            throw IllegalArgumentException("Protocol $protocolType is not available or not connected")
        }
    }
}

/**
 * Configuration for unified communication manager
 */
data class UnifiedConnectionConfig(
    var webSocketUrl: String = "ws://localhost:8080",
    var mavlinkConnection: String = "/dev/ttyUSB0",
    var enabledProtocols: MutableSet<ProtocolType> = mutableSetOf(),
    var preferMAVLink: Boolean = false,
    var commandDistributionMode: CommandDistributionMode = CommandDistributionMode.PRIMARY_ONLY
)

/**
 * Command distribution modes
 */
enum class CommandDistributionMode {
    PRIMARY_ONLY,    // Send commands only to primary protocol
    ALL_PROTOCOLS,   // Send commands to all active protocols
    PROTOCOL_SPECIFIC // Choose protocol based on command type
}
