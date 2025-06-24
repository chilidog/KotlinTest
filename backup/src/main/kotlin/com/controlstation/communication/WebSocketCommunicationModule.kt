package com.controlstation.communication

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import okhttp3.*
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import kotlin.random.Random

/**
 * Robust WebSocket-based communication module for drone control station.
 * Provides real-time bidirectional communication with error handling and reconnection logic.
 */
class WebSocketCommunicationModule(
    private val baseUrl: String = "ws://controlstation.local:8080",
    private val reconnectDelayMs: Long = 5000,
    private val maxReconnectAttempts: Int = 10,
    private val heartbeatIntervalMs: Long = 30000,
    private val telemetryIntervalMs: Long = 1000
) : CommunicationProtocol {
    private val logger = LoggerFactory.getLogger(WebSocketCommunicationModule::class.java)
    
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS) // No timeout for WebSocket
        .writeTimeout(10, TimeUnit.SECONDS)
        .pingInterval(20, TimeUnit.SECONDS) // Built-in ping/pong
        .build()
    
    private val isRunning = AtomicBoolean(false)
    private val webSocket = AtomicReference<WebSocket?>(null)
    private val reconnectAttempts = AtomicLong(0)
    
    // Connection health tracking
    private val connectionStartTime = AtomicLong(0)
    private val lastMessageReceived = AtomicLong(0)
    private val lastMessageSent = AtomicLong(0)
    private val messagesSent = AtomicLong(0)
    private val messagesReceived = AtomicLong(0)
    private val latency = AtomicLong(-1)
    private val errors = mutableListOf<String>()
    
    // Channels for communication
    private val incomingCommands = Channel<DroneCommand>(Channel.UNLIMITED)
    private val outgoingTelemetry = Channel<TelemetryData>(Channel.UNLIMITED)
    private val commandResponses = Channel<CommandResponse>(Channel.UNLIMITED)
    
    // Flow for telemetry data (for external observers)
    private val _telemetryFlow = MutableSharedFlow<TelemetryData>(replay = 1)
    val telemetryFlow: SharedFlow<TelemetryData> = _telemetryFlow.asSharedFlow()
    
    // Flow for incoming commands
    private val _commandFlow = MutableSharedFlow<DroneCommand>(replay = 0)
    val commandFlow: SharedFlow<DroneCommand> = _commandFlow.asSharedFlow()
    
    // Flow for connection status
    private val _connectionStatusFlow = MutableStateFlow(false)
    val connectionStatusFlow: StateFlow<Boolean> = _connectionStatusFlow.asStateFlow()
    
    private val webSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            logger.info("WebSocket connection opened to $baseUrl")
            connectionStartTime.set(System.currentTimeMillis())
            reconnectAttempts.set(0)
            _connectionStatusFlow.value = true
            clearError("Connection established")
        }
        
        override fun onMessage(webSocket: WebSocket, text: String) {
            lastMessageReceived.set(System.currentTimeMillis())
            messagesReceived.incrementAndGet()
            
            try {
                val message = JsonUtils.fromJson<WebSocketMessage>(text)
                handleIncomingMessage(message)
            } catch (e: Exception) {
                logger.error("Failed to parse incoming message: $text", e)
                addError("Message parsing error: ${e.message}")
            }
        }
        
        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            logger.error("WebSocket connection failed", t)
            _connectionStatusFlow.value = false
            addError("Connection failure: ${t.message}")
            
            if (isRunning.get()) {
                scheduleReconnect()
            }
        }
        
        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            logger.info("WebSocket connection closed: $code - $reason")
            _connectionStatusFlow.value = false
        }
    }
    
    /**
     * Start the communication module
     */
    suspend fun start(scope: CoroutineScope) {
        if (isRunning.compareAndSet(false, true)) {
            logger.info("Starting WebSocket communication module")
            
            // Launch coroutines for various tasks
            scope.launch { connectWithRetry() }
            scope.launch { telemetryProcessor() }
            scope.launch { commandProcessor() }
            scope.launch { heartbeatSender() }
            scope.launch { messageProcessor() }
        }
    }
    
    /**
     * Stop the communication module
     */
    fun stop() {
        if (isRunning.compareAndSet(true, false)) {
            logger.info("Stopping WebSocket communication module")
            webSocket.get()?.close(1000, "Shutting down")
            webSocket.set(null)
            _connectionStatusFlow.value = false
            
            // Close channels
            incomingCommands.close()
            outgoingTelemetry.close()
            commandResponses.close()
        }
    }
    
    /**
     * Send a command to the drone
     */
    suspend fun sendCommand(command: DroneCommand): Boolean {
        return try {
            val message = JsonUtils.createCommandMessage(command)
            sendMessage(message)
        } catch (e: Exception) {
            logger.error("Failed to send command: $command", e)
            addError("Command send error: ${e.message}")
            false
        }
    }
    
    /**
     * Send telemetry data
     */
    suspend fun sendTelemetry(telemetry: TelemetryData): Boolean {
        return try {
            outgoingTelemetry.send(telemetry)
            true
        } catch (e: Exception) {
            logger.error("Failed to queue telemetry data", e)
            false
        }
    }
    
    /**
     * Get current connection health status
     */
    fun getConnectionHealth(): ConnectionHealth {
        val currentTime = System.currentTimeMillis()
        val isConnected = _connectionStatusFlow.value
        
        return ConnectionHealth(
            isConnected = isConnected,
            connectionStartTime = if (connectionStartTime.get() > 0) connectionStartTime.get() else null,
            lastMessageReceived = if (lastMessageReceived.get() > 0) lastMessageReceived.get() else null,
            lastMessageSent = if (lastMessageSent.get() > 0) lastMessageSent.get() else null,
            reconnectAttempts = reconnectAttempts.get().toInt(),
            latencyMs = if (latency.get() >= 0) latency.get() else null,
            messagesSent = messagesSent.get(),
            messagesReceived = messagesReceived.get(),
            errors = errors.toList()
        )
    }
    
    private suspend fun connectWithRetry() {
        while (isRunning.get()) {
            try {
                connect()
                break // Success, exit retry loop
            } catch (e: Exception) {
                logger.error("Connection attempt failed", e)
                addError("Connection attempt failed: ${e.message}")
                
                if (reconnectAttempts.incrementAndGet() > maxReconnectAttempts) {
                    logger.error("Max reconnection attempts reached, stopping")
                    addError("Max reconnection attempts exceeded")
                    stop()
                    break
                }
                
                delay(reconnectDelayMs)
            }
        }
    }
    
    private fun connect() {
        logger.info("Connecting to WebSocket at $baseUrl")
        val request = Request.Builder()
            .url(baseUrl)
            .build()
        
        val ws = okHttpClient.newWebSocket(request, webSocketListener)
        webSocket.set(ws)
    }
    
    private fun scheduleReconnect() {
        if (isRunning.get() && reconnectAttempts.get() < maxReconnectAttempts) {
            logger.info("Scheduling reconnection attempt in ${reconnectDelayMs}ms")
            CoroutineScope(Dispatchers.IO).launch {
                delay(reconnectDelayMs)
                connectWithRetry()
            }
        }
    }
    
    private suspend fun telemetryProcessor() {
        while (isRunning.get()) {
            try {
                val telemetry = outgoingTelemetry.receive()
                val message = JsonUtils.createTelemetryMessage(telemetry)
                sendMessage(message)
                _telemetryFlow.emit(telemetry)
            } catch (e: Exception) {
                if (isRunning.get()) {
                    logger.error("Error processing telemetry", e)
                    addError("Telemetry processing error: ${e.message}")
                }
            }
        }
    }
    
    private suspend fun commandProcessor() {
        while (isRunning.get()) {
            try {
                val command = incomingCommands.receive()
                _commandFlow.emit(command)
                
                // Send acknowledgment
                val response = CommandResponse(
                    commandId = command.id,
                    status = CommandStatus.ACCEPTED,
                    message = "Command received and queued"
                )
                commandResponses.send(response)
            } catch (e: Exception) {
                if (isRunning.get()) {
                    logger.error("Error processing command", e)
                    addError("Command processing error: ${e.message}")
                }
            }
        }
    }
    
    private suspend fun heartbeatSender() {
        while (isRunning.get()) {
            try {
                if (_connectionStatusFlow.value) {
                    val heartbeatMessage = JsonUtils.createHeartbeatMessage()
                    sendMessage(heartbeatMessage)
                }
                delay(heartbeatIntervalMs)
            } catch (e: Exception) {
                logger.error("Error sending heartbeat", e)
                addError("Heartbeat error: ${e.message}")
            }
        }
    }
    
    private suspend fun messageProcessor() {
        while (isRunning.get()) {
            try {
                val response = commandResponses.receive()
                val message = WebSocketMessage(
                    type = MessageType.COMMAND_RESPONSE,
                    payload = JsonUtils.toJson(response)
                )
                sendMessage(JsonUtils.toJson(message))
            } catch (e: Exception) {
                if (isRunning.get()) {
                    logger.error("Error processing command response", e)
                    addError("Response processing error: ${e.message}")
                }
            }
        }
    }
    
    private fun handleIncomingMessage(message: WebSocketMessage) {
        when (message.type) {
            MessageType.COMMAND -> {
                try {
                    val command = JsonUtils.fromJson<DroneCommand>(message.payload)
                    CoroutineScope(Dispatchers.Default).launch {
                        incomingCommands.send(command)
                    }
                } catch (e: Exception) {
                    logger.error("Failed to parse command message", e)
                    addError("Command parsing error: ${e.message}")
                }
            }
            
            MessageType.TELEMETRY -> {
                try {
                    val telemetry = JsonUtils.fromJson<TelemetryData>(message.payload)
                    CoroutineScope(Dispatchers.Default).launch {
                        _telemetryFlow.emit(telemetry)
                    }
                } catch (e: Exception) {
                    logger.error("Failed to parse telemetry message", e)
                    addError("Telemetry parsing error: ${e.message}")
                }
            }
            
            MessageType.HEARTBEAT -> {
                // Update latency calculation if this is a heartbeat response
                val currentTime = System.currentTimeMillis()
                latency.set(currentTime - message.timestamp)
            }
            
            MessageType.COMMAND_RESPONSE -> {
                // Handle command responses if needed
                logger.debug("Received command response: ${message.payload}")
            }
            
            MessageType.ERROR -> {
                logger.warn("Received error message: ${message.payload}")
                addError("Remote error: ${message.payload}")
            }
        }
    }
    
    private fun sendMessage(message: String): Boolean {
        return try {
            val ws = webSocket.get()
            if (ws != null && _connectionStatusFlow.value) {
                val success = ws.send(message)
                if (success) {
                    lastMessageSent.set(System.currentTimeMillis())
                    messagesSent.incrementAndGet()
                }
                success
            } else {
                addError("Cannot send message: not connected")
                false
            }
        } catch (e: Exception) {
            logger.error("Failed to send message", e)
            addError("Message send error: ${e.message}")
            false
        }
    }
    
    private fun addError(errorMessage: String) {
        synchronized(errors) {
            errors.add("${System.currentTimeMillis()}: $errorMessage")
            // Keep only last 50 errors
            while (errors.size > 50) {
                errors.removeAt(0)
            }
        }
    }
    
    private fun clearError(successMessage: String) {
        synchronized(errors) {
            errors.clear()
            errors.add("${System.currentTimeMillis()}: $successMessage")
        }
    }
}

