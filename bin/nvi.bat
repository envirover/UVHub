@echo off

if "%SPL_HOME%"=="" (
  echo Environment variable NVI_HOME is not set.
  exit /b
)

set CLASSPATH=%NVI_HOME%\conf;%NVI_HOME%\lib\*
java -cp "%CLASSPATH%" com.envirover.nvi.NVIGroundControl %*
