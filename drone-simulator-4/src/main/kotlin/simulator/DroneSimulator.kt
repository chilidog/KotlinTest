package simulator

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File

class DroneSimulator {

    private val droneController = DroneController()

    fun loadMission(filePath: String) {
        val missionFile = File(filePath)
        if (missionFile.exists()) {
            val mission: Mission = jacksonObjectMapper().readValue(missionFile)
            executeMission(mission)
        } else {
            println("Mission file not found: $filePath")
        }
    }

    private fun executeMission(mission: Mission) {
        for (command in mission.commands) {
            when (command.type) {
                "climb" -> droneController.climb(command.altitude)
                "pause" -> droneController.pause(command.duration)
                "circle" -> droneController.circle(command.radius)
                "land" -> droneController.land()
                else -> println("Unknown command: ${command.type}")
            }
        }
    }
}