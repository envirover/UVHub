# Control script for SPL Ground Control server
#
# Environment Variable Prerequisites:
#
#   SPL_HOME    Must point at your SPL "build" directory.
#

set CLASSPATH=%SPL_HOME%\conf;%SPL_HOME%\lib\*
java -cp "%CLASSPATH%" com.envirover.spl.SPLGroundControl %*
