#!/bin/bash

echo "📥 Starting Gateway..."
java -Xmx512m -Xms256m -cp "./target/SD_Project-1.0-SNAPSHOT.jar;lib/jsoup-1.19.1.jar" com.example.Gateway > "./logs/gateway_20250507002018.log" 2>&1 &
GATEWAY_PID=

trap "echo '🛑 Stopping Gateway...'; kill ; exit 0" SIGINT
wait 
