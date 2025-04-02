#!/bin/bash

echo "📥 Starting Downloader..."
java -Xmx512m -Xms256m -cp "./target/SD_Project-1.0-SNAPSHOT.jar;lib/jsoup-1.19.1.jar" com.example.Downloader > "./logs/downloader_20250402130000.log" 2>&1 &
DOWNLOADER_PID=

trap "echo '🛑 Stopping Downloader...'; kill ; exit 0" SIGINT
wait 
