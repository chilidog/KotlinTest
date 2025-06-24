package com.controlstation.communication

import com.controlstation.mavlink.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Protocol adapter that bridges WebSocket JSON and MAVLink binary protocols.
 * Enables bidirectional message translation and protocol multiplexing.
 */
class ProtocolAdapter(
    private val webSocketProtocol: WebSocketCommunicationModule? = null,
    private val mavlinkProtocol: MockMAVLinkModule? = null
) {
    
    private val logger = LoggerFactory.getLogger(ProtocolAdapter::class.java)
    private val mapper = jacksonObjectMapper()
    private val isActive = AtomicBoolean(false)
    private var bridgeJob: Job? = null
    
    // Translation channels
    private val webSocketToMAVLink = Channel<Command>(Channel.UNLIMITED)
    private val mavlinkToWebSocket = Channel<Telemetry>(Channel.UNLIMITED)
    
    /**
     * Start the protocol bridge
     */
    suspend fun startBridge() {
        if (isActive.get()) {
            logger.warn("Protocol bridge already active")
            return
        }
        
        logger.info("Starting protocol bridge between WebSocket and MAVLink")
        isActive.set(true)
        
        bridgeJob = CoroutineScope(Dispatchers.Default).launch {
            launch { bridgeWebSocketToMAVLink() }
            launch { bridgeMAVLinkToWebSocket() }
            launch { synchronizeTelemetry() }
        }
    }
    
    /**
     * Stop the protocol bridge
     */
    suspend fun stopBridge() {
        logger.info("Stopping protocol bridge")
        isActive.set(false)
        bridgeJob?.cancel()
        bridgeJob = null
    }
    
    /**
     * Bridge commands from WebSocket to MAVLink
     */
    private suspend fun bridgeWebSocketToMAVLink() {
        if (webSocketProtocol == null || mavlinkProtocol == null) return
        
        try {
            // Monitor WebSocket commands and forward to MAVLink
            webSocketProtocol.getTelemetryFlow()
                .collect { telemetry ->
                    // In a real implementation, this would monitor for command patterns
                    // For now, we'll focus on telemetry bridging
                }
        } catch (e: Exception) {
            logger.error("Error bridging WebSocket to MAVLink: ${e.message}", e)
        }
    }
    
    /**
     * Bridge telemetry from MAVLink to WebSocket
     */
    private suspend fun bridgeMAVLinkToWebSocket() {
        if (webSocketProtocol == null || mavlinkProtocol == null) return
        
        try {
            mavlinkProtocol.getTelemetryFlow()
                .collect { mavlinkTelemetry ->
                    val webSocketTelemetry = translateMAVLinkToWebSocket(mavlinkTelemetry)
                    // In a real implementation, you might inject this into WebSocket protocol
                    logger.debug("Translated MAVLink telemetry to WebSocket format: $webSocketTelemetry")
                }
        } catch (e: Exception) {
            logger.error("Error bridging MAVLink to WebSocket: ${e.message}", e)
        }
    }
    
    /**
     * Synchronize telemetry between protocols
     */
    private suspend fun synchronizeTelemetry() {
        if (webSocketProtocol == null || mavlinkProtocol == null) return
        
        try {
            // Combine telemetry from both protocols
            combine(
                webSocketProtocol.getTelemetryFlow(),
                mavlinkProtocol.getTelemetryFlow()
            ) { webSocketTelemetry, mavlinkTelemetry ->
                mergeTelemetry(webSocketTelemetry, mavlinkTelemetry)
            }.collect { mergedTelemetry ->
                logger.debug("Merged telemetry: $mergedTelemetry")
                // Merged telemetry could be used by unified communication manager
            }
        } catch (e: Exception) {
            logger.error("Error synchronizing telemetry: ${e.message}", e)
        }
    }
    
    /**
     * Translate WebSocket command to MAVLink format
     */
    fun translateWebSocketToMAVLink(command: Command): Command {
        // For most cases, commands are already in a compatible format
        // Additional translation logic can be added here if needed
        return when (command.type) {
            CommandType.TAKEOFF -> command.copy(
                parameters = command.parameters + mapOf(
                    "mavlink_command" to "MAV_CMD_NAV_TAKEOFF",
                    "confirmation" to "0"
                )
            )
            CommandType.LAND -> command.copy(
                parameters = command.parameters + mapOf(
                    "mavlink_command" to "MAV_CMD_NAV_LAND",
                    "confirmation" to "0"
                )
            )
            CommandType.HOVER -> command.copy(
                parameters = command.parameters + mapOf(
                    "mavlink_command" to "MAV_CMD_NAV_LOITER_UNLIM",
                    "confirmation" to "0"
                )
            )
            CommandType.EMERGENCY -> command.copy(
                parameters = command.parameters + mapOf(
                    "mavlink_command" to "MAV_CMD_COMPONENT_ARM_DISARM",
                    "param1" to "0", // Disarm
                    "param2" to "21196", // Force disarm magic number
                    "confirmation" to "0"
                )
            )
            CommandType.SET_MODE -> command.copy(
                parameters = command.parameters + mapOf(
                    "mavlink_command" to "MAV_CMD_DO_SET_MODE",
                    "confirmation" to "0"
                )
            )
        }
    }
    
    /**
     * Translate MAVLink telemetry to WebSocket JSON format
     */
    fun translateMAVLinkToWebSocket(telemetry: Telemetry): Map<String, Any> {
        return mapOf(
            "type" to "telemetry",
            "data" to mapOf(
                "altitude" to telemetry.altitude,
                "battery_level" to telemetry.batteryLevel,
                "gps_status" to telemetry.gpsStatus,
                "flight_mode" to telemetry.flightMode,
                "timestamp" to telemetry.timestamp,
                "source" to "mavlink"
            )
        )
    }
    
    /**
     * Translate WebSocket JSON to MAVLink telemetry format
     */
    fun translateWebSocketToMAVLink(jsonData: Map<String, Any>): Telemetry? {
        return try {
            val data = jsonData["data"] as? Map<String, Any> ?: return null
            
            Telemetry(
                altitude = (data["altitude"] as? Number)?.toDouble() ?: 0.0,
                batteryLevel = (data["battery_level"] as? Number)?.toDouble() ?: 0.0,
                gpsStatus = data["gps_status"] as? String ?: "UNKNOWN",
                flightMode = data["flight_mode"] as? String ?: "UNKNOWN",
                timestamp = (data["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            logger.error("Error translating WebSocket JSON to telemetry: ${e.message}")
            null
        }
    }
    
    /**
     * Merge telemetry from different protocols, preferring more recent data
     */
    fun mergeTelemetry(webSocketTelemetry: Telemetry, mavlinkTelemetry: Telemetry): Telemetry {
        return when {
            webSocketTelemetry.timestamp > mavlinkTelemetry.timestamp -> {
                // Use WebSocket as primary, fill gaps with MAVLink
                webSocketTelemetry.copy(
                    gpsStatus = if (webSocketTelemetry.gpsStatus == "UNKNOWN") mavlinkTelemetry.gpsStatus else webSocketTelemetry.gpsStatus,
                    flightMode = if (webSocketTelemetry.flightMode == "UNKNOWN") mavlinkTelemetry.flightMode else webSocketTelemetry.flightMode
                )
            }
            else -> {
                // Use MAVLink as primary, fill gaps with WebSocket
                mavlinkTelemetry.copy(
                    altitude = if (mavlinkTelemetry.altitude == 0.0) webSocketTelemetry.altitude else mavlinkTelemetry.altitude,
                    batteryLevel = if (mavlinkTelemetry.batteryLevel == 0.0) webSocketTelemetry.batteryLevel else mavlinkTelemetry.batteryLevel
                )
            }
        }
    }
    
    /**
     * Create protocol-specific connection health message
     */
    fun translateConnectionHealth(health: ConnectionHealth, protocol: ProtocolType): Map<String, Any> {
        return mapOf(
            "type" to "connection_health",
            "protocol" to protocol.name,
            "status" to health.name,
            "timestamp" to System.currentTimeMillis()
        )
    }
    
    /**
     * Convert command to protocol-neutral format
     */
    fun normalizeCommand(command: Command, sourceProtocol: ProtocolType): Command {
        return command.copy(
            parameters = command.parameters + mapOf(
                "source_protocol" to sourceProtocol.name,
                "normalized_at" to System.currentTimeMillis().toString()
            )
        )
    }
    
    /**
     * Validate command compatibility between protocols
     */
    fun isCommandCompatible(command: Command, targetProtocol: ProtocolType): Boolean {
        return when (targetProtocol) {
            ProtocolType.MAVLINK -> {
                // All current command types are supported by MAVLink
                true
            }
            ProtocolType.WEBSOCKET -> {
                // All command types can be serialized to JSON
                true
            }
            ProtocolType.UNIFIED -> {
                // Unified protocol supports all commands
                true
            }
        }
    }
    
    /**
     * Get translation statistics
     */
    fun getTranslationStats(): Map<String, Any> {
        return mapOf(
            "bridge_active" to isActive.get(),
            "websocket_available" to (webSocketProtocol != null),
            "mavlink_available" to (mavlinkProtocol != null),
            "timestamp" to System.currentTimeMillis()
        )
    }
}
