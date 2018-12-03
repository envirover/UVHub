require([
    "esri/config",
    "esri/views/MapView",
    "esri/Map",
    "esri/layers/FeatureLayer",
    "esri/layers/support/Field",
    "esri/Graphic",
    "esri/geometry/Point",
    "esri/geometry/Polyline",
    "esri/renderers/SimpleRenderer",
    "esri/symbols/SimpleMarkerSymbol",
    "esri/symbols/SimpleLineSymbol",
    "esri/symbols/TextSymbol",
    "esri/symbols/PictureMarkerSymbol",
    "esri/request",
    "dojo/_base/array",
    "dojo/dom",
    "dojo/on",
    "dojo/domReady!"
], function (esriConfig, MapView, Map, FeatureLayer, Field, Graphic, Point, Polyline,
    SimpleRenderer, SimpleMarkerSymbol, SimpleLineSymbol, TextSymbol, PictureMarkerSymbol, esriRequest,
    arrayUtils, dom, on
) {
    var trackPoint = {
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
        renderer2d: new SimpleRenderer({
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
        })
    };

    var trackLine = {
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
        renderer2d: new SimpleRenderer({
            symbol: new SimpleLineSymbol({
                width: 5,
                color: [256, 0, 0]
            })
        })
    }

    var missionPoint = {
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
    var missionLine = {
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
        renderer2d: new SimpleRenderer({
            symbol: new SimpleLineSymbol({
                width: 2,
                color: [255, 255, 0]
            })
        })
    };

    /**************************************************
     * Create the map and view
     **************************************************/

    var map = new Map({
        basemap: "satellite",
    });

    var view = new MapView({
        container: "viewDiv",
        map: map
    });

    view.when(function () {
        var missions = getMissions()
            .then(fixHomeAltitude);

        missions
            .then(createMissionLinesGraphics)
            .then(createMissionLinesLayer)
            .otherwise(errback);

        missions
            .then(createMissionPointsGraphics)
            .then(createMissionPointsLayer)
            .otherwise(errback);

        var tracks = missions
            .then(getTracks)
            .then(fixTracksAltitude);

        tracks
            .then(createTrackLinesGraphics)
            .then(createTrackLinesLayer)
            .otherwise(errback);

        tracks
            .then(createTrackPointsGraphics)
            .then(createTrackPointsLayer)
            .then(zoomToLayer)
            .otherwise(errback);
    });

    function getTracks() {
        var url = uvCockpitConfig.uvTracksBaseURL + "/uvtracks/api/v1/tracks" + window.location.search;

        return esriRequest(url, {
            responseType: "json"
        });
    }

    function getMissions() {
        var url = uvCockpitConfig.uvTracksBaseURL + "/uvtracks/api/v1/missions" + window.location.search;

        return esriRequest(url, {
            responseType: "json"
        });
    }

    // Do nothing for 2D view.
    async function fixHomeAltitude(response) {
        return response.data;
    }

    function fixTracksAltitude(response) {
        return response.data;
    }


    function zoomToLayer(layer) {
        view.whenLayerView(layer).then(function (layerView) {
            layerView.queryExtent().then(function (response) {
                // go to the extent of all the graphics in the layer view
                if (response.extent != null) {
                    view.scale = 10000;
                    view.goTo(response.extent);
                }
            });
        });
    }

    /**************************************************
     * Create graphics with returned geojson data
     **************************************************/

    function createTrackPointsGraphics(geoJson) {
        if (geoJson.features.length == 0)
            return [];

        var feature = geoJson.features[0];
        // Create an array of Graphics from each GeoJSON feature

        var graphics = {
            geometry: new Point({
                x: feature.geometry.coordinates[0],
                y: feature.geometry.coordinates[1],
                z: feature.geometry.coordinates[2]
            }),
            attributes: feature.properties
        };

        graphics.attributes.latitude /= 10000000.0;
        graphics.attributes.longitude /= 10000000.0;
        graphics.attributes.heading /= 100.0;
        graphics.attributes.pitch /= 100.0;
        graphics.attributes.roll /= 100.0;
        graphics.attributes.tilt = graphics.attributes.roll - 90;

        return [graphics];
    }

    function createTrackLinesGraphics(geoJson) {
        var minTime = -1;
        var maxTime = -1;
        var coordinates = [];

        for (i = 0; i < geoJson.features.length; i++) {
            var feature = geoJson.features[i];

            coordinates.push(feature.geometry.coordinates);

            var recordTime = feature.properties.time;

            if (minTime < 0 || recordTime < minTime) {
                minTime = recordTime;
            }

            if (maxTime < 0 || recordTime > maxTime) {
                maxTime = recordTime;
            }
        }

        // Create an array of Graphics from each GeoJSON feature
        return [{
            geometry: new Polyline({
                hasZ: true,
                hasM: false,
                paths: coordinates
            }),
            attributes: {
                from_time: minTime,
                to_time: maxTime
            }
        }];
    }

    function createMissionPointsGraphics(response) {
        var plan = response;

        if (plan.mission.plannedHomePosition.length < 3)
            return [];

        // Create an array of Graphics from mission items
        var points = [];

        points.push({
            geometry: new Point({
                x: plan.mission.plannedHomePosition[1],
                y: plan.mission.plannedHomePosition[0],
                z: plan.mission.plannedHomePosition[2],
            }),
            attributes: {
                seq: 0,
                command: 16,
                autoContinue: 1,
                frame: 0,
                doJumpId: 1,
                type: "PlannedHome",
                params: []
            }
        });

        for (i = 0; i < plan.mission.items.length; i++) {
            var item = plan.mission.items[i];

            if (item.params[5] != 0 && item.params[6] != 0) {
                var point = new Point({
                    x: item.params[5],
                    y: item.params[4],
                    z: item.params[6] + points[0].geometry.z
                });

                if (item.command == 22) { //takeoff
                    point = new Point({
                        x: points[0].geometry.x,
                        y: points[0].geometry.y,
                        z: points[0].geometry.z + item.params[6]
                    });
                }

                points.push({
                    geometry: point,
                    attributes: {
                        seq: i + 1,
                        command: item.command,
                        autoContinue: item.autoContinue,
                        frame: item.frame,
                        doJumpId: item.doJumpId,
                        type: item.type,
                        params: item.params
                    }
                });
            }
        }

        return points;
    }

    function createMissionLinesGraphics(response) {
        // raw plan data
        var plan = response;

        if (plan.mission.plannedHomePosition.length < 3)
            return [];

        var coordinates = [];

        coordinates.push([plan.mission.plannedHomePosition[1],
            plan.mission.plannedHomePosition[0],
            plan.mission.plannedHomePosition[2]
        ]);

        for (i = 0; i < plan.mission.items.length; i++) {
            var item = plan.mission.items[i];
            var z = item.params[6] + plan.mission.plannedHomePosition[2];
            if (item.params[5] != 0 && item.params[6] != 0) {
                if (item.command == 22) //takeoff
                    coordinates.push([plan.mission.plannedHomePosition[1], plan.mission.plannedHomePosition[0], z]);
                else
                    coordinates.push([item.params[5], item.params[4], z]);
            }
        }

        // Create an array of Graphics from each GeoJSON feature
        return [{
            geometry: new Polyline({
                hasZ: true,
                hasM: false,
                paths: coordinates
            }),
            attributes: {
                cruiseSpeed: plan.mission.cruiseSpeed,
                hoverSpeed: plan.mission.hoverSpeed
            }
        }];
    }

    /**************************************************
     * Create a FeatureLayer with the array of graphics
     **************************************************/

    function createTrackPointsLayer(graphics) {
        pointsLayer = new FeatureLayer({
            source: graphics, // autocast as an array of esri/Graphic
            fields: trackPoint.fields,
            objectIdField: "sysid",
            renderer: trackPoint.renderer2d,
            spatialReference: {
                wkid: 4326
            },
            geometryType: "point",
            popupTemplate: trackPoint.template
        });

        map.add(pointsLayer);

        return pointsLayer;
    }

    function createTrackLinesLayer(graphics) {
        linesLayer = new FeatureLayer({
            source: graphics, // autocast as an array of esri/Graphic
            fields: trackLine.fields,
            objectIdField: "sysid",
            renderer: trackLine.renderer2d,
            spatialReference: {
                wkid: 4326
            },
            geometryType: "polyline",
            popupTemplate: trackLine.template
        });

        map.add(linesLayer);
        return linesLayer;
    }

    function createMissionPointsLayer(graphics) {
        pointsLayer = new FeatureLayer({
            source: graphics, // autocast as an array of esri/Graphic
            fields: missionPoint.fields,
            objectIdField: "seq",
            renderer: missionPoint.renderer2d,
            spatialReference: {
                wkid: 4326
            },
            geometryType: "point",
            popupTemplate: missionPoint.template,
            labelingInfo: [missionPoint.labelClass]
        });

        map.add(pointsLayer);
        return pointsLayer;
    }

    function createMissionLinesLayer(graphics) {
        if (graphics == null || graphics.length == 0)
            return null;

        linesLayer = new FeatureLayer({
            source: graphics, // autocast as an array of esri/Graphic
            fields: missionLine.fields,
            objectIdField: "sysid",
            renderer: missionLine.renderer2d,
            spatialReference: {
                wkid: 4326
            },
            geometryType: "polyline",
            popupTemplate: missionLine.template
        });

        map.add(linesLayer);
        return linesLayer;
    }

    // Executes if data retrieval was unsuccessful.
    function errback(error) {
        console.error("Error. ", error);
    }

});