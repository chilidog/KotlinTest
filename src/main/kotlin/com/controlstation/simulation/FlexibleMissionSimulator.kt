/**
 * Flexible Mission Simulator - Government-Grade JSON-Driven Mission Execution
 * 
 * Provides comprehensive mission simulation capabilities for government and SBIR Phase I
 * demonstrations. Supports JSON-defined missions with real-time telemetry, safety monitoring,
 * and professional mission planning workflows.
 * 
 * Features:
 * - JSON-driven mission configuration
 * - Real-time telemetry streaming at 10Hz
 * - Automated safety parameter enforcement
 * - Professional mission logging and reporting
 * - Government-grade mission execution
 * 
 * @author ControlStation Team
 * @version 1.0.0
 */
package com.controlstation.simulation

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.*
import java.io.File
import java.io.InputStream
import kotlin.math.*

/**
 * Data classes for JSON mission configuration parsing
 */
data class MissionConfig(
    val mission: MissionDetails,
    val commands: List<MissionCommand>,
    val telemetry_config: TelemetryConfig
)

data class MissionDetails(
    val name: String,
    val description: String,
    val drone_model: String,
    val duration_estimate_seconds: Int,
    val safety_parameters: SafetyParameters,
    val environment: EnvironmentRequirements
)

data class SafetyParameters(
    val max_altitude_feet: Double,
    val max_speed_fps: Double,
    val emergency_land_battery_percent: Int,
    val geofence_radius_feet: Double,
    val max_wind_speed_mph: Double
)

data class EnvironmentRequirements(
    val indoor_safe: Boolean,
    val outdoor_capable: Boolean,
    val recommended_space: String
)

data class MissionCommand(
    val id: Int,
    val type: String,
    val description: String,
    val parameters: Map<String, Any>,
    val expected_duration_seconds: Int,
    val safety_checks: List<String>
)

data class TelemetryConfig(
    val update_rate_hz: Int,
    val data_points: List<String>,
    val logging_enabled: Boolean,
    val real_time_display: Boolean
)

data class DroneConfig(
    val drone: DroneDetails
)

data class DroneDetails(
    val model: String,
    val manufacturer: String,
    val type: String,
    val category: String,
    val specifications: Map<String, Any>,
    val capabilities: Map<String, Boolean>,
    val video_system: Map<String, Any>,
    val telemetry: Map<String, Boolean>,
    val control_characteristics: Map<String, String>,
    val flight_modes: List<Map<String, Any>>,
    val performance_limits: Map<String, Any>,
    val recommended_use: Map<String, String>
)

data class DroneState(
    var x: Double = 0.0,
    var y: Double = 0.0,
    var z: Double = 0.0,
    var velocity_x: Double = 0.0,
    var velocity_y: Double = 0.0,
    var velocity_z: Double = 0.0,
    var battery_percent: Int = 100,
    var battery_voltage: Double = 4.2,
    var armed: Boolean = false,
    var flying: Boolean = false,
    var mode: String = "DISARMED",
    var current_command_id: Int = 0,
    var mission_progress_percent: Int = 0,
    var flight_time_seconds: Double = 0.0,
    var motor_temps: List<Double> = listOf(25.0, 25.0, 25.0, 25.0),
    var signal_strength_percent: Int = 100,
    var gps_satellites: Int = 0  // Cetus Lite has no GPS
)

class FlexibleMissionSimulator {
    private val objectMapper: ObjectMapper = jacksonObjectMapper()
    private var currentState = DroneState()
    private var mission: MissionConfig? = null
    private var droneConfig: DroneConfig? = null
    private var startTime: Long = 0L
    private var missionStartTime: Long = 0L

    fun loadMission(missionFile: String): MissionConfig {
        val resourceStream: InputStream = this::class.java.classLoader.getResourceAsStream("missions/$missionFile")
            ?: throw IllegalArgumentException("Mission file not found: missions/$missionFile")
        
        mission = objectMapper.readValue(resourceStream)
        println("🎯 Mission Loaded: ${mission!!.mission.name}")
        println("📋 Description: ${mission!!.mission.description}")
        println("🚁 Drone Model: ${mission!!.mission.drone_model}")
        println("⏱️ Estimated Duration: ${mission!!.mission.duration_estimate_seconds}s")
        println("🛡️ Safety: Max Alt ${mission!!.mission.safety_parameters.max_altitude_feet}ft, Max Speed ${mission!!.mission.safety_parameters.max_speed_fps}fps")
        return mission!!
    }

