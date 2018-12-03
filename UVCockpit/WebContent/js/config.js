// UV Cockpit web applications configuration 

var UVTRACKS_PROTOCOL = location.protocol;
var UVTRACKS_HOSTNAME = location.hostname;
var UVTRACKS_PORT = location.port ? ':' + location.port : '';
// Base URL of UV Tracks web service
var UVTRACKS_BASE_URL = UVTRACKS_PROTOCOL + '//' + UVTRACKS_HOSTNAME + UVTRACKS_PORT;

var uvCockpitConfig = {
     uvTracksBaseURL :  UVTRACKS_BASE_URL
}