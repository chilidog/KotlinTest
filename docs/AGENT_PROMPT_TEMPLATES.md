# 🤖 Agent Mode Prompt Templates for ControlStation Development

## 🎯 Context Setting Templates

### Initial Project Understanding

```text
You are working on an advanced ControlStation project with:
- Hybrid WebSocket + MAVLink communication (operational)
- Cross-platform deployment (Codespaces, WSL:CachyOS, Windows, Unix)
- Enhanced pom.xml with professional dependencies
- Production-ready architecture with protocol adapter patterns
- Confirmed working demo on WSL:CachyOS environment

Current mission: Integrate WiFiLink 2 video streaming as third protocol.
```

### Architecture Continuation Template

```text
The ControlStation uses established patterns:

- Protocol Adapter Pattern: WebSocket ↔ MAVLink translation (proven)
- Coroutine Concurrency: Real-time telemetry streaming (operational)
- Cross-Platform Profiles: Universal deployment (confirmed)
- Interactive Configuration: User-friendly setup (working)

Follow these same patterns for WiFiLink 2 video integration.

```

## 🚀 Implementation Request Templates

### Video Dependencies Template

```markdown
Add WiFiLink 2 video dependencies to the existing pom.xml:
- JavaCV platform for video processing
- Netty for high-performance UDP networking  
- FFmpeg for H.264 stream decoding
- OpenCV for video analytics (optional)

Preserve all existing dependencies and structure.
```

Add WiFiLink 2 video dependencies to the existing pom.xml:

- JavaCV platform for video processing
- Netty for high-performance UDP networking  
- FFmpeg for H.264 stream decoding
- OpenCV for video analytics (optional)

Preserve all existing dependencies and structure.

### Video Adapter Implementation Template

```text
Create WiFiLink2Adapter following existing protocol patterns:

- UDP video stream reception (port 5600 default)
- H.264 frame decoding with circular buffering
- Integration with existing ProtocolAdapter architecture
- Coroutine-based concurrent processing
- Telemetry synchronization with WebSocket/MAVLink flows
```

### Communication Manager Enhancement Template

```text
Enhance existing UnifiedCommunicationManager:

- Add WiFiLink2Adapter as third protocol
- Implement triple-protocol telemetry using Flow.combine()
- Preserve all existing WebSocket+MAVLink functionality
- Add unified telemetry frame with video data
- Maintain real-time performance standards
```

### Demo Integration Template

```text
Update SimpleControlStation.kt to demonstrate video integration:

- Add video communication modes to existing options
- Implement video stream selection and configuration
- Show real-time video frame processing
- Display telemetry+video synchronization
- Maintain existing WebSocket+MAVLink demo functionality
```

## 🔧 Testing and Validation Templates

### Compilation Validation Template

```text
Validate implementation with these commands:

1. mvn clean compile (must succeed without errors)
2. mvn dependency:tree | grep video (confirm video dependencies)
3. mvn help:active-profiles (verify WSL profile activation)
4. mvn exec:java (test enhanced demo with video options)
```

### Functionality Testing Template

```text
Test integration comprehensively:

1. Existing WebSocket mode (must work unchanged)
2. Existing MAVLink mode (must work unchanged)  
3. Existing Unified mode (must work unchanged)
4. New Video modes (demonstrate video integration)
5. Performance validation (real-time operation confirmed)
```

### Cross-Platform Testing Template

```text
Verify cross-platform compatibility:

- WSL:CachyOS environment (primary test platform)
- GitHub Codespaces (cloud development)
- Windows native (if applicable)
- Unix/Linux systems (profile compatibility)
```

## 🎯 Problem-Solving Templates

### Dependency Issues Template

```text
If video dependencies cause conflicts:

1. Check for version compatibility with existing libraries
2. Verify platform-specific native libraries are included
3. Test with mvn clean install -U to force updates
4. Isolate video dependencies with separate Maven profile if needed
```

### Performance Issues Template

```text
If video processing affects performance:

1. Implement circular buffering for frame management
2. Use separate Dispatcher.IO for video processing
3. Optimize memory usage with proper garbage collection
4. Monitor resource usage and adjust buffer sizes
```

