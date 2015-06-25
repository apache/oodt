define(["jquery","underscore","lib/backbone"],function($,_,Backbone) {
    /**
     * A basic model that holds the file-system stuff
     */
    return Backbone.Model.extend({
        "urlRoot":"services/directory",
        "defaults":{"files":{}},
        "parse":function (response) {
                    return {"files":response};
                }
    });
});