/**
 * Simulated drone telemetry generator for testing
 */
class SimulatedTelemetryGenerator {
    private var altitude = 0.0
    private var latitude = 47.6062
    private var longitude = -122.3321
    private var batteryPercentage = 100
    private var isFlying = false
    private var heading = 0.0
    
    fun generateTelemetry(): TelemetryData {
        // Simulate some movement and changes
        if (isFlying) {
            altitude += Random.nextDouble(-0.5, 0.5)
            latitude += Random.nextDouble(-0.0001, 0.0001)
            longitude += Random.nextDouble(-0.0001, 0.0001)
            heading = (heading + Random.nextDouble(-5.0, 5.0)) % 360.0
            batteryPercentage = maxOf(0, batteryPercentage - Random.nextInt(0, 2))
        }
        
        return TelemetryData(
            position = Position(latitude, longitude, altitude),
            velocity = Velocity(
                velocityX = Random.nextDouble(-2.0, 2.0),
                velocityY = Random.nextDouble(-2.0, 2.0),
                velocityZ = Random.nextDouble(-1.0, 1.0),
                speed = Random.nextDouble(0.0, 5.0)
            ),
            battery = BatteryStatus(
                voltage = 22.2 + Random.nextDouble(-0.5, 0.5),
                current = Random.nextDouble(1.0, 15.0),
                percentage = batteryPercentage,
                remainingTimeMinutes = (batteryPercentage * 20) / 100
            ),
            gps = GpsStatus(
                fixType = 3, // 3D fix
                satelliteCount = Random.nextInt(8, 12),
                horizontalDilution = Random.nextDouble(0.8, 1.5),
                signalStrength = Random.nextInt(80, 100)
            ),
            altitude = altitude,
            heading = heading,
            flightMode = if (isFlying) "AUTO" else "LAND",
            armed = isFlying
        )
    }
    
