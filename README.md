![NVI System Architecture](https://s3-us-west-2.amazonaws.com/envirover/images/SPL-2.0.jpg)

# NVI GroundControl

NVI is a communication technology designed to:
* Track position, attitude, and velocity of your drones anywhere on Earth;
* Monitor vital signs signs of your drone, such as battery charge, system status, and temperature;
* Update missions, parameters, and send commands to your drone;
* Control gymbals, and RC servos connected to AutoPilot.

NVIGroundControl is a MAVLink proxy server for ArduPilot drones that uses Iridium sort burst data (ISBD) satellite communication system provided by [RockBLOCK](http://www.rock7mobile.com/products-rockblock) unit. It is designed to work with [NVIRadioRoom](https://github.com/envirover/NVIRadioRoom) field application, providing two way communication channel between ArduPilot based drones and MAVLink ground control stations such as MAVProxy, Mission Planer, or QGroundControl.

### NVI GroundControl Installation and Use

The machine that runs NVIGroundControl must be accessible from the Internet. Port 8080 must be accessible from RockBLOCK services, and port 5760 must be accessible from the ground control station client machines.

NVIGroundControl installation instructions for different environments are available on [wiki](https://github.com/envirover/NVIGroundControl/wiki) pages. Probably the easiest way to get started with NVIGroundControl is to [deploy it on Amazon AWS](https://github.com/envirover/NVIGroundControl/wiki/NVIGroundControl-Installation-on-Amazon-AWS).

Once NVIGroundControl is started, you can connect to it from MAVProxy, Mission Planer, or QGroundControl using TCP connection on port 5760. For example, MAVPoxy ground control could be connected this way: 

``mavproxy.py --master=tcp:<IP>:5760 --mav10``

Currently NVIGroundControl supports one GCS client connection at a time.

## NVI Stream and NVI Tracks

NVIStream and NVITracks web services provide a solution for storing and visualizing data reported by NVIRadioRoom. See [NVIStream and NVITracks wiki page](https://github.com/envirover/NVIGroundControl/wiki/NVIStream-and-NVITracks-Web-Services) for more information on deployment and use of these web services.

## Issues

Find a bug or want to request a new feature?  Please let us know by submitting an [issue](https://github.com/envirover/NVIGroundControl/issues).

## Contributing

Envirover welcomes contributions from anyone and everyone. Please see our [guidelines for contributing](https://github.com/envirover/NVIGroundControl/blob/master/CONTRIBUTING.md).

Licensing
---------
```
/*
 * Envirover confidential
 * 
 *  [2017] Envirover
 *  All Rights Reserved.
 * 
 * NOTICE:  All information contained herein is, and remains the property of 
 * Envirover and its suppliers, if any.  The intellectual and technical concepts
 * contained herein are proprietary to Envirover and its suppliers and may be 
 * covered by U.S. and Foreign Patents, patents in process, and are protected
 * by trade secret or copyright law.
 * 
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Envirover.
 */
```
