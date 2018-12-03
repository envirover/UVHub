// UV Cockpit web applications configuration 

var UVTRACKS_PROTOCOL = location.protocol;
var UVTRACKS_HOSTNAME = location.hostname;
var UVTRACKS_PORT = ":8080";//location.port ? ':' + location.port : '';
// Base URL of UV Tracks web service
var UVTRACKS_BASE_URL = UVTRACKS_PROTOCOL + '//' + UVTRACKS_HOSTNAME + UVTRACKS_PORT;

export var uvCockpitConfig = {
     uvTracksBaseURL :  UVTRACKS_BASE_URL
}