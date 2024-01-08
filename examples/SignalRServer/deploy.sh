#!/bin/bash

#dotnet publish -r linux-x64 -c release -o publish /p:useapphost=true
#cd publish && zip -r server.zip *

az webapp deployment source config-zip --name signalr-perf --src publish/server.zip --resource-group perf-jmeter
