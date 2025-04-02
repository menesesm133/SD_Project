#!/bin/bash

echo "🛢️ Starting StorageBarrel..."
java -Xmx512m -Xms256m -cp "./target/SD_Project-1.0-SNAPSHOT.jar;lib/jsoup-1.19.1.jar" com.example.StorageBarrel > "./logs/storage_barrel_20250402130000.log" 2>&1 &
BARREL_PID=

trap "echo '🛑 Stopping StorageBarrel...'; kill ; exit 0" SIGINT
wait 
