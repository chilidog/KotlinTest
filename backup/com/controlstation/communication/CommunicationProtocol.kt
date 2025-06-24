package com.controlstation.communication

import kotlinx.coroutines.flow.Flow

/**
 * Universal communication protocol interface for both WebSocket and MAVLink protocols.
 * This abstraction allows seamless switching between different communication methods.
 */
interface CommunicationProtocol {
    
    /**
     * Establishes connection to the vehicle or ground station
     * @param connectionString Protocol-specific connection string (ws://url for WebSocket, /dev/ttyUSB0 for MAVLink)
     */
    suspend fun connect(connectionString: String)
    
    /**
     * Sends a command to the vehicle
     * @param command The command to send
     */
    suspend fun sendCommand(command: Command)
    
    /**
     * Returns a flow of telemetry data from the vehicle
     * @return Flow of telemetry updates
     */
    fun getTelemetryFlow(): Flow<Telemetry>
    
    /**
     * Returns a flow of connection health information
     * @return Flow of connection health status
     */
    fun getHealthFlow(): Flow<ConnectionHealth>
    
    /**
     * Checks if the protocol is currently connected
     * @return true if connected, false otherwise
     */
    fun isConnected(): Boolean
    
    /**
     * Disconnects from the vehicle or ground station
     */
    suspend fun disconnect()
    
    /**
     * Gets the protocol type identifier
     * @return Protocol identifier (WEBSOCKET, MAVLINK)
     */
    fun getProtocolType(): ProtocolType
    
    /**
     * Gets protocol-specific connection information
     * @return Connection info string
     */
    fun getConnectionInfo(): String
}

/**
 * Supported communication protocol types
 */
enum class ProtocolType {
    WEBSOCKET,
    MAVLINK,
    UNIFIED
}

/**
 * Protocol capabilities for auto-detection
 */
data class ProtocolCapabilities(
    val supportsWebSocket: Boolean = false,
    val supportsMAVLink: Boolean = false,
    val primaryProtocol: ProtocolType,
    val fallbackProtocol: ProtocolType? = null,
    val connectionString: String,
    val description: String
)
