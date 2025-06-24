// WiFiLink 2 Video Streaming Adapter for ControlStation
// Integrates WiFiLink 2 UDP video streams with existing hybrid architecture

package com.controlstation.video

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.slf4j.LoggerFactory
import java.net.*
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

// Data classes for video integration
data class VideoFrame(
    val data: ByteArray,
    val timestamp: Long,
    val frameNumber: Long,
    val width: Int = 1920,
    val height: Int = 1080
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as VideoFrame
        return data.contentEquals(other.data) && timestamp == other.timestamp
    }
    
    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + timestamp.hashCode()
        return result
    }
}

data class FPVTelemetryData(
    val latency: Int,
    val quality: String,
    val signalStrength: Int,
    val frameRate: Int,
    val bitRate: Long
)

data class UnifiedTelemetryFrame(
    val missionTelemetry: Any?, // Will integrate with existing telemetry
    val videoFrame: VideoFrame?,
    val fpvData: FPVTelemetryData?,
    val timestamp: Long = System.currentTimeMillis()
)

// WiFiLink 2 Video Adapter - follows existing protocol patterns
class WiFiLink2Adapter {
    private val logger = LoggerFactory.getLogger(WiFiLink2Adapter::class.java)
    private val isConnected = AtomicBoolean(false)
    private val isRunning = AtomicBoolean(false)
    
    // Video stream management
    private val frameBuffer = ConcurrentLinkedQueue<VideoFrame>()
    private val maxBufferSize = 30 // 30 frames buffer (1 second at 30fps)
    private var frameCounter = 0L
    
    // Flow publishers for integration with existing architecture
    private val _videoFrameFlow = MutableSharedFlow<VideoFrame>(
        replay = 1,
        extraBufferCapacity = 10
    )
    val videoFrameFlow: SharedFlow<VideoFrame> = _videoFrameFlow.asSharedFlow()
    
    private val _fpvTelemetryFlow = MutableSharedFlow<FPVTelemetryData>(
        replay = 1,
        extraBufferCapacity = 10
    )
    val fpvTelemetryFlow: SharedFlow<FPVTelemetryData> = _fpvTelemetryFlow.asSharedFlow()
    
    // Network components
    private var videoSocket: DatagramSocket? = null
    private var telemetrySocket: DatagramSocket? = null
    
    /**
     * Connect to WiFiLink 2 video system
     * @param videoPort UDP port for video stream (default 5600)
     * @param telemetryPort UDP port for telemetry (default 5601)
     */
    suspend fun connectToWiFiLink2(
        videoPort: Int = 5600,
        telemetryPort: Int = 5601
    ) = withContext(Dispatchers.IO) {
        logger.info("🔗 Connecting to WiFiLink 2 - Video: $videoPort, Telemetry: $telemetryPort")
        
        try {
            // Initialize UDP sockets
            videoSocket = DatagramSocket(videoPort)
            telemetrySocket = DatagramSocket(telemetryPort)
            
            isConnected.set(true)
            isRunning.set(true)
            
            // Start video reception coroutine
            launch { receiveVideoStream() }
            
            // Start telemetry reception coroutine
            launch { receiveTelemetryStream() }
            
            // Start frame buffer management
            launch { manageFrameBuffer() }
            
            logger.info("✅ WiFiLink 2 connection established")
            
        } catch (e: Exception) {
            logger.error("❌ Failed to connect to WiFiLink 2: ${e.message}")
            isConnected.set(false)
            throw e
        }
    }
    
    /**
     * Receive UDP video stream from WiFiLink 2
     */
    private suspend fun receiveVideoStream() = withContext(Dispatchers.IO) {
        val buffer = ByteArray(65536) // 64KB buffer for video packets
        
        while (isRunning.get() && isConnected.get()) {
            try {
                val packet = DatagramPacket(buffer, buffer.size)
                videoSocket?.receive(packet)
                
                // Process H.264 video frame
                val frameData = ByteArray(packet.length)
                System.arraycopy(packet.data, packet.offset, frameData, 0, packet.length)
                
                val videoFrame = VideoFrame(
                    data = frameData,
                    timestamp = System.currentTimeMillis(),
                    frameNumber = frameCounter++
                )
                
                // Add to buffer with size management
                addFrameToBuffer(videoFrame)
                
                // Emit to flow for integration with existing telemetry
                _videoFrameFlow.tryEmit(videoFrame)
                
                logger.debug("📹 Video frame received: ${frameData.size} bytes, Frame #${videoFrame.frameNumber}")
                
            } catch (e: Exception) {
                if (isRunning.get()) {
                    logger.warn("⚠️ Video reception error: ${e.message}")
                    delay(100) // Brief delay before retry
                }
            }
        }
    }
    
