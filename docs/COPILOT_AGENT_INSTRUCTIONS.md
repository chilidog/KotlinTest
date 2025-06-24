# 🚁 ControlStation WiFiLink 2 Integration - Complete Agent Instructions

## 🎯 Mission Context
You are implementing WiFiLink 2 video streaming integration with an existing, working ControlStation that has hybrid WebSocket+MAVLink communication. The system is production-ready and operational.

## 📊 Current System Analysis (Confirmed Working)

### Architecture Foundation
- **Protocol Adapter Pattern**: Proven with WebSocket ↔ MAVLink translation
- **Coroutine-Based Concurrency**: Real-time telemetry streaming operational
- **Cross-Platform Profiles**: WSL (CachyOS), Unix, Windows, Codespaces
- **Enterprise Dependencies**: MAVLink, OkHttp, Jackson, Logback integrated

### Verified Components
```
pom.xml: Enhanced with MAVLink dependencies ✅
├── io.dronefleet.mavlink:1.1.9 (industry-standard protocol)
├── com.fazecast.jSerialComm:2.10.4 (hardware communication)
├── com.squareup.okhttp3:4.12.0 (WebSocket communication)
├── kotlinx-coroutines-core:1.7.3 (concurrent operations)
└── Cross-platform profiles (WSL, Unix, Windows, Codespaces)

SimpleControlStation.kt: Working demo ✅
├── Interactive configuration system
├── Real-time telemetry streaming  
├── Hybrid communication demonstrated
├── WSL:CachyOS optimization confirmed
└── Protocol switching operational
```

## 🚀 WiFiLink 2 Integration Implementation

### Phase 1: Enhanced Dependencies
Add these video dependencies to existing pom.xml (preserve current structure):

```xml
<!-- Insert after existing MAVLink dependencies -->

<!-- Video Processing for WiFiLink 2 Integration -->
<dependency>
    <groupId>org.bytedeco</groupId>
    <artifactId>javacv-platform</artifactId>
    <version>1.5.9</version>
</dependency>

<!-- Netty for High-Performance UDP Video Streaming -->
<dependency>
    <groupId>io.netty</groupId>
    <artifactId>netty-all</artifactId>
    <version>4.1.100.Final</version>
</dependency>

<!-- FFmpeg for H.264 Video Processing -->
<dependency>
    <groupId>org.bytedeco</groupId>
    <artifactId>ffmpeg-platform</artifactId>
    <version>6.0-1.5.9</version>
</dependency>

<!-- OpenCV for Video Analytics (Optional Enhancement) -->
<dependency>
    <groupId>org.bytedeco</groupId>
    <artifactId>opencv-platform</artifactId>
    <version>4.8.0-1.5.9</version>
</dependency>
```

### Phase 2: WiFiLink2Adapter Implementation
Create: `src/main/kotlin/com/controlstation/video/WiFiLink2Adapter.kt`

```kotlin
// Follow existing protocol patterns from WebSocket/MAVLink modules
class WiFiLink2Adapter(
    private val protocolAdapter: ProtocolAdapter
) {
    private val udpReceiver = UDPVideoReceiver()
    private val h264Decoder = H264StreamDecoder()
    private val frameBuffer = CircularFrameBuffer(30) // 30 FPS
    
    suspend fun connectToWiFiLink2(
        videoPort: Int = 5600,     // WiFiLink 2 default
        telemetryPort: Int = 5601  // WiFiLink 2 telemetry
    ) {
        // UDP video stream reception
        launch(Dispatchers.IO) {
            udpReceiver.receiveVideoStream(videoPort) { h264Frame ->
                val decodedFrame = h264Decoder.decode(h264Frame)
                frameBuffer.addFrame(decodedFrame)
                emitVideoFrame(decodedFrame)
            }
        }
        
        // Telemetry integration with existing MAVLink
        launch(Dispatchers.IO) {
            udpReceiver.receiveTelemetry(telemetryPort) { telemetryData ->
                bridgeWiFiLinkTelemetryToMAVLink(telemetryData)
            }
        }
    }
    
    // Integration with existing protocol adapter
    suspend fun bridgeWiFiLinkTelemetryToMAVLink(wifiLinkData: ByteArray) {
        val mavlinkMessage = convertToMAVLink(wifiLinkData)
        protocolAdapter.sendToMAVLink(mavlinkMessage)
    }
    
    fun getVideoFlow(): Flow<VideoFrame> = videoFrameFlow
    fun getFPVTelemetry(): Flow<FPVTelemetryData> = fpvTelemetryFlow
}
```

### Phase 3: UnifiedCommunicationManager Enhancement
Modify existing UnifiedCommunicationManager to add video as third protocol:

