#!/bin/bash

# Ensure Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Maven not found. Please install Maven first!"
    exit 1
fi

# Clean and build the project using Maven
echo "Building project with Maven..."
mvn clean package

# Start RMI registry if it's not already running
if ! jps | grep -q rmiregistry; then
    echo "Starting RMI registry..."
    rmiregistry &
    sleep 2
fi

# Build classpath from Maven dependencies
CLASSPATH="target/classes;$(mvn dependency:build-classpath -Dmdep.outputFile=/dev/stdout)"

# Run the Gateway
echo "Starting Gateway..."
java -cp "$CLASSPATH" com.example.Gateway &
GATEWAY_PID=$!
sleep 2

# Run the Client
echo "Starting Client..."
java -cp "$CLASSPATH" com.example.Client &
CLIENT_PID=$!
sleep 2

# Run the Downloader
echo "Starting Downloader..."
java -cp "$CLASSPATH" com.example.Downloader &
DOWNLOADER_PID=$!
sleep 2

echo "All services started successfully."

# Wait for all processes to finish
wait $GATEWAY_PID $CLIENT_PID $DOWNLOADER_PID
