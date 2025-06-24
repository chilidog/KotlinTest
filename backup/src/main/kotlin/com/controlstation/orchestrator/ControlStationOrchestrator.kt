package com.controlstation.orchestrator

import com.controlstation.communication.*
import com.controlstation.mavlink.*
import com.controlstation.safety.*
import com.controlstation.flight.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import java.util.UUID

/**
 * Main orchestrator that coordinates all subsystems of the control station
 * Enhanced with multi-protocol communication support
 */
class ControlStationOrchestrator(
    private val configuration: ControlStationConfig
) {
    private val logger = LoggerFactory.getLogger(ControlStationOrchestrator::class.java)
    
    private val isRunning = AtomicBoolean(false)
    private val orchestratorScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    // Enhanced communication subsystems
    private lateinit var webSocketModule: WebSocketCommunicationModule
    private lateinit var mavlinkModule: MockMAVLinkModule
    private lateinit var protocolAdapter: ProtocolAdapter
    private lateinit var unifiedCommunicationManager: UnifiedCommunicationManager
    
    // Core subsystems
    private lateinit var flightController: EnhancedFlightController
    private lateinit var safetyModule: EnhancedSafetyModule
    
    // System status
    private val _systemStatus = MutableStateFlow(SystemStatus.INITIALIZING)
    val systemStatus: StateFlow<SystemStatus> = _systemStatus.asStateFlow()
    
    private val _missionStatus = MutableStateFlow(MissionStatus.IDLE)
    val missionStatus: StateFlow<MissionStatus> = _missionStatus.asStateFlow()
    
    private val _protocolStatus = MutableStateFlow<Map<String, Any>>(emptyMap())
    val protocolStatus: StateFlow<Map<String, Any>> = _protocolStatus.asStateFlow()
    
    /**
     * Initialize and start all subsystems with multi-protocol support
     */
    suspend fun start() {
        if (isRunning.compareAndSet(false, true)) {
            logger.info("Starting Enhanced ControlStation with configuration: ${configuration.environment}")
            logger.info("Communication mode: ${configuration.communicationMode}")
            
            try {
                initializeSubsystems()
                startSubsystems()
                setupSystemMonitoring()
                
                _systemStatus.value = SystemStatus.OPERATIONAL
                logger.info("Enhanced ControlStation fully operational with multi-protocol support")
                
            } catch (e: Exception) {
                logger.error("Failed to start Enhanced ControlStation", e)
                _systemStatus.value = SystemStatus.ERROR
                stop()
                throw e
            }
        }
    }
    
    /**
     * Stop all subsystems gracefully with multi-protocol cleanup
     */
    suspend fun stop() {
        if (isRunning.compareAndSet(true, false)) {
            logger.info("Stopping Enhanced ControlStation")
            
            _systemStatus.value = SystemStatus.SHUTTING_DOWN
            
            try {
                // Stop subsystems in reverse order
                safetyModule.stop()
                unifiedCommunicationManager.disconnect()
                protocolAdapter.stopBridge()
                
                // Cancel all coroutines
                orchestratorScope.cancel()
                
                _systemStatus.value = SystemStatus.STOPPED
                logger.info("Enhanced ControlStation stopped successfully")
                
            } catch (e: Exception) {
                logger.error("Error during shutdown", e)
                _systemStatus.value = SystemStatus.ERROR
            }
        }
    }
    
    /**
     * Execute a predefined mission
     */
    suspend fun executeMission(missionType: MissionType = MissionType.BASIC_FLIGHT) {
        if (_systemStatus.value != SystemStatus.OPERATIONAL) {
            logger.error("Cannot execute mission: system not operational")
            return
        }
        
        _missionStatus.value = MissionStatus.EXECUTING
        logger.info("Starting mission: $missionType")
        
        try {
            when (missionType) {
                MissionType.BASIC_FLIGHT -> executeBasicFlightMission()
                MissionType.AUTONOMOUS_PATROL -> executePatrolMission()
                MissionType.EMERGENCY_RESPONSE -> executeEmergencyMission()
            }
            
            _missionStatus.value = MissionStatus.COMPLETED
            logger.info("Mission completed successfully")
            
        } catch (e: Exception) {
            logger.error("Mission failed", e)
            _missionStatus.value = MissionStatus.FAILED
            
            // Trigger emergency landing
            flightController.performEmergencyLanding("Mission failure: ${e.message}")
        }
    }
    
    /**
     * Send a command to the drone
     */
    suspend fun sendDroneCommand(
        command: String,
        parameters: Map<String, Any> = emptyMap(),
        priority: CommandPriority = CommandPriority.NORMAL
    ): Boolean {
        val droneCommand = DroneCommand(
            id = UUID.randomUUID().toString(),
            command = command,
            parameters = parameters,
            priority = priority
        )
        
        return flightController.sendCommand(droneCommand)
    }
    
    /**
     * Get comprehensive system status
     */
    fun getSystemStatus(): ComprehensiveSystemStatus {
        val communicationHealth = communicationModule.getConnectionHealth()
        val flightStatus = flightController.getStatus()
        val safetySummary = safetyModule.getSafetySummary()
        
        return ComprehensiveSystemStatus(
            systemStatus = _systemStatus.value,
            missionStatus = _missionStatus.value,
            communicationHealth = communicationHealth,
            flightControllerStatus = flightStatus,
            safetySummary = safetySummary,
            configuration = configuration
        )
    }
    
    private suspend fun initializeSubsystems() {
        logger.info("Initializing enhanced subsystems with multi-protocol support...")
        
        // Initialize WebSocket communication module
        val wsUrl = when (configuration.environment) {
            Environment.ALPINE -> "ws://localhost:8080"
            Environment.CACHYOS -> "ws://controlstation.local:8080"
            Environment.UBUNTU -> "ws://production.domain:8080"
        }
        
        webSocketModule = WebSocketCommunicationModule(
            baseUrl = wsUrl,
            reconnectDelayMs = 5000,
            maxReconnectAttempts = if (configuration.flightMode == FlightMode.REAL) 20 else 10,
            heartbeatIntervalMs = 30000,
            telemetryIntervalMs = 1000
        )
        
        // Initialize MAVLink communication module if enabled
        val mavlinkConfig = MAVLinkConnectionConfig(
            connectionType = when (configuration.environment) {
                Environment.ALPINE -> MAVLinkConnectionType.TCP
                Environment.CACHYOS -> MAVLinkConnectionType.SERIAL
                Environment.UBUNTU -> MAVLinkConnectionType.SERIAL
            },
            connectionString = when (configuration.environment) {
                Environment.ALPINE -> "127.0.0.1:5760"
                Environment.CACHYOS -> "/dev/ttyUSB0"
                Environment.UBUNTU -> "/dev/ttyACM0"
            },
            baudRate = 57600,
            systemId = 255,
            componentId = 190
        )
        
        mavlinkModule = MockMAVLinkModule()
        
        // Initialize protocol adapter
        protocolAdapter = ProtocolAdapter(
            webSocketProtocol = webSocketModule,
            mavlinkProtocol = mavlinkModule
        )
        
        // Initialize unified communication manager
        val communicationProtocol = when (configuration.communicationMode) {
            CommunicationMode.WEBSOCKET_ONLY -> webSocketModule
            CommunicationMode.MAVLINK_ONLY -> mavlinkModule
            CommunicationMode.UNIFIED -> {
                unifiedCommunicationManager = UnifiedCommunicationManager(
                    webSocketProtocol = webSocketModule,
                    mavlinkProtocol = mavlinkModule,
                    protocolAdapter = protocolAdapter
                )
                unifiedCommunicationManager
            }
        }
        
        // Initialize flight controller based on configuration
        flightController = when (configuration.flightMode) {
            FlightMode.REAL -> EnhancedRealFlightController()
            FlightMode.SIMULATED -> EnhancedSimulatedFlightController()
            FlightMode.HYBRID -> EnhancedSimulatedFlightController() // Use simulated for hybrid for now
        }
        
        // Initialize enhanced safety module with multi-protocol support
        safetyModule = EnhancedSafetyModule(
            communicationModule = communicationProtocol,
            emergencyLandingCallback = { reason ->
                logger.error("SAFETY TRIGGER: Emergency landing - $reason")
                flightController.performEmergencyLanding(reason)
            },
            warningCallback = { warning ->
                logger.warn("SAFETY WARNING: $warning")
            }
        )
        
        logger.info("Subsystems initialized")
    }
    
    private suspend fun startSubsystems() {
        logger.info("Starting enhanced subsystems...")
        
        // Start communication based on mode
        when (configuration.communicationMode) {
            CommunicationMode.WEBSOCKET_ONLY -> {
                webSocketModule.start(orchestratorScope)
                waitForConnection(webSocketModule, "WebSocket")
            }
            CommunicationMode.MAVLINK_ONLY -> {
                mavlinkModule.connect(mavlinkModule.getConnectionInfo())
                waitForConnection(mavlinkModule, "MAVLink")
            }
            CommunicationMode.UNIFIED -> {
                val connectionString = buildUnifiedConnectionString()
                unifiedCommunicationManager.connect(connectionString)
                waitForConnection(unifiedCommunicationManager, "Unified")
            }
        }
        
        // Initialize flight controller with appropriate communication module
        val communicationProtocol = when (configuration.communicationMode) {
            CommunicationMode.WEBSOCKET_ONLY -> webSocketModule
            CommunicationMode.MAVLINK_ONLY -> mavlinkModule
            CommunicationMode.UNIFIED -> unifiedCommunicationManager
        }
        
        flightController.initialize(communicationProtocol)
        
        // Start safety monitoring
        safetyModule.start(orchestratorScope)
        
        logger.info("All enhanced subsystems started")
    }
    
    private suspend fun waitForConnection(protocol: CommunicationProtocol, name: String) {
        var connectionWaitTime = 0
        while (!protocol.isConnected() && connectionWaitTime < 30000) {
            delay(1000)
            connectionWaitTime += 1000
        }
        
        if (!protocol.isConnected()) {
            logger.warn("$name protocol not connected after 30s, continuing with limited functionality")
        } else {
            logger.info("$name protocol connected successfully")
        }
    }
    
    private fun buildUnifiedConnectionString(): String {
        val wsUrl = when (configuration.environment) {
            Environment.ALPINE -> "ws://localhost:8080"
            Environment.CACHYOS -> "ws://controlstation.local:8080"
            Environment.UBUNTU -> "ws://production.domain:8080"
        }
        
        val mavlinkConnection = when (configuration.environment) {
            Environment.ALPINE -> "127.0.0.1:5760"
            Environment.CACHYOS -> "/dev/ttyUSB0"
            Environment.UBUNTU -> "/dev/ttyACM0"
        }
        
        return "WebSocket: $wsUrl, MAVLink: $mavlinkConnection"
    }
    
    private fun setupSystemMonitoring() {
        // Monitor safety alerts
        orchestratorScope.launch {
            safetyModule.safetyAlerts.collect { alert ->
                logger.info("Safety Alert: ${alert.level} - ${alert.event} - ${alert.message}")
                
                if (alert.level == SafetyLevel.CRITICAL) {
                    _systemStatus.value = SystemStatus.CRITICAL
                }
            }
        }
        
        // Monitor protocol status
        orchestratorScope.launch {
            when (configuration.communicationMode) {
                CommunicationMode.UNIFIED -> {
                    unifiedCommunicationManager.getHealthFlow().collect { health ->
                        _protocolStatus.value = unifiedCommunicationManager.getProtocolStatus()
                        if (health == ConnectionHealth.DISCONNECTED && _systemStatus.value == SystemStatus.OPERATIONAL) {
                            logger.warn("All communication protocols lost during operation")
                        }
                    }
                }
                else -> {
                    // Monitor individual protocols
                    val protocol = when (configuration.communicationMode) {
                        CommunicationMode.WEBSOCKET_ONLY -> webSocketModule
                        CommunicationMode.MAVLINK_ONLY -> mavlinkModule
                        else -> null
                    }
                    
                    protocol?.getHealthFlow()?.collect { health ->
                        if (health == ConnectionHealth.DISCONNECTED && _systemStatus.value == SystemStatus.OPERATIONAL) {
                            logger.warn("Communication lost during operation")
                        }
                    }
                }
            }
        }
        
        // Periodic system health check
        orchestratorScope.launch {
            while (isRunning.get()) {
                performSystemHealthCheck()
                delay(10000) // Check every 10 seconds
            }
        }
    }
    
    private fun performSystemHealthCheck() {
        val status = getSystemStatus()
        
        logger.debug("System Health Check:")
        logger.debug("- System Status: ${status.systemStatus}")
        logger.debug("- Communication: ${if (status.communicationHealth.isConnected) "Connected" else "Disconnected"}")
        logger.debug("- Flight Controller: ${if (status.flightControllerStatus.isConnected) "Connected" else "Disconnected"}")
        logger.debug("- Safety Status: ${status.safetySummary.status}")
        
        // Log any issues
        if (status.communicationHealth.errors.isNotEmpty()) {
            logger.warn("Communication errors: ${status.communicationHealth.errors.takeLast(3)}")
        }
    }
    
    private suspend fun executeBasicFlightMission() {
        logger.info("Executing basic flight mission")
        
        // Pre-flight checks
        delay(2000)
        
        // Arm the drone
        sendDroneCommand("ARM")
        delay(2000)
        
        // Takeoff
        sendDroneCommand("TAKEOFF", mapOf("altitude" to 15.0))
        delay(5000)
        
        // Hover
        sendDroneCommand("HOVER")
        delay(5000)
        
        // Land
        sendDroneCommand("LAND")
        delay(5000)
        
        // Disarm
        sendDroneCommand("DISARM")
    }
    
    private suspend fun executePatrolMission() {
        logger.info("Executing autonomous patrol mission")
        
        sendDroneCommand("ARM")
        delay(2000)
        
        sendDroneCommand("START_MISSION")
        
        // Let the flight controller handle the autonomous mission
        delay(30000) // 30 second patrol
        
        sendDroneCommand("ABORT_MISSION")
        sendDroneCommand("LAND")
        delay(5000)
        sendDroneCommand("DISARM")
    }
    
    private suspend fun executeEmergencyMission() {
        logger.info("Executing emergency response mission")
        
        sendDroneCommand("ARM")
        delay(1000)
        
        sendDroneCommand("TAKEOFF", mapOf("altitude" to 30.0))
        delay(3000)
        
        // Navigate to emergency coordinates
        sendDroneCommand("GOTO", mapOf(
            "latitude" to 47.6062,
            "longitude" to -122.3321,
            "altitude" to 25.0
        ))
        delay(10000)
        
        // Hover for observation
        sendDroneCommand("HOVER")
        delay(10000)
        
        // Return and land
        sendDroneCommand("LAND")
        delay(5000)
        sendDroneCommand("DISARM")
    }
}

