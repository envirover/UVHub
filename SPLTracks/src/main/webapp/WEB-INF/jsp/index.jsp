<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="initial-scale=1,maximum-scale=1,user-scalable=no">
  <title>Create a FeatureLayer with client side graphics - 4.3</title>
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
      "esri/renderers/SimpleRenderer",
      "esri/symbols/SimpleMarkerSymbol",
      "esri/widgets/Legend",
      "esri/request",
      "dojo/_base/array",
      "dojo/dom",
      "dojo/on",
      "dojo/domReady!"
    ], function(MapView, Map, FeatureLayer, Field, Point,
      SimpleRenderer, SimpleMarkerSymbol, Legend, esriRequest,
      arrayUtils, dom, on
    ) {

      var lyr, legend;

      /**************************************************
       * Define the specification for each field to create
       * in the layer
       **************************************************/

      var fields = [
      {
        name: "ObjectID",
        alias: "ObjectID",
        type: "oid"
      }, {
        name: "device_id",
        alias: "device_id",
        type: "string"
      }];

      // Set up popup template for the layer
      var pTemplate = {
        title: "{device_id}",
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
        basemap: "gray"
      });

      // Create MapView
      var view = new MapView({
        container: "viewDiv",
        map: map,
        center: [-144.492, 62.771],
        zoom: 5,
        // customize ui padding for legend placement
        ui: {
          padding: {
            bottom: 15,
            right: 0
          }
        }
      });

      /**************************************************
       * Define the renderer for symbolizing earthquakes
       **************************************************/

      var quakesRenderer = new SimpleRenderer({
        symbol: new SimpleMarkerSymbol({
          style: "circle",
          size: 20,
          color: [211, 255, 0, 0],
          outline: {
            width: 1,
            color: "#FF0055",
            style: "solid"
          }
        }),
        visualVariables: [
        {
          type: "size",
          field: "time", // earthquake magnitude
          valueUnit: "unknown",
          minDataValue: 2,
          maxDataValue: 7,
          // Define size of mag 2 quakes based on scale
          minSize: {
            type: "size",
            expression: "view.scale",
            stops: [
            {
              value: 1128,
              size: 12
            },
            {
              value: 36111,
              size: 12
            },
            {
              value: 9244649,
              size: 6
            },
            {
              value: 73957191,
              size: 4
            },
            {
              value: 591657528,
              size: 2
            }]
          },
          // Define size of mag 7 quakes based on scale
          maxSize: {
            type: "size",
            expression: "view.scale",
            stops: [
            {
              value: 1128,
              size: 80
            },
            {
              value: 36111,
              size: 60
            },
            {
              value: 9244649,
              size: 50
            },
            {
              value: 73957191,
              size: 50
            },
            {
              value: 591657528,
              size: 25
            }]
          }
        }]
      });

      view.then(function() {
        // Request the earthquake data from USGS when the view resolves
        getData()
          .then(createGraphics) // then send it to the createGraphics() method
          .then(createLayer) // when graphics are created, create the layer
          .otherwise(errback);
      });

      // Request the earthquake data
      function getData() {

        // data downloaded from the USGS at http://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/ on 4/4/16
        // month.geojson represents recorded earthquakes between 03/04/2016 and 04/04/2016
        // week.geojson represents recorded earthquakes betwen 03/28/2016 and 04/04/2016

        var url = "http://localhost:8080/spltracks/features?deviceId=300234064280890";

        return esriRequest(url, {
          responseType: "json"
        });
      }

      /**************************************************
       * Create graphics with returned geojson data
       **************************************************/

      function createGraphics(response) {
        // raw GeoJSON data
        var geoJson = response.data;

        // Create an array of Graphics from each GeoJSON feature
        return arrayUtils.map(geoJson.features, function(feature, i) {
          return {
            geometry: new Point({
              x: feature.geometry.coordinates[0],
              y: feature.geometry.coordinates[1]
            }),
            // select only the attributes you care about
            attributes: {
              ObjectID: i,
              device_id: feature.properties.device_id
            }
          };
        });
      }

      /**************************************************
       * Create a FeatureLayer with the array of graphics
       **************************************************/

      function createLayer(graphics) {

        lyr = new FeatureLayer({
          source: graphics, // autocast as an array of esri/Graphic
          // create an instance of esri/layers/support/Field for each field object
          fields: fields, // This is required when creating a layer from Graphics
          objectIdField: "ObjectID", // This must be defined when creating a layer from Graphics
          renderer: quakesRenderer, // set the visualization on the layer
          spatialReference: {
            wkid: 4326
          },
          geometryType: "point", // Must be set when creating a layer from Graphics
          popupTemplate: pTemplate
        });

        map.add(lyr);
        return lyr;
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
    <h2>Worldwide Earthquakes</h2>
    Reported from 03/28/16 to 04/04/16
  </div>
</body>
</html>