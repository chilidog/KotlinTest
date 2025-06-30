import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import simulator.models.Command
import simulator.models.Mission
import config.MissionLoader
import java.io.File

class MissionLoaderTest {

    @Test
    fun `test loading mission from JSON`() {
        val missionFile = File("src/main/resources/missions/cetus-lite-demo.json")
        val mission = MissionLoader.loadMission(missionFile)

        assertEquals("Cetus Lite Beta FPV", mission.droneModel)
        assertEquals(4, mission.commands.size)

        val expectedCommands = listOf(
            Command("climb", 6),
            Command("pause", 5),
            Command("circle", 6),
            Command("land", 0)
        )

        assertEquals(expectedCommands, mission.commands)
    }
}