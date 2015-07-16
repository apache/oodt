/**
 * View  that draws the directory structure as a tree view
 * @author starchmd
 */
define(["jquery",
        "underscore",
        "lib/backbone",
        "lib/jstree",
        "js-new/utils/utils"],
    function($,_,Backbone,jstree,utils) {
        /**
         * Augments a given object for use with JS tree
         * @param 
         */
        function jsTreeAug(object) {
            if ("name" in object && "type" in object) {
                object.text = object.name + (object.type == "DIRECTORY"?"/":"");
                object.icon = (object.type == "DIRECTORY"?"icons/directory.png":"icons/file.png");
            }
        }
        /**
         * Get selection update function
         * @param selection - selection collection
         * @return function to update collection with new selection
         */
        function getSelectionUpdater(selection,view){
            /**
             * Updates closed function to update given selection
             * @param e - event
             * @param data - new jsTree selection 
             */
            return function(e,data) {
                selection.reset();
                for (var i = 0; i < data.selected.length; i++) {
                    var node = data.instance.get_node(data.selected[i]);
                    var path = node.text;
                    //Do NOT allow selection of directories
                    if (node.original.type == "DIRECTORY")
                        continue;
                    while (node.parent != "#") {
                        node = data.instance.get_node(node.parent);
                        path = node.text +path;
                    }
                    selection.add({"id":path});
                    selection.trigger("change");
                }
                selection.each(function(elem) {
                    elem.fetch({"success":
                        function() {
                            view.render();
                        }
                    });
                });
            };
        };
        /**
         * Init function for Metadata tree view
         * @param options - options for init
         */
        function init(options) {
            //Replicate options locally
            for (var key in options)
                this[key] = options[key];
            //Post templates
            var tmp = _.template($("script#template-jstree").html());
            this.$el.append(tmp({"name":this.name}));
            //Setup inital jsTree
            var core = {"core":{"data":[]},
                        "plugins" : ["checkbox"]};
            $("#"+this.name).on("changed.jstree",getSelectionUpdater(this.selection,this.metview)).jstree(core);
            //Register view update on directory change
            this.directory.on("change:files",this.render,this);
            this.render();
        };
        /**
         * Render this view
         */
        function render() {
            var data = utils.deep(this.directory.get("files"),jsTreeAug);
            $("#"+this.name).jstree(true).settings.core.data = data;
            $("#"+this.name).jstree(true).refresh();
        };
        /**
         * Tree view object
         */
        return Backbone.View.extend({
            initialize: init,
            render: render
        });
    }
);