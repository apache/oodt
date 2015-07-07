/**
 * Configuration object for javascript application
 * @author starchmd
 */
define(["jquery"],
    //TODO: Return configuration from server via REST call
    function($) {
        var path = $(location).attr("pathname");
        return {
            "METADATA_REST_SERVICE":"services/metadata",
            "DIRECTORY_REST_SERVICE":"services/directory",
            "EXTRACTOR_REST_SERVICE":"services/metadata/extractors",
            //Dropzone requires full path
            "UPLOAD_REST_SERVICE":path.substr(0,path.lastIndexOf("/")+1)+"service/upload/file",
            "FILE_SYSTEM_REFRESH_INTERVAL":100000
        };
    }
);