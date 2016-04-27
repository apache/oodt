/**
 * View  that draws the directory structure as a tree view
 * @author starchmd
 */
define(["jquery",
        "underscore",
        "lib/backbone",
        "lib/jstree",
        "js-new/utils/utils",
        "popover"],
    function($,_,Backbone,jstree,utils) {
        /**
         * Augments a given object for use with JS tree
         * @param 
         */
        function jsTreeAug(object) {
            if (typeof object === 'object' && "name" in object && "type" in object) {
                object.text = object.name + (object.type == "DIRECTORY"?"/":"");
                object.icon = (object.type == "DIRECTORY"?"icons/directory.png":"icons/file.png");
                parseValidation(object)
            }

        }

        /**
         * Validates the object from the validation information returned alongside it.
         * @param object
         */
        function parseValidation(object){
            if(!object.validation.valid){
                var errormessage="";
                _.each(object.validation.validationelements, function(ele){
                    if(!ele.valid){
                        errormessage += "•"+ele.message+"<br/>";
                    }
                });
                object.state={
                    disabled:true
                };
                object.a_attr = {
                    "valid":object.validation.valid,
                    "title": "Validation Issues",
                    "data-content": errormessage,
                    "class":"invalid_node",
                    "data-toggle":"popover",
                    "data-html":"true"
                };

            }
        }
        /**
         * Get selection update function
         * @param selection - selection collection
         * @param context - contect to look for updater
         * @return function to update collection with new selection
         */
        function getSelectionUpdater(selection,context){
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
                var pending = 0;
                function onFetched(elem) {
                    utils.updateFromPresets(elem);
                    pending -= 1;
                    if (pending <= 0 && context.updater != null) {
                        context.updater();
                    }
                };
                selection.each(function(elem) {
                    pending += 1;
                    elem.fetch({"success":onFetched.bind(this,elem)});
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
                        "plugins" : ["checkbox"]//,
                        //"checkbox" :{"tie_selection":false}
                       };
            this.updater = null;
            $("#"+this.name).jstree(core);
            $("#"+this.name).on("refresh.jstree",this.gussy.bind(this));
            $("#"+this.name).on("loaded.jstree open_node.jstree", function(event, data){
                $('[data-toggle="popover"]').popover({trigger: 'hover','placement': 'top',delay: { "show": 500, "hide": 100 }});

            })
            this._updateSelection = getSelectionUpdater(this.selection,this);
            //Register view update on directory change
            this.directory.on("change:files",this.render,this);
            this.render();
        };
        /**
         * Set a function for updating externally
         */
        function setUpdate(updater) {
            this.updater = updater;
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
            setUpdate: setUpdate,
            //Private functions
            gussy: gussy
        });
    }
);