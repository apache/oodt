/**
 * A backbone model that wraps the list of extractors
 * @author starchmd
 */
define(["jquery",
        "underscore",
        "lib/backbone",
        "js-new/config/Confiugration"],
    function($,_,Backbone,Config) {
        return Backbone.collection.extend({
                "url":Config.EXTRACTOR_REST_SERVICE,
                "parse":function (response) {
                    return response["extractors"];
                }
            });
    }
);