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
         * Validation function to validate Metadata Model attributes
         * @param attrs - attributes to validate
         * @param options - ??
         * @returns validation successful true/false
         */
        function validate(attrs,options) {
            //Get validation setup
            var type = ("root" in attrs && "ProductType" in attrs["root"].children
                        && attrs["root"].children["ProductType"].values.length >= 1) ?
                        attrs["root"].children["ProductType"].values[0] : "GenericFile";
            var dataModel = this.collection["datamodel"].get("types")[type];
            //Check all the fields
            error = {};
            for (var i = 0; i < dataModel.length; i++) {
                var attachments = dataModel[i].attachments;
                var key = dataModel[i].elementName;
                if ("required" in attachments && 
                   (!(key in attrs["root"].children) || 
                    (attrs["root"].children[key].values.length == 0) ||
                    (attrs["root"].children[key].values[0] == ""))) {
                    error[key] = key +" is required.";
                } else if ("values" in attachments && 
                       (!(key in attrs["root"].children) || 
                        (attrs["root"].children[key].values.length == 0) ||
                        (attachments.values.split(",").indexOf(attrs["root"].children[key].values[0]) == -1))) {
                    var tmptmptmp = attachments.values.split(",");
                    error[key] = key +" must be one of: "+attachments.values;
                }
            }
            //Valid only returns something on error
            if (!($.isEmptyObject(error))) {
                return error;
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
            "defaults":{"root":{"children":[]},"treeId":""},
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