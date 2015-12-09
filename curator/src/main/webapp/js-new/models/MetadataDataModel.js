/**
 * This model represents the data model of the metadata structure.
 * It should read from the Validation Rest end point.
 * 
 * @starchmd
 */
define(["lib/backbone",
        "js-new/config/Configuration"],
    function(Backbone,Config) {
        /**
         * Parse the response
         * @param response - response from the backend server
         */
        function parse(response) {
            //Cleanse the results of CAS.
            for (type in response) {
                for (var i = 0; i < response[type].length; i++) {
                    if (response[type][i].elementName.indexOf("CAS.") == 0) {
                        response[type][i].elementName = response[type][i].elementName.replace("CAS.","");
                    }
                }
            }
            return {"types":response};
        };
        
        //Return very basic model
        return Backbone.Model.extend({
                "defaults":{"types":{}},
                "url":Config.VALIDATION_REST_SERVICE,
                "parse": parse
            });
    });