#!/bin/bash

kill `cat RUNNING_PID`
rm -f nohup.out RUNNING_PID
nohup java -jar tommypush.jar firebase.json application.json & echo $! > RUNNING_PID
