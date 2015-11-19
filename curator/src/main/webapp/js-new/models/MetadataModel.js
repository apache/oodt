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
        //Validation model
        var Validation = new (Backbone.Model.extend({
            "defaults":{"types":{}},
            "url":Config.VALIDATION_REST_SERVICE,
            "parse":function(res) {
                return res;
            }
        }))();
        Validation.fetch();
        /**
         * Validation function to validate Metadata Model attributes
         * @param attrs - attributes to validate
         * @param options - ??
         * @returns validation successful true/false
         */
        function validate(attrs,options) {
            var type = attrs["root"].children["ProductType"].values[0];
            var useDefault = typeof(type) == "undefined" || !(type in Validation.attributes);
            var valids = Validation.get(useDefault?"default":type);
            //Check each key in the validation, and register errors
            var errors = {};
            for (var key in valids) {
                if ("required" in valids[key] && valids[key].required == "true" && !(key in attrs)) {
                    errors[key] = "Field required";
                } else if ("values" in valids[key] && key in attrs && valids[key].values.indexOf(attrs[key]) == -1) {
                    errors[key] = "Field must be one of: "+valids[key].values.join()
                }
            }
            //Valid only returns something on error
            if (!($.isEmptyObject(errors))) {
                return errors;
            }
        };
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
            "url":url,
            "validate":validate
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