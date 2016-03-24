/**
 * This model represents the data model of the metadata structure.
 * It should read from the Validation Rest end point.
 * 
 * @starchmd
 */
define(["lib/backbone",
        "js-new/utils/utils",
        "js-new/config/Configuration"],
    function(Backbone,utils,Config) {
        /**
         * Parse the response
         * @param response - response from the backend server
         */
        function parse(response) {
            //Modify the response
            for (type in response) {
                //Cleanse the results of CAS. from element fields
                for (var i = 0; i < response[type].length; i++) {
                    if (response[type][i].elementName.indexOf("CAS.") == 0) {
                        response[type][i].elementName = response[type][i].elementName.replace("CAS.","");
                    }
                }
                //Apply hidden elements
                var toHide = utils.getHiddenConfigFields();
                for (var i = 0; i < toHide.length; i++) {
                    for (var j = 0; j < response[type].length; j++) {
                        if (toHide[i] == response[type][j].elementName)
                        {
                            response[type][j].attachments["hidden"] = "";
                        }
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