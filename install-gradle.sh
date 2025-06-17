#!/bin/bash

echo "Installing Gradle for Kotlin debugging..."

# Download and install Gradle
cd /tmp
wget https://services.gradle.org/distributions/gradle-8.4-bin.zip
unzip -q gradle-8.4-bin.zip
sudo mv gradle-8.4 /opt/gradle
sudo ln -sf /opt/gradle/bin/gradle /usr/local/bin/gradle

echo "Gradle installed successfully!"
echo "You can now use: gradle build"
