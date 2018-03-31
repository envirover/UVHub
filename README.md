[![Build Status](https://codebuild.us-west-2.amazonaws.com/badges?uuid=eyJlbmNyeXB0ZWREYXRhIjoicmFlNzBGdXdpOU5VNkVhQnVQWld1cnVET3dqWVN0YnpxNzJWZzVaTXlFYVhxWmZhRkx6N1UwTHJWTUVTRmlxbXN5bHlBVzlTT3l5VGNRREJCMklGNVhVPSIsIml2UGFyYW1ldGVyU3BlYyI6IkxrVDRVUnlqamdBMG1PQTgiLCJtYXRlcmlhbFNldFNlcmlhbCI6MX0%3D&branch=master)](https://us-west-2.console.aws.amazon.com/codebuild/home?region=us-west-2#/projects/NVIGroundControl/view)

# NVI GroundControl

NVI GroundControl is a MAVLink proxy server for TCP/IP communication with unmanned vehicles controlled by ArduPilot or PX4 autopilots. It is designed to work with NVI RadioRoom companion computer, providing two way communication channel between the vehicles and MAVLink ground control stations such as Mission Planer or QGroundControl.

## Download NVI GroundControl Package

Download the latest build of NVI GroundControl from [https://s3-us-west-2.amazonaws.com/envirover/nvi/shared/NVIGroundControl/nvi-1.0-bin.zip](https://s3-us-west-2.amazonaws.com/envirover/nvi/shared/NVIGroundControl/nvi-1.0-bin.zip). Extract the archive to a folder on a local disk drive such as C:\nvi-1.0.

## Install Latest Java SE Development Kit

Download and install the latest release for Java SE DK (8): [Java SE Download Page](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

## Set Environment Variables

This reference explains what Environmental Variables are and how to set them: [Superuser Guide to Environmental Variables](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

Create the following variables in `System Variables`:  
Variable  |  Value  
NVI_HOME  |  Directory to where NVIGrouncontrol is located.  
JAVA_HOME |  Directory to where Java is located.  
PATH      |  Add %JAVA_HOME%\bin and %NVI_HOME%\bin to PATH.

## Test Java functionality and check Environmental Variables  

Open a Windows Command Prompt and type `java -version`.  
The terminal should return several lines about which Java version is running. If it is not running, recheck Step 2.  
  
Type `set` into the Command Prompt and you should see directory entries in `JAVA_HOME`, `NVI_HOME` and `PATH`.  

## Open Ports in Windows Firewall

The machine that runs NVI GroundControl must be accessible from the network. Port 5060 must be accessible from the NVI RadioRoom computer, and port 5760 must be accessible from the ground control station client machines.  

Use this guide to create two new rules for opening ports 5060 and 5760: [How to Block or Open a Port in Windows Firewall](http://www.thewindowsclub.com/block-open-port-windows-8-firewall)  

## Create On-board Parameters File
 
NVI GroundControl uses parameters file format supported by QGroundControl GCS. Connect QGroundControl to the ardupilot and save the parameters in %NVI_HOME%\conf\default.parameters file.

## Specify the Vehicle Type

Set mav.type property in `app.properties` file to the vehicle type (1 - FIXED_WING, 2 - QUADROTOR, 10 - GROUND_ROVER, 12 - SUBMARINE).

## Start NVI GroundControl

Either:  
1. Double click `nvi.bat` in `%NVI_HOME%\bin` directory, or  
2. Open a command prompt and enter `start %NVI_HOME%\bin\nvi.bat`  

## Connect to NVI GroundControl from GCS 

Once NVI GroundControl is started, you can connect to it from a ground control station such as QGroundControl or Mission Planner using TCP connection on port 5760. 

## Licensing
```
Envirover confidential

[2018] Envirover
All Rights Reserved.

NOTICE:  All information contained herein is, and remains the property of
Envirover and its suppliers, if any.  The intellectual and technical concepts
contained herein are proprietary to Envirover and its suppliers and may be
covered by U.S. and Foreign Patents, patents in process, and are protected
by trade secret or copyright law.

Dissemination of this information or reproduction of this material
is strictly forbidden unless prior written permission is obtained
from Envirover.
```
