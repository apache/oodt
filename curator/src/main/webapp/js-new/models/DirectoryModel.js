/**
 * A model that represents a directory listing.
 * @author starchmd
 */
define(["jquery",
        "underscore",
        "lib/backbone",
        "js-new/config/Configuration"],
    function($,_,Backbone,Config) {
        return Backbone.Model.extend({
            "urlRoot":Config.DIRECTORY_REST_SERVICE,
            "defaults":{"files":{}},
            "parse":function (response) {
                    var tmp = this.get("files");
                    if (!_.isEqual(response,tmp))
                        return {"files":response};
                    else
                        return {};
                }
            });
    }
);