[![Build Status](https://travis-ci.org/envirover/SPLGroundControl.svg?branch=master)](https://travis-ci.org/envirover/SPLGroundControl)
[![Join the chat at https://gitter.im/SPLRadioRoom/Lobby](https://badges.gitter.im/SPLRadioRoom/Lobby.svg)](https://gitter.im/SPLRadioRoom/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

# SPLGroundControl

SPL is a communication technology designed to:
* Track position, attitude, and velocity of your drone anywhere on Earth;
* Monitor vital signs signs of your drone, such as battery charge, system status, and temperature;
* Update missions, parameters, and send commands to your drone;
* Control gymbals, and RC servos connected to AutoPilot.

![SPL System Architecture](https://s3-us-west-2.amazonaws.com/envirover/images/Satellite+Proxy+Link+(SPL).jpg)

SPLGroundControl is a MAVLink proxy server for ArduPilot drones that uses Iridium sort burst data (ISBD) satellite communication system provided by [RockBLOCK](http://www.rock7mobile.com/products-rockblock) unit. It is designed to work with [SPLRadioRoom](https://github.com/envirover/SPLRadioRoom) field application, providing two way communication channel between ArduPilot based drones and MAVLink ground control stations such as MAVProxy, Mission Planer, or QGroundControl.

## Installation and use

The machine that runs SPLGroundControl must be accessible from the Internet. Port 8080 must be accessible from RockBLOCK services, and port 5760 must be accessible from the ground control station client machines.

SPLGroundControl installation instructions for different environments are available on [wiki](https://github.com/envirover/SPLGroundControl/wiki) pages. Probably the easiest way to get started with SPLGroundControl is to [deploy it on Amazon AWS](https://github.com/envirover/SPLGroundControl/wiki/SPLGroundControl-Installation-on-Amazon-AWS).

Once SPLGroundControl is started, you can connect to it from MAVProxy, Mission Planer, or QGroundControl using TCP connection on port 5760. For example, MAVPoxy ground control could be connected this way: 

``mavproxy.py --master=tcp:<IP>:5760 --mav10``

Currently SPLGroundControl supports one GCS client connection at a time.

## Issues

Find a bug or want to request a new feature?  Please let us know by submitting an [issue](https://github.com/envirover/SPLGroundControl/issues).

## Contributing

Envirover welcomes contributions from anyone and everyone. Please see our [guidelines for contributing](https://github.com/envirover/SPLGroundControl/blob/master/CONTRIBUTING.md).

Licensing
---------
```
Copyright (C) 2017 Envirover

SPLGroundControl is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

SPLGroundControl is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with SPLGroundControl. If not, see <http://www.gnu.org/licenses/>.
```
