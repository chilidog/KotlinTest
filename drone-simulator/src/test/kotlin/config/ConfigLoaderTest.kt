import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import simulator.models.Drone
import config.DroneConfigLoader

class ConfigLoaderTest {

    @Test
    fun `test loading Cetus Lite Beta drone configuration`() {
        val droneConfigLoader = DroneConfigLoader()
        val drone = droneConfigLoader.load("drones/cetus-lite-beta.json")

        assertEquals("Cetus Lite Beta", drone.name)
        assertEquals(6.0, drone.maxAltitude)
        assertEquals(1.5, drone.maxSpeed)
        assertEquals("FPV", drone.type)
    }
}