### Integration Issues Template

```text
If video integration breaks existing functionality:

1. Verify existing protocol adapter patterns are preserved
2. Check that WebSocket+MAVLink flows remain independent
3. Ensure video processing doesn't block telemetry streams
4. Test existing demo modes before testing video features
```

## 🏆 Enhancement Templates

### Video Analytics Template

```text
For future video analytics enhancement:

- Add OpenCV integration for object detection
- Implement video overlay with telemetry data
- Create video recording and playback capabilities
- Add performance metrics and quality monitoring
```

### Multi-Stream Template

```text
For fleet video management:

- Extend WiFiLink2Adapter for multiple streams
- Implement stream selection and switching
- Add video stream monitoring and status
- Create unified multi-vehicle video dashboard
```

### Web Dashboard Template

```text
For web-based video integration:

- Create WebSocket server for video streaming to browsers
- Implement video frame encoding for web transmission
- Add web dashboard with embedded video player
- Integrate with existing telemetry web interfaces
```

## 📚 Continuation Templates

### Session Handoff Template

```text
For future Agent Mode sessions:

1. Read AGENT_CONTINUATION.md for immediate context
2. Review docs/COPILOT_AGENT_INSTRUCTIONS.md for complete details
3. Understand existing hybrid architecture before modifications
4. Test existing functionality before implementing new features
5. Follow established patterns and maintain code quality
```

### Enhancement Planning Template

```text
For additional integrations:

1. Analyze existing protocol adapter patterns
2. Identify integration points in UnifiedCommunicationManager
3. Plan enhancement without breaking existing functionality
4. Design cross-platform compatibility from the start
5. Create documentation following established templates
```

### Quality Assurance Template

```text
Maintain ControlStation quality standards:

1. Professional dependency management
2. Cross-platform compatibility
3. Real-time performance optimization
4. Comprehensive error handling
5. Enterprise-grade architecture patterns
```

These templates ensure consistent development approach across all Agent Mode sessions and maintain the high
quality standards established in the ControlStation project.

## 🚁 Parrot SDK Analysis Templates

### Parrot Ecosystem Deep Dive Template

```text
You are analyzing the complete Parrot SDK ecosystem for government-grade ControlStation integration:

AVAILABLE REPOSITORIES (12 downloaded in /workspaces/KotlinTest/parrot-sdk-analysis/):
- groundsdk-android (Android GCS framework) - PRIMARY INTEGRATION TARGET
- groundsdk-ios (iOS GCS framework) - Cross-platform patterns  
- arsdk-ng (Core Air SDK protocols) - PROTOCOL FOUNDATION
- pdraw (Professional video pipeline) - VIDEO STREAMING MASTERY
- olympe (Python automation SDK) - Mission automation patterns
- samples (Reference implementations) - Best practices guide
- alchemy (Build system) - Development infrastructure
- dragon_build (Advanced builds) - CI/CD patterns
- firmwared (Firmware tools) - Device management
- telemetry (Data collection) - Analytics patterns
- libpomp (Networking) - Low-level protocols
- developer.parrot.com (Documentation) - API references

MISSION: Extract enterprise-grade patterns for multi-vendor ControlStation enhancement.
FOCUS: Government compliance, Blue sUAS integration, SBIR positioning.
```

### Architecture Analysis Session Template

```text
Conduct comprehensive architecture analysis of Parrot SDKs:

1. PROTOCOL LAYER ANALYSIS (arsdk-ng + libpomp)
   - Map communication protocols and command structures
   - Identify security and encryption patterns
   - Extract protocol adapter patterns
   - Document multi-vendor compatibility approaches

2. APPLICATION FRAMEWORK STUDY (groundsdk-android/ios)
   - Analyze MVC/MVVM patterns and state management
   - Study real-time telemetry handling
   - Extract UI/UX patterns for government applications
   - Identify enterprise-grade error handling

3. VIDEO PIPELINE MASTERY (pdraw)
   - Study H.264/H.265 streaming architectures
   - Analyze low-latency processing techniques
   - Extract multi-stream management patterns
   - Document performance optimization strategies

4. AUTOMATION FRAMEWORK (olympe)
   - Study mission planning and execution patterns
   - Extract autonomous operation frameworks
   - Analyze fleet management approaches
   - Document scripting and automation APIs

Focus on patterns applicable to government/enterprise drone operations.
```

