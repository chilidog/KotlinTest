package simulator.models

data class Command(
    val action: String,
    val parameters: Map<String, Any>
)