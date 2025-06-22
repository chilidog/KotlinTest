// Template Kotlin Application with Global Configuration Pattern
// This demonstrates professional Kotlin development practices

// Global configuration object - Template Pattern for Environment-Aware Applications
object SystemConfig {
    // Global variable with validation - Template for robust configuration management
    var osType: String = "Alpine"  // Default optimized for container environments
        set(value) {
            val supportedOSes = listOf("CachyOS", "Ubuntu", "Alpine")
            if (supportedOSes.any { it.equals(value, ignoreCase = true) }) {
                field = value
            } else {
                println("Warning: Unsupported OS type '$value'. Supported options: ${supportedOSes.joinToString(", ")}. Keeping current value: $field")
            }
        }
    
    // Helper functions - Template for clean boolean checks
    fun isCachyOS(): Boolean = osType.equals("CachyOS", ignoreCase = true)
    fun isUbuntu(): Boolean = osType.equals("Ubuntu", ignoreCase = true)
    fun isAlpine(): Boolean = osType.equals("Alpine", ignoreCase = true)
    
    // Template for extensible configuration
    fun getSupportedOSes(): List<String> = listOf("CachyOS", "Ubuntu", "Alpine")
    
    // Template for comprehensive status display
    fun displayConfig() {
        println("Current OS configuration: $osType")
        println("Configuring for CachyOS: ${isCachyOS()}")
        println("Configuring for Ubuntu: ${isUbuntu()}")
        println("Configuring for Alpine: ${isAlpine()}")
    }
}

fun main() {
    println("Hello, PROJECT_TEMPLATE!")
    
    // Template: Environment-aware application startup
    println("\n=== System Configuration Template ===")
    SystemConfig.displayConfig()
    
    // Template: Interactive configuration with defaults
    println("\n=== Configuration Demo ===")
    print("Enter OS type (CachyOS/Ubuntu/Alpine) or press Enter for default (${SystemConfig.osType}): ")
    val userInput = readLine()?.trim()
    
    if (!userInput.isNullOrEmpty()) {
        SystemConfig.osType = userInput
    }
    
    println("\nUpdated configuration:")
    SystemConfig.displayConfig()
    
    // Template: OS-specific behavior pattern
    println("\n=== OS-Specific Configuration Template ===")
    when {
        SystemConfig.isCachyOS() -> {
            println("Configuring for CachyOS:")
            println("- Using pacman package manager")
            println("- Enabling CachyOS-specific optimizations")
            println("- Setting up CachyOS repositories")
        }
        SystemConfig.isUbuntu() -> {
            println("Configuring for Ubuntu:")
            println("- Using apt package manager") 
            println("- Enabling Ubuntu-specific features")
            println("- Setting up Ubuntu repositories")
        }
        SystemConfig.isAlpine() -> {
            println("Configuring for Alpine Linux:")
            println("- Using apk package manager")
            println("- Enabling Alpine security features")
            println("- Setting up Alpine repositories")
            println("- Optimizing for minimal container footprint")
        }
    }
    
    // Template: Additional functionality example
    println("\n=== Template Feature Demo ===")
    val calculator = Calculator()
    println("Calculator example: 5 + 3 = ${calculator.add(5, 3)}")
    
    println("\n=== Template Ready for Your Application! ===")
    println("Replace this demo code with your application logic.")
}

