export var trackPoint = {
    fields: [{
            name: "sysid",
            alias: "sysid",
            type: "integer"
        },
        {
            name: "time",
            alias: "time",
            type: "long"
        },
        {
            name: "airspeed",
            alias: "airspeed",
            type: "integer"
        },
        {
            name: "airspeed_sp",
            alias: "airspeed_sp",
            type: "integer"
        },
        {
            name: "altitude_amsl",
            alias: "altitude_amsl",
            type: "integer"
        },
        {
            name: "altitude_sp",
            alias: "altitude_sp",
            type: "integer"
        },
        {
            name: "base_mode",
            alias: "base_mode",
            type: "integer"
        },
        {
            name: "battery_remaining",
            alias: "battery_remaining",
            type: "integer"
        },
        {
            name: "climb_rate",
            alias: "climb_rate",
            type: "integer"
        },
        {
            name: "custom_mode",
            alias: "custom_mode",
            type: "integer"
        },
        {
            name: "failsafe",
            alias: "failsafe",
            type: "integer"
        },
        {
            name: "gps_fix_type",
            alias: "gps_fix_type",
            type: "integer"
        },
        {
            name: "gps_nsat",
            alias: "gps_nsat",
            type: "integer"
        },
        {
            name: "groundspeed",
            alias: "groundspeed",
            type: "integer"
        },
        {
            name: "heading",
            alias: "heading",
            type: "double"
        },
        {
            name: "heading_sp",
            alias: "heading_sp",
            type: "integer"
        },
        {
            name: "landed_state",
            alias: "landed_state",
            type: "integer"
        },
        {
            name: "latitude",
            alias: "latitude",
            type: "double"
        },
        {
            name: "longitude",
            alias: "longitude",
            type: "double"
        },
        {
            name: "pitch",
            alias: "pitch",
            type: "double"
        },
        {
            name: "roll",
            alias: "roll",
            type: "double"
        },
        {
            name: "temperature",
            alias: "temperature",
            type: "integer"
        },
        {
            name: "temperature_air",
            alias: "temperature_air",
            type: "integer"
        },
        {
            name: "throttle",
            alias: "throttle",
            type: "integer"
        },
        {
            name: "wp_distance",
            alias: "wp_distance",
            type: "integer"
        },
        {
            name: "wp_num",
            alias: "wp_num",
            type: "integer"
        }
    ],
    // Set up popup template for the layer
    template: {
        title: "System {sysid} at {time}",
        content: [{
            type: "fields",
            fieldInfos: [{
                    fieldName: "airspeed",
                    label: "Air speed (m/s)",
                    visible: true
                },
                {
                    fieldName: "airspeed_sp",
                    label: "Air speed setpoint (m/s)",
                    visible: true
                },
                {
                    fieldName: "groundspeed",
                    label: "Ground speed (m/s)",
                    visible: true
                },
                {
                    fieldName: "heading",
                    label: "Heading (decimal degrees)",
                    visible: true
                },
                {
                    fieldName: "heading_sp",
                    label: "Heading setpoint (decimal degrees)",
                    visible: true
                },
                {
                    fieldName: "pitch",
                    label: "Pitch (decimal degrees)",
                    visible: true
                },
                {
                    fieldName: "roll",
                    label: "Roll (decimal degrees)",
                    visible: true
                }, {
                    fieldName: "altitude_amsl",
                    label: "Altitude above mean sea level (m)",
                    visible: true
                },
                {
                    fieldName: "altitude_sp",
                    label: "Altitude setpoint relative home (m)",
                    visible: true
                },
                {
                    fieldName: "climb_rate",
                    label: "Climb rate (m/s)",
                    visible: true
                },
                {
                    fieldName: "latitude",
                    label: "Latitude (decimal degrees)",
                    visible: true
                },
                {
                    fieldName: "longitude",
                    label: "Longitude (decimal degrees)",
                    visible: true
                },
                {
                    fieldName: "gps_fix_type",
                    label: "GPS Fix type",
                    visible: true
                },
                {
                    fieldName: "gps_nsat",
                    label: "Number of GPS satellites visible",
                    visible: true
                },
                {
                    fieldName: "battery_remaining",
                    label: "Remaining battery (%)",
                    visible: true
                },
                {
                    fieldName: "temperature",
                    label: "Battery voltage (volts)",
                    visible: true
                },
                {
                    fieldName: "temperature_air",
                    label: "Battery current (amp)",
                    visible: true
                },
                {
                    fieldName: "throttle",
                    label: "Throttle (%)",
                    visible: true
                },
                {
                    fieldName: "wp_distance",
                    label: "Distance to target (m)",
                    visible: true
                },
                {
                    fieldName: "wp_num",
                    label: "Current waypoint number",
                    visible: true
                },
                {
                    fieldName: "base_mode",
                    label: "Bitmap of enabled system modes",
                    visible: true
                },
                {
                    fieldName: "custom_mode",
                    label: "Bitfield of autopilot-specific flags",
                    visible: true
                },
                {
                    fieldName: "failsafe",
                    label: "Failsafe flags",
                    visible: true
                },
                {
                    fieldName: "landed_state",
                    label: "Landed state",
                    visible: true
                }
            ]
        }]
    },
    renderer2d: {
        type: "simple",
        symbol: {
            type: "simple-marker",
            path: "M-29,14.5 0,23.5 -8,14.5 0,5.5z",
            color: "magenta",
            outline: {
                color: "magenta",
                width: 0.5
            },
            angle: 0,
            size: 40,
            xoffset: 14.5,
            yoffset: 9
        },
        visualVariables: [{
            "type": "rotation",
            "valueExpression": "-$feature.heading",
            "axis": "heading"
        }]
    },
    renderer3d: {
        type: "simple",
        symbol: {
            type: "point-3d", // autocasts as new PointSymbol3D()
            symbolLayers: [{
                type: "object", // autocasts as new ObjectSymbol3DLayer()
                width: 3, // diameter of the object from east to west in meters
                height: 4, // height of the object in meters
                depth: 1, // diameter of the object from north to south in meters
                resource: {
                    primitive: "cone"
                },
                material: {
                    color: "magenta"
                }
            }]
        },
        visualVariables: [{
            "type": "rotation",
            "field": "heading",
            "axis": "heading"
        }, {
            "type": "rotation",
            "field": "roll",
            "axis": "roll"
        }, {
            "type": "rotation",
            "field": "tilt",
            "axis": "tilt"
        }]
    }
};

