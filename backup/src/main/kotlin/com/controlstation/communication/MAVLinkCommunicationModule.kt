package com.controlstation.communication

import com.controlstation.mavlink.*
import io.dronefleet.mavlink.MavlinkConnection
import io.dronefleet.mavlink.MavlinkMessage
import io.dronefleet.mavlink.common.*
import io.dronefleet.mavlink.serialization.MavlinkSerializationException
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import com.fazecast.jSerialComm.SerialPort
import kotlin.math.cos
import kotlin.math.sin

/**
 * MAVLink communication module that implements the CommunicationProtocol interface.
 * Supports serial, UDP, and TCP connections to MAVLink-compatible autopilots.
 */
class MAVLinkCommunicationModule(
    private val config: MAVLinkConnectionConfig
) : CommunicationProtocol {
    
    private val logger = LoggerFactory.getLogger(MAVLinkCommunicationModule::class.java)
    private val isConnected = AtomicBoolean(false)
    private val connectionJob = AtomicReference<Job?>(null)
    
    // Communication channels
    private val telemetryChannel = Channel<Telemetry>(Channel.UNLIMITED)
    private val healthChannel = Channel<ConnectionHealth>(Channel.UNLIMITED)
    private val commandChannel = Channel<Command>(Channel.UNLIMITED)
    
    // MAVLink connection
    private var mavlinkConnection: MavlinkConnection? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    private var serialPort: SerialPort? = null
    private var socket: Socket? = null
    
    // System state
    private var systemInfo: MAVLinkSystemInfo? = null
    private val stats = MAVLinkStats()
    private val lastHeartbeat = AtomicLong(0)
    private val messagesReceived = AtomicLong(0)
    private val messagesSent = AtomicLong(0)
    
    // Current telemetry state
    private var currentTelemetry = Telemetry(
        altitude = 0.0,
        batteryLevel = 0.0,
        gpsStatus = "NO_GPS",
        flightMode = "UNKNOWN",
        timestamp = System.currentTimeMillis()
    )
    
    override suspend fun connect(connectionString: String) {
        if (isConnected.get()) {
            logger.warn("Already connected to MAVLink system")
            return
        }
        
        try {
            logger.info("Connecting to MAVLink system: $connectionString")
            
            when (config.connectionType) {
                MAVLinkConnectionType.SERIAL -> connectSerial(connectionString)
                MAVLinkConnectionType.UDP -> connectUDP(connectionString)
                MAVLinkConnectionType.TCP -> connectTCP(connectionString)
            }
            
            mavlinkConnection = MavlinkConnection.create(inputStream!!, outputStream!!)
            isConnected.set(true)
            
            // Start communication coroutines
            val job = CoroutineScope(Dispatchers.IO).launch {
                launch { messageReceiver() }
                launch { heartbeatSender() }
                launch { commandProcessor() }
                launch { healthMonitor() }
            }
            connectionJob.set(job)
            
            logger.info("Successfully connected to MAVLink system")
            publishHealth(ConnectionHealth.CONNECTED)
            
        } catch (e: Exception) {
            logger.error("Failed to connect to MAVLink system: ${e.message}", e)
            cleanup()
            publishHealth(ConnectionHealth.DISCONNECTED)
            throw e
        }
    }
    
    private fun connectSerial(port: String) {
        serialPort = SerialPort.getCommPort(port).apply {
            baudRate = config.baudRate
            setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 1000, 0)
            if (!openPort()) {
                throw IOException("Failed to open serial port: $port")
            }
        }
        inputStream = serialPort!!.inputStream
        outputStream = serialPort!!.outputStream
    }
    
    private fun connectUDP(address: String) {
        // UDP implementation would go here
        throw NotImplementedError("UDP connection not yet implemented")
    }
    
    private fun connectTCP(address: String) {
        val (host, port) = address.split(":").let { parts ->
            if (parts.size != 2) throw IllegalArgumentException("Invalid TCP address format: $address")
            parts[0] to parts[1].toInt()
        }
        
        socket = Socket(host, port)
        inputStream = socket!!.getInputStream()
        outputStream = socket!!.getOutputStream()
    }
    
    private suspend fun messageReceiver() {
        logger.info("Starting MAVLink message receiver")
        
        try {
            while (isConnected.get() && !currentCoroutineContext().job.isCancelled) {
                try {
                    val message = mavlinkConnection?.next()
                    if (message != null) {
                        processIncomingMessage(message)
                        messagesReceived.incrementAndGet()
                    }
                } catch (e: MavlinkSerializationException) {
                    logger.warn("Failed to deserialize MAVLink message: ${e.message}")
                } catch (e: IOException) {
                    logger.error("IO error while reading MAVLink messages: ${e.message}")
                    break
                }
            }
        } catch (e: Exception) {
            logger.error("Error in message receiver: ${e.message}", e)
        } finally {
            logger.info("MAVLink message receiver stopped")
        }
    }
    
    private suspend fun processIncomingMessage(message: MavlinkMessage<*>) {
        try {
            when (val payload = message.payload) {
                is Heartbeat -> processHeartbeat(payload)
                is Attitude -> processAttitude(payload)
                is GpsRawInt -> processGpsRaw(payload)
                is SysStatus -> processSysStatus(payload)
                else -> logger.debug("Received unhandled MAVLink message: ${payload.javaClass.simpleName}")
            }
        } catch (e: Exception) {
            logger.error("Error processing MAVLink message: ${e.message}", e)
        }
    }
    
    private suspend fun processHeartbeat(heartbeat: Heartbeat) {
        lastHeartbeat.set(System.currentTimeMillis())
        
        // Update system info
        systemInfo = MAVLinkSystemInfo(
            systemId = heartbeat.systemId(),
            componentId = heartbeat.componentId(),
            type = heartbeat.type(),
            autopilot = heartbeat.autopilot()
        )
        
        // Update flight mode in telemetry
        val flightMode = when (heartbeat.baseMode().value() and MavModeFlag.MAV_MODE_FLAG_CUSTOM_MODE_ENABLED.value()) {
            0 -> mapBaseMode(heartbeat.baseMode())
            else -> "CUSTOM_${heartbeat.customMode()}"
        }
        
        currentTelemetry = currentTelemetry.copy(
            flightMode = flightMode,
            timestamp = System.currentTimeMillis()
        )
        
        telemetryChannel.trySend(currentTelemetry)
        publishHealth(ConnectionHealth.CONNECTED)
    }
    
    private fun mapBaseMode(baseMode: EnumValue<MavModeFlag>): String {
        return when {
            baseMode.entry() == MavModeFlag.MAV_MODE_FLAG_STABILIZE_ENABLED -> "STABILIZE"
            baseMode.entry() == MavModeFlag.MAV_MODE_FLAG_GUIDED_ENABLED -> "GUIDED"
            baseMode.entry() == MavModeFlag.MAV_MODE_FLAG_AUTO_ENABLED -> "AUTO"
            baseMode.entry() == MavModeFlag.MAV_MODE_FLAG_TEST_ENABLED -> "TEST"
            else -> "MANUAL"
        }
    }
    
    private suspend fun processAttitude(attitude: Attitude) {
        // Convert attitude to altitude estimation (simplified)
        val estimatedAltitude = sin(attitude.pitch().toDouble()) * 100.0 + 100.0
        
        currentTelemetry = currentTelemetry.copy(
            altitude = estimatedAltitude,
            timestamp = System.currentTimeMillis()
        )
        
        telemetryChannel.trySend(currentTelemetry)
    }
    
    private suspend fun processGpsRaw(gps: GpsRawInt) {
        val gpsStatus = when (gps.fixType().entry()) {
            GpsFixType.GPS_FIX_TYPE_NO_GPS -> "NO_GPS"
            GpsFixType.GPS_FIX_TYPE_NO_FIX -> "NO_FIX"
            GpsFixType.GPS_FIX_TYPE_2D_FIX -> "2D_FIX"
            GpsFixType.GPS_FIX_TYPE_3D_FIX -> "3D_FIX"
            GpsFixType.GPS_FIX_TYPE_DGPS -> "DGPS"
            GpsFixType.GPS_FIX_TYPE_RTK_FLOAT -> "RTK_FLOAT"
            GpsFixType.GPS_FIX_TYPE_RTK_FIXED -> "RTK_FIXED"
            else -> "UNKNOWN"
        }
        
        currentTelemetry = currentTelemetry.copy(
            gpsStatus = gpsStatus,
            altitude = gps.alt().toDouble() / 1000.0, // Convert mm to meters
            timestamp = System.currentTimeMillis()
        )
        
        telemetryChannel.trySend(currentTelemetry)
    }
    
    private suspend fun processSysStatus(sysStatus: SysStatus) {
        val batteryVoltage = sysStatus.voltageBattery().toDouble() / 1000.0 // Convert mV to V
        val batteryPercentage = sysStatus.batteryRemaining().toDouble()
        
        currentTelemetry = currentTelemetry.copy(
            batteryLevel = batteryPercentage,
            timestamp = System.currentTimeMillis()
        )
        
        telemetryChannel.trySend(currentTelemetry)
    }
    
    private suspend fun heartbeatSender() {
        logger.info("Starting MAVLink heartbeat sender")
        
        while (isConnected.get() && !currentCoroutineContext().job.isCancelled) {
            try {
                sendHeartbeat()
                delay(config.heartbeatInterval)
            } catch (e: Exception) {
                logger.error("Error sending heartbeat: ${e.message}", e)
                break
            }
        }
        
        logger.info("MAVLink heartbeat sender stopped")
    }
    
    private suspend fun sendHeartbeat() {
        val heartbeat = Heartbeat.builder()
            .type(MavType.MAV_TYPE_GCS)
            .autopilot(MavAutopilot.MAV_AUTOPILOT_INVALID)
            .baseMode(EnumValue.of(MavModeFlag.MAV_MODE_FLAG_CUSTOM_MODE_ENABLED))
            .customMode(0)
            .systemStatus(MavState.MAV_STATE_ACTIVE)
            .mavlinkVersion(3)
            .build()
        
        sendMAVLinkMessage(heartbeat, config.systemId, config.componentId)
    }
    
    private suspend fun commandProcessor() {
        logger.info("Starting MAVLink command processor")
        
        try {
            for (command in commandChannel) {
                processCommand(command)
            }
        } catch (e: Exception) {
            logger.error("Error in command processor: ${e.message}", e)
        }
        
        logger.info("MAVLink command processor stopped")
    }
    
    private suspend fun processCommand(command: Command) {
        try {
            val mavCommand = when (command.type) {
                CommandType.TAKEOFF -> createTakeoffCommand(command)
                CommandType.LAND -> createLandCommand(command)
                CommandType.HOVER -> createHoverCommand(command)
                CommandType.EMERGENCY -> createEmergencyCommand(command)
                CommandType.SET_MODE -> createSetModeCommand(command)
            }
            
            sendMAVLinkMessage(mavCommand, systemInfo?.systemId ?: 1, systemInfo?.componentId ?: 1)
            messagesSent.incrementAndGet()
            
        } catch (e: Exception) {
            logger.error("Error processing command ${command.type}: ${e.message}", e)
        }
    }
    
    private fun createTakeoffCommand(command: Command): CommandLong {
        return CommandLong.builder()
            .command(MavCmd.MAV_CMD_NAV_TAKEOFF)
            .param1(0f) // Minimum pitch
            .param2(0f) // Empty
            .param3(0f) // Empty
            .param4(0f) // Yaw angle
            .param5(0f) // Latitude
            .param6(0f) // Longitude
            .param7(command.parameters["altitude"]?.toFloat() ?: 10f) // Altitude
            .targetSystem(systemInfo?.systemId ?: 1)
            .targetComponent(systemInfo?.componentId ?: 1)
            .confirmation(0)
            .build()
    }
    
    private fun createLandCommand(command: Command): CommandLong {
        return CommandLong.builder()
            .command(MavCmd.MAV_CMD_NAV_LAND)
            .param1(0f) // Abort altitude
            .param2(0f) // Landing mode
            .param3(0f) // Empty
            .param4(0f) // Yaw angle
            .param5(0f) // Latitude
            .param6(0f) // Longitude
            .param7(0f) // Altitude
            .targetSystem(systemInfo?.systemId ?: 1)
            .targetComponent(systemInfo?.componentId ?: 1)
            .confirmation(0)
            .build()
    }
    
    private fun createHoverCommand(command: Command): CommandLong {
        return CommandLong.builder()
            .command(MavCmd.MAV_CMD_NAV_LOITER_UNLIM)
            .param1(0f) // Empty
            .param2(0f) // Empty
            .param3(command.parameters["radius"]?.toFloat() ?: 5f) // Radius
            .param4(0f) // Yaw
            .param5(0f) // Latitude
            .param6(0f) // Longitude
            .param7(command.parameters["altitude"]?.toFloat() ?: 0f) // Altitude
            .targetSystem(systemInfo?.systemId ?: 1)
            .targetComponent(systemInfo?.componentId ?: 1)
            .confirmation(0)
            .build()
    }
    
    private fun createEmergencyCommand(command: Command): CommandLong {
        return CommandLong.builder()
            .command(MavCmd.MAV_CMD_COMPONENT_ARM_DISARM)
            .param1(0f) // Disarm
            .param2(21196f) // Force disarm magic number
            .targetSystem(systemInfo?.systemId ?: 1)
            .targetComponent(systemInfo?.componentId ?: 1)
            .confirmation(0)
            .build()
    }
    
    private fun createSetModeCommand(command: Command): CommandLong {
        val mode = command.parameters["mode"] ?: "GUIDED"
        val customMode = when (mode) {
            "STABILIZE" -> 0
            "ACRO" -> 1
            "ALT_HOLD" -> 2
            "AUTO" -> 3
            "GUIDED" -> 4
            "LOITER" -> 5
            "RTL" -> 6
            "LAND" -> 9
            else -> 4 // Default to GUIDED
        }
        
        return CommandLong.builder()
            .command(MavCmd.MAV_CMD_DO_SET_MODE)
            .param1(MavMode.MAV_MODE_FLAG_CUSTOM_MODE_ENABLED.value().toFloat())
            .param2(customMode.toFloat())
            .targetSystem(systemInfo?.systemId ?: 1)
            .targetComponent(systemInfo?.componentId ?: 1)
            .confirmation(0)
            .build()
    }
    
    private suspend fun sendMAVLinkMessage(payload: Any, systemId: Int, componentId: Int) {
        try {
            val message = MavlinkMessage.create(systemId, componentId, payload)
            mavlinkConnection?.send(message)
        } catch (e: Exception) {
            logger.error("Error sending MAVLink message: ${e.message}", e)
            throw e
        }
    }
    
    private suspend fun healthMonitor() {
        logger.info("Starting MAVLink health monitor")
        
        while (isConnected.get() && !currentCoroutineContext().job.isCancelled) {
            try {
                val now = System.currentTimeMillis()
                val timeSinceLastHeartbeat = now - lastHeartbeat.get()
                
                val health = when {
                    timeSinceLastHeartbeat > config.timeoutMs -> {
                        logger.warn("MAVLink heartbeat timeout: ${timeSinceLastHeartbeat}ms")
                        ConnectionHealth.DEGRADED
                    }
                    timeSinceLastHeartbeat > config.timeoutMs / 2 -> {
                        ConnectionHealth.DEGRADED
                    }
                    else -> ConnectionHealth.CONNECTED
                }
                
                publishHealth(health)
                delay(1000) // Check every second
                
            } catch (e: Exception) {
                logger.error("Error in health monitor: ${e.message}", e)
                break
            }
        }
        
        logger.info("MAVLink health monitor stopped")
    }
    
    private suspend fun publishHealth(status: ConnectionHealth) {
        healthChannel.trySend(status)
    }
    
    override suspend fun sendCommand(command: Command) {
        if (!isConnected.get()) {
            throw IllegalStateException("MAVLink not connected")
        }
        commandChannel.trySend(command)
    }
    
    override fun getTelemetryFlow(): Flow<Telemetry> {
        return telemetryChannel.receiveAsFlow()
    }
    
    override fun getHealthFlow(): Flow<ConnectionHealth> {
        return healthChannel.receiveAsFlow()
    }
    
    override fun isConnected(): Boolean = isConnected.get()
    
    override suspend fun disconnect() {
        logger.info("Disconnecting from MAVLink system")
        isConnected.set(false)
        cleanup()
        publishHealth(ConnectionHealth.DISCONNECTED)
    }
    
    private fun cleanup() {
        connectionJob.get()?.cancel()
        connectionJob.set(null)
        
        try {
            mavlinkConnection?.close()
        } catch (e: Exception) {
            logger.warn("Error closing MAVLink connection: ${e.message}")
        }
        
        try {
            serialPort?.closePort()
        } catch (e: Exception) {
            logger.warn("Error closing serial port: ${e.message}")
        }
        
        try {
            socket?.close()
        } catch (e: Exception) {
            logger.warn("Error closing socket: ${e.message}")
        }
        
        mavlinkConnection = null
        inputStream = null
        outputStream = null
        serialPort = null
        socket = null
    }
    
    override fun getProtocolType(): ProtocolType = ProtocolType.MAVLINK
    
    override fun getConnectionInfo(): String {
        return "MAVLink ${config.connectionType} - ${config.connectionString}"
    }
    
    /**
     * Get current MAVLink statistics
     */
    fun getStats(): MAVLinkStats {
        return stats.copy(
            messagesReceived = messagesReceived.get(),
            messagesSent = messagesSent.get(),
            lastHeartbeat = lastHeartbeat.get(),
            connectionUptime = if (isConnected.get()) System.currentTimeMillis() - lastHeartbeat.get() else 0
        )
    }
    
    /**
     * Get current system information
     */
    fun getSystemInfo(): MAVLinkSystemInfo? = systemInfo
}
