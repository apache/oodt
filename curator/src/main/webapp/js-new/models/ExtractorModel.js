/**
 * A backbone model that wraps the list of extractors
 * @author starchmd
 */
define(["jquery",
        "underscore",
        "lib/backbone",
        "js-new/config/Configuration"],
    function($,_,Backbone,Config) {
        /**
         * Parse results of extractor REST call
         * @return extractor model
         */
        function parse(result) {
            var tmp = this.get("extractors");
            if (!_.isEqual(tmp,result))
                return {"extractors":result};
            return {};
        }
        /**
         * Backbone model for extractors
         */
        return Backbone.Model.extend({
                "url":Config.EXTRACTOR_REST_SERVICE,
                "parse":parse
            });
    }
);