#!/bin/bash

# Get the directory of the script
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
# echo "The script directory is: $SCRIPT_DIR"

# Define project and output directories using the script's directory
PROJECT_DIR="$(cd "$(dirname "$SCRIPT_DIR")" && pwd)"
OUTPUT_DIR="$SCRIPT_DIR/output/"
# echo "The project directory is : $PROJECT_DIR"

# Create the output directory if it doesn't exist
mkdir -p "$OUTPUT_DIR"

# Navigate to the project directory
cd "$PROJECT_DIR"

# Clean the project
./gradlew clean

# Define the path to your keystore.properties file (update as needed)
KEYSTORE_PROPERTIES_FILE="$PROJECT_DIR/keystore.properties"

# Check if the keystore.properties file exists
if [ -f "$KEYSTORE_PROPERTIES_FILE" ]; then
  echo "Using keystore.properties file for signing."
else
  echo "Error: keystore.properties file not found. Please create one with the keystore information."
  exit 1
fi

# Build the release APK
./gradlew assembleRelease -Pkeystore.properties="$KEYSTORE_PROPERTIES_FILE"

# Copy the APK to the output directory
cp "$PROJECT_DIR/app/build/outputs/apk/release/app-release.apk" "$OUTPUT_DIR"

# Notify completion
echo "APK build completed and copied to $OUTPUT_DIR"
