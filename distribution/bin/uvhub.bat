@echo off

if "%UVHUB_HOME%"=="" (
  echo Environment variable UVHUB_HOME is not set.
  exit /b
)

set CLASSPATH=%UVHUB_HOME%\conf;%UVHUB_HOME%\lib\*
java -cp "%CLASSPATH%" com.envirover.uvhub.UVHub %*
