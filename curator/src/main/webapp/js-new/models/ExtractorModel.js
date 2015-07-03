/**
 * A backbone model that wraps the list of extractors
 * @author starchmd
 */
define(["jquery",
        "underscore",
        "lib/backbone",
        "js-new/config/Configuration"],
    function($,_,Backbone,Config) {
        return Backbone.Collection.extend({
                "url":Config.EXTRACTOR_REST_SERVICE
            });
    }
);