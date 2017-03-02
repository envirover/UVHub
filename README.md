[![Build Status](https://travis-ci.org/envirover/SPLGroundControl.svg?branch=master)](https://travis-ci.org/envirover/SPLGroundControl)
[![Join the chat at https://gitter.im/SPLRadioRoom/Lobby](https://badges.gitter.im/SPLRadioRoom/Lobby.svg)](https://gitter.im/SPLRadioRoom/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

# SPLGroundControl

SPLGroundControl is a MAVLink proxy server for ArduPilot rovers over [RockBLOCK](http://www.rock7mobile.com/products-rockblock) Iridium satellite communication system. It is designed to work with [SPLRadioRoom](https://github.com/envirover/SPLRadioRoom) field application, providing a two way communication channel between ArduPilot based rovers and MAVLink ground control stations such as MAVProxy, Mission Planer. or QGroundControl.

SPLGroundControl consists of two message pipelines: mobile-originated (MO) pipeline and mobile-terminated (MT) piplene. 

In the MO pipeline, RockBLOCK HTTP handler receives MAVLink messages from HTTP port (POSTed there by RockBLOCK services) and pushes them to MO message queue. MO message pump receives messages from the queue and forwards them to MAVLink socket, if a client is connected to the socket. HIGH\_LATENCY type MAVLink messages are not forwarded directly, instead they are split into multiple high-frequency messages of types SYS\_STATUS, GPS\_RAW\_INT, ATTITUDE, GLOBAL\_POSITION\_INT, MISSION\_CURRENT, NAV\_CONTROLLER\_OUTPUT, and VFR\_HUD and periodically sent to the socket along with HEARTBEAT messages.

In the MT piplene, MAVLink handler receives messages from the socket, filters out HEARTBEAT, PARAM\_REQUEST\_LIST, and REQUEST\_DATA\_STREAM messages, and pushes other messages to MT message queue. MT message pump receives messages from the queue and sends them to RockBLOCK Web Services.

## Installation and use

The machine that runs SPLGroundControl must be accessible from the Internet. Port 8080 must be accessible from RockBLOCK services, and port 5760 must be accessible from the ground control station client machines.

SPLGroundControl requires Java SE 7 to run.

Download distribution assembly archive from [releases](https://github.com/envirover/SPLGroundControl/releases) page, or download the source code and build it by running ``mvn package``. Extract the archive into the local directory and set environmental variable SPL_HOME to the directory path.

Set rockblock.imei, rockblock.username, and rockblock.password properties in $SPL_HOME/conf/app.properties file to your RockBLOCK IMEI, Rock 7 Core username, and password respectively.

Determine the public IP address of the machine (http://checkip.amazonaws.com/) and configure your RockBLOCK message delivery destinations at https://core.rock7.com to deliver messages to `http://<IP>:8080/mo` URL.

It is recommended to configure SPLGroundControl to run as a service (for windows platforms use [procrun](https://commons.apache.org/proper/commons-daemon/procrun.html), for Linux platforms use [jsvc](https://commons.apache.org/proper/commons-daemon/jsvc.html)). The daemon java class is com.envirover.spl.SPLDaemon.

For testing purpose SPLGroundControl could be started by running $SPL_HOME/bin/spl.sh (Linux) or $SPL_HOME/bin/spl.bat (Windows).

Once SPLGroundControl is started you can connect to it from ground control station client using TCP connection on port 5760. For example, MAVPoxy ground control could be connected this way: 

``mavproxy.py --master=tcp:<IP>:5760 --mav10``

Currently SPLGroundControl supports one GC client connection at a time.



