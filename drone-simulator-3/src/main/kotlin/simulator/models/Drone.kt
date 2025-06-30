package simulator.models

data class Position(
    var x: Double = 0.0,
    var y: Double = 0.0,
    var altitude: Double = 0.0
)

class Drone(
    var name: String,
    var position: Position = Position(),
    var isFlying: Boolean = false
) {
    fun climb(targetAltitude: Double) {
        isFlying = true
        position.altitude = targetAltitude
    }

    fun land() {
        isFlying = false
        position.altitude = 0.0
    }

    fun circle(radius: Double) {
        // Logic to fly in a circle with the given radius
    }

    fun pause(duration: Long) {
        // Logic to pause for the specified duration
    }
}