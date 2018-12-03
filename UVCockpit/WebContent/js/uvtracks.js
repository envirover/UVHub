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
                    label: "airspeed",
                    visible: true
                },
                {
                    fieldName: "airspeed_sp",
                    label: "airspeed_sp",
                    visible: true
                },
                {
                    fieldName: "altitude_amsl",
                    label: "altitude_amsl",
                    visible: true
                },
                {
                    fieldName: "altitude_sp",
                    label: "altitude_sp",
                    visible: true
                },
                {
                    fieldName: "base_mode",
                    label: "base_mode",
                    visible: true
                },
                {
                    fieldName: "battery_remaining",
                    label: "battery_remaining",
                    visible: true
                },
                {
                    fieldName: "climb_rate",
                    label: "climb_rate",
                    visible: true
                },
                {
                    fieldName: "custom_mode",
                    label: "custom_mode",
                    visible: true
                },
                {
                    fieldName: "failsafe",
                    label: "failsafe",
                    visible: true
                },
                {
                    fieldName: "gps_fix_type",
                    label: "gps_fix_type",
                    visible: true
                },
                {
                    fieldName: "gps_nsat",
                    label: "gps_nsat",
                    visible: true
                },
                {
                    fieldName: "groundspeed",
                    label: "groundspeed",
                    visible: true
                },
                {
                    fieldName: "heading",
                    label: "heading",
                    visible: true
                },
                {
                    fieldName: "heading_sp",
                    label: "heading_sp",
                    visible: true
                },
                {
                    fieldName: "landed_state",
                    label: "landed_state",
                    visible: true
                },
                {
                    fieldName: "latitude",
                    label: "latitude",
                    visible: true
                },
                {
                    fieldName: "longitude",
                    label: "longitude",
                    visible: true
                },
                {
                    fieldName: "pitch",
                    label: "pitch",
                    visible: true
                },
                {
                    fieldName: "roll",
                    label: "roll",
                    visible: true
                },
                {
                    fieldName: "temperature",
                    label: "voltage",
                    visible: true
                },
                {
                    fieldName: "temperature_air",
                    label: "current",
                    visible: true
                },
                {
                    fieldName: "throttle",
                    label: "throttle",
                    visible: true
                },
                {
                    fieldName: "wp_distance",
                    label: "wp_distance",
                    visible: true
                },
                {
                    fieldName: "wp_num",
                    label: "wp_num",
                    visible: true
                }
            ]
        }]
    },
    renderer2d: {
        type: "simple",
        symbol: {
            type: "picture-marker", // autocasts as new PictureMarkerSymbol()
            url: "http://static.arcgis.com/images/Symbols/Arrows/icon34.png",
            width: "60px",
            height: "60px",
            xoffset: 0,
            yoffset: 6,
        },
        visualVariables: [{
            type: "rotation",
            field: "heading",
            rotation_type: "geographic"
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
        type: "unique-value", // autocasts as new UniqueValueRenderer()
        field: "type",
        defaultSymbol: {
            type: "simple-marker",  // autocasts as new SimpleMarkerSymbol()
            color: "yellow",
            outline: {  // autocasts as new SimpleLineSymbol()
              color: "yellow",
              width: "0.5px"
            }
          }, // autocasts as new SimpleFillSymbol()
        uniqueValueInfos: [{
            // All features with value of "PlannedHome" will be green
            value: "PlannedHome",
            symbol: {
                type: "simple-marker",  // autocasts as new SimpleMarkerSymbol()
                color: "green",
                outline: {  // autocasts as new SimpleLineSymbol()
                  color: "green",
                  width: "0.5px"
                }
              }
        }]
    },
    renderer3d: {
        type: "unique-value", // autocasts as new UniqueValueRenderer()
        field: "type",
        defaultSymbol: {
            type: "point-3d", // autocasts as new PointSymbol3D()
            symbolLayers: [{
                type: "object", // autocasts as new ObjectSymbol3DLayer()
                width: 4, // diameter of the object from east to west in meters
                height: 4, // height of the object in meters
                depth: 4, // diameter of the object from north to south in meters
                resource: {
                    primitive: "sphere"
                },
                material: {
                    color: [255, 255, 0, 1]
                }
            }]
        }, // autocasts as new SimpleFillSymbol()
        uniqueValueInfos: [{
            // All features with value of "PlannedHome" will be green
            value: "PlannedHome",
            symbol: {
                type: "point-3d", // autocasts as new PointSymbol3D()
                symbolLayers: [{
                    type: "object", // autocasts as new ObjectSymbol3DLayer()
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
        // autocasts as new LabelClass()
        symbol: {
            type: "text", // autocasts as new TextSymbol()
            color: "white",
            haloColor: "black",
            font: { // autocast as new Font()
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
        }
    ],
    template: {
        title: "Mission",
        content: [{
            type: "fields",
            fieldInfos: [{
                    fieldName: "cruiseSpeed",
                    label: "cruiseSpeed",
                    visible: true
                },
                {
                    fieldName: "hoverSpeed",
                    label: "hoverSpeed",
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