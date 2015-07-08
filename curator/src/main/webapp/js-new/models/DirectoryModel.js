/**
 * A model that represents a directory listing.
 * @author starchmd
 */
define(["jquery",
        "underscore",
        "lib/backbone",
        "js-new/config/Configuration"],
    function($,_,Backbone,Config) {
        /**
         * Parse the results of the REST-call from the file request
         * @param response - JSON response from directory listing
         */
        function parse(response) {
            if (!_.isEqual(response,this.get("files")))
                return {"files":response};
            return {};
        };
        /**
         * Backbone instance 
         */
        return Backbone.Model.extend({
            "urlRoot":Config.DIRECTORY_REST_SERVICE,
            "defaults":{"files":{}},
            "parse":parse
            });
    }
);