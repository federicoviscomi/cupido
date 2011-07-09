#!/bin/bash

./build.sh

PIDS=""

for script in rmiregistry gtm chat ltm
do
  ./run_${script}.sh &
  PIDS="$PIDS $!"
  sleep 2
done

echo "*****************************"
echo "* All processes started.    *"
echo "* Press Enter to kill them. *"
echo "*****************************"

read

echo "Terminating child processes, please wait..."

for pid in $PIDS
do
  kill "$pid"
done

sleep 1
