/**
 * Created by bugg on 20/05/16.
 */

define(["jquery","underscore","lib/backbone"],
    function($,_, Backbone) {
        return {
            events: _.extend({}, Backbone.Events)
        }
    });