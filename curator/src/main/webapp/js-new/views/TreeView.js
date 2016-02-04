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
                        var splits = node.text.split("/");
                        var localName = (splits[splits.length -1] != "")?splits[splits.length -1]:splits[splits.length - 2]
                        path = localName+"/"+path;
                    }
                    selection.add({"id":path,"treeId":node.id});
                }
                selection.each(function(elem) {
                    elem.fetch({"success":view.render.bind(view,false)});
                });
                view.render(true);
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
                        "plugins" : ["checkbox"]//,
                        //"checkbox" :{"tie_selection":false}
                       };
            $("#"+this.name).jstree(core);
            $("#"+this.name).on("refresh.jstree",this.gussy.bind(this));
            this._updateSelection = getSelectionUpdater(this.selection,this.metview);
            //Register view update on directory change
            this.directory.on("change:files",this.render,this);
            this.render();
        };
        /**
         * Gussie up the tree view
         */
        function gussy() {
            $("#"+this.name).jstree(true).open_all();
            $("#"+this.name).jstree(true).deselect_all();
            this.selection.each(
                function(elem) {
                    $("#tree-view").jstree("select_node", elem.get("treeId"));
                }
            );
            //Turn updates back on (clear first)
            $("#"+this.name).off("changed.jstree");
            $("#"+this.name).on("changed.jstree",this._updateSelection);
        };
        /**
         * Render this view
         */
        function render() {
            //Turn off updates
            $("#"+this.name).off("changed.jstree");
            var data = utils.deep(this.directory.get("files"),jsTreeAug);
            $("#"+this.name).jstree(true).settings.core.data = data;
            $("#"+this.name).jstree(true).refresh();
        };
        /**
         * Tree view object
         */
        return Backbone.View.extend({
            initialize: init,
            render: render,
            //Private functions
            gussy: gussy
        });
    }
);