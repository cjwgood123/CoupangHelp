#!/bin/bash
PID_FILE="app.pid"

if [ ! -f $PID_FILE ]; then
    echo "No PID file found. Application may not be running."
    exit 1
fi

PID=$(cat $PID_FILE)
echo "Stopping application (PID: $PID)..."

kill $PID
sleep 5

# SIGTERM 실패 시 SIGKILL
if ps -p $PID > /dev/null ; then
    echo "Process didn't stop. Force killing..."
    kill -9 $PID
else
    echo "Stopped gracefully."
fi

rm -f $PID_FILE