/**
 * Configuration and status data classes
 */
data class ControlStationConfig(
    val environment: Environment,
    val flightMode: FlightMode,
    val communicationMode: CommunicationMode = CommunicationMode.UNIFIED,
    val enableAdvancedFeatures: Boolean = true,
    val logLevel: String = "INFO"
)

enum class Environment {
    ALPINE,
    CACHYOS,
    UBUNTU
}

enum class CommunicationMode {
    WEBSOCKET_ONLY,
    MAVLINK_ONLY,
    UNIFIED
}

enum class FlightMode {
    REAL,
    SIMULATED,
    HYBRID
    }
}

enum class SystemStatus {
    INITIALIZING,
    OPERATIONAL,
    CRITICAL,
    ERROR,
    SHUTTING_DOWN,
    STOPPED
}

enum class MissionStatus {
    IDLE,
    EXECUTING,
    COMPLETED,
    FAILED,
    ABORTED
}

enum class MissionType {
    BASIC_FLIGHT,
    AUTONOMOUS_PATROL,
    EMERGENCY_RESPONSE
}

data class ComprehensiveSystemStatus(
    val systemStatus: SystemStatus,
    val missionStatus: MissionStatus,
    val communicationHealth: ConnectionHealth,
    val flightControllerStatus: FlightControllerStatus,
    val safetySummary: SafetySummary,
    val configuration: ControlStationConfig
)
