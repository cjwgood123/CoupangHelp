#!/bin/bash
APP_NAME="cou-html-0.0.1-SNAPSHOT.jar"
LOG_DIR="./logs"
PID_FILE="app.pid"
PORT=9000

JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

mkdir -p "$LOG_DIR"

# ✅ 3일 지난 로그 자동 삭제
find "$LOG_DIR" -name "cou-html-*.log" -mtime +3 -delete

# ✅ 오늘 날짜 로그 파일로 저장
LOG_FILE="$LOG_DIR/cou-html-$(date +%Y%m%d).log"

# PID 파일이 있어도 실제 프로세스가 없으면 stale PID로 보고 제거
if [ -f "$PID_FILE" ]; then
    OLD_PID=$(cat "$PID_FILE")
    if kill -0 "$OLD_PID" 2>/dev/null; then
        echo "Application already running with PID: $OLD_PID"
        exit 1
    else
        echo "Stale PID file found. Removing."
        rm -f "$PID_FILE"
    fi
fi

echo "Starting $APP_NAME ..."
echo "==== $(date '+%F %T') Starting app ====" >> "$LOG_FILE"

nohup java $JAVA_OPTS -jar "$APP_NAME" --server.port=$PORT >> "$LOG_FILE" 2>&1 &
PID=$!
echo $PID > "$PID_FILE"
echo "Started with PID $PID"

# Health Check
echo -n "Waiting for application to be ready"
for i in {1..20}; do
    if nc -z localhost $PORT; then
        echo -e "\nApplication is running on port $PORT"
        exit 0
    fi
    echo -n "."
    sleep 1
done

echo -e "\nApplication failed to start or taking too long."
exit 1
