import { uvCockpitConfig } from './config.js';
import { trackPoint, trackLine, missionPoint, missionLine } from './uvtracks.js';

require([
    "esri/config",
    "esri/views/MapView",
    "esri/Map",
    "esri/layers/FeatureLayer",
    "esri/layers/support/Field",
    "esri/geometry/Point",
    "esri/geometry/Polyline",
    "esri/geometry/geometryEngine",
    "esri/request",
    "dojo/_base/array",
    "dojo/dom",
    "dojo/on",
    "dojo/domReady!"
], function (esriConfig, MapView, Map, FeatureLayer, Field, Point, Polyline, geometryEngine,
    esriRequest, arrayUtils, dom, on
) {
    /**************************************************
     * Create the map and view
     **************************************************/

    var map = new Map({
        basemap: "satellite",
        spatialReference: {
            wkid: 3857
        }
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

        for (var i = 0; i < geoJson.features.length; i++) {
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

        for (var i = 0; i < plan.mission.items.length; i++) {
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

        for (var i = 0; i < plan.mission.items.length; i++) {
            var item = plan.mission.items[i];
            var z = item.params[6] + plan.mission.plannedHomePosition[2];
            if (item.params[5] != 0 && item.params[6] != 0) {
                if (item.command == 22) //takeoff
                    coordinates.push([plan.mission.plannedHomePosition[1], plan.mission.plannedHomePosition[0], z]);
                else
                    coordinates.push([item.params[5], item.params[4], z]);
            }
        }

        var polyline = new Polyline({
            hasZ: true,
            hasM: false,
            paths: coordinates
        });

        var length = geometryEngine.geodesicLength(polyline, "meters");

        // Create an array of Graphics from each GeoJSON feature
        return [{
            geometry: polyline,
            attributes: {
                cruiseSpeed: plan.mission.cruiseSpeed,
                hoverSpeed: plan.mission.hoverSpeed,
                length: length
            }
        }];
    }

    /**************************************************
     * Create a FeatureLayer with the array of graphics
     **************************************************/

    function createTrackPointsLayer(graphics) {
        var pointsLayer = new FeatureLayer({
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
        var linesLayer = new FeatureLayer({
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
        var pointsLayer = new FeatureLayer({
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

        var linesLayer = new FeatureLayer({
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