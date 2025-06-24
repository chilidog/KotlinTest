// Enhanced UnifiedCommunicationManager with WiFiLink 2 Video Integration
// Extends existing hybrid WebSocket+MAVLink architecture with video streaming

package com.controlstation.communication

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.slf4j.LoggerFactory

// Import video components
import com.controlstation.video.WiFiLink2Adapter
import com.controlstation.video.VideoFrame
import com.controlstation.video.FPVTelemetryData
import com.controlstation.video.UnifiedTelemetryFrame

/**
 * Enhanced Unified Communication Manager with Triple Protocol Support
 * Integrates WebSocket + MAVLink + WiFiLink 2 Video streaming
 */
class EnhancedUnifiedCommunicationManager(
    private val wifiLink2Adapter: WiFiLink2Adapter
) {
    private val logger = LoggerFactory.getLogger(EnhancedUnifiedCommunicationManager::class.java)
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    // Communication mode enumeration
    enum class CommunicationMode {
        WebSocket_Only,
        MAVLink_Only,
        Unified,           // WebSocket + MAVLink (existing)
        Video_Only,        // WiFiLink 2 video only
        Triple_Protocol    // WebSocket + MAVLink + Video (new)
    }
    
    private var currentMode = CommunicationMode.Triple_Protocol
    private val isRunning = kotlinx.coroutines.flow.MutableStateFlow(false)
    
    // Unified telemetry flow combining all protocols
    private val _unifiedTelemetryFlow = MutableSharedFlow<UnifiedTelemetryFrame>(
        replay = 1,
        extraBufferCapacity = 10
    )
    val unifiedTelemetryFlow: SharedFlow<UnifiedTelemetryFrame> = _unifiedTelemetryFlow.asSharedFlow()
    
    /**
     * Initialize the enhanced unified communication system
     */
    suspend fun initializeEnhancedSystem(mode: CommunicationMode = CommunicationMode.Triple_Protocol) {
        logger.info("🚀 Initializing Enhanced ControlStation with mode: $mode")
        currentMode = mode
        isRunning.value = true
        
        when (mode) {
            CommunicationMode.Video_Only -> {
                initializeVideoOnly()
            }
            CommunicationMode.Triple_Protocol -> {
                initializeTripleProtocol()
            }
            else -> {
                // For existing modes, maintain current functionality
                initializeExistingModes(mode)
            }
        }
        
        // Start unified telemetry collection
        startUnifiedTelemetryCollection()
        
        logger.info("✅ Enhanced ControlStation initialization complete")
    }
    
    /**
     * Initialize video-only communication mode
     */
    private suspend fun initializeVideoOnly() {
        logger.info("📹 Initializing Video-Only mode (WiFiLink 2)")
        
        scope.launch {
            try {
                wifiLink2Adapter.connectToWiFiLink2()
                logger.info("✅ WiFiLink 2 video system connected")
            } catch (e: Exception) {
                logger.error("❌ Failed to initialize video system: ${e.message}")
            }
        }
    }
    
    /**
     * Initialize triple protocol mode (WebSocket + MAVLink + Video)
     */
    private suspend fun initializeTripleProtocol() {
        logger.info("🔄 Initializing Triple Protocol mode (WebSocket + MAVLink + Video)")
        
        // Initialize existing protocols (preserve current functionality)
        scope.launch {
            initializeExistingProtocols()
        }
        
        // Initialize video streaming
        scope.launch {
            try {
                wifiLink2Adapter.connectToWiFiLink2()
                logger.info("✅ Triple protocol system fully operational")
            } catch (e: Exception) {
                logger.error("❌ Video integration failed, continuing with WebSocket+MAVLink: ${e.message}")
            }
        }
    }
    
    /**
     * Initialize existing WebSocket and MAVLink protocols
     * (Mock implementation - integrates with existing code)
     */
    private suspend fun initializeExistingProtocols() {
        logger.info("🔧 Initializing existing WebSocket + MAVLink protocols")
        
        // Mock existing protocol initialization
        // In real implementation, this would call existing communication modules
        scope.launch {
            logger.info("📡 WebSocket protocol initialized (mock)")
        }
        
        scope.launch {
            logger.info("📡 MAVLink protocol initialized (mock)")
        }
    }
    
    /**
     * Handle existing communication modes
     */
    private suspend fun initializeExistingModes(mode: CommunicationMode) {
        logger.info("🔧 Initializing existing mode: $mode")
        // Delegate to existing implementation
        initializeExistingProtocols()
    }
    
    /**
     * Start unified telemetry collection from all active protocols
     */
    private fun startUnifiedTelemetryCollection() {
        scope.launch {
            when (currentMode) {
                CommunicationMode.Video_Only -> {
                    collectVideoOnlyTelemetry()
                }
                CommunicationMode.Triple_Protocol -> {
                    collectTripleProtocolTelemetry()
                }
                else -> {
                    collectExistingTelemetry()
                }
            }
        }
    }
    
    /**
     * Collect telemetry for video-only mode
     */
    private suspend fun collectVideoOnlyTelemetry() {
        combine(
            wifiLink2Adapter.videoFrameFlow,
            wifiLink2Adapter.fpvTelemetryFlow
        ) { videoFrame, fpvData ->
            UnifiedTelemetryFrame(
                missionTelemetry = null,
                videoFrame = videoFrame,
                fpvData = fpvData,
                timestamp = System.currentTimeMillis()
            )
        }.collect { unifiedFrame ->
            _unifiedTelemetryFlow.tryEmit(unifiedFrame)
            logger.debug("📊 Video-only telemetry: Frame #${unifiedFrame.videoFrame?.frameNumber}, Latency: ${unifiedFrame.fpvData?.latency}ms")
        }
    }
    
    /**
     * Collect telemetry for triple protocol mode
     */
    private suspend fun collectTripleProtocolTelemetry() {
        combine(
            getMockMissionTelemetryFlow(),      // Mock existing telemetry
            wifiLink2Adapter.videoFrameFlow,    // Video frames
            wifiLink2Adapter.fpvTelemetryFlow   // FPV telemetry
        ) { missionTelemetry, videoFrame, fpvData ->
            UnifiedTelemetryFrame(
                missionTelemetry = missionTelemetry,
                videoFrame = videoFrame,
                fpvData = fpvData,
                timestamp = System.currentTimeMillis()
            )
        }.collect { unifiedFrame ->
            _unifiedTelemetryFlow.tryEmit(unifiedFrame)
            logger.debug("📊 Triple protocol telemetry: Mission + Video + FPV data synchronized")
        }
    }
    
    /**
     * Collect telemetry for existing modes (WebSocket, MAVLink, Unified)
     */
    private suspend fun collectExistingTelemetry() {
        getMockMissionTelemetryFlow().collect { missionTelemetry ->
            val unifiedFrame = UnifiedTelemetryFrame(
                missionTelemetry = missionTelemetry,
                videoFrame = null,
                fpvData = null,
                timestamp = System.currentTimeMillis()
            )
            _unifiedTelemetryFlow.tryEmit(unifiedFrame)
        }
    }
    
    /**
     * Mock mission telemetry flow (integrates with existing telemetry system)
     */
    private fun getMockMissionTelemetryFlow(): Flow<Map<String, Any>> = flow {
        while (isRunning.value) {
            val telemetry = mapOf(
                "altitude" to (100..200).random(),
                "speed" to (10..30).random(),
                "battery" to (60..100).random(),
                "gps_status" to listOf("LOCKED", "SEARCHING", "NO_FIX").random(),
                "mode" to listOf("HOVER", "MISSION", "RETURN").random()
            )
            emit(telemetry)
            delay(1000) // 1Hz telemetry rate
        }
    }
    
    /**
     * Send command through appropriate protocol
     */
    suspend fun sendCommand(command: String, protocol: String? = null) {
        when (protocol?.uppercase()) {
            "VIDEO", "WIFILINK2" -> {
                logger.info("📤 Sending command to WiFiLink 2: $command")
                // Video commands (camera control, etc.)
            }
            "WEBSOCKET" -> {
                logger.info("📤 Sending WebSocket command: $command")
                // Delegate to existing WebSocket implementation
            }
            "MAVLINK" -> {
                logger.info("📤 Sending MAVLink command: $command")
                // Delegate to existing MAVLink implementation
            }
            else -> {
                // Auto-select best protocol based on command type
                logger.info("📤 Auto-routing command: $command")
            }
        }
    }
    
    /**
     * Get system status including video integration
     */
    fun getSystemStatus(): Map<String, Any> {
        return mapOf(
            "mode" to currentMode.name,
            "running" to isRunning.value,
            "protocols" to mapOf(
                "websocket" to "mock_connected", // Replace with actual status
                "mavlink" to "mock_connected",   // Replace with actual status
                "video" to wifiLink2Adapter.isConnected()
            ),
            "video_stats" to wifiLink2Adapter.getVideoStats(),
            "video_connection" to wifiLink2Adapter.getConnectionInfo()
        )
    }
    
    /**
     * Switch communication mode at runtime
     */
    suspend fun switchMode(newMode: CommunicationMode) {
        logger.info("🔄 Switching from ${currentMode.name} to ${newMode.name}")
        
        // Stop current operations
        stopCurrentMode()
        
        // Initialize new mode
        initializeEnhancedSystem(newMode)
    }
    
    /**
     * Stop current communication mode
     */
    private suspend fun stopCurrentMode() {
        logger.info("⏹️ Stopping current mode: ${currentMode.name}")
        
        if (currentMode == CommunicationMode.Video_Only || currentMode == CommunicationMode.Triple_Protocol) {
            wifiLink2Adapter.disconnect()
        }
        
        // Stop existing protocols if needed
        // (Delegate to existing implementation)
    }
    
    /**
     * Shutdown the enhanced communication system
     */
    suspend fun shutdown() {
        logger.info("🔌 Shutting down Enhanced ControlStation")
        
        isRunning.value = false
        
        // Disconnect video system
        wifiLink2Adapter.disconnect()
        
        // Cancel all coroutines
        scope.cancel()
        
        logger.info("✅ Enhanced ControlStation shutdown complete")
    }
    
    /**
     * Get current communication mode
     */
    fun getCurrentMode(): CommunicationMode = currentMode
    
    /**
     * Check if video is available
     */
    fun isVideoAvailable(): Boolean = wifiLink2Adapter.isConnected()
    
    /**
     * Get latest video frame
     */
    fun getLatestVideoFrame(): VideoFrame? = wifiLink2Adapter.getLatestFrame()
}
