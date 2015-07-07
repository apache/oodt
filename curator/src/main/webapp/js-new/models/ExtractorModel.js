/**
 * A backbone model that wraps the list of extractors
 * @author starchmd
 */
define(["jquery",
        "underscore",
        "lib/backbone",
        "js-new/config/Configuration"],
    function($,_,Backbone,Config) {
        return Backbone.Model.extend({
                "url":Config.EXTRACTOR_REST_SERVICE,
                "parse":function(result) {
                    var tmp = this.get("extractors");
                    if (!_.isEqual(tmp,result))
                        return {"extractors":result};
                    return {};
                }
            });
    }
);