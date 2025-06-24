package com.controlstation.mavlink

import io.dronefleet.mavlink.common.*
import java.nio.ByteBuffer

/**
 * MAVLink-specific message wrapper for type-safe message handling
 */
sealed class MAVLinkMessage {
    
    data class HeartbeatMessage(
        val systemId: Int,
        val componentId: Int,
        val type: MavType,
        val autopilot: MavAutopilot,
        val baseMode: Int,
        val customMode: Int,
        val systemStatus: MavState,
        val mavlinkVersion: Int
    ) : MAVLinkMessage()
    
    data class AttitudeMessage(
        val timeBootMs: Long,
        val roll: Float,
        val pitch: Float,
        val yaw: Float,
        val rollspeed: Float,
        val pitchspeed: Float,
        val yawspeed: Float
    ) : MAVLinkMessage()
    
    data class GpsRawMessage(
        val timeUsec: Long,
        val fixType: Int,
        val lat: Int,
        val lon: Int,
        val alt: Int,
        val eph: Int,
        val epv: Int,
        val vel: Int,
        val cog: Int,
        val satellitesVisible: Int,
        val altEllipsoid: Int? = null,
        val hAcc: Long? = null,
        val vAcc: Long? = null,
        val velAcc: Long? = null,
        val hdgAcc: Long? = null,
        val yaw: Int? = null
    ) : MAVLinkMessage()
    
    data class CommandLongMessage(
        val targetSystem: Int,
        val targetComponent: Int,
        val command: MavCmd,
        val confirmation: Int,
        val param1: Float = 0f,
        val param2: Float = 0f,
        val param3: Float = 0f,
        val param4: Float = 0f,
        val param5: Float = 0f,
        val param6: Float = 0f,
        val param7: Float = 0f
    ) : MAVLinkMessage()
    
    data class SysStatusMessage(
        val onboardControlSensorsPresent: Int,
        val onboardControlSensorsEnabled: Int,
        val onboardControlSensorsHealth: Int,
        val load: Int,
        val voltageBattery: Int,
        val currentBattery: Int,
        val batteryRemaining: Int,
        val dropRateComm: Int,
        val errorsComm: Int,
        val errorsCount1: Int,
        val errorsCount2: Int,
        val errorsCount3: Int,
        val errorsCount4: Int
    ) : MAVLinkMessage()
    
    data class UnknownMessage(
        val messageId: Int,
        val payload: ByteArray
    ) : MAVLinkMessage() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as UnknownMessage
            if (messageId != other.messageId) return false
            if (!payload.contentEquals(other.payload)) return false
            return true
        }
        
        override fun hashCode(): Int {
            var result = messageId
            result = 31 * result + payload.contentHashCode()
            return result
        }
    }
}

/**
 * MAVLink connection configuration
 */
data class MAVLinkConnectionConfig(
    val connectionType: MAVLinkConnectionType,
    val connectionString: String,
    val baudRate: Int = 57600,
    val systemId: Int = 255,
    val componentId: Int = 190,
    val heartbeatInterval: Long = 1000L,
    val timeoutMs: Long = 5000L
)

/**
 * MAVLink connection types
 */
enum class MAVLinkConnectionType {
    SERIAL,
    UDP,
    TCP
}

/**
 * MAVLink system information
 */
data class MAVLinkSystemInfo(
    val systemId: Int,
    val componentId: Int,
    val type: MavType,
    val autopilot: MavAutopilot,
    val firmwareVersion: String? = null,
    val capabilities: Set<MavProtocolCapability> = emptySet()
)

/**
 * MAVLink connection statistics
 */
data class MAVLinkStats(
    val messagesReceived: Long = 0,
    val messagesSent: Long = 0,
    val packetsDropped: Long = 0,
    val lastHeartbeat: Long = 0,
    val connectionUptime: Long = 0,
    val averageLatency: Double = 0.0
)
