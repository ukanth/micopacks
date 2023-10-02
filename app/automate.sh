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

# Build the release APK
./gradlew assembleRelease

# Copy the APK to the output directory
cp "$PROJECT_DIR/app/build/outputs/apk/release/app-release-unsigned.apk" "$OUTPUT_DIR"

# Notify completion
echo "APK build completed and copied to $OUTPUT_DIR"
