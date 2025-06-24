package com.controlstation.communication

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

/**
 * Enum definitions for the ControlStation system
 */
enum class Environment {
    ALPINE, CACHYOS, UBUNTU
}

enum class FlightMode {
    REAL, SIMULATED, HYBRID
}

enum class CommunicationMode {
    WEBSOCKET_ONLY, MAVLINK_ONLY, UNIFIED
}

enum class MissionType {
    BASIC_FLIGHT, AUTONOMOUS_PATROL, EMERGENCY_RESPONSE
}

enum class CommandType {
    ARM, DISARM, TAKEOFF, LAND, MOVE_TO, SET_MODE, EMERGENCY_STOP
}

enum class ConnectionStatus {
    CONNECTED, DISCONNECTED, DEGRADED
}

/**
 * Configuration classes
 */
data class ControlStationConfig(
    val environment: Environment,
    val flightMode: FlightMode,
    val communicationMode: CommunicationMode,
    val enableAdvancedFeatures: Boolean = true,
    val logLevel: String = "INFO"
)

/**
 * Command and Telemetry classes (using both naming conventions for compatibility)
 */
typealias Command = DroneCommand
typealias Telemetry = TelemetryData

/**
 * Data models for drone communication
 */
data class TelemetryData(
    @JsonProperty("timestamp") val timestamp: Long = System.currentTimeMillis(),
    @JsonProperty("position") val position: Position,
    @JsonProperty("velocity") val velocity: Velocity,
    @JsonProperty("battery") val battery: BatteryStatus,
    @JsonProperty("gps") val gps: GpsStatus,
    @JsonProperty("altitude") val altitude: Double,
    @JsonProperty("heading") val heading: Double,
    @JsonProperty("flight_mode") val flightMode: String,
    @JsonProperty("armed") val armed: Boolean
)

data class Position(
    @JsonProperty("lat") val latitude: Double,
    @JsonProperty("lon") val longitude: Double,
    @JsonProperty("alt") val altitude: Double
)

data class Velocity(
    @JsonProperty("vx") val velocityX: Double,
    @JsonProperty("vy") val velocityY: Double,
    @JsonProperty("vz") val velocityZ: Double,
    @JsonProperty("speed") val speed: Double
)

data class BatteryStatus(
    @JsonProperty("voltage") val voltage: Double,
    @JsonProperty("current") val current: Double,
    @JsonProperty("percentage") val percentage: Int,
    @JsonProperty("remaining_time") val remainingTimeMinutes: Int
)

data class GpsStatus(
    @JsonProperty("fix_type") val fixType: Int,
    @JsonProperty("satellites") val satelliteCount: Int,
    @JsonProperty("hdop") val horizontalDilution: Double,
    @JsonProperty("signal_strength") val signalStrength: Int
)

data class DroneCommand(
    @JsonProperty("id") val id: String,
    @JsonProperty("timestamp") val timestamp: Long = System.currentTimeMillis(),
    @JsonProperty("command") val command: String,
    @JsonProperty("type") val type: CommandType,
    @JsonProperty("parameters") val parameters: Map<String, Any> = emptyMap(),
    @JsonProperty("priority") val priority: CommandPriority = CommandPriority.NORMAL
)

enum class CommandPriority {
    @JsonProperty("emergency") EMERGENCY,
    @JsonProperty("high") HIGH,
    @JsonProperty("normal") NORMAL,
    @JsonProperty("low") LOW
}

data class CommandResponse(
    @JsonProperty("command_id") val commandId: String,
    @JsonProperty("status") val status: CommandStatus,
    @JsonProperty("message") val message: String,
    @JsonProperty("timestamp") val timestamp: Long = System.currentTimeMillis()
)

enum class CommandStatus {
    @JsonProperty("accepted") ACCEPTED,
    @JsonProperty("rejected") REJECTED,
    @JsonProperty("executing") EXECUTING,
    @JsonProperty("completed") COMPLETED,
    @JsonProperty("failed") FAILED
}

data class ConnectionHealth(
    val isConnected: Boolean,
    val connectionStartTime: Long?,
    val lastMessageReceived: Long?,
    val lastMessageSent: Long?,
    val reconnectAttempts: Int,
    val latencyMs: Long?,
    val messagesSent: Long,
    val messagesReceived: Long,
    val errors: List<String>
)

/**
 * WebSocket message wrapper for different message types
 */
data class WebSocketMessage(
    @JsonProperty("type") val type: MessageType,
    @JsonProperty("payload") val payload: String,
    @JsonProperty("timestamp") val timestamp: Long = System.currentTimeMillis()
)

enum class MessageType {
    @JsonProperty("telemetry") TELEMETRY,
    @JsonProperty("command") COMMAND,
    @JsonProperty("command_response") COMMAND_RESPONSE,
    @JsonProperty("heartbeat") HEARTBEAT,
    @JsonProperty("error") ERROR
}

/**
 * JSON utilities
 */
object JsonUtils {
    val objectMapper = jacksonObjectMapper()
    
    fun <T> toJson(obj: T): String = objectMapper.writeValueAsString(obj)
    
    inline fun <reified T> fromJson(json: String): T = objectMapper.readValue(json)
    
    fun createTelemetryMessage(telemetry: TelemetryData): String {
        val message = WebSocketMessage(MessageType.TELEMETRY, toJson(telemetry))
        return toJson(message)
    }
    
    fun createCommandMessage(command: DroneCommand): String {
        val message = WebSocketMessage(MessageType.COMMAND, toJson(command))
        return toJson(message)
    }
    
    fun createHeartbeatMessage(): String {
        val message = WebSocketMessage(MessageType.HEARTBEAT, "{}")
        return toJson(message)
    }
}
