<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="initial-scale=1,maximum-scale=1,user-scalable=no">
  <title>SPL Tracker Service Demo</title>
  <style>
    html,
    body,
    #viewDiv {
      padding: 0;
      margin: 0;
      height: 100%;
      width: 100%;
    }

    #infoDiv {
      position: absolute;
      bottom: 15px;
      right: 0;
      max-height: 80%;
      max-width: 300px;
      background-color: black;
      padding: 8px;
      border-top-left-radius: 5px;
      color: white;
      opacity: 0.8;
    }
  </style>

  <link rel="stylesheet" href="https://js.arcgis.com/4.3/esri/css/main.css">
  <script src="https://js.arcgis.com/4.3/"></script>

  <script>
     require([
      "esri/views/MapView",
      "esri/Map",
      "esri/layers/FeatureLayer",
      "esri/layers/support/Field",
      "esri/geometry/Point",
      "esri/geometry/Polyline",
      "esri/renderers/SimpleRenderer",
      "esri/symbols/SimpleMarkerSymbol",
      "esri/symbols/SimpleLineSymbol",
      "esri/symbols/TextSymbol",
      "esri/renderers/SimpleRenderer",
      "esri/widgets/Legend",
      "esri/request",
      "dojo/_base/array",
      "dojo/dom",
      "dojo/on",
      "dojo/domReady!"
    ], function(MapView, Map, FeatureLayer, Field, Point, Polyline,
      SimpleRenderer, SimpleMarkerSymbol, SimpleLineSymbol, TextSymbol, SimpleRenderer, Legend, esriRequest,
      arrayUtils, dom, on
    ) {

      var pointsLayer, legend;

      /**************************************************
       * Define the specification for each field to create
       * in the layer
       **************************************************/

      var pointFields = [
      {
        name: "ObjectID",
        alias: "ObjectID",
        type: "oid"
      }, {
        name: "device_id",
        alias: "device_id",
        type: "string"
      },{
          name: "time",
          alias: "time",
          type: "integer"
      }, {
          name: "airspeed",
          alias: "airspeed",
          type: "integer"
      }, {
          name: "airspeed_sp",
          alias: "airspeed_sp",
          type: "integer"
      }, {
          name: "altitude_amsl",
          alias: "altitude_amsl",
          type: "integer"
      }, {
          name: "altitude_sp",
          alias: "altitude_sp",
          type: "integer"
      }, {
          name: "base_mode",
          alias: "base_mode",
          type: "integer"
      }, {
          name: "battery_remaining",
          alias: "battery_remaining",
          type: "integer"
      }, {
          name: "climb_rate",
          alias: "climb_rate",
          type: "integer"
      }, {
          name: "custom_mode",
          alias: "custom_mode",
          type: "integer"
      }, {
          name: "failsafe",
          alias: "failsafe",
          type: "integer"
      }, {
          name: "gps_fix_type",
          alias: "gps_fix_type",
          type: "integer"
      }, {
          name: "gps_nsat",
          alias: "gps_nsat",
          type: "integer"
      }, {
          name: "groundspeed",
          alias: "groundspeed",
          type: "integer"
      }, {
          name: "heading",
          alias: "heading",
          type: "double"
      }, {
          name: "heading_sp",
          alias: "heading_sp",
          type: "integer"
      }, {
          name: "landed_state",
          alias: "landed_state",
          type: "integer"
      }, {
          name: "latitude",
          alias: "latitude",
          type: "integer"
      }, {
          name: "longitude",
          alias: "longitude",
          type: "integer"
      }, {
          name: "pitch",
          alias: "pitch",
          type: "integer"
      }, {
          name: "roll",
          alias: "roll",
          type: "integer"
      }, {
          name: "temperature",
          alias: "temperature",
          type: "integer"
      }, {
          name: "temperature_air",
          alias: "temperature_air",
          type: "integer"
      }, {
          name: "throttle",
          alias: "throttle",
          type: "integer"
      }, {
          name: "wp_distance",
          alias: "wp_distance",
          type: "integer"
      }, {
          name: "wp_num",
          alias: "wp_num",
          type: "integer"
      }];

      var lineFields = [{
        name: "ObjectID",
        alias: "ObjectID",
        type: "oid"
      }, {
        name: "device_id",
        alias: "device_id",
        type: "string"
      }];

      // Set up popup template for the layer
      var pointsTemplate = {
        title: "Device {device_id} at {time}",
        content: [{
          type: "fields",
          fieldInfos: [{
            fieldName: "airspeed",
            label: "airspeed",
            visible: true
          }, {
              fieldName: "airpeed_sp",
              label: "airpeed_sp",
              visible: true
          }, {
              fieldName: "altitude_amsl",
              label: "altitude_amsl",
              visible: true
          }, {
              fieldName: "altitude_sp",
              label: "altitude_sp",
              visible: true
          }, {
              fieldName: "base_mode",
              label: "base_mode",
              visible: true
          }, {
              fieldName: "battery_remaining",
              label: "battery_remaining",
              visible: true
          }, {
              fieldName: "climb_rate",
              label: "climb_rate",
              visible: true
          }, {
              fieldName: "custom_mode",
              label: "custom_mode",
              visible: true
          }, {
              fieldName: "failsafe",
              label: "failsafe",
              visible: true
          }, {
              fieldName: "gps_fix_type",
              label: "gps_fix_type",
              visible: true
          }, {
              fieldName: "gps_nsat",
              label: "gps_nsat",
              visible: true
          }, {
              fieldName: "groundspeed",
              label: "groundspeed",
              visible: true
          }, {
              fieldName: "heading",
              label: "heading",
              visible: true
          }, {
              fieldName: "heading_sp",
              label: "heading_sp",
              visible: true
          }, {
              fieldName: "landed_state",
              label: "landed_state",
              visible: true
          }, {
              fieldName: "latitude",
              label: "latitude",
              visible: true
          }, {
              fieldName: "longitude",
              label: "longitude",
              visible: true
          }, {
              fieldName: "pitch",
              label: "pitch",
              visible: true
          }, {
              fieldName: "roll",
              label: "roll",
              visible: true
          }, {
              fieldName: "temperature",
              label: "temperature",
              visible: true
          }, {
              fieldName: "temperature_air",
              label: "temperature_air",
              visible: true
          }, {
              fieldName: "throttle",
              label: "throttle",
              visible: true
          }, {
              fieldName: "wp_distance",
              label: "wp_distance",
              visible: true
          }, {
              fieldName: "wp_num",
              label: "wp_num",
              visible: true
          }]
        }]
      };

      // Set up popup template for the layer
      var linesTemplate = {
        title: "Device {device_id}",
        content: [{
          type: "fields",
          fieldInfos: [{
            fieldName: "device_id",
            label: "device_id",
            visible: true
          }]
        }]
      };

      /**************************************************
       * Create the map and view
       **************************************************/

      var map = new Map({
        basemap: "satellite"
      });

      // Create MapView
      var view = new MapView({
        container: "viewDiv",
        map: map,
        center: [0, 0],
        zoom: 3,
        // customize ui padding for legend placement
        ui: {
          padding: {
            bottom: 15,
            right: 0
          }
        }
      });

      /**************************************************
       * Define the renderer for symbolizing points
       **************************************************/

      var pointsRenderer = new SimpleRenderer({
        symbol:  new TextSymbol({
            color: [256, 0, 256],
            text: "\ue900", // esri-icon-map-pin
            yoffset: -3,
            font: { // autocast as esri/symbols/Font
              size: 16,
              family: "CalciteWebCoreIcons",
              weight: "bolder"
            }
          }),
        visualVariables: [
        {
          type: "rotation",
          field: "heading", 
          rotation_type: "geographic"
        }]
      });

      var linesRenderer = new SimpleRenderer({
        symbol: new SimpleLineSymbol({
          width: 2,
          color: [256, 0, 0]
        })
      });
  
      view.then(function() {
        getLines()
          .then(createLinesGraphics) // then send it to the createPointsGraphics() method
          .then(createLinesLayer) // when graphics are created, create the layer
          .otherwise(errback);

        getPoints()
          .then(createPointsGraphics) // then send it to the createPointsGraphics() method
          .then(createPointsLayer) // when graphics are created, create the layer
          .then(zoomToLayer)
          .otherwise(errback);
      });

      // Request the points data
      function getPoints() {
        var url = "/uvtracks/features" + window.location.search;

        return esriRequest(url, {
          responseType: "json"
        });
      }

      // Request the linestring data
      function getLines() {
        var url = "/uvtracks/features" + window.location.search + "&type=linestring";

        return esriRequest(url, {
          responseType: "json"
        });
      }

      function zoomToLayer(layer) {
    	  view.whenLayerView(layer).then(function(layerView){
    		  layerView.watch("updating", function(val){
    		    // wait for the layer view to finish updating
    		    if(!val){
    		      layerView.queryExtent().then(function(response){
    		        // go to the extent of all the graphics in the layer view
    		        view.goTo(response.extent);
    		      });
    		    }
    		  });
    		});
      }

      /**************************************************
       * Create graphics with returned geojson data
       **************************************************/

      function createPointsGraphics(response) {
        // raw GeoJSON data
        var geoJson = response.data;

        // Create an array of Graphics from each GeoJSON feature
        return arrayUtils.map(geoJson.features, function(feature, i) {
          var graphics = {
            geometry: new Point({
              x: feature.geometry.coordinates[0],
              y: feature.geometry.coordinates[1]
            }),
            attributes: feature.properties
          };
          graphics.attributes.latitude /= 10000000.0;
          graphics.attributes.longitude /= 10000000.0;
          graphics.attributes.heading /= 100.0;
          graphics.attributes.pitch /= 100.0;
          graphics.attributes.roll /= 100.0;
          return graphics;
        });
      }

      function createLinesGraphics(response) {
        // raw GeoJSON data
        var geoJson = response.data;

        // Create an array of Graphics from each GeoJSON feature
        return arrayUtils.map(geoJson.features, function(feature, i) {
          return {
            geometry: new Polyline({
              hasZ: true,
              hasM: false,
              paths: feature.geometry.coordinates
            }),
            attributes: feature.properties
          };
        });
      }

      /**************************************************
       * Create a FeatureLayer with the array of graphics
       **************************************************/

      function createPointsLayer(graphics) {

        pointsLayer = new FeatureLayer({
          source: graphics, // autocast as an array of esri/Graphic
          // create an instance of esri/layers/support/Field for each field object
          fields: pointFields, // This is required when creating a layer from Graphics
          objectIdField: "ObjectID", // This must be defined when creating a layer from Graphics
          renderer: pointsRenderer, // set the visualization on the layer
          spatialReference: {
            wkid: 4326
          },
          geometryType: "point", // Must be set when creating a layer from Graphics
          popupTemplate: pointsTemplate
        });

        map.add(pointsLayer);

        return pointsLayer;
      }

      function createLinesLayer(graphics) {

          linesLayer = new FeatureLayer({
            source: graphics, // autocast as an array of esri/Graphic
            // create an instance of esri/layers/support/Field for each field object
            fields: lineFields, // This is required when creating a layer from Graphics
            objectIdField: "ObjectID", // This must be defined when creating a layer from Graphics
            renderer: linesRenderer, // set the visualization on the layer
            spatialReference: {
              wkid: 4326
            },
            geometryType: "polyline", // Must be set when creating a layer from Graphics
            popupTemplate: linesTemplate
          });

          map.add(linesLayer);
          return linesLayer;
        }

      /******************************************************************
       * Add layer to layerInfos in the legend
       ******************************************************************/

      function createLegend(layer) {
        // if the legend already exists, then update it with the new layer
    
      }

      // Executes if data retrieval was unsuccessful.
      function errback(error) {
        console.error("Creating legend failed. ", error);
      }

    });
  </script>
</head>

<body>
  <div id="viewDiv"></div>
  <div id="infoDiv">
    <h2>SPL Tracker Demo</h2>
  </div>
</body>
</html>