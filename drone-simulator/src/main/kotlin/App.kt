package simulator

import config.MissionLoader

fun main() {
    val missionLoader = MissionLoader()
    val mission = missionLoader.loadMission("missions/cetus-lite-demo.json")
    
    val droneController = DroneController()
    droneController.executeMission(mission)
}