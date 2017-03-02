#!/bin/bash

# -----------------------------------------------------------------------------
# Control Script for SPL Ground Control server
#
# Environment Variable Prerequisites
#
#   SPL_HOME        May point at your SPL "build" directory.
#

CLASSPATH=$SPL_HOME/conf:$SPL_HOME/lib/*

java -cp "$CLASSPATH" com.envirover.spl.SPLGroundControl "$@"