### Enterprise Feature Mining Template

```text
Mine Parrot repositories for government-grade features and compliance patterns:

SECURITY & COMPLIANCE ANALYSIS:
- Authentication and authorization mechanisms
- Encryption and secure communication protocols
- Audit logging and compliance tracking
- Blue sUAS compliance indicators
- NDAA compliance patterns

ENTERPRISE FUNCTIONALITY:
- Fleet management and multi-vehicle coordination
- Role-based access control systems
- Enterprise deployment and configuration
- Performance monitoring and analytics
- Failover and redundancy mechanisms

GOVERNMENT-SPECIFIC FEATURES:
- Secure telemetry and command protocols
- Video encryption and secure streaming
- Mission recording and audit trails
- Compliance reporting and documentation
- Integration with government systems

Extract these patterns for ControlStation integration planning.
```

### Integration Strategy Development Template

```text
Develop comprehensive integration strategy using Parrot SDK insights:

PHASE 1: PROTOCOL INTEGRATION
- Implement Parrot-inspired protocol adapters
- Add ARSDK-compatible command structures
- Integrate secure communication patterns
- Enhance multi-vendor protocol support

PHASE 2: VIDEO SYSTEM ENHANCEMENT
- Integrate PDrAW-inspired video pipeline
- Add professional streaming capabilities
- Implement multi-stream management
- Enhance video analytics and overlays

PHASE 3: ENTERPRISE FEATURES
- Add GroundSDK-inspired state management
- Implement fleet coordination capabilities
- Enhance security and compliance features
- Add enterprise deployment patterns

PHASE 4: GOVERNMENT POSITIONING
- Implement Blue sUAS compliance features
- Add government-grade security measures
- Create audit and compliance reporting
- Prepare SBIR-ready documentation

Focus on maintaining ControlStation's multi-vendor approach while adding enterprise capabilities.
```

### Partnership & SBIR Positioning Template

```text
Analyze Parrot ecosystem for strategic partnership and SBIR opportunities:

PARTNERSHIP ANALYSIS:
- Identify collaboration opportunities with Parrot
- Map complementary technology areas
- Analyze open-source vs commercial boundaries
- Evaluate joint development possibilities

COMPETITIVE POSITIONING:
- Compare ControlStation advantages vs GroundSDK
- Identify unique value propositions
- Document multi-vendor superiority
- Highlight government-specific enhancements

SBIR VALUE PROPOSITION:
- Document technology differentiation
- Highlight government compliance advantages
- Demonstrate innovation beyond existing solutions
- Prepare commercialization strategy

MARKET POSITIONING:
- Position as enterprise-grade alternative
- Emphasize multi-vendor compatibility
- Highlight government/Blue sUAS focus
- Document partnership-ready architecture

Create compelling narrative for federal funding and enterprise sales.
```

### Technical Deep Dive Session Template

```text
Conduct systematic technical analysis of specific Parrot components:

SELECT TARGET REPOSITORY: [groundsdk-android|arsdk-ng|pdraw|olympe]

ANALYSIS METHODOLOGY:
1. Repository structure and organization study
2. Key API and interface identification
3. Architecture pattern extraction
4. Performance optimization techniques
5. Security and compliance features
6. Integration point identification
7. Adaptation strategy for ControlStation

DOCUMENTATION REQUIREMENTS:
- Architecture diagrams and flow charts
- API compatibility matrices
- Performance benchmarking data
- Security assessment findings
- Integration implementation plan
- Code samples and examples

Focus on actionable insights for immediate ControlStation enhancement.
```

## 🎯 Government & Enterprise Templates

### Blue sUAS Compliance Analysis Template

