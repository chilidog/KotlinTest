// Global configuration object for OS detection
object SystemConfig {
    // Global variable to determine OS type with default value "Alpine" (matches Codespace environment)
    var osType: String = "Alpine"
        set(value) {
            val supportedOSes = listOf("CachyOS", "Ubuntu", "Alpine")
            if (supportedOSes.any { it.equals(value, ignoreCase = true) }) {
                field = value
            } else {
                println("Warning: Unsupported OS type '$value'. Supported options: ${supportedOSes.joinToString(", ")}. Keeping current value: $field")
            }
        }
    
    // Helper function to check if we're configuring for CachyOS
    fun isCachyOS(): Boolean = osType.equals("CachyOS", ignoreCase = true)
    
    // Helper function to check if we're configuring for Ubuntu
    fun isUbuntu(): Boolean = osType.equals("Ubuntu", ignoreCase = true)
    
    // Helper function to check if we're configuring for Alpine
    fun isAlpine(): Boolean = osType.equals("Alpine", ignoreCase = true)
    
    // Function to get supported OS types
    fun getSupportedOSes(): List<String> = listOf("CachyOS", "Ubuntu", "Alpine")
    
    // Function to display current configuration
    fun displayConfig() {
        println("Current OS configuration: $osType")
        println("Configuring for CachyOS: ${isCachyOS()}")
        println("Configuring for Ubuntu: ${isUbuntu()}")
        println("Configuring for Alpine: ${isAlpine()}")
    }
}

fun main() {
    println("Hello, Kotlin Test World!")
    
    // Display default configuration
    println("\n=== System Configuration ===")
    SystemConfig.displayConfig()
    
    // Demonstrate changing the OS type
    println("\n=== Changing OS Configuration ===")
    print("Enter OS type (CachyOS/Ubuntu/Alpine) or press Enter for default (${SystemConfig.osType}): ")
    val userInput = readLine()?.trim()
    
    if (!userInput.isNullOrEmpty()) {
        SystemConfig.osType = userInput
    }
    
    println("\nUpdated configuration:")
    SystemConfig.displayConfig()
    
    // Demonstrate OS-specific behavior
    println("\n=== OS-Specific Configuration ===")
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
    
    // Calculator demonstration
    println("\n=== Calculator Demo ===")
    val calculator = Calculator()
    println("5 + 3 = ${calculator.add(5, 3)}")
    println("10 - 4 = ${calculator.subtract(10, 4)}")
    println("6 * 7 = ${calculator.multiply(6, 7)}")
    println("15 / 3 = ${calculator.divide(15, 3)}")
}

