/**
 * A backbone model that wraps the ingest control
 * @author starchmd
 */
define(["jquery",
        "underscore",
        "lib/backbone",
        "js-new/config/Configuration"],
    function($,_,Backbone,Config) {
        /**
         * Backbone model for ingest
         */
        return Backbone.Model.extend({
                "url":Config.INGEST_REST_SERVICE
            });
    }
);