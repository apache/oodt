/**
 * Model representing metadata backend
 * @author starchmd
 */
define(["jquery",
        "underscore",
        "lib/backbone",
        "js-new/config/Configuration"],
    function($,_,Backbone,Config) {
        var Metadata = Backbone.Model.extend({
            "parse":function(response) {
                return {"root":response["root"]};
            }
        });
        return Backbone.Collection.extend({
            "model":Metadata,
            "url":Config.METADATA_REST_SERVICE
        });
    }
);