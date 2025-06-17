# KotlinTest

A Kotlin project workspace for testing and development with proper project structure, build configuration, and testing framework setup.

## Project Structure

```
KotlinTest/
├── src/
│   ├── main/kotlin/
│   │   ├── Main.kt              # Main application entry point
│   │   └── Calculator.kt        # Sample Calculator class
│   └── test/kotlin/
│       └── CalculatorTest.kt    # Unit tests for Calculator
├── build.gradle.kts             # Kotlin DSL build configuration
├── gradle.properties            # Gradle properties
└── README.md                    # This file
```

## Features

- **Kotlin JVM**: Latest Kotlin version (1.9.24) with Java 11 toolchain
- **Testing Framework**: JUnit 5 with Kotlin test libraries
- **Additional Testing Libraries**:
  - Mockito for mocking
  - AssertJ for fluent assertions
- **Build Tool**: Maven with Kotlin plugin
- **IDE Support**: Configured for IntelliJ IDEA and VS Code

## Getting Started

### Prerequisites

- Java 11 or higher
- Maven
- Java 17 LTS as a requirement

### Building the Project

```bash
mvn clean compile
```

### Running the Application

```bash
mvn exec:java
```

### Running Tests

```bash
mvn test
```

### Running Tests with Coverage

```bash
mvn test jacoco:report
```

## Example Usage

The project includes a simple `Calculator` class with basic arithmetic operations and comprehensive unit tests demonstrating:

- Basic unit testing with JUnit 5
- Exception testing
- Edge case handling
- Test naming conventions with Kotlin backtick syntax

### Calculator Operations

```kotlin
val calculator = Calculator()
println(calculator.add(5, 3))        // 8
println(calculator.subtract(10, 4))  // 6
println(calculator.multiply(6, 7))   // 42
println(calculator.divide(15, 3))    // 5
println(calculator.power(2, 3))      // 8
```

## Testing Best Practices

This project demonstrates several testing best practices:

1. **Descriptive test names**: Using backticks for readable test method names
2. **Edge case testing**: Including boundary conditions and error scenarios
3. **Exception testing**: Verifying that exceptions are thrown when expected
4. **Test organization**: Grouping related tests in the same test class
5. **Comprehensive coverage**: Testing both positive and negative scenarios

## Development

### Adding New Features

1. Write tests first (TDD approach)
2. Implement the feature to make tests pass
3. Refactor if needed
4. Ensure all tests pass

### Code Style

- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Prefer immutable data structures (`val` over `var`)
- Use string templates for string formatting
- Keep functions small and focused

## Dependencies

- **Kotlin Standard Library**: Core Kotlin functionality
- **JUnit 5**: Modern testing framework for Java/Kotlin
- **Mockito**: Mocking framework for unit tests
- **Mockito-Kotlin**: Kotlin-specific extensions for Mockito
- **AssertJ**: Fluent assertion library

## License

This project is for educational and testing purposes.