```text
Analyze Parrot ecosystem for Blue sUAS compliance patterns and government requirements:

COMPLIANCE AREAS:
- NDAA Section 848 compliance indicators
- Approved component usage patterns
- Supply chain security measures
- Data sovereignty and protection

ANAFI USA ANALYSIS:
- Government-specific feature implementations
- Security enhancements and certifications
- Compliance documentation patterns
- Federal approval processes

INTEGRATION STRATEGY:
- Apply compliance patterns to ControlStation
- Enhance multi-vendor compliance approach
- Document government-grade features
- Prepare certification pathways

Goal: Position ControlStation as Blue sUAS-ready platform.
```

### Federal Market Positioning Template

```text
Develop federal market strategy using Parrot ecosystem insights:

MARKET ANALYSIS:
- Federal drone technology requirements
- Government procurement patterns
- Compliance and certification needs
- Competitive landscape assessment

VALUE PROPOSITION:
- Multi-vendor platform advantages
- Government-grade security features
- Blue sUAS compliance readiness
- Enterprise deployment capabilities

DIFFERENTIATION STRATEGY:
- Beyond single-vendor limitations
- Enhanced security and compliance
- Multi-platform compatibility
- Open architecture benefits

COMMERCIALIZATION PLAN:
- SBIR Phase I/II positioning
- Federal sales strategy
- Partnership development
- Technology transfer opportunities

Create compelling federal market entry strategy.
```

## 🔗 KOTLink Integration Templates

### KOTLink Strategic Analysis Template

```text
You are analyzing KOTLink (https://github.com/mmoczkowski/KOTLink) for professional MAVLink integration in our government-grade ControlStation:

CURRENT MAVLINK IMPLEMENTATION:
- Custom MAVLink protocol handling in protocol adapters
- Manual WebSocket ↔ MAVLink translation
- Hand-coded telemetry parsing and serialization
- Basic message routing and processing

KOTLINK STRATEGIC VALUE:
- Pure Kotlin MAVLink implementation (perfect language fit)
- Type-safe message handling (eliminates runtime errors)
- Coroutine-friendly API (seamless with our architecture)
- Modern Kotlin patterns (aligns with our codebase style)
- Community-maintained (reduces maintenance burden)
- Professional implementation (government-grade reliability)

MISSION: Replace custom MAVLink code with KOTLink for enhanced reliability, maintainability, and government compliance.
FOCUS: Type safety, professional standards, reduced risk, better maintenance.
```

### KOTLink Integration Strategy Template

```text
Develop comprehensive KOTLink integration strategy for ControlStation enhancement:

PHASE 1: DEPENDENCY INTEGRATION
- Add KOTLink Maven dependency to pom.xml
- Maintain existing protocol adapter architecture
- Create KOTLink wrapper for seamless integration
- Preserve all existing WebSocket + Video functionality

PHASE 2: PROTOCOL ADAPTER ENHANCEMENT
- Replace custom MAVLink parsing with KOTLink
- Implement type-safe message handling
- Add coroutine-based message processing
- Maintain backward compatibility with existing interfaces

PHASE 3: ADVANCED FEATURES
- Leverage KOTLink's type safety for better error handling
- Add professional message validation and verification
- Implement enhanced telemetry processing capabilities
- Create government-grade audit and logging features

PHASE 4: GOVERNMENT POSITIONING
- Document professional dependency usage for compliance
- Highlight reduced risk through proven library usage
- Demonstrate enhanced reliability and maintainability
- Prepare for government certification requirements

Focus on maintaining our multi-vendor approach while professionalizing MAVLink handling.
```

### KOTLink Technical Deep Dive Template

```text
Conduct systematic technical analysis of KOTLink for ControlStation integration:

REPOSITORY ANALYSIS:
- Study KOTLink repository structure and organization
- Analyze API design and Kotlin patterns
- Identify coroutine integration points
- Review message handling capabilities

INTEGRATION ASSESSMENT:
- Map KOTLink APIs to existing protocol adapter interfaces
- Identify required wrapper patterns for seamless integration
- Assess performance implications and optimizations
- Plan migration strategy from custom implementation

GOVERNMENT-GRADE EVALUATION:
- Assess library stability and maintenance status
- Review code quality and testing coverage
- Evaluate security implications and best practices
- Document compliance benefits and risk reduction

IMPLEMENTATION PLANNING:
- Design integration architecture preserving existing patterns
- Plan phased migration approach minimizing disruption
- Create testing strategy for validation
- Prepare documentation for government submissions

Focus on actionable integration plan with minimal risk and maximum benefit.
```

