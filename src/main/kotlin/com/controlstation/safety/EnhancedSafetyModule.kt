package com.controlstation.safety

import com.controlstation.communication.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

/**
 * Enhanced safety module that monitors communication health and drone status
 */
class EnhancedSafetyModule(
    private val communicationModule: WebSocketCommunicationModule,
    private val emergencyLandingCallback: suspend (String) -> Unit = {},
    private val warningCallback: (String) -> Unit = {}
) {
    private val logger = LoggerFactory.getLogger(EnhancedSafetyModule::class.java)
    
    private val isRunning = AtomicBoolean(false)
    private val lastSafetyCheck = AtomicLong(System.currentTimeMillis())
    private var latestTelemetry: TelemetryData? = null
    
    // Safety thresholds
    private val criticalBatteryLevel = 15
    private val warningBatteryLevel = 25
    private val maxCommunicationLossMs = 10000L // 10 seconds
    private val maxReconnectAttempts = 5
    private val minGpsSatellites = 6
    private val maxAltitude = 120.0 // 120 meters (400 feet AGL limit)
    
    // Safety state tracking
    private val _safetyStatus = MutableStateFlow(SafetyStatus.NORMAL)
    val safetyStatus: StateFlow<SafetyStatus> = _safetyStatus.asStateFlow()
    
    private val _safetyAlerts = MutableSharedFlow<SafetyAlert>()
    val safetyAlerts: SharedFlow<SafetyAlert> = _safetyAlerts.asSharedFlow()
    
    /**
     * Start safety monitoring
     */
    suspend fun start(scope: CoroutineScope) {
        if (isRunning.compareAndSet(false, true)) {
            logger.info("Starting enhanced safety monitoring")
            
            scope.launch { monitorCommunicationHealth() }
            scope.launch { monitorTelemetryData() }
            scope.launch { performPeriodicSafetyChecks() }
        }
    }
    
    /**
     * Stop safety monitoring
     */
    fun stop() {
        if (isRunning.compareAndSet(true, false)) {
            logger.info("Stopping safety monitoring")
        }
    }
    
    /**
     * Monitor communication health continuously
     */
    private suspend fun monitorCommunicationHealth() {
        communicationModule.connectionStatusFlow.collect { isConnected ->
            if (!isConnected) {
                val health = communicationModule.getConnectionHealth()
                
                when {
                    health.reconnectAttempts >= maxReconnectAttempts -> {
                        handleCriticalSafetyEvent(
                            SafetyEvent.COMMUNICATION_LOST,
                            "Communication completely lost after ${health.reconnectAttempts} attempts"
                        )
                    }
                    
                    health.lastMessageReceived?.let { 
                        System.currentTimeMillis() - it > maxCommunicationLossMs 
                    } == true -> {
                        handleWarningSafetyEvent(
                            SafetyEvent.COMMUNICATION_DEGRADED,
                            "No messages received for ${(System.currentTimeMillis() - health.lastMessageReceived!!) / 1000}s"
                        )
                    }
                }
            } else {
                // Connection restored
                if (_safetyStatus.value == SafetyStatus.CRITICAL_COMMUNICATION) {
                    logger.info("Communication restored")
                    _safetyStatus.value = SafetyStatus.NORMAL
                    emitAlert(SafetyAlert(
                        SafetyEvent.COMMUNICATION_RESTORED,
                        SafetyLevel.INFO,
                        "Communication link restored"
                    ))
                }
            }
        }
    }
    
    /**
     * Monitor incoming telemetry data for safety issues
     */
    private suspend fun monitorTelemetryData() {
        communicationModule.telemetryFlow.collect { telemetry ->
            latestTelemetry = telemetry
            lastSafetyCheck.set(System.currentTimeMillis())
            
            // Check battery levels
            checkBatteryStatus(telemetry.battery)
            
            // Check GPS status
            checkGpsStatus(telemetry.gps)
            
            // Check altitude limits
            checkAltitudeLimits(telemetry.altitude)
            
            // Check flight mode consistency
            checkFlightMode(telemetry)
            
            // Check for emergency conditions
            checkEmergencyConditions(telemetry)
        }
    }
    
    /**
     * Perform periodic safety checks
     */
    private suspend fun performPeriodicSafetyChecks() {
        while (isRunning.get()) {
            try {
                val currentTime = System.currentTimeMillis()
                val timeSinceLastCheck = currentTime - lastSafetyCheck.get()
                
                // Check for stale telemetry
                if (timeSinceLastCheck > maxCommunicationLossMs) {
                    handleCriticalSafetyEvent(
                        SafetyEvent.TELEMETRY_TIMEOUT,
                        "No telemetry received for ${timeSinceLastCheck / 1000}s"
                    )
                }
                
                // Perform health check
                performSystemHealthCheck()
                
                delay(5000) // Check every 5 seconds
            } catch (e: Exception) {
                logger.error("Error in periodic safety check", e)
            }
        }
    }
    
    private fun checkBatteryStatus(battery: BatteryStatus) {
        when {
            battery.percentage <= criticalBatteryLevel -> {
                CoroutineScope(Dispatchers.Default).launch {
                    handleCriticalSafetyEvent(
                        SafetyEvent.CRITICAL_BATTERY,
                        "Critical battery level: ${battery.percentage}%"
                    )
                }
            }
            
            battery.percentage <= warningBatteryLevel -> {
                handleWarningSafetyEvent(
                    SafetyEvent.LOW_BATTERY,
                    "Low battery warning: ${battery.percentage}%"
                )
            }
        }
        
        // Check battery voltage
        if (battery.voltage < 20.0) {
            handleWarningSafetyEvent(
                SafetyEvent.LOW_VOLTAGE,
                "Low battery voltage: ${battery.voltage}V"
            )
        }
    }
    
    private fun checkGpsStatus(gps: GpsStatus) {
        when {
            gps.fixType < 2 -> {
                handleWarningSafetyEvent(
                    SafetyEvent.GPS_POOR,
                    "Poor GPS fix: type ${gps.fixType}"
                )
            }
            
            gps.satelliteCount < minGpsSatellites -> {
                handleWarningSafetyEvent(
                    SafetyEvent.GPS_POOR,
                    "Low satellite count: ${gps.satelliteCount}"
                )
            }
            
            gps.horizontalDilution > 3.0 -> {
                handleWarningSafetyEvent(
                    SafetyEvent.GPS_POOR,
                    "Poor GPS accuracy: HDOP ${gps.horizontalDilution}"
                )
            }
        }
    }
    
    private fun checkAltitudeLimits(altitude: Double) {
        if (altitude > maxAltitude) {
            CoroutineScope(Dispatchers.Default).launch {
                handleCriticalSafetyEvent(
                    SafetyEvent.ALTITUDE_LIMIT,
                    "Altitude limit exceeded: ${altitude}m (max: ${maxAltitude}m)"
                )
            }
        }
    }
    
    private fun checkFlightMode(telemetry: TelemetryData) {
        // Check for unsafe flight mode combinations
        if (telemetry.armed && telemetry.altitude < 1.0 && 
            !telemetry.flightMode.contains("LAND", ignoreCase = true)) {
            handleWarningSafetyEvent(
                SafetyEvent.UNSAFE_MODE,
                "Armed at ground level without landing mode"
            )
        }
    }
    
    private fun checkEmergencyConditions(telemetry: TelemetryData) {
        // Check for extreme velocity values that might indicate system failure
        if (telemetry.velocity.speed > 30.0) { // 30 m/s = ~67 mph
            CoroutineScope(Dispatchers.Default).launch {
                handleCriticalSafetyEvent(
                    SafetyEvent.EXCESSIVE_SPEED,
                    "Excessive speed detected: ${telemetry.velocity.speed} m/s"
                )
            }
        }
        
        // Check for rapid altitude changes
        // This would require historical data - simplified for now
        if (kotlin.math.abs(telemetry.velocity.velocityZ) > 10.0) {
            handleWarningSafetyEvent(
                SafetyEvent.RAPID_DESCENT,
                "Rapid vertical movement: ${telemetry.velocity.velocityZ} m/s"
            )
        }
    }
    
    private fun performSystemHealthCheck() {
        val health = communicationModule.getConnectionHealth()
        
        // Check communication health
        if (health.errors.isNotEmpty()) {
            val recentErrors = health.errors.takeLast(5)
            logger.warn("Recent communication errors: $recentErrors")
        }
        
        // Check message rates
        val currentTime = System.currentTimeMillis()
        health.lastMessageReceived?.let { lastReceived ->
            if (currentTime - lastReceived > 30000) { // 30 seconds
                handleWarningSafetyEvent(
                    SafetyEvent.COMMUNICATION_DEGRADED,
                    "No messages received for ${(currentTime - lastReceived) / 1000}s"
                )
            }
        }
    }
    
    private suspend fun handleCriticalSafetyEvent(event: SafetyEvent, message: String) {
        logger.error("CRITICAL SAFETY EVENT: $event - $message")
        _safetyStatus.value = when (event) {
            SafetyEvent.COMMUNICATION_LOST, SafetyEvent.TELEMETRY_TIMEOUT -> SafetyStatus.CRITICAL_COMMUNICATION
            SafetyEvent.CRITICAL_BATTERY -> SafetyStatus.CRITICAL_BATTERY
            SafetyEvent.ALTITUDE_LIMIT, SafetyEvent.EXCESSIVE_SPEED -> SafetyStatus.CRITICAL_FLIGHT
            else -> SafetyStatus.CRITICAL_OTHER
        }
        
        emitAlert(SafetyAlert(event, SafetyLevel.CRITICAL, message))
        
        // Trigger emergency procedures
        when (event) {
            SafetyEvent.COMMUNICATION_LOST, 
            SafetyEvent.TELEMETRY_TIMEOUT,
            SafetyEvent.CRITICAL_BATTERY,
            SafetyEvent.ALTITUDE_LIMIT,
            SafetyEvent.EXCESSIVE_SPEED -> {
                emergencyLandingCallback("Emergency landing triggered: $message")
            }
            else -> {
                // Other critical events might need different responses
            }
        }
    }
    
    private fun handleWarningSafetyEvent(event: SafetyEvent, message: String) {
        logger.warn("SAFETY WARNING: $event - $message")
        
        if (_safetyStatus.value == SafetyStatus.NORMAL) {
            _safetyStatus.value = SafetyStatus.WARNING
        }
        
        CoroutineScope(Dispatchers.Default).launch {
            emitAlert(SafetyAlert(event, SafetyLevel.WARNING, message))
        }
        warningCallback(message)
    }
    
    private suspend fun emitAlert(alert: SafetyAlert) {
        _safetyAlerts.emit(alert)
    }
    
    /**
     * Get current safety summary
     */
    fun getSafetySummary(): SafetySummary {
        val health = communicationModule.getConnectionHealth()
        val telemetry = latestTelemetry
        
        return SafetySummary(
            status = _safetyStatus.value,
            communicationHealth = health,
            lastTelemetryTime = telemetry?.timestamp,
            batteryPercentage = telemetry?.battery?.percentage,
            gpsStatus = telemetry?.gps?.let { "${it.fixType}D fix, ${it.satelliteCount} sats" },
            altitude = telemetry?.altitude,
            isArmed = telemetry?.armed ?: false,
            lastSafetyCheck = lastSafetyCheck.get()
        )
    }
}

/**
 * Safety-related data classes
 */
enum class SafetyStatus {
    NORMAL,
    WARNING,
    CRITICAL_COMMUNICATION,
    CRITICAL_BATTERY,
    CRITICAL_FLIGHT,
    CRITICAL_OTHER
}

enum class SafetyLevel {
    INFO,
    WARNING,
    CRITICAL
}

enum class SafetyEvent {
    COMMUNICATION_LOST,
    COMMUNICATION_DEGRADED,
    COMMUNICATION_RESTORED,
    TELEMETRY_TIMEOUT,
    CRITICAL_BATTERY,
    LOW_BATTERY,
    LOW_VOLTAGE,
    GPS_POOR,
    ALTITUDE_LIMIT,
    EXCESSIVE_SPEED,
    RAPID_DESCENT,
    UNSAFE_MODE
}

data class SafetyAlert(
    val event: SafetyEvent,
    val level: SafetyLevel,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class SafetySummary(
    val status: SafetyStatus,
    val communicationHealth: ConnectionHealth,
    val lastTelemetryTime: Long?,
    val batteryPercentage: Int?,
    val gpsStatus: String?,
    val altitude: Double?,
    val isArmed: Boolean,
    val lastSafetyCheck: Long
)
