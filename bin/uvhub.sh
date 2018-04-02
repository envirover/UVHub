#!/bin/bash

# -----------------------------------------------------------------------------
# Control Script for UV Hub server
#
# Environment Variable Prerequisites
#
#   UVHUB_HOME        May point at your UVHub "build" directory.
#

CLASSPATH=$UVHUB_HOME/conf:$UVHUB_HOME/lib/*

java -cp "$CLASSPATH" com.envirover.uvhub.UVHub "$@"
