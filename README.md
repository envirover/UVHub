[![Build Status](https://codebuild.us-west-2.amazonaws.com/badges?uuid=eyJlbmNyeXB0ZWREYXRhIjoiRW1vUVROVFc3bis4ZXhncEg5M1ZuR2d4ck43cDU4TE8vdGo1RU9ZNk5QR01RWW5uZkZCYnBab1pHS0ZnQ0lUcDFQOGFTQmM2eUx2SjczSko0VFcvUjRRPSIsIml2UGFyYW1ldGVyU3BlYyI6InljK1JHbXRVa04xc0M4cDciLCJtYXRlcmlhbFNldFNlcmlhbCI6MX0%3D&branch=master)](https://us-west-2.console.aws.amazon.com/codebuild/home?region=us-west-2#/projects/UVHub/view)

# UV Hub

UV Hub repository contains java projects used by backend servers of SPL system.

| Project | Description |
|---------|--------------------------------|
| MAVLink | Auto-generated MAVLink classes |
| UVHub  | MAVLink proxy server for TCP/IP and Iridium SBD communication with unmanned vehicles controlled by ArduPilot or PX4 autopilots. |
| UVTracks | Web service that provides access to the mission plans and reported states saved in the vehicle shadow. |
| UVHUbTest | Integration tests for UV Hub and UV Tracks |

## Build

Build system requirements:

- Maven
- Docker

Build the projects:

```bash
mvn clean install
```

Build Docker container images:

```bash
docker build -t uvhub ./UVHub/
docker build -t uvtracks ./UVTracks
docker build -t uvhub-test ./UVHubTest
```

## Running UV Hub

Create file docker-compose.yaml with the following content:

```yaml
version: "3.7"
services:
  uvhub:
    image: uvhub
    restart: always
    ports:
      - "5080:5080" # HTTP port used by RockBLOCK services to POST mobile-originated messages
      - "5060:5060" # TCP port used by connections from SPL RadioRoom
      - "5760:5760" # TCP port used for MAVLink ground control stations connections
      - "5757:5757" # TCP port used to update reported parameters and missions in the shadow
    environment:
      - "ROCKBLOCK_IMEI=${ROCKBLOCK_IMEI}"
      - "ROCKBLOCK_USERNAME=${ROCKBLOCK_USERNAME}"
      - "ROCKBLOCK_PASSWORD=${ROCKBLOCK_PASSWORD}"
      - "MAV_AUTOPILOT=3"
      - "MAT_TYPE=2"
      - "ELASTICSEARCH_ENDPOINT=elasticsearch"
```

Run

```bash
docker-compose -f ./docker-compose.yaml up
```

Once UV Hub container is started, you can connect to it from a ground control station such as QGroundControl or Mission Planner using TCP connection on port 5760.

## Running UV Tracks

Create file docker-compose.yaml with the following content:

```yaml
version: "3.7"
services:
  uvtracks:
    image: uvtracks
    restart: always
    hostname: uvtracks
    ports:
      - "8080:8080"
    environment:
      - "ELASTICSEARCH_ENDPOINT=elasticsearch"
```

Run

```bash
docker-compose -f ./docker-compose.yaml up
```

Once UV Tracks container is started the web service will be availablev at http://localhost:8080/uvtracks/api/v1.

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
