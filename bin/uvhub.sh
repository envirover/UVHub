#!/bin/bash

# -----------------------------------------------------------------------------
# Control Script for NVI Ground Control server
#
# Environment Variable Prerequisites
#
#   NVI_HOME        May point at your NVI "build" directory.
#

CLASSPATH=$NVI_HOME/conf:$NVI_HOME/lib/*

java -cp "$CLASSPATH" com.envirover.nvi.NVIGroundControl "$@"
