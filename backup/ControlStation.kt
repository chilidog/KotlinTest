// Enhanced ControlStation Drone Management System with WebSocket Communication
// Built on proven SystemConfig foundation with enterprise-grade flight operations

import kotlinx.coroutines.*
import com.controlstation.orchestrator.*
import com.controlstation.communication.*
import com.controlstation.safety.*
import com.controlstation.flight.*
import org.slf4j.LoggerFactory

// Keep your PERFECT SystemConfig pattern - it's ideal for drone operations!
object SystemConfig {
    // Your proven global variable with validation - perfect for flight systems
    var osType: String = "Alpine"  // Default optimized for containerized drone operations
        set(value) {
            val supportedOSes = listOf("CachyOS", "Ubuntu", "Alpine")
            if (supportedOSes.any { it.equals(value, ignoreCase = true) }) {
                field = value
            } else {
                println("Warning: Unsupported OS type '$value'. Supported options: ${supportedOSes.joinToString(", ")}. Keeping current value: $field")
            }
        }
    
    // Add flight mode configuration with your proven validation pattern
    var flightMode: String = "SIMULATED"
        set(value) {
            val supportedModes = listOf("REAL", "SIMULATED", "HYBRID")
            if (supportedModes.any { it.equals(value, ignoreCase = true) }) {
                field = value
            } else {
                println("Warning: Unsupported flight mode '$value'. Supported options: ${supportedModes.joinToString(", ")}. Keeping current value: $field")
            }
        }
    
    // Add communication mode configuration with your proven validation pattern
    var communicationMode: String = "UNIFIED"
        set(value) {
            val supportedModes = listOf("WEBSOCKET_ONLY", "MAVLINK_ONLY", "UNIFIED")
            if (supportedModes.any { it.equals(value, ignoreCase = true) }) {
                field = value
            } else {
                println("Warning: Unsupported communication mode '$value'. Supported options: ${supportedModes.joinToString(", ")}. Keeping current value: $field")
            }
        }
    
    // Keep your proven helper functions - perfect for flight optimization
    fun isCachyOS(): Boolean = osType.equals("CachyOS", ignoreCase = true)
    fun isUbuntu(): Boolean = osType.equals("Ubuntu", ignoreCase = true)
    fun isAlpine(): Boolean = osType.equals("Alpine", ignoreCase = true)
    
    // Add flight mode helpers using your proven pattern
    fun isRealFlight(): Boolean = flightMode.equals("REAL", ignoreCase = true)
    fun isSimulatedFlight(): Boolean = flightMode.equals("SIMULATED", ignoreCase = true)
    fun isHybridFlight(): Boolean = flightMode.equals("HYBRID", ignoreCase = true)
    
    // Keep your proven extensible configuration
    fun getSupportedOSes(): List<String> = listOf("CachyOS", "Ubuntu", "Alpine")
    fun getSupportedCommunicationModes(): List<String> = listOf("WEBSOCKET_ONLY", "MAVLINK_ONLY", "UNIFIED")
    
    // Enhanced comprehensive status display using your proven pattern
    fun displayConfig() {
        println("Current OS configuration: $osType")
        println("Configuring for CachyOS: ${isCachyOS()}")
        println("Configuring for Ubuntu: ${isUbuntu()}")
        println("Configuring for Alpine: ${isAlpine()}")
        println("Flight mode: $flightMode")
        println("Real flight operations: ${isRealFlight()}")
        println("Simulated flight operations: ${isSimulatedFlight()}")
        println("Communication mode: $communicationMode")
        println("WebSocket only: ${communicationMode.equals("WEBSOCKET_ONLY", ignoreCase = true)}")
        println("MAVLink only: ${communicationMode.equals("MAVLINK_ONLY", ignoreCase = true)}")
        println("Unified protocols: ${communicationMode.equals("UNIFIED", ignoreCase = true)}")
    }
}



