#!/bin/bash

echo "Installing Maven for Kotlin development..."

# Check if Maven is already installed
if command -v mvn &> /dev/null; then
    echo "Maven is already installed:"
    mvn -version
    exit 0
fi

# Download and install Maven
cd /tmp
MAVEN_VERSION="3.9.5"
wget https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz

if [ $? -ne 0 ]; then
    echo "Failed to download Maven. Trying with curl..."
    curl -O https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz
fi

# Extract and install
tar -xzf apache-maven-${MAVEN_VERSION}-bin.tar.gz
sudo mv apache-maven-${MAVEN_VERSION} /opt/maven
sudo ln -sf /opt/maven/bin/mvn /usr/local/bin/mvn

# Set environment variables
echo "export MAVEN_HOME=/opt/maven" | sudo tee -a /etc/environment
echo "export PATH=\$PATH:\$MAVEN_HOME/bin" | sudo tee -a /etc/environment

echo "Maven installed successfully!"
echo "You can now use: mvn clean compile"
echo "Note: You may need to restart your terminal or run 'source /etc/environment'"