export var trackLine = {
    fields: [{
            name: "sysid",
            alias: "sysid",
            type: "oid"
        },
        {
            name: "from_time",
            alias: "from_time",
            type: "long"
        },
        {
            name: "to_time",
            alias: "to_time",
            type: "long"
        }
    ],
    template: {
        title: "Track",
        content: [{
            type: "fields",
            fieldInfos: [{
                    fieldName: "from_time",
                    label: "from_time",
                    visible: true
                },
                {
                    fieldName: "to_time",
                    label: "to_time",
                    visible: true
                }
            ]
        }]
    },
    renderer2d: {
        type: "simple",
        symbol: {
            type: "simple-line",
            width: 5,
            color: [256, 0, 0]
        }
    },
    renderer3d: {
        type: "simple",
        symbol: {
            type: "simple-line",
            width: 5,
            color: [256, 0, 0]
        }
    }
}

export var missionPoint = {
    fields: [{
            name: "seq",
            alias: "seq",
            type: "oid"
        },
        {
            name: "autoContinue",
            alias: "autoContinue"
        },
        {
            name: "command",
            alias: "command",
            type: "integer"
        },
        {
            name: "doJumpId",
            alias: "doJumpId",
            type: "integer"
        },
        {
            name: "frame",
            alias: "frame",
            type: "integer"
        },
        {
            name: "params",
            alias: "params"
        },
        {
            name: "type",
            alias: "type",
            type: "string"
        }
    ],
    template: {
        title: "Mission item {seq}",
        content: [{
            type: "fields",
            fieldInfos: [{
                    fieldName: "seq",
                    label: "seq",
                    visible: true
                },
                {
                    fieldName: "autoContinue",
                    label: "autoContinue",
                    visible: true
                },
                {
                    fieldName: "command",
                    label: "command",
                    visible: true
                },
                {
                    fieldName: "doJumpId",
                    label: "doJumpId",
                    visible: true
                },
                {
                    fieldName: "frame",
                    label: "frame",
                    visible: true
                },
                {
                    fieldName: "params",
                    label: "params",
                    visible: true
                },
                {
                    fieldName: "type",
                    label: "type",
                    visible: true
                }
            ]
        }]
    },
    renderer2d: {
        type: "unique-value",
        field: "type",
        defaultSymbol: {
            type: "simple-marker",
            color: "yellow",
            outline: {
                color: "yellow",
                width: "0.5px"
            }
        },
        uniqueValueInfos: [{
            // All features with value of "PlannedHome" will be green
            value: "PlannedHome",
            symbol: {
                type: "simple-marker",
                color: "green",
                outline: {
                    color: "green",
                    width: "0.5px"
                }
            }
        }]
    },
    renderer3d: {
        type: "unique-value",
        field: "type",
        defaultSymbol: {
            type: "point-3d",
            symbolLayers: [{
                type: "object",
                width: 4,
                height: 4, // height of the object in meters
                depth: 4, // diameter of the object from north to south in meters
                resource: {
                    primitive: "sphere"
                },
                material: {
                    color: [255, 255, 0, 1]
                }
            }]
        },
        uniqueValueInfos: [{
            // All features with value of "PlannedHome" will be green
            value: "PlannedHome",
            symbol: {
                type: "point-3d",
                symbolLayers: [{
                    type: "object",
                    width: 4, // diameter of the object from east to west in meters
                    height: 4, // height of the object in meters
                    depth: 4, // diameter of the object from north to south in meters
                    resource: {
                        primitive: "sphere"
                    },
                    material: {
                        color: [0, 255, 0, 1]
                    }
                }]
            }
        }]
    },
    labelClass: {
        symbol: {
            type: "text",
            color: "white",
            haloColor: "black",
            font: {
                family: "playfair-display",
                size: 24,
                weight: "bold"
            }
        },
        labelPlacement: "above-center",
        labelExpressionInfo: {
            expression: "$feature.seq"
        }
    }
};

// Set up popup template for the mission lines layer
export var missionLine = {
    fields: [{
            name: "sysid",
            alias: "sysid",
            type: "oid"
        },
        {
            name: "cruiseSpeed",
            alias: "cruiseSpeed",
            type: "integer"
        },
        {
            name: "hoverSpeed",
            alias: "hoverSpeed",
            type: "integer"
        },
        {
            name: "length",
            alias: "length",
            type: "double"
        }
    ],
    template: {
        title: "Mission",
        content: [{
            type: "fields",
            fieldInfos: [{
                    fieldName: "cruiseSpeed",
                    label: "Cruise speed (m/s)",
                    visible: true
                },
                {
                    fieldName: "hoverSpeed",
                    label: "Hover speed (m/s)",
                    visible: true
                },
                {
                    fieldName: "length",
                    label: "Length (m)",
                    visible: true
                }
            ]
        }]
    },
    renderer2d: {
        type: "simple",
        symbol: {
            type: "simple-line",
            width: 2,
            color: [255, 255, 0]
        }
    },
    renderer3d: {
        type: "simple",
        symbol: {
            type: "simple-line",
            width: 2,
            color: [255, 255, 0]
        }
    }
};