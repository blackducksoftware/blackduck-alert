#! /bin/sh
#*******************************************************************************
# Copyright (C) 2017 Black Duck Software, Inc.
# http://www.blackducksoftware.com/
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements. See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership. The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License. You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.
#*******************************************************************************

PID_FILE=blackduck_alert.pid
TITLE="BlackDuck Alert"
MAX_ATTEMPTS=20
PROGRAM="$0"
DIR=`dirname "$PROGRAM"`
DIRECTORY_NAME="blackduck-alert"

checkIsRunning() {
  if [ -f $DIR/$PID_FILE ]; then
    local running_pid=$(ps -eo pid,args | grep java | grep $DIRECTORY_NAME | awk '{print $1}')
    local saved_pid=`cat $DIR/$PID_FILE`
    if [ ! -z $running_pid  ]; then
        if [ "$saved_pid" -eq "$running_pid" ]; then
            return 1
        fi
    fi
    return 0
  fi
  return 0
}

startExtension() {
  checkIsRunning
  returnValue=$?
  if [ "$returnValue" -eq "1" ]; then
      echo "$TITLE is already running"
      return 0;
  fi
  echo "Starting $TITLE"
  $DIR/$DIRECTORY_NAME &
  sleep 1s
  local pid=$(ps -eo pid,args | grep java | grep $DIRECTORY_NAME | awk '{print $1}')
  echo "$pid" > $DIR/$PID_FILE
  echo "Started $TITLE with PID: $pid"
}

stopExtension() {
  checkIsRunning
  returnValue=$?
  if [ "$returnValue" -eq "1" ]; then
     local pid=`cat $DIR/$PID_FILE`
     echo "Stopping $TITLE with PID: $pid"
     for index in `seq 1 $MAX_ATTEMPTS`; do
       checkIsRunning
       stillRunning=$?
       if [ "$stillRunning" -eq "0" ]; then
         rm -f $PID_FILE
         echo "Stopped $TITLE with PID: $pid"
         return 0;
       fi
       kill $pid
       sleep .5
     done
  fi
  return 1
}

extensionStatus () {
  checkIsRunning
  returnValue=$?
  if [ "$returnValue" -eq "1" ]; then
     local pid=`cat $DIR/$PID_FILE`
     echo "$TITLE is running with PID: $pid"
  else
     echo "$TITLE is not running"
  fi
}

case "$1" in
    start)
          startExtension
          ;;
    stop)
          stopExtension
          ;;
    restart)
          stopExtension
          startExtension
          ;;
    status)
          extensionStatus
          ;;
    *)
          echo "Usage: start|stop|restart|status"
          exit 1
          ;;
esac
