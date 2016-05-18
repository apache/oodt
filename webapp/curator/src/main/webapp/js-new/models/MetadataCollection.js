/**
 * Model representing metadata collection and backend
 * @author starchmd
 */
define(["jquery",
        "underscore",
        "lib/backbone",
        "js-new/models/MetadataModel",
        "js-new/config/Configuration"],
    function($,_,Backbone,Metadata,Config) {
        /**
         * Initialize the collection, with extractors
         * @param options - options defining extractors
         */
        function init(dumby,options) {
            //Replicate options locally
            for (var key in options)
                this[key] = options[key];
        };
        /**
         * Backbone Metadata collection
         */
        return Backbone.Collection.extend({
            "initialize":init,
            "model":Metadata,
            "url":Config.METADATA_REST_SERVICE
        });
    }
);