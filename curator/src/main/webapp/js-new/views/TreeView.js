/**
 * View  that draws the directory structure as a tree view
 * @author starchmd
 */
define(["jquery",
        "underscore",
        "lib/backbone",
        "lib/jstree"],
    function($,_,Backbone,jstree) {
        /**
         * Recurse refining the directory tree for use for jsTree
         * @param object - directory tree listing
         */
        function remakeRecurse(object) {
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
        };
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
         * Init function for Metadat tree view
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
            var core = {"core":{"data":[]}};
            $("#"+this.name).on("changed.jstree",getSelectionUpdater(this.selection,this.metview)).jstree(core);
            //Register view update on directory change
            this.directory.on("change:files",this.render,this);
            this.render();
        };
        /**
         * Render this view
         */
        function render() {
            var data = _.clone(this.directory.get("files"));
            remakeRecurse(data);
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