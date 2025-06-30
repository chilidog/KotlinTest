# Drone Simulator

## Overview
The Drone Simulator project is designed to simulate the behavior of various drone models, allowing users to execute predefined missions through a flexible command structure. The simulator can read mission specifications from JSON files, enabling easy customization and extension of drone behaviors.

## Features
- **Mission Execution**: Load and execute missions defined in JSON format.
- **Drone Control**: Control various drone actions such as climbing, circling, and landing.
- **Modular Design**: Easily extendable architecture with separate classes for commands, missions, and drone models.

## Project Structure
- **src/main/kotlin/simulator**: Contains the main logic for the drone simulator.
  - `DroneSimulator.kt`: Initializes the simulator and manages mission execution.
  - `DroneController.kt`: Handles drone commands and interactions.
  - **models**: Contains classes representing the drone's properties and mission commands.
    - `Drone.kt`: Represents the drone's state and position.
    - `Mission.kt`: Represents a mission consisting of a series of commands.
    - `Command.kt`: Represents individual commands for the drone.
    - `Position.kt`: Represents the drone's position in 3D space.

- **src/main/kotlin/config**: Contains classes for loading configurations.
  - `MissionLoader.kt`: Loads mission configurations from JSON files.
  - `DroneConfigLoader.kt`: Loads drone specifications from JSON files.

- **src/main/resources**: Contains resource files.
  - **missions**: JSON files specifying missions for different drones.
    - `cetus-lite-demo.json`: Mission for the Cetus Lite Beta FPV drone.
    - `sample-mission.json`: Template for other missions.
  - **drones**: JSON files containing specifications for drone models.
    - `cetus-lite-beta.json`: Specifications for the Cetus Lite Beta FPV drone.
    - `default-drone.json`: Default specifications for a generic drone model.
  - `application.properties`: Configuration properties for the application.

- **src/test/kotlin**: Contains unit tests for the simulator and configuration loaders.
  - **simulator**: Tests for the main simulator logic.
    - `DroneSimulatorTest.kt`: Unit tests for the DroneSimulator class.
    - `MissionLoaderTest.kt`: Unit tests for the MissionLoader class.
  - **config**: Tests for configuration loaders.
    - `ConfigLoaderTest.kt`: Unit tests for the DroneConfigLoader class.

## Getting Started
1. **Clone the Repository**: 
   ```
   git clone <repository-url>
   cd drone-simulator
   ```

2. **Build the Project**: 
   ```
   mvn clean package
   ```

3. **Run the Simulator**: 
   ```
   mvn exec:java -Dexec.mainClass="AppKt"
   ```

4. **Load a Mission**: 
   Modify the JSON files in the `src/main/resources/missions` directory to create or customize missions.

## Usage
To execute a mission, ensure the corresponding JSON file is correctly formatted and placed in the `missions` directory. The simulator will read the commands and execute them sequentially.

## Contributing
Contributions are welcome! Please submit a pull request or open an issue for any enhancements or bug fixes.

## License
This project is licensed under the MIT License. See the LICENSE file for more details.