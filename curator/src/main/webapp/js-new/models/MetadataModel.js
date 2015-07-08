/**
 * Model representing metadata backend
 * @author starchmd
 */
define(["jquery",
        "underscore",
        "lib/backbone",
        "js-new/config/Configuration"],
    function($,_,Backbone,Config) {
        /**
         * Parse the REST-call for metadata
         * @param response - JSON metadata object
         * @return metadata under the root node
         */
        function parse(response) {
            var ret = {}; 
            if (typeof(response["root"].children) != "undefined" && typeof(response["root"].children.fill) != "undefined") {
                ret["fill"] = response["root"].children.fill.values[0];
                delete response["root"].children.fill;
            }
            ret["root"] = response["root"];
            return ret;
        }
        /**
         * Return the url for the Metadata object
         * @return url to the Metadata REST service
         */
        function url() {
            var extractor = this.collection.extractors.get("selected");
            var query = "";
            if (extractor != "" && extractor != null && typeof(extractor) != "undefined")
                query += "?extractor="+extractor;
            return Config.METADATA_REST_SERVICE+"/"+this.get("id")+query;
        }
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
         * Backbone metadata object
         */
        var Metadata = Backbone.Model.extend({
            "parse":parse,
            "url":url
        });
        
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