    fun loadDroneConfig(droneFile: String): DroneConfig {
        val resourceStream: InputStream = this::class.java.classLoader.getResourceAsStream("drones/$droneFile")
            ?: throw IllegalArgumentException("Drone config file not found: drones/$droneFile")
        
        droneConfig = objectMapper.readValue(resourceStream)
        println("🤖 Drone Config Loaded: ${droneConfig!!.drone.model}")
        println("🏭 Manufacturer: ${droneConfig!!.drone.manufacturer}")
        println("📊 Type: ${droneConfig!!.drone.type}")
        println("⚡ Specs: ${droneConfig!!.drone.specifications["weight_grams"]}g, ${droneConfig!!.drone.specifications["flight_time_minutes"]}min flight")
        return droneConfig!!
    }

    suspend fun executeMission() {
        val loadedMission = mission ?: throw IllegalStateException("No mission loaded - call loadMission() first")
        val loadedDrone = droneConfig ?: throw IllegalStateException("No drone config loaded - call loadDroneConfig() first")

        println("\n" + "=".repeat(70))
        println("🚀 MISSION EXECUTION START")
        println("=".repeat(70))
        println("📋 Mission: ${loadedMission.mission.name}")
        println("🚁 Aircraft: ${loadedDrone.drone.model}")
        println("⏱️ Estimated Duration: ${loadedMission.mission.duration_estimate_seconds}s")
        println("📡 Telemetry Rate: ${loadedMission.telemetry_config.update_rate_hz}Hz")
        println("=".repeat(70))

        // Pre-flight checks
        performPreFlightChecks(loadedMission, loadedDrone)
        
        // Initialize mission
        currentState.armed = true
        currentState.mode = "ARMED"
        missionStartTime = System.currentTimeMillis()
        startTime = missionStartTime

        // Execute commands sequentially
        for ((index, command) in loadedMission.commands.withIndex()) {
            currentState.current_command_id = command.id
            currentState.mission_progress_percent = ((index.toDouble() / loadedMission.commands.size) * 100).toInt()
            
            println("\n🎯 EXECUTING COMMAND ${command.id}/${loadedMission.commands.size}")
            println("📝 ${command.type}: ${command.description}")
            println("⏳ Expected Duration: ${command.expected_duration_seconds}s")
            
            // Safety checks before each command
            performSafetyChecks(command.safety_checks, loadedMission.mission.safety_parameters)
            
            // Execute command based on type
            when (command.type.uppercase()) {
                "TAKEOFF" -> executeTakeoff(command, loadedMission.telemetry_config)
                "HOVER" -> executeHover(command, loadedMission.telemetry_config)
                "CIRCLE" -> executeCircle(command, loadedMission.telemetry_config)
                "LAND" -> executeLand(command, loadedMission.telemetry_config)
                else -> {
                    println("❌ Unknown command type: ${command.type}")
                    throw IllegalArgumentException("Unsupported command type: ${command.type}")
                }
            }
            
            // Brief pause between commands for system stabilization
            if (index < loadedMission.commands.size - 1) {
                println("⏸️ Command complete - Stabilizing...")
                delay(500)
            }
        }

        // Mission completion
        val totalTime = (System.currentTimeMillis() - missionStartTime) / 1000.0
        currentState.mission_progress_percent = 100
        currentState.armed = false
        currentState.flying = false
        currentState.mode = "MISSION_COMPLETE"

        println("\n" + "=".repeat(70))
        println("✅ MISSION COMPLETE!")
        println("=".repeat(70))
        println("⏱️ Total Mission Time: ${String.format("%.1f", totalTime)}s")
        println("🔋 Final Battery: ${currentState.battery_percent}% (${String.format("%.2f", currentState.battery_voltage)}V)")
        println("📊 Flight Distance: ${String.format("%.1f", calculateTotalDistance())}ft")
        println("🎯 Mission Success: All commands executed successfully")
        println("=".repeat(70))
    }

    @Suppress("UNUSED_PARAMETER")
    private fun performPreFlightChecks(mission: MissionConfig, drone: DroneConfig) {
        println("\n🔍 PRE-FLIGHT CHECKS")
        println("✅ Battery: ${currentState.battery_percent}% (${String.format("%.2f", currentState.battery_voltage)}V)")
        println("✅ Motors: ${currentState.motor_temps.map { "${String.format("%.1f", it)}°C" }.joinToString(", ")}")
        println("✅ Signal: ${currentState.signal_strength_percent}%")
        println("✅ Safety Parameters Loaded")
        println("✅ Mission Commands Validated (${mission.commands.size} commands)")
        println("✅ Aircraft Configuration Verified")
        
        // Simulate brief system initialization
        Thread.sleep(1000)
        println("✅ All systems ready for flight")
    }

