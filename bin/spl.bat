@echo off

if "%SPL_HOME%"=="" (
  echo Environment variable SPL_HOME is not set.
  exit /b
)

set CLASSPATH=%SPL_HOME%\conf;%SPL_HOME%\lib\*
java -cp "%CLASSPATH%" com.envirover.spl.SPLGroundControl %*