    fun updateFlightState(flying: Boolean) {
        isFlying = flying
        if (flying) {
            altitude = maxOf(10.0, altitude)
        } else {
            altitude = 0.0
        }
    }

    // CommunicationProtocol interface implementation
    override suspend fun connect(connectionString: String) {
        // Use the provided connection string, otherwise fall back to baseUrl
        val actualUrl = if (connectionString.isNotEmpty()) connectionString else baseUrl
        logger.info("Connecting to WebSocket at: $actualUrl")
        connectWithRetry()
    }

    override suspend fun sendCommand(command: Command) {
        sendCommand(command as DroneCommand)
    }

    override fun getTelemetryFlow(): Flow<Telemetry> {
        return flow {
            while (isRunning.get()) {
                emit(generateTelemetry())
                delay(telemetryIntervalMs)
            }
        }.flowOn(Dispatchers.IO)
    }

    override fun getHealthFlow(): Flow<ConnectionHealth> {
        return flow {
            while (isRunning.get()) {
                emit(getConnectionHealth())
                delay(5000) // Emit health every 5 seconds
            }
        }.flowOn(Dispatchers.IO)
    }

    override fun isConnected(): Boolean {
        return webSocket.get()?.let { true } ?: false
    }

    override suspend fun disconnect() {
        stop()
    }

    override fun getProtocolType(): ProtocolType {
        return ProtocolType.WEBSOCKET
    }

    override fun getConnectionInfo(): String {
        return "WebSocket connection to $baseUrl"
    }
}
