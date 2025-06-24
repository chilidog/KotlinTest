# 🤖 Agent Mode Prompt Templates for ControlStation Development

## 🎯 Context Setting Templates

### Initial Project Understanding

```
You are working on an advanced ControlStation project with:
- Hybrid WebSocket + MAVLink communication (operational)
- Cross-platform deployment (Codespaces, WSL:CachyOS, Windows, Unix)
- Enhanced pom.xml with professional dependencies
- Production-ready architecture with protocol adapter patterns
- Confirmed working demo on WSL:CachyOS environment

Current mission: Integrate WiFiLink 2 video streaming as third protocol.
```

### Architecture Continuation Template

```
The ControlStation uses established patterns:
- Protocol Adapter Pattern: WebSocket ↔ MAVLink translation (proven)
- Coroutine Concurrency: Real-time telemetry streaming (operational)
- Cross-Platform Profiles: Universal deployment (confirmed)
- Interactive Configuration: User-friendly setup (working)

Follow these same patterns for WiFiLink 2 video integration.
```

## 🚀 Implementation Request Templates

### Video Dependencies Template

```
Add WiFiLink 2 video dependencies to the existing pom.xml:
- JavaCV platform for video processing
- Netty for high-performance UDP networking  
- FFmpeg for H.264 stream decoding
- OpenCV for video analytics (optional)

Preserve all existing dependencies and structure.
```

### Video Adapter Implementation Template

```
Create WiFiLink2Adapter following existing protocol patterns:
- UDP video stream reception (port 5600 default)
- H.264 frame decoding with circular buffering
- Integration with existing ProtocolAdapter architecture
- Coroutine-based concurrent processing
- Telemetry synchronization with WebSocket/MAVLink flows
```

### Communication Manager Enhancement Template

```
Enhance existing UnifiedCommunicationManager:
- Add WiFiLink2Adapter as third protocol
- Implement triple-protocol telemetry using Flow.combine()
- Preserve all existing WebSocket+MAVLink functionality
- Add unified telemetry frame with video data
- Maintain real-time performance standards
```

### Demo Integration Template

```
Update SimpleControlStation.kt to demonstrate video integration:
- Add video communication modes to existing options
- Implement video stream selection and configuration
- Show real-time video frame processing
- Display telemetry+video synchronization
- Maintain existing WebSocket+MAVLink demo functionality
```

## 🔧 Testing and Validation Templates

### Compilation Validation Template

```
Validate implementation with these commands:
1. mvn clean compile (must succeed without errors)
2. mvn dependency:tree | grep video (confirm video dependencies)
3. mvn help:active-profiles (verify WSL profile activation)
4. mvn exec:java (test enhanced demo with video options)
```

### Functionality Testing Template

```
Test integration comprehensively:
1. Existing WebSocket mode (must work unchanged)
2. Existing MAVLink mode (must work unchanged)  
3. Existing Unified mode (must work unchanged)
4. New Video modes (demonstrate video integration)
5. Performance validation (real-time operation confirmed)
```

### Cross-Platform Testing Template

```
Verify cross-platform compatibility:
- WSL:CachyOS environment (primary test platform)
- GitHub Codespaces (cloud development)
- Windows native (if applicable)
- Unix/Linux systems (profile compatibility)
```

## 🎯 Problem-Solving Templates

### Dependency Issues Template

```
If video dependencies cause conflicts:
1. Check for version compatibility with existing libraries
2. Verify platform-specific native libraries are included
3. Test with mvn clean install -U to force updates
4. Isolate video dependencies with separate Maven profile if needed
```

### Performance Issues Template

```
If video processing affects performance:
1. Implement circular buffering for frame management
2. Use separate Dispatcher.IO for video processing
3. Optimize memory usage with proper garbage collection
4. Monitor resource usage and adjust buffer sizes
```

### Integration Issues Template

```
If video integration breaks existing functionality:
1. Verify existing protocol adapter patterns are preserved
2. Check that WebSocket+MAVLink flows remain independent
3. Ensure video processing doesn't block telemetry streams
4. Test existing demo modes before testing video features
```

## 🏆 Enhancement Templates

### Video Analytics Template

```
For future video analytics enhancement:
- Add OpenCV integration for object detection
- Implement video overlay with telemetry data
- Create video recording and playback capabilities
- Add performance metrics and quality monitoring
```

### Multi-Stream Template

```
For fleet video management:
- Extend WiFiLink2Adapter for multiple streams
- Implement stream selection and switching
- Add video stream monitoring and status
- Create unified multi-vehicle video dashboard
```

### Web Dashboard Template

```
For web-based video integration:
- Create WebSocket server for video streaming to browsers
- Implement video frame encoding for web transmission
- Add web dashboard with embedded video player
- Integrate with existing telemetry web interfaces
```

## 📚 Continuation Templates

### Session Handoff Template

```
For future Agent Mode sessions:
1. Read AGENT_CONTINUATION.md for immediate context
2. Review docs/COPILOT_AGENT_INSTRUCTIONS.md for complete details
3. Understand existing hybrid architecture before modifications
4. Test existing functionality before implementing new features
5. Follow established patterns and maintain code quality
```

### Enhancement Planning Template

```
For additional integrations:
1. Analyze existing protocol adapter patterns
2. Identify integration points in UnifiedCommunicationManager
3. Plan enhancement without breaking existing functionality
4. Design cross-platform compatibility from the start
5. Create documentation following established templates
```

### Quality Assurance Template

```
Maintain ControlStation quality standards:
1. Professional dependency management
2. Cross-platform compatibility
3. Real-time performance optimization
4. Comprehensive error handling
5. Enterprise-grade architecture patterns
```

These templates ensure consistent development approach across all Agent Mode sessions and maintain the high quality standards established in the ControlStation project.
