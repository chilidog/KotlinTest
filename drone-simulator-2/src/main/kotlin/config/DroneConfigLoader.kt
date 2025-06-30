package config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File

data class DroneConfig(val name: String, val model: String, val maxAltitude: Int)

class DroneConfigLoader(private val objectMapper: ObjectMapper) {
    fun loadConfig(filePath: String): DroneConfig {
        return objectMapper.readValue(File(filePath))
    }
}