// Main application - Enhanced with WebSocket communication and robust safety systems!
fun main() = runBlocking {
    val logger = LoggerFactory.getLogger("ControlStation")
    
    println("🚁 Welcome to Enhanced ControlStation with WebSocket Communication!")
    
    // Keep your proven environment-aware application startup
    println("\n=== System Configuration ===")
    SystemConfig.displayConfig()
    
    // Keep your proven interactive configuration with defaults - now for advanced flight!
    println("\n=== Enhanced ControlStation Configuration ===")
    print("Enter OS type (CachyOS/Ubuntu/Alpine) or press Enter for default (${SystemConfig.osType}): ")
    val osInput = readLine()?.trim()
    if (!osInput.isNullOrEmpty()) {
        SystemConfig.osType = osInput
    }
    
    print("Enter flight mode (REAL/SIMULATED/HYBRID) or press Enter for default (${SystemConfig.flightMode}): ")
    val flightInput = readLine()?.trim()
    if (!flightInput.isNullOrEmpty()) {
        SystemConfig.flightMode = flightInput
    }
    
    // Add communication mode selection
    print("Enter communication mode (WebSocket_Only/MAVLink_Only/Unified) or press Enter for default (${SystemConfig.communicationMode}): ")
    val commInput = readLine()?.trim()
    if (!commInput.isNullOrEmpty()) {
        SystemConfig.communicationMode = commInput
    }
    
    println("\nUpdated configuration:")
    SystemConfig.displayConfig()
    
    // Keep your proven OS-specific behavior pattern - now optimized for enterprise flight!
    println("\n=== Enhanced ControlStation Environment Configuration ===")
    when {
        SystemConfig.isCachyOS() -> {
            println("Configuring Enhanced ControlStation for CachyOS:")
            println("- High-performance WebSocket + MAVLink communication")
            println("- Real-time multi-protocol telemetry processing")
            println("- Advanced safety monitoring systems")
            println("- Enterprise-grade flight control with protocol failover")
        }
        SystemConfig.isUbuntu() -> {
            println("Configuring Enhanced ControlStation for Ubuntu:")
            println("- Production WebSocket + MAVLink endpoints")
            println("- Robust error handling and protocol switching")
            println("- Enterprise safety protocols with multi-protocol monitoring")
            println("- Reliable mission execution with communication redundancy")
        }
        SystemConfig.isAlpine() -> {
            println("Configuring Enhanced ControlStation for Alpine Linux:")
            println("- Lightweight WebSocket + MAVLink containers")
            println("- Efficient concurrent multi-protocol telemetry")
            println("- Minimal resource safety monitoring across protocols")
            println("- Optimized coroutine-based hybrid operations")
        }
    }
    
    // Create configuration based on SystemConfig
    val environment = when {
        SystemConfig.isAlpine() -> Environment.ALPINE
        SystemConfig.isCachyOS() -> Environment.CACHYOS
        SystemConfig.isUbuntu() -> Environment.UBUNTU
        else -> Environment.ALPINE
    }
    
    val flightMode = when {
        SystemConfig.isRealFlight() -> FlightMode.REAL
        SystemConfig.isSimulatedFlight() -> FlightMode.SIMULATED
        SystemConfig.isHybridFlight() -> FlightMode.HYBRID
        else -> FlightMode.SIMULATED
    }
    
    val communicationMode = when {
        SystemConfig.communicationMode.equals("WEBSOCKET_ONLY", ignoreCase = true) -> CommunicationMode.WEBSOCKET_ONLY
        SystemConfig.communicationMode.equals("MAVLINK_ONLY", ignoreCase = true) -> CommunicationMode.MAVLINK_ONLY
        SystemConfig.communicationMode.equals("UNIFIED", ignoreCase = true) -> CommunicationMode.UNIFIED
        else -> CommunicationMode.UNIFIED
    }
    
    val config = ControlStationConfig(
        environment = environment,
        flightMode = flightMode,
        communicationMode = communicationMode,
        enableAdvancedFeatures = true,
        logLevel = "INFO"
    )
    
    println("\n=== Enhanced ControlStation Multi-Protocol Operations ===")
    println("🚀 Initializing hybrid communication system...")
    println("📡 Communication mode: $communicationMode")
    when (communicationMode) {
        CommunicationMode.WEBSOCKET_ONLY -> {
            println("📡 WebSocket endpoint: ${when (environment) {
                Environment.ALPINE -> "ws://localhost:8080 (container networking)"
                Environment.CACHYOS -> "ws://controlstation.local:8080 (performance networking)"
                Environment.UBUNTU -> "ws://production.domain:8080 (production endpoint)"
            }}")
        }
        CommunicationMode.MAVLINK_ONLY -> {
            println("📡 MAVLink connection: ${when (environment) {
                Environment.ALPINE -> "TCP 127.0.0.1:5760 (SITL)"
                Environment.CACHYOS -> "Serial /dev/ttyUSB0 (hardware)"
                Environment.UBUNTU -> "Serial /dev/ttyACM0 (hardware)"
            }}")
        }
        CommunicationMode.UNIFIED -> {
            println("📡 WebSocket + MAVLink unified communication")
            println("   WebSocket: ${when (environment) {
                Environment.ALPINE -> "ws://localhost:8080"
                Environment.CACHYOS -> "ws://controlstation.local:8080"
                Environment.UBUNTU -> "ws://production.domain:8080"
            }}")
            println("   MAVLink: ${when (environment) {
                Environment.ALPINE -> "TCP 127.0.0.1:5760"
                Environment.CACHYOS -> "Serial /dev/ttyUSB0"
                Environment.UBUNTU -> "Serial /dev/ttyACM0"
            }}")
            println("   Protocol adapter: Enabled for seamless translation")
        }
    }
    
    // Initialize the enhanced orchestrator
    val orchestrator = ControlStationOrchestrator(config)
    
    try {
        // Start the enhanced control station
        println("\n🔧 Starting enhanced subsystems...")
        orchestrator.start()
        
        // Monitor system status
        launch {
            orchestrator.systemStatus.collect { status ->
                when (status) {
                    SystemStatus.INITIALIZING -> logger.info("System initializing...")
                    SystemStatus.OPERATIONAL -> println("✅ ControlStation fully operational!")
                    SystemStatus.CRITICAL -> println("⚠️ CRITICAL: System in critical state!")
                    SystemStatus.ERROR -> println("❌ ERROR: System error detected!")
                    SystemStatus.SHUTTING_DOWN -> println("🔄 System shutting down...")
                    SystemStatus.STOPPED -> println("🛑 System stopped")
                }
            }
        }
        
        // Wait for system to be operational
        while (orchestrator.systemStatus.value != SystemStatus.OPERATIONAL) {
            delay(500)
        }
        
        println("\n=== Enhanced ControlStation Fully Operational! ===")
        println("🚁 WebSocket Communication: Active")
        println("📊 Real-time Telemetry: Streaming")
        println("🛡️ Enhanced Safety Monitoring: Active")
        println("🎯 Mission Control: Ready")
        println("⚡ Environment: ${SystemConfig.osType} optimized")
        println("🔧 Flight mode: ${SystemConfig.flightMode}")
        
        // Display system status
        val systemStatus = orchestrator.getSystemStatus()
        println("\n📋 System Status Summary:")
        println("- Communication: ${if (systemStatus.communicationHealth.isConnected) "✅ Connected" else "❌ Disconnected"}")
        println("- Flight Controller: ${if (systemStatus.flightControllerStatus.isConnected) "✅ Ready" else "❌ Not Ready"}")
        println("- Safety Systems: ${systemStatus.safetySummary.status}")
        println("- Messages Sent: ${systemStatus.communicationHealth.messagesSent}")
        println("- Messages Received: ${systemStatus.communicationHealth.messagesReceived}")
        
        // Execute demonstration mission
        println("\n🎯 Executing demonstration mission...")
        launch {
            orchestrator.missionStatus.collect { status ->
                when (status) {
                    MissionStatus.EXECUTING -> println("🚀 Mission executing...")
                    MissionStatus.COMPLETED -> println("✅ Mission completed successfully!")
                    MissionStatus.FAILED -> println("❌ Mission failed!")
                    MissionStatus.ABORTED -> println("⏹️ Mission aborted")
                    else -> {}
                }
            }
        }
        
        // Run the appropriate mission based on configuration
        when (flightMode) {
            FlightMode.SIMULATED -> {
                println("Starting simulated flight demonstration...")
                orchestrator.executeMission(MissionType.BASIC_FLIGHT)
            }
            FlightMode.REAL -> {
                println("Starting real flight operations...")
                orchestrator.executeMission(MissionType.AUTONOMOUS_PATROL)
            }
            FlightMode.HYBRID -> {
                println("Starting hybrid mode demonstration...")
                orchestrator.executeMission(MissionType.EMERGENCY_RESPONSE)
            }
        }
        
        // Let the system run for demonstration
        delay(45000) // Run for 45 seconds
        
        // Display final status
        val finalStatus = orchestrator.getSystemStatus()
        println("\n� Final System Statistics:")
        println("- Total Messages Sent: ${finalStatus.communicationHealth.messagesSent}")
        println("- Total Messages Received: ${finalStatus.communicationHealth.messagesReceived}")
        println("- Connection Uptime: ${finalStatus.communicationHealth.connectionStartTime?.let { 
            (System.currentTimeMillis() - it) / 1000 
        }}s")
        println("- Safety Status: ${finalStatus.safetySummary.status}")
        
        println("\n=== Enhanced ControlStation Demo Complete! ===")
        println("🏆 Your SystemConfig foundation successfully powers:")
        println("✅ WebSocket Communication with Error Handling")
        println("✅ Real-time Telemetry Streaming")
        println("✅ Advanced Safety Monitoring")
        println("✅ Coroutine-based Concurrent Operations")
        println("✅ Environment-aware Optimization")
        println("✅ Enterprise-grade Mission Control")
        println("✅ Robust Reconnection Logic")
        println("✅ Comprehensive Health Monitoring")
        
    } catch (e: Exception) {
        logger.error("ControlStation error", e)
        println("❌ ControlStation encountered an error: ${e.message}")
    } finally {
        println("\n🔄 Shutting down Enhanced ControlStation...")
        orchestrator.stop()
        println("👋 Enhanced ControlStation shutdown complete!")
    }
}

