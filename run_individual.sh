#!/bin/bash

# Set project paths
if [[ "$(uname)" == "MINGW64"* || "$(uname)" == "CYGWIN"* ]]; then
    SEP=";"
else
    SEP=":"
fi

CLASSPATH="./target/SD_Project-1.0-SNAPSHOT.jar${SEP}lib/jsoup-1.19.1.jar"
JAVA_OPTS="-Xmx512m -Xms256m"
LOG_DIR="./logs"
mkdir -p "$LOG_DIR"

# Ensure Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Maven not found. Please install Maven first!"
    exit 1
fi

## Script 1: Run StorageBarrel
cat <<EOL > run_storage_barrel.sh
#!/bin/bash

echo "🛢️ Starting StorageBarrel..."
java $JAVA_OPTS -cp "$CLASSPATH" com.example.StorageBarrel > "$LOG_DIR/storage_barrel_$(date +%Y%m%d%H%M%S).log" 2>&1 &
BARREL_PID=$!

trap "echo '🛑 Stopping StorageBarrel...'; kill $BARREL_PID; exit 0" SIGINT
wait $BARREL_PID
EOL
chmod +x run_storage_barrel.sh


## Script 2: Run Downloader
cat <<EOL > run_downloader.sh
#!/bin/bash

echo "📥 Starting Downloader..."
java $JAVA_OPTS -cp "$CLASSPATH" com.example.Downloader > "$LOG_DIR/downloader_$(date +%Y%m%d%H%M%S).log" 2>&1 &
DOWNLOADER_PID=$!

trap "echo '🛑 Stopping Downloader...'; kill $DOWNLOADER_PID; exit 0" SIGINT
wait $DOWNLOADER_PID
EOL
chmod +x run_downloader.sh


## Script 3: Run Client
cat <<EOL > run_client.sh
#!/bin/bash

echo "💻 Starting Client..."
java $JAVA_OPTS -cp "$CLASSPATH" com.example.Client > "$LOG_DIR/client_$(date +%Y%m%d%H%M%S).log" 2>&1
EOL
chmod +x run_client.sh


echo "✅ Scripts created: run_storage_barrel.sh, run_downloader.sh, run_client.sh"
