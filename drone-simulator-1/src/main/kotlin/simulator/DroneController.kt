package simulator

import models.Drone
import models.Command
import models.Position

class DroneController(private val drone: Drone) {

    fun executeCommand(command: Command) {
        when (command.type) {
            "climb" -> climb(command.value)
            "pause" -> pause(command.value)
            "circle" -> circle(command.value)
            "land" -> land()
        }
    }

    private fun climb(altitude: Double) {
        drone.position.altitude += altitude
        println("Climbing to ${drone.position.altitude} feet.")
    }

    private fun pause(seconds: Int) {
        println("Pausing for $seconds seconds.")
        Thread.sleep((seconds * 1000).toLong())
    }

    private fun circle(radius: Double) {
        println("Flying in a circle with a radius of $radius feet.")
        // Implement circle flying logic here
    }

    private fun land() {
        drone.position.altitude = 0.0
        println("Landing. Current altitude: ${drone.position.altitude} feet.")
    }
}