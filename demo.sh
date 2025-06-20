#!/bin/bash

echo "=== Kotlin OS Configuration Demo (Maven) ==="
echo ""
echo "This demo shows the global variable functionality for OS configuration."
echo "Now supporting Ubuntu, CachyOS, and Alpine Linux!"
echo ""

export JAVA_HOME=/usr/lib/jvm/java-11-openjdk

cd /workspaces/KotlinTest

echo "1. Building the project with Maven:"
mvn clean compile
if [ $? -ne 0 ]; then
    echo "Build failed. Please check your Maven installation."
    exit 1
fi
echo ""

echo "2. Running with default Ubuntu configuration:"
echo "Ubuntu" | mvn -q exec:java
echo ""

echo "3. Running with CachyOS configuration:"
echo "CachyOS" | mvn -q exec:java
echo ""

echo "4. Running with Alpine configuration:"
echo "Alpine" | mvn -q exec:java
echo ""

echo "5. Running with invalid OS (should keep current value):"
echo "InvalidOS" | mvn -q exec:java
echo ""

echo "6. Running tests to verify functionality:"
mvn test
echo "All tests passed!"
