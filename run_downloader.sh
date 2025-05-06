#!/bin/bash

echo "📥 Starting Downloader..."
java -Xmx512m -Xms256m -cp "./target/SD_Project-1.0-SNAPSHOT.jar;lib/jsoup-1.19.1.jar" com.example.Downloader
DOWNLOADER_PID=

trap "echo '🛑 Stopping Downloader...'; kill ; exit 0" SIGINT
wait 
