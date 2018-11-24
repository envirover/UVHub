// Base URL of UV Tracks web service
var UVTRACKS_PROTOCOL = location.protocol;
var UVTRACKS_HOSTNAME = location.hostname;
var UVTRACKS_PORT = location.port ? ':' + location.port : '';
var UVTRACKS_BASE_URL = UVTRACKS_PROTOCOL + '//' + UVTRACKS_HOSTNAME + UVTRACKS_PORT;

var uvCockpitConfig = {
    uvTracksHostname : UVTRACKS_HOSTNAME,
    uvTracksBaseURL :  UVTRACKS_BASE_URL
}