    /**
     * Receive telemetry data from WiFiLink 2
     */
    private suspend fun receiveTelemetryStream() = withContext(Dispatchers.IO) {
        val buffer = ByteArray(1024) // 1KB buffer for telemetry
        
        while (isRunning.get() && isConnected.get()) {
            try {
                val packet = DatagramPacket(buffer, buffer.size)
                telemetrySocket?.receive(packet)
                
                // Parse WiFiLink 2 telemetry (mock implementation)
                val fpvTelemetry = parseWiFiLinkTelemetry(packet.data, packet.length)
                
                // Emit to flow
                _fpvTelemetryFlow.tryEmit(fpvTelemetry)
                
                logger.debug("📊 FPV telemetry: Latency ${fpvTelemetry.latency}ms, Quality: ${fpvTelemetry.quality}")
                
            } catch (e: Exception) {
                if (isRunning.get()) {
                    logger.warn("⚠️ Telemetry reception error: ${e.message}")
                    delay(100)
                }
            }
        }
    }
    
    /**
     * Manage frame buffer to prevent memory overflow
     */
    private suspend fun manageFrameBuffer() = withContext(Dispatchers.Default) {
        while (isRunning.get()) {
            // Remove old frames if buffer is full
            while (frameBuffer.size > maxBufferSize) {
                frameBuffer.poll()
            }
            delay(50) // Check every 50ms
        }
    }
    
    /**
     * Add frame to circular buffer
     */
    private fun addFrameToBuffer(frame: VideoFrame) {
        frameBuffer.offer(frame)
        if (frameBuffer.size > maxBufferSize) {
            frameBuffer.poll() // Remove oldest frame
        }
    }
    
    /**
     * Parse WiFiLink 2 telemetry data (mock implementation)
     */
    private fun parseWiFiLinkTelemetry(data: ByteArray, length: Int): FPVTelemetryData {
        // Mock telemetry parsing - in real implementation, parse actual WiFiLink 2 protocol
        return FPVTelemetryData(
            latency = (20..40).random(), // WiFiLink 2 typical latency range
            quality = listOf("Excellent", "Good", "Fair").random(),
            signalStrength = (70..100).random(),
            frameRate = 30,
            bitRate = (5_000_000L..15_000_000L).random()
        )
    }
    
    /**
     * Get current connection status
     */
    fun isConnected(): Boolean = isConnected.get()
    
    /**
     * Get latest video frame from buffer
     */
    fun getLatestFrame(): VideoFrame? = frameBuffer.lastOrNull()
    
    /**
     * Get video statistics
     */
    fun getVideoStats(): Map<String, Any> {
        return mapOf(
            "connected" to isConnected.get(),
            "bufferSize" to frameBuffer.size,
            "frameCount" to frameCounter,
            "maxBufferSize" to maxBufferSize
        )
    }
    
    /**
     * Integration with existing protocol adapter for telemetry bridging
     */
    suspend fun bridgeWiFiLinkTelemetryToMAVLink(wifiLinkData: ByteArray) {
        // Future integration point with existing ProtocolAdapter
        // Convert WiFiLink 2 telemetry format to MAVLink if needed
        logger.debug("🌉 Bridging WiFiLink telemetry to MAVLink protocol")
    }
    
    /**
     * Disconnect from WiFiLink 2
     */
    suspend fun disconnect() = withContext(Dispatchers.IO) {
        logger.info("🔌 Disconnecting from WiFiLink 2")
        
        isRunning.set(false)
        isConnected.set(false)
        
        videoSocket?.close()
        telemetrySocket?.close()
        
        frameBuffer.clear()
        
        logger.info("✅ WiFiLink 2 disconnected")
    }
    
    /**
     * Get protocol type for integration with existing communication manager
     */
    fun getProtocolType(): String = "WiFiLink2_Video"
    
    /**
     * Get connection info for monitoring
     */
    fun getConnectionInfo(): Map<String, Any> {
        return mapOf(
            "protocol" to "WiFiLink 2",
            "type" to "UDP Video Stream",
            "connected" to isConnected.get(),
            "running" to isRunning.get(),
            "videoPort" to 5600,
            "telemetryPort" to 5601
        )
    }
}