### KOTLink Professional Benefits Template

```text
Document KOTLink integration benefits for government and enterprise positioning:

TECHNICAL ADVANTAGES:
- Type Safety: Eliminates runtime errors in telemetry processing
- Modern Kotlin: Leverages latest language features and patterns
- Coroutine Integration: Seamless async processing with existing architecture
- Professional Implementation: Community-tested and maintained

GOVERNMENT COMPLIANCE BENEFITS:
- Reduced Risk: Replace custom code with proven library
- Better Maintenance: Professional dependency management
- Standards Compliance: Proper MAVLink specification implementation
- Code Quality: Type-safe, modern development patterns

ENTERPRISE VALUE PROPOSITION:
- Professional Dependencies: Industry-standard library usage
- Reduced Development Time: Focus on business logic vs protocol implementation
- Enhanced Reliability: Proven implementation vs custom code
- Future-Proof: Community maintenance and updates

COMPETITIVE DIFFERENTIATION:
- Professional Engineering: Modern Kotlin patterns and best practices
- Government-Ready: Reduced risk through proven dependencies
- Multi-Vendor Excellence: Enhanced protocol handling for all drone types
- Enterprise Architecture: Professional dependency management

Create compelling narrative for SBIR submissions and enterprise sales.
```

### KOTLink Migration Planning Template

```text
Plan systematic migration from custom MAVLink to KOTLink integration:

MIGRATION STRATEGY:
1. DEPENDENCY ADDITION
   - Add KOTLink to Maven dependencies
   - Create wrapper interfaces for gradual integration
   - Maintain existing functionality during transition

2. ADAPTER ENHANCEMENT
   - Replace custom parsing with KOTLink message handling
   - Implement type-safe telemetry processing
   - Add enhanced error handling and validation

3. TESTING AND VALIDATION
   - Create comprehensive test suite for KOTLink integration
   - Validate backward compatibility with existing protocols
   - Test performance implications and optimizations

4. DOCUMENTATION AND COMPLIANCE
   - Document professional library usage for government submissions
   - Create integration guides and best practices
   - Prepare compliance documentation and risk assessments

RISK MITIGATION:
- Phased rollout maintaining existing functionality
- Comprehensive testing at each integration step
- Fallback mechanisms during transition period
- Professional dependency management practices

Focus on zero-disruption migration with enhanced capabilities.
```

### KOTLink Government Positioning Template

```text
Position KOTLink integration for government compliance and SBIR opportunities:

GOVERNMENT VALUE PROPOSITION:
- Professional Engineering: Replace custom protocol code with industry library
- Reduced Risk: Proven implementation vs in-house development
- Enhanced Reliability: Community-tested and maintained solution
- Standards Compliance: Proper MAVLink specification implementation

BLUE SUAS COMPLIANCE BENEFITS:
- Professional Dependencies: Government-grade library management
- Open Source Transparency: Full source code availability for security review
- Community Maintenance: Ongoing updates and security patches
- Standards Adherence: Proper protocol implementation reducing compliance risk

SBIR POSITIONING ADVANTAGES:
- Technical Innovation: Modern Kotlin patterns and professional architecture
- Risk Reduction: Proven libraries vs custom implementation
- Maintenance Benefits: Reduced long-term support burden
- Government Focus: Professional engineering practices for federal deployment

COMPETITIVE DIFFERENTIATION:
- Beyond Custom Implementation: Professional library usage vs hand-coded protocols
- Multi-Vendor Excellence: Enhanced MAVLink handling for all drone manufacturers
- Enterprise Architecture: Professional dependency management and patterns
- Government-Ready: Compliance-focused engineering practices

Create compelling federal funding and procurement narrative.
```
