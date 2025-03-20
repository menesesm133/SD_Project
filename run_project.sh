#!/bin/bash

# Set project paths
PROJECT_ROOT=$(dirname "$(realpath "$0")")
CLASSPATH="$PROJECT_ROOT/target/classes:$PROJECT_ROOT/lib/jsoup-1.19.1.jar"
JAVA_OPTS="-Xmx512m -Xms256m"
LOG_DIR="$PROJECT_ROOT/logs"
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

# Start RMI registry if it's not running
if ! jps | grep -q rmiregistry; then
    echo "🛠️ Starting RMI registry..."
    rmiregistry &
    RMI_PID=$!
    sleep 2
else
    echo "✅ RMI registry is already running."
fi

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

# Start Client
echo "💻 Starting Client..."
java $JAVA_OPTS -cp "$CLASSPATH" com.example.Client > "$LOG_DIR/client_$(date +%Y%m%d%H%M%S).log" 2>&1 &
CLIENT_PID=$!
sleep 2

echo "✅ All services started successfully!"
echo "👉 Gateway PID: $GATEWAY_PID"
echo "👉 Downloader PID: $DOWNLOADER_PID"
echo "👉 Client PID: $CLIENT_PID"

# Shutdown handler (CTRL+C)
trap "echo '🛑 Stopping everything...'; kill $GATEWAY_PID $DOWNLOADER_PID $CLIENT_PID; exit 0" SIGINT

# Wait for all processes to finish
wait $GATEWAY_PID $DOWNLOADER_PID $CLIENT_PID

# If RMI was started by this script, stop it too
if [ -n "$RMI_PID" ]; then
    echo "🛑 Stopping RMI registry..."
    kill "$RMI_PID"
fi

echo "✅ All services stopped cleanly!"
