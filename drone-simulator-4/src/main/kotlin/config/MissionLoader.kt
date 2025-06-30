package config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import simulator.models.Mission
import java.io.File

class MissionLoader(private val objectMapper: ObjectMapper) {

    fun loadMission(filePath: String): Mission {
        val file = File(filePath)
        return objectMapper.readValue(file)
    }
}