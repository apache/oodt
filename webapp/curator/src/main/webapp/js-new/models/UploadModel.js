/**
 * Non-backbone "model" for dropzone upload
 * @author starchmd
 */
define(["js-new/config/Configuration"],
    /**
     * Non-backbone model REST hookup
     */
    function(Config) {
        return {"url": Config.UPLOAD_REST_SERVICE};
    }
);