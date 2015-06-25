define(["jquery","underscore","lib/backbone","lib/jstree"],function($,_,Backbone,jstree) {

    /**
     * Function to take a model object, and rewire it for JSTree
     */
    var remake = function selfRecurse(object) {
        if (!("name" in object))
            return;
        object.text = object.name + (object.type == "DIRECTORY"?"/":"");
        if ("children" in object) {
            for (var i = 0; i < object.children.length; i++) {
                selfRecurse(object.children[i]);
            }
        }
    }
    /**
     * A basic view module that constructs a jstree from templates
     */
    return Backbone.View.extend({
        initialize: function(options) {
            var _self = this;
            this.name = options.name;
            this.model = options.model;
            var tmp = _.template($("script#template-jstree").html());
            this.$el.append(tmp({"name":this.name}));
            $("#"+this.name).jstree(
                {
                    "core" : {
                        "data" : ["Initial Fill Data"]
                    }
                }        
            );
            this.model.on("change",_self.render,this);
            this.render();
        },
        render: function(full) {
            var _self = this;
            //If full update, rerender on fetch
            if (full !== "undefined" && full) {
                this.model.fetch();
                return;
            }
            var data = this.model.get("files");
            remake(data);
            $("#"+this.name).jstree(true).settings.core.data = data;
            $("#"+this.name).jstree(true).refresh();
        }
    });
});