```kotlin
// Enhance existing class - DO NOT replace
class UnifiedCommunicationManager(
    private val webSocketProtocol: WebSocketCommunicationModule,
    private val mavlinkProtocol: MAVLinkCommunicationModule,
    private val protocolAdapter: ProtocolAdapter,
    // NEW: Add video integration
    private val wifiLink2Adapter: WiFiLink2Adapter
) {
    // Preserve existing methods, add video integration
    suspend fun initializeUnifiedSystemWithVideo() {
        // Existing protocols (preserve functionality)
        launch { webSocketProtocol.connect("ws://localhost:8080") }
        launch { mavlinkProtocol.connect("/dev/ttyUSB0") }
        
        // NEW: Video integration
        launch { wifiLink2Adapter.connectToWiFiLink2() }
        
        // NEW: Triple-protocol telemetry
        launch { startUnifiedTelemetryWithVideo() }
    }
    
    private suspend fun startUnifiedTelemetryWithVideo() {
        combine(
            getTelemetryFlow(),                    // Existing WebSocket+MAVLink
            wifiLink2Adapter.getVideoFlow(),       // New video frames
            wifiLink2Adapter.getFPVTelemetry()     // New FPV telemetry
        ) { telemetry, videoFrame, fpvData ->
            UnifiedTelemetryFrame(
                missionTelemetry = telemetry,
                videoFrame = videoFrame,
                fpvData = fpvData,
                timestamp = System.currentTimeMillis()
            )
        }.collect { unifiedFrame ->
            broadcastUnifiedFrame(unifiedFrame)
        }
    }
}
```

### Phase 4: SimpleControlStation.kt Enhancement
Update existing demo to include video options:

```kotlin
// Add video communication mode to existing configuration
enum class CommunicationMode {
    WebSocket_Only,
    MAVLink_Only,
    Unified,           // Existing hybrid mode
    Video_Only,        // NEW: WiFiLink 2 only
    Triple_Protocol    // NEW: WebSocket + MAVLink + Video
}

// Enhance existing configureCommunicationMode function
private fun configureCommunicationMode(): CommunicationMode {
    println("Select Communication Mode:")
    println("1. WebSocket Only (Pure JSON communication)")
    println("2. MAVLink Only (Binary protocol for hardware)")
    println("3. Unified (WebSocket + MAVLink hybrid)")
    println("4. Video Only (WiFiLink 2 streaming)")        // NEW
    println("5. Triple Protocol (WebSocket + MAVLink + Video)") // NEW
    
    // Enhanced selection logic...
}
```

## 🎯 Integration Requirements

### Preserve Existing Functionality
- **CRITICAL**: All existing WebSocket+MAVLink functionality must remain operational
- **Protocol Adapter**: Video integration follows same patterns as WebSocket/MAVLink
- **Cross-Platform**: Video must work on WSL:CachyOS, Codespaces, Windows, Unix
- **Performance**: Maintain real-time telemetry while adding video processing

### Video-Specific Requirements
- **UDP Reception**: WiFiLink 2 video streams on port 5600 (default)
- **H.264 Decoding**: Real-time video frame processing
- **Frame Buffering**: Circular buffer for smooth playback (30 FPS target)
- **Telemetry Sync**: Video overlay with existing mission telemetry
- **Error Handling**: Graceful degradation if video unavailable

### Testing Validation
1. **Compile**: `mvn clean compile` (must succeed)
2. **Run Demo**: `mvn exec:java` (video options appear)
3. **Existing Functions**: WebSocket+MAVLink modes still work
4. **Video Integration**: New video modes operational
5. **Performance**: Real-time operation on WSL:CachyOS

## 🏆 Expected Results

### Enhanced System Capabilities
- **Triple Protocol Support**: WebSocket + MAVLink + WiFiLink 2 video
- **Unified Telemetry**: Mission data + real-time video streams
- **Protocol Bridging**: Seamless translation between all three protocols
- **Enterprise Integration**: Video analytics and monitoring capabilities
- **Cross-Platform Video**: Universal deployment with video support

### Demo Enhancement
- **Interactive Video Config**: User selects video streaming options
- **Real-Time Display**: Video frame processing demonstration
- **Telemetry Overlay**: Mission data synchronized with video
- **Performance Metrics**: Video latency and frame rate monitoring
- **Multi-Stream Ready**: Foundation for fleet video management

## 📊 Success Criteria Checklist
- [ ] Enhanced pom.xml with video dependencies
- [ ] WiFiLink2Adapter implemented and integrated
- [ ] UnifiedCommunicationManager enhanced with video
- [ ] SimpleControlStation.kt demo updated with video options
- [ ] All existing functionality preserved and operational
- [ ] Video integration demonstrated on WSL:CachyOS
- [ ] Cross-platform compatibility maintained
- [ ] Real-time performance achieved
- [ ] Production-ready code quality maintained

**CRITICAL**: Test existing WebSocket+MAVLink modes first to ensure no regression before demonstrating new video capabilities.
