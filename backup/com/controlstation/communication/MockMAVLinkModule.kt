package com.controlstation.communication

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.slf4j.LoggerFactory

/**
 * Mock MAVLink communication module for development and testing.
 * This provides a working implementation without actual MAVLink dependencies.
 */
class MockMAVLinkModule : CommunicationProtocol {
    private val logger = LoggerFactory.getLogger(MockMAVLinkModule::class.java)
    private var isConnected = false
    private var connectionString = ""
    
    override suspend fun connect(connectionString: String) {
        this.connectionString = connectionString
        logger.info("Mock MAVLink connecting to: $connectionString")
        delay(1000) // Simulate connection time
        isConnected = true
        logger.info("Mock MAVLink connected successfully")
    }

    override suspend fun sendCommand(command: Command) {
        if (!isConnected) {
            logger.warn("Cannot send command - MAVLink not connected")
            return
        }
        logger.info("Mock MAVLink sending command: ${command.command}")
        // Simulate command processing
        delay(100)
    }

    override fun getTelemetryFlow(): Flow<Telemetry> {
        return flow {
            while (isConnected) {
                emit(generateMockTelemetry())
                delay(1000)
            }
        }.flowOn(Dispatchers.IO)
    }

    override fun getHealthFlow(): Flow<ConnectionHealth> {
        return flow {
            while (true) {
                emit(ConnectionHealth(
                    isConnected = isConnected,
                    connectionStartTime = if (isConnected) System.currentTimeMillis() else null,
                    lastMessageReceived = if (isConnected) System.currentTimeMillis() else null,
                    lastMessageSent = if (isConnected) System.currentTimeMillis() else null,
                    reconnectAttempts = 0,
                    latencyMs = if (isConnected) 50L else null,
                    messagesSent = if (isConnected) 100L else 0L,
                    messagesReceived = if (isConnected) 98L else 0L,
                    errors = emptyList()
                ))
                delay(5000)
            }
        }.flowOn(Dispatchers.IO)
    }

    override fun isConnected(): Boolean = isConnected

    override suspend fun disconnect() {
        logger.info("Mock MAVLink disconnecting")
        isConnected = false
    }

    override fun getProtocolType(): ProtocolType = ProtocolType.MAVLINK

    override fun getConnectionInfo(): String = "Mock MAVLink connection to $connectionString"

    private fun generateMockTelemetry(): Telemetry {
        return TelemetryData(
            position = Position(
                latitude = 37.7749 + (Math.random() - 0.5) * 0.001,
                longitude = -122.4194 + (Math.random() - 0.5) * 0.001,
                altitude = 100.0 + (Math.random() - 0.5) * 10.0
            ),
            velocity = Velocity(
                velocityX = (Math.random() - 0.5) * 10.0,
                velocityY = (Math.random() - 0.5) * 10.0,
                velocityZ = (Math.random() - 0.5) * 2.0,
                speed = Math.random() * 15.0
            ),
            battery = BatteryStatus(
                voltage = 12.6 + (Math.random() - 0.5) * 0.5,
                current = 5.0 + (Math.random() - 0.5) * 2.0,
                percentage = (75 + Math.random() * 20).toInt(),
                remainingTimeMinutes = (30 + Math.random() * 30).toInt()
            ),
            gps = GpsStatus(
                fixType = 3,
                satelliteCount = (8 + Math.random() * 4).toInt(),
                horizontalDilution = 1.0 + Math.random() * 0.5,
                signalStrength = (80 + Math.random() * 15).toInt()
            ),
            altitude = 100.0 + (Math.random() - 0.5) * 10.0,
            heading = Math.random() * 360.0,
            flightMode = "AUTO",
            armed = true
        )
    }
}
