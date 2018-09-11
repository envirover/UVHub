<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="initial-scale=1,maximum-scale=1,user-scalable=no">
  <title>UV Tracks Service 3D Demo</title>
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

  <link rel="stylesheet" href="https://js.arcgis.com/4.8/esri/css/main.css">
  <script src="https://js.arcgis.com/4.8/"></script>

  <script>
     require([
      "esri/views/SceneView",
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
    ], function(SceneView, Map, FeatureLayer, Field, Point, Polyline,
      SimpleRenderer, SimpleMarkerSymbol, SimpleLineSymbol, TextSymbol, SimpleRenderer, Legend, esriRequest,
      arrayUtils, dom, on
    ) {

      var pointsLayer, legend;

      var elevationDelta = 0.0;
      
      /**************************************************
       * Define the specification for each field to create
       * in the layer
       **************************************************/

      var pointFields = [
	      { name: "ObjectID", alias: "ObjectID", type: "oid" }, 
	      { name: "sysid", alias: "sysid", type: "string" },
	      { name: "time", alias: "time", type: "integer" },
	      { name: "airspeed", alias: "airspeed", type: "integer" },
	      { name: "airspeed_sp", alias: "airspeed_sp", type: "integer" }, 
	      { name: "altitude_amsl", alias: "altitude_amsl", type: "integer" }, 
	      { name: "altitude_sp", alias: "altitude_sp", type: "integer" }, 
	      { name: "base_mode", alias: "base_mode", type: "integer" }, 
	      { name: "battery_remaining", alias: "battery_remaining", type: "integer" }, 
	      { name: "climb_rate", alias: "climb_rate", type: "integer" }, 
	      { name: "custom_mode", alias: "custom_mode", type: "integer" }, 
	      { name: "failsafe", alias: "failsafe", type: "integer" }, 
	      { name: "gps_fix_type", alias: "gps_fix_type", type: "integer" }, 
	      { name: "gps_nsat", alias: "gps_nsat", type: "integer" }, 
	      { name: "groundspeed", alias: "groundspeed", type: "integer" }, 
	      { name: "heading", alias: "heading", type: "double" }, 
	      { name: "heading_sp", alias: "heading_sp", type: "integer" }, 
	      { name: "landed_state", alias: "landed_state", type: "integer" }, 
	      { name: "latitude", alias: "latitude", type: "integer" }, 
	      { name: "longitude", alias: "longitude", type: "integer" },
	      { name: "pitch", alias: "pitch", type: "integer" }, 
	      { name: "roll", alias: "roll", type: "integer" }, 
	      { name: "temperature", alias: "temperature", type: "integer" }, 
	      { name: "temperature_air", alias: "temperature_air", type: "integer" }, 
	      { name: "throttle", alias: "throttle", type: "integer" }, 
	      { name: "wp_distance", alias: "wp_distance", type: "integer" }, 
	      { name: "wp_num", alias: "wp_num", type: "integer" }];

      var lineFields = [
    	  { name: "ObjectID", alias: "ObjectID", type: "oid" }, 
    	  { name: "from_time", alias: "from_time", type: "string" }, 
    	  { name: "to_time", alias: "to_time", type: "string" }];

      var missionLineFields = [
    	  { name: "ObjectID", alias: "ObjectID", type: "oid" }, 
    	  { name: "cruiseSpeed", alias: "cruiseSpeed", type: "string" }, 
    	  { name: "hooverSpeed", alias: "hooverSpeed", type: "string" }];
      
      // Set up popup template for the layer
      var pointsTemplate = {
        title: "System {sysid} at {time}",
        content: [{
          type: "fields",
          fieldInfos: [
        	  { fieldName: "airspeed", label: "airspeed", visible: true }, 
        	  { fieldName: "airpeed_sp", label: "airpeed_sp", visible: true }, 
        	  { fieldName: "altitude_amsl", label: "altitude_amsl", visible: true }, 
        	  { fieldName: "altitude_sp", label: "altitude_sp", visible: true }, 
        	  { fieldName: "base_mode", label: "base_mode", visible: true }, 
        	  { fieldName: "battery_remaining", label: "battery_remaining", visible: true }, 
        	  { fieldName: "climb_rate", label: "climb_rate", visible: true }, 
        	  { fieldName: "custom_mode", label: "custom_mode", visible: true }, 
        	  { fieldName: "failsafe", label: "failsafe", visible: true }, 
        	  { fieldName: "gps_fix_type", label: "gps_fix_type", visible: true }, 
        	  { fieldName: "gps_nsat", label: "gps_nsat", visible: true }, 
        	  { fieldName: "groundspeed", label: "groundspeed", visible: true }, 
        	  { fieldName: "heading", label: "heading", visible: true }, 
        	  { fieldName: "heading_sp", label: "heading_sp", visible: true }, 
        	  { fieldName: "landed_state", label: "landed_state", visible: true }, 
        	  { fieldName: "latitude", label: "latitude", visible: true }, 
        	  { fieldName: "longitude", label: "longitude", visible: true }, 
        	  { fieldName: "pitch", label: "pitch", visible: true }, 
        	  { fieldName: "roll", label: "roll", visible: true }, 
        	  { fieldName: "temperature", label: "voltage", visible: true }, 
        	  { fieldName: "temperature_air", label: "current", visible: true }, 
        	  { fieldName: "throttle", label: "throttle", visible: true }, 
        	  { fieldName: "wp_distance", label: "wp_distance", visible: true }, 
        	  { fieldName: "wp_num", label: "wp_num", visible: true }]
        }]
      };

      // Set up popup template for the layer
      var missionPointsTemplate = {
        title: "Mission item {seq}",
        content: [{
          type: "fields",
          fieldInfos: [
        	  { fieldName: "seq", label: "seq", visible: true },
        	  { fieldName: "autoContinue", label: "autoContinue", visible: true }, 
        	  { fieldName: "command", label: "command", visible: true }, 
        	  { fieldName: "doJumpId", label: "doJumpId", visible: true }, 
        	  { fieldName: "frame", label: "frame", visible: true }, 
        	  { fieldName: "params", label: "params", visible: true }, 
        	  { fieldName: "type", label: "type", visible: true }]
        }]
      };

      // Set up popup template for the layer
      var linesTemplate = {
        title: "Track",
        content: [{
          type: "fields",
          fieldInfos: [
        	  { fieldName: "from_time", label: "from_time", visible: true },
        	  { fieldName: "to_time", label: "to_time", visible: true }]
        }]
      };
      
      // Set up popup template for the layer
      var linesMissionTemplate = {
        title: "Mission",
        content: [{
          type: "fields",
          fieldInfos: [
        	  { fieldName: "cruiseSpeed", label: "cruiseSpeed", visible: true },
        	  { fieldName: "hooverSpeed", label: "hooverSpeed", visible: true }]
        }]
      };

      /**************************************************
       * Create the map and view
       **************************************************/

      var map = new Map({
        basemap: "satellite",
        ground: "world-elevation"
      });
      
      map.ground.navigationConstraint = {
    	type: "none"
      };

      map.ground.opacity = 0.8;
      
      // Create MapView
      var view = new SceneView({
   	    container: "viewDiv",  // Reference to the DOM node that will contain the view
   	    map: map  // References the map object created in step 3
   	  });
      
      /**************************************************
       * Define the renderer for symbolizing points
       **************************************************/

      var pointsRenderer = new SimpleRenderer({
        symbol: {
       	  type: "point-3d",  // autocasts as new PointSymbol3D()
       	  symbolLayers: [{
       	    type: "object",  // autocasts as new ObjectSymbol3DLayer()
       	    width: 3,  // diameter of the object from east to west in meters
       	    height: 4,  // height of the object in meters
       	    depth: 1,  // diameter of the object from north to south in meters
       	    resource: { primitive: "cone" },
       	    material: { color: "magenta" }
       	  }]
       	},
       	visualVariables: [{
     	  "type": "rotation",
          "field": "heading",
          "axis" :"heading"
        }, {
       	  "type": "rotation",
          "field": "roll",
          "axis" :"roll"
        }, {
      	  "type": "rotation",
          "field": "tilt",
          "axis" :"tilt"
        }]
      });

      var linesRenderer = new SimpleRenderer({
        symbol: new SimpleLineSymbol({
          width: 5,
          color: [256, 0, 0]
        })
      });
  
      var pointsMissionRendererOld = new SimpleRenderer({
    	  symbol: {
	        type: "point-3d",  // autocasts as new PointSymbol3D()
	      	symbolLayers: [{
	      	  type: "object",  // autocasts as new ObjectSymbol3DLayer()
	      	  width: 4,  // diameter of the object from east to west in meters
	      	  height: 4,  // height of the object in meters
	      	  depth: 4,  // diameter of the object from north to south in meters
	      	  resource: { primitive: "sphere" },
	      	  material: {  color: [255, 255, 0, 1] }
	       }]
	     }
      });
      
      var pointsMissionRenderer = {
		  type: "unique-value",  // autocasts as new UniqueValueRenderer()
		  field: "type",
		  defaultSymbol: {
	        type: "point-3d",  // autocasts as new PointSymbol3D()
	      	symbolLayers: [{
	      	  type: "object",  // autocasts as new ObjectSymbol3DLayer()
	      	  width: 4,  // diameter of the object from east to west in meters
	      	  height: 4,  // height of the object in meters
	      	  depth: 4,  // diameter of the object from north to south in meters
	      	  resource: { primitive: "sphere" },
	      	  material: {  color: [255, 255, 0, 1] }
	       }]
		  },  // autocasts as new SimpleFillSymbol()
		  uniqueValueInfos: [{
		    // All features with value of "North" will be blue
		    value: "PlannedHome",
		    symbol: {
		        type: "point-3d",  // autocasts as new PointSymbol3D()
		      	symbolLayers: [{
		      	  type: "object",  // autocasts as new ObjectSymbol3DLayer()
		      	  width: 4,  // diameter of the object from east to west in meters
		      	  height: 4,  // height of the object in meters
		      	  depth: 4,  // diameter of the object from north to south in meters
		      	  resource: { primitive: "sphere" },
		      	  material: {  color: [0, 255, 0, 1] }
		       }]
		     }
		  }]
		};
      
       
      var linesMissionRenderer = new SimpleRenderer({
        symbol: new SimpleLineSymbol({
          width: 2,
          color: [255, 255, 0]
        })
      });
      
      var labelClass = {
  		  // autocasts as new LabelClass()
  		  symbol: {
  		    type: "text",  // autocasts as new TextSymbol()
  		    color: "white",
  		    haloColor: "black",
  		    font: {  // autocast as new Font()
  		      family: "playfair-display",
  		      size: 24,
  		      weight: "bold"
  		    }
  		  },
  		  labelPlacement: "above-center",
  		  labelExpressionInfo: {
  		    expression: "$feature.seq"
  		  }
  	  };
     
      view.when(function() {
        var missions = getMissions()
          .then(fixHomeAltitude);
        
        missions
          .then(createMissionLinesGraphics) // then send it to the createPointsGraphics() method
          .then(createMissionLinesLayer) // when graphics are created, create the layer
          .otherwise(errback);
      
        missions
	      .then(createMissionPointsGraphics) // then send it to the createPointsGraphics() method
	      .then(createMissionPointsLayer) // when graphics are created, create the layer
	      .otherwise(errback);
        
    	var points = missions
    	  .then(getTracks)
    	  .then(fixTracksAltitude);
    	
    	points
          .then(createLinesGraphics) // then send it to the createPointsGraphics() method
          .then(createLinesLayer) // when graphics are created, create the layer
          .otherwise(errback);

        points
          .then(createPointsGraphics) // then send it to the createPointsGraphics() method
          .then(createPointsLayer) // when graphics are created, create the layer
          .then(zoomToLayer)
          .otherwise(errback);
      });
      

      // Request the points data
      function getTracks() {
        var url = "/uvtracks/tracks" + window.location.search;

        return esriRequest(url, {
          responseType: "json"
        });
      }

      // Request the points data
      function getMissions() {
        var url = "/uvtracks/missions" + window.location.search;

        return esriRequest(url, {
          responseType: "json"
        });
      }

      async function fixHomeAltitude(response) {
    	  var plan = response.data;
    	  
    	  var home =  new Point({
          	x: plan.mission.plannedHomePosition[1],
        	y: plan.mission.plannedHomePosition[0],
        	z: plan.mission.plannedHomePosition[2],
          });
    	  
          var elevation = await map.ground.layers.items[0].queryElevation(home);
      	
          elevationDelta = elevation.geometry.z - plan.mission.plannedHomePosition[2];
          
          plan.mission.plannedHomePosition[2] = elevation.geometry.z;
          
          return plan;
      }
      
      function fixTracksAltitude(response) {
    	  var geoJson = response.data;
    	  
    	  for (i = 0; i < geoJson.features.length; i++) {
          	var feature = geoJson.features[i];
          	feature.geometry.coordinates[2] += elevationDelta;
    	  }
    	  
    	  return geoJson;
      }
      
      function zoomToLayer(layer) {
    	  view.whenLayerView(layer).then(function(layerView){
   		      layerView.queryExtent().then(function(response){
   		        // go to the extent of all the graphics in the layer view
   		        view.scale = 10000;
   		        view.goTo(response.extent);
   		      });
      	  });
      }

      /**************************************************
       * Create graphics with returned geojson data
       **************************************************/

      function createPointsGraphics(geoJson) {
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

      function createLinesGraphics(geoJson) {
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
        return [ {
            geometry: new Polyline({
              hasZ: true,
              hasM: false,
              paths: coordinates
            }),
            attributes: {
            	from_time: minTime,
            	to_time: maxTime
            }
        } ];
      }

      function createMissionPointsGraphics(response) {
        var plan = response;

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

        var coordinates = [];
        
        coordinates.push([plan.mission.plannedHomePosition[1], 
        	              plan.mission.plannedHomePosition[0],
        	              plan.mission.plannedHomePosition[2]]);
        
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
        return [ {
            geometry: new Polyline({
              hasZ: true,
              hasM: false,
              paths: coordinates
            }),
            attributes: {
              cruiseSpeed: plan.mission.cruiseSpeed,
              hooverSpeed: plan.mission.hooverSpeed
            }
        } ];
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

      function createMissionPointsLayer(graphics) {

          pointsLayer = new FeatureLayer({
            source: graphics, // autocast as an array of esri/Graphic
            // create an instance of esri/layers/support/Field for each field object
            fields: pointFields, // This is required when creating a layer from Graphics
            objectIdField: "ObjectID", // This must be defined when creating a layer from Graphics
            renderer: pointsMissionRenderer, // set the visualization on the layer
            spatialReference: {
              wkid: 4326
            },
            geometryType: "point", // Must be set when creating a layer from Graphics
            popupTemplate: missionPointsTemplate,
            labelingInfo: [ labelClass ]
          });

          map.add(pointsLayer);

          return pointsLayer;
        }
      
      function createMissionLinesLayer(graphics) {

          linesLayer = new FeatureLayer({
            source: graphics, // autocast as an array of esri/Graphic
            // create an instance of esri/layers/support/Field for each field object
            fields: missionLineFields, // This is required when creating a layer from Graphics
            objectIdField: "ObjectID", // This must be defined when creating a layer from Graphics
            renderer: linesMissionRenderer, // set the visualization on the layer
            spatialReference: {
              wkid: 4326
            },
            geometryType: "polyline", // Must be set when creating a layer from Graphics
            popupTemplate: linesMissionTemplate
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
        console.error("Error. ", error);
      }

    });
  </script>
</head>

<body>
  <div id="viewDiv"></div>
  <div id="infoDiv">
    <h2>UV Tracks Demo</h2>
  </div>
</body>
</html>