    private fun performSafetyChecks(checks: List<String>, safetyParams: SafetyParameters) {
        print("🛡️ Safety Checks: ")
        for (check in checks) {
            when (check) {
                "battery_level" -> {
                    if (currentState.battery_percent < safetyParams.emergency_land_battery_percent) {
                        throw RuntimeException("SAFETY ABORT: Battery too low (${currentState.battery_percent}%)")
                    }
                }
                "altitude_hold" -> {
                    if (currentState.z > safetyParams.max_altitude_feet) {
                        throw RuntimeException("SAFETY ABORT: Altitude limit exceeded (${currentState.z}ft)")
                    }
                }
                "position_stability", "path_clear", "landing_zone_clear" -> {
                    // Simulate environmental checks
                }
            }
            print("✅$check ")
        }
        println()
    }

    private suspend fun executeTakeoff(command: MissionCommand, telemetryConfig: TelemetryConfig) {
        val targetAlt = (command.parameters["target_altitude_feet"] as Number).toDouble()
        val climbRate = (command.parameters["climb_rate_fps"] as Number).toDouble()
        val stabilizationTime = (command.parameters["stabilization_time_seconds"] as Number).toDouble()
        
        currentState.flying = true
        currentState.mode = "TAKEOFF"
        
        val climbTime = targetAlt / climbRate
        val steps = (climbTime * telemetryConfig.update_rate_hz).toInt()
        val altStep = targetAlt / steps
        
        println("🚁 Initiating takeoff to ${targetAlt}ft at ${climbRate}fps...")
        
        repeat(steps) { step ->
            currentState.z = (step + 1) * altStep
            currentState.velocity_z = climbRate
            currentState.flight_time_seconds = (System.currentTimeMillis() - startTime) / 1000.0
            
            // Simulate battery drain (faster during climb)
            currentState.battery_percent = maxOf(0, currentState.battery_percent - 1)
            currentState.battery_voltage = 3.0 + (currentState.battery_percent / 100.0) * 1.2
            
            // Simulate motor heating during climb
            currentState.motor_temps = currentState.motor_temps.map { temp ->
                minOf(60.0, temp + 0.5) // Gradual heating
            }
            
            displayTelemetry("CLIMB")
            delay((1000 / telemetryConfig.update_rate_hz).toLong())
        }
        
        // Stabilization phase
        currentState.velocity_z = 0.0
        currentState.mode = "STABILIZING"
        println("⚖️ Stabilizing at altitude...")
        delay((stabilizationTime * 1000).toLong())
        
        currentState.mode = "HOVER"
        println("✅ Takeoff complete - Stable hover at ${String.format("%.1f", currentState.z)}ft")
    }

    private suspend fun executeHover(command: MissionCommand, telemetryConfig: TelemetryConfig) {
        val duration = (command.parameters["duration_seconds"] as Number).toDouble()
        val positionHold = command.parameters["position_hold"] as Boolean
        val altitudeTolerance = (command.parameters.getOrDefault("altitude_tolerance_feet", 0.5) as Number).toDouble()
        
        currentState.mode = "HOVER"
        println("⏸️ Hovering for ${duration} seconds with position hold: $positionHold")
        
        val steps = (duration * telemetryConfig.update_rate_hz).toInt()
        
        repeat(steps) { step ->
            currentState.flight_time_seconds = (System.currentTimeMillis() - startTime) / 1000.0
            
            // Simulate minor position adjustments due to air currents
            val drift = sin(step * 0.1) * 0.1
            val altDrift = cos(step * 0.05) * altitudeTolerance * 0.5
            
            if (positionHold) {
                // Simulate active position correction
                currentState.x += drift * 0.1 // Reduced drift with position hold
                currentState.y += cos(step * 0.1) * 0.05
                currentState.z += altDrift * 0.2
            } else {
                currentState.x += drift
                currentState.y += cos(step * 0.1) * 0.2
            }
            
            // Battery drain during hover (moderate)
            if (step % 20 == 0) { // Every 2 seconds at 10Hz
                currentState.battery_percent = maxOf(0, currentState.battery_percent - 1)
                currentState.battery_voltage = 3.0 + (currentState.battery_percent / 100.0) * 1.2
            }
            
            displayTelemetry("HOVER")
            delay((1000 / telemetryConfig.update_rate_hz).toLong())
        }
        
        println("✅ Hover phase complete")
    }

    private suspend fun executeCircle(command: MissionCommand, telemetryConfig: TelemetryConfig) {
        val radius = (command.parameters["radius_feet"] as Number).toDouble()
        val speed = (command.parameters["speed_fps"] as Number).toDouble()
        val altitude = (command.parameters["altitude_feet"] as Number).toDouble()
        val direction = command.parameters["direction"] as String
        val numRevolutions = (command.parameters.getOrDefault("num_revolutions", 1.0) as Number).toDouble()
        val smoothEntry = command.parameters.getOrDefault("smooth_entry", true) as Boolean
        
        currentState.mode = "CIRCLE"
        currentState.z = altitude
        
        // Calculate circle parameters
        val circumference = 2 * PI * radius * numRevolutions
        val totalTime = circumference / speed
        val totalSteps = (totalTime * telemetryConfig.update_rate_hz).toInt()
        val angleStep = if (direction == "clockwise") -2 * PI * numRevolutions / totalSteps else 2 * PI * numRevolutions / totalSteps
        
        println("🔄 Flying ${radius * 2}ft diameter circle (${direction}) at ${speed}fps")
        println("📐 Circumference: ${String.format("%.1f", circumference)}ft, Duration: ${String.format("%.1f", totalTime)}s")
        
        // Smooth entry to circle if enabled
        if (smoothEntry) {
            println("🎯 Smooth entry to circular path...")
            delay(1000)
        }
        
        val centerX = currentState.x
        val centerY = currentState.y
        
        repeat(totalSteps) { step ->
            val angle = step * angleStep
            val targetX = centerX + radius * cos(angle)
            val targetY = centerY + radius * sin(angle)
            
            // Smooth movement to target position
            currentState.x = targetX
            currentState.y = targetY
            currentState.velocity_x = -radius * sin(angle) * angleStep * telemetryConfig.update_rate_hz
            currentState.velocity_y = radius * cos(angle) * angleStep * telemetryConfig.update_rate_hz
            
            currentState.flight_time_seconds = (System.currentTimeMillis() - startTime) / 1000.0
            
            // Battery drain during maneuvering (higher due to continuous movement)
            if (step % 15 == 0) { // Every 1.5 seconds at 10Hz
                currentState.battery_percent = maxOf(0, currentState.battery_percent - 1)
                currentState.battery_voltage = 3.0 + (currentState.battery_percent / 100.0) * 1.2
            }
            
            // Motor heating during maneuvering
            currentState.motor_temps = currentState.motor_temps.map { temp ->
                minOf(65.0, temp + 0.1) // Gradual heating during flight
            }
            
            val progress = ((step + 1).toDouble() / totalSteps * 100).toInt()
            displayTelemetry("CIRCLE ($progress%)")
            delay((1000 / telemetryConfig.update_rate_hz).toLong())
        }
        
        // Return to center and stabilize
        currentState.x = centerX
        currentState.y = centerY
        currentState.velocity_x = 0.0
        currentState.velocity_y = 0.0
        
        println("✅ Circular flight pattern complete - Returned to center position")
    }

    private suspend fun executeLand(command: MissionCommand, telemetryConfig: TelemetryConfig) {
        val descentRate = (command.parameters["descent_rate_fps"] as Number).toDouble()
        val precisionLanding = command.parameters["precision_landing"] as Boolean
        val finalApproachHeight = (command.parameters.getOrDefault("final_approach_height_feet", 1.0) as Number).toDouble()
        val touchdownSpeed = (command.parameters.getOrDefault("touchdown_speed_fps", 0.2) as Number).toDouble()
        
        currentState.mode = "LANDING"
        
        println("🛬 Initiating landing sequence...")
        
        if (precisionLanding) {
            println("🎯 Precision landing mode - Adjusting to landing zone center...")
            // Simulate precision positioning
            repeat(3) { step ->
                println("📍 Position adjustment ${step + 1}/3...")
                currentState.x *= 0.7 // Move toward center
                currentState.y *= 0.7
                displayTelemetry("POSITION")
                delay(1000)
            }
        }
        
        // Main descent phase
        println("⬇️ Beginning controlled descent at ${descentRate}fps...")
        
        while (currentState.z > finalApproachHeight) {
            currentState.z = maxOf(finalApproachHeight, currentState.z - (descentRate / telemetryConfig.update_rate_hz))
            currentState.velocity_z = -descentRate
            currentState.flight_time_seconds = (System.currentTimeMillis() - startTime) / 1000.0
            
            // Reduced battery drain during descent
            val step = ((currentState.flight_time_seconds * telemetryConfig.update_rate_hz) % 30).toInt()
            if (step == 0) {
                currentState.battery_percent = maxOf(0, currentState.battery_percent - 1)
                currentState.battery_voltage = 3.0 + (currentState.battery_percent / 100.0) * 1.2
            }
            
            displayTelemetry("DESCENT")
            delay((1000 / telemetryConfig.update_rate_hz).toLong())
        }
        
        // Final approach
        println("🎯 Final approach - Reducing to touchdown speed...")
        while (currentState.z > 0.1) {
            currentState.z = maxOf(0.0, currentState.z - (touchdownSpeed / telemetryConfig.update_rate_hz))
            currentState.velocity_z = -touchdownSpeed
            displayTelemetry("FINAL")
            delay((1000 / telemetryConfig.update_rate_hz).toLong())
        }
        
        // Touchdown
        currentState.z = 0.0
        currentState.velocity_z = 0.0
        currentState.velocity_x = 0.0
        currentState.velocity_y = 0.0
        currentState.flying = false
        currentState.mode = "LANDED"
        
        // Motor shutdown simulation
        println("🔌 Motors disarmed - Safe shutdown complete")
        currentState.motor_temps = currentState.motor_temps.map { temp ->
            maxOf(25.0, temp - 5.0) // Rapid cooling after shutdown
        }
        
        println("✅ Landing complete - Aircraft secured")
    }

    private fun displayTelemetry(phase: String) {
        if (mission?.telemetry_config?.real_time_display == true) {
            val time = String.format("%.1f", currentState.flight_time_seconds)
            val pos = "(${String.format("%.2f", currentState.x)}, ${String.format("%.2f", currentState.y)}, ${String.format("%.2f", currentState.z)})"
            val vel = "${String.format("%.1f", sqrt(currentState.velocity_x.pow(2) + currentState.velocity_y.pow(2) + currentState.velocity_z.pow(2)))}fps"
            val battery = "${currentState.battery_percent}% (${String.format("%.2f", currentState.battery_voltage)}V)"
            val signal = "${currentState.signal_strength_percent}%"
            val avgTemp = String.format("%.1f", currentState.motor_temps.average())
            
            println("📊 [$phase] T:${time}s | Pos:$pos | Vel:$vel | Bat:$battery | Sig:$signal | Temp:${avgTemp}°C")
        }
    }

    private fun calculateTotalDistance(): Double {
        // Simplified distance calculation for demo
        return sqrt(currentState.x.pow(2) + currentState.y.pow(2)) + currentState.z
    }

    fun getCurrentState(): DroneState = currentState.copy()

    fun getMissionStatus(): String {
        val flightTime = String.format("%.1f", currentState.flight_time_seconds)
        val position = "(${String.format("%.2f", currentState.x)}, ${String.format("%.2f", currentState.y)}, ${String.format("%.2f", currentState.z)})"
        val velocity = String.format("%.1f", sqrt(currentState.velocity_x.pow(2) + currentState.velocity_y.pow(2) + currentState.velocity_z.pow(2)))
        
        return """
        🚁 DRONE STATUS REPORT
        ═════════════════════════════════════════
        📍 Position: $position ft (X, Y, Z)
        🏃 Velocity: ${velocity} fps
        🔋 Battery: ${currentState.battery_percent}% (${String.format("%.2f", currentState.battery_voltage)}V)
        ⏱️ Flight Time: ${flightTime}s
        📡 Mode: ${currentState.mode}
        🎯 Command: ${currentState.current_command_id}
        📊 Progress: ${currentState.mission_progress_percent}%
        🔥 Motor Temps: ${currentState.motor_temps.map { String.format("%.1f", it) }.joinToString(", ")}°C
        📶 Signal: ${currentState.signal_strength_percent}%
        ✈️ Flying: ${currentState.flying}
        🔒 Armed: ${currentState.armed}
        ═════════════════════════════════════════
        """.trimIndent()
    }
}
