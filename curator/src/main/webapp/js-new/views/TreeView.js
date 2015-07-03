/**
 * View  that draws the directory structure as a tree view
 * @author starchmd
 */
define(["jquery",
        "underscore",
        "lib/backbone",
        "lib/jstree"],
    function($,_,Backbone,jstree) {
        //Remake recursively clones the object, and augments it for TreeView.js
        var remake = function remakeRecurse(object) {
            if (!("name" in object))
                return;
            object.text = object.name + (object.type == "DIRECTORY"?"/":"");
            if ("children" in object) {
                object.children = _.clone(object.children);
                for (var i = 0; i < object.children.length; i++) {
                    object.children[i] = _.clone(object.children[i]);
                    remakeRecurse(object.children[i]);
                }
            }
        }
        return Backbone.View.extend({
            initialize: function(options) {
                var _self = this;
                this.name = options.name;
                this.model = options.directory;
                var tmp = _.template($("script#template-jstree").html());
                this.$el.append(tmp({"name":this.name}));
                $("#"+this.name).jstree(
                    {
                        "core" : {
                            "data" : ["Initial Fill Data"]
                        }
                    }        
                );
                this.model.on("change:files",_self.render,_self);
                this.render();
            },
            render: function(full) {
                var data = _.clone(this.model.get("files"));
                remake(data);
                $("#"+this.name).jstree(true).settings.core.data = data;
                $("#"+this.name).jstree(true).refresh();
            }
        });
    }
);