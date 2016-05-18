/**
 * A backbone model that wraps the ingest control
 * @author starchmd
 */
define(["jquery",
        "underscore",
        "lib/backbone",
        "js-new/config/Configuration"],
    function($,_,Backbone,Config) {
        /**
         * Generate an ingest URL
         */
        function url() {
            var query = "";
            if (GLOBAL_USER != "" && typeof(GLOBAL_USER) !== "undefined") {   
                query += ((query == "")?"?":"&")+ "user="+GLOBAL_USER;
            }
            return Config.INGEST_REST_SERVICE + query;
        }
        /**
         * Parse a model
         */
        function parse(response) {
            var ret = [];
            var map = {};
            if (typeof(this.get("status")) !== "undefined") {
                var key = "";
                /*for (var i = 0; i < this.get("status").length; i++) {
                    key = this.get("status")[i]["timestamp"]+"-"+this.get("status")[i]["file"];
                    ret.push(this.get("status")[i]);
                    map[key] = i;
                }*/
                for (var i = 0; i < response["status"].length; i++) {
                    //key = response["status"][i]["timestamp"]+"-"+response["status"][i]["file"];
                    //if (key in map) {
                    //    ret[map[key]] = response["status"][i];
                    //} else {
                        ret.push(response["status"][i]);
                    //}
                }
            }
            return {"status":ret};
        }
        /**
         * Backbone model for ingest
         */
        return Backbone.Model.extend({
                "url":url,
                "defualts": {"status":[]},
                "parse":parse
            });
    }
);
