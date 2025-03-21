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

# Clean and compile the project
echo "🔧 Building project with Maven..."
mvn clean package
if [ $? -ne 0 ]; then
    echo "❌ Build failed!"
    exit 1
fi

# # Check if RMI registry is already running
# RMI_RUNNING=$(netstat -an | grep ":1099 " | grep LISTEN)

# if [ -z "$RMI_RUNNING" ]; then
#     echo "🛠️ Starting RMI registry on port 1099..."
#     rmiregistry 1099 &
#     RMI_PID=$!
#     sleep 2
# else
#     echo "✅ RMI registry is already running on port 1099."
#     RMI_PID=""
# fi

# Start Gateway
echo "🚀 Starting Gateway..."
java $JAVA_OPTS -cp "$CLASSPATH" com.example.Gateway > "$LOG_DIR/gateway_$(date +%Y%m%d%H%M%S).log" 2>&1 &
GATEWAY_PID=$!
sleep 2

# Start Downloader
echo "📥 Starting Downloader..."
java $JAVA_OPTS -cp "$CLASSPATH" com.example.Downloader > "$LOG_DIR/downloader_$(date +%Y%m%d%H%M%S).log" 2>&1 &
DOWNLOADER_PID=$!
sleep 2

echo "🛢️ Starting Barrel - 1..."
java $JAVA_OPTS -cp "$CLASSPATH" com.example.StorageBarrel > "$LOG_DIR/Barrel-1_$(date +%Y%m%d%H%M%S).log" 2>&1 &
BARREL1_PID=$!
sleep 2


echo "✅ All services started successfully!"
echo "👉 Gateway PID: $GATEWAY_PID"
echo "👉 Downloader PID: $DOWNLOADER_PID"
echo "👉 Barrel-1 PID: $BARREL1_PID"

# Start Client
echo "💻 Starting Client..."
java $JAVA_OPTS -cp "$CLASSPATH" com.example.Client
# java $JAVA_OPTS -cp "$CLASSPATH" com.example.Client > "$LOG_DIR/client_$(date +%Y%m%d%H%M%S).log" 2>&1 &
sleep 2

echo "ℹ️  Press CTRL+C (^C) to stop all processes ℹ️"

# Shutdown handler (CTRL+C)
trap "echo '\n🛑 Stopping everything...'; kill $GATEWAY_PID $DOWNLOADER_PID $CLIENT_PID $BARREL1_PID; exit 0" SIGINT

# Wait for all processes to finish
wait $GATEWAY_PID $DOWNLOADER_PID $BARREL1_PID

# If RMI was started by this script, stop it too
if [ -n "$RMI_PID" ]; then
    echo "🛑 Stopping RMI registry..."
    pkill -f rmiregistry
fi

echo "✅ All services stopped cleanly!"
