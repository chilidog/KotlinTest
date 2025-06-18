# Create the directory
mkdir -p .devcontainer

# Create devcontainer.json
cat > .devcontainer/devcontainer.json << 'EOF'
{
	"name": "KotlinTest",
	"image": "mcr.microsoft.com/devcontainers/java:17",
	"features": {
		"ghcr.io/devcontainers/features/java:1": {
			"version": "17",
			"installMaven": true,
			"installGradle": true
		}
	},
	"customizations": {
		"vscode": {
			"extensions": [
				"fwcd.kotlin",
				"mathiasfrohlich.kotlin",
				"vscjava.vscode-java-pack",
				"github.copilot",
				"github.vscode-pull-request-github"
			],
			"settings": {
				"java.configuration.updateBuildConfiguration": "automatic",
				"kotlin.languageServer.enabled": true,
				"kotlin.debugAdapter.enabled": true
			}
		}
	},
	"forwardPorts": [],
	"postCreateCommand": "sh .devcontainer/post-create.sh"
}
EOF

# Create post-create.sh script
cat > .devcontainer/post-create.sh << 'EOF'
#!/bin/bash

# Install the Kotlin compiler
curl -s https://get.sdkman.io | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install kotlin

echo "KotlinTest environment setup complete!"
EOF

# Make the script executable
chmod +x .devcontainer/post-create.sh