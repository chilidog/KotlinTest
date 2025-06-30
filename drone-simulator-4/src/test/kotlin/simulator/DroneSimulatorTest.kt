package simulator

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import simulator.models.Drone
import simulator.models.Mission
import simulator.config.MissionLoader

class DroneSimulatorTest {

    private val missionLoader = MissionLoader()
    private val drone = Drone("Cetus Lite Beta", 0.0, 0.0, 0.0)

    @Test
    fun testMissionExecution() {
        val mission: Mission = missionLoader.loadMission("missions/cetus-lite-demo.json")
        drone.executeMission(mission)

        assertEquals(6.0, drone.altitude)
        assertEquals(0.0, drone.position.x)
        assertEquals(0.0, drone.position.y)
    }

    @Test
    fun testClimbCommand() {
        drone.climb(6.0)
        assertEquals(6.0, drone.altitude)
    }

    @Test
    fun testPauseCommand() {
        val initialTime = System.currentTimeMillis()
        drone.pause(5000)
        val elapsedTime = System.currentTimeMillis() - initialTime
        assert(elapsedTime >= 5000)
    }

    @Test
    fun testCircleCommand() {
        drone.climb(6.0)
        drone.circle(6.0)
        assertEquals(6.0, drone.position.x)
        assertEquals(0.0, drone.position.y)
    }

    @Test
    fun testLandingCommand() {
        drone.climb(6.0)
        drone.land()
        assertEquals(0.0, drone.altitude)
    }
}