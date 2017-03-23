/**
 * View  that draws the directory structure as a tree view
 * @author starchmd
 */
define(["jquery",
        "underscore",
        "lib/backbone",
        "lib/jstree",
        "js-new/utils/utils",
        "js-new/models/SetupModels",
        "popover",
        "blockui"],
    function($,_,Backbone,jst4ree,utils, Models) {

        var opennodes = false;
        var nodestates = [];

        /**
         * Augments a given object for use with JS tree
         * @param 
         */
        function jsTreeAug(object) {
            if (typeof object === 'object' && "name" in object && "type" in object) {
                object.text = object.name + (object.type == "DIRECTORY"?"/":"");
                object.icon = (object.type == "DIRECTORY"?"icons/directory.png":"icons/file.png");
                parseValidation(object);
                var override = false;
                _.each(nodestates, function(node){
                    if(node.path === object.path){
                        object.state = node.state;
                        override = true;
                    }
                });
                if(!override && "children" in object && object.children.length>0){
                    var hasfiles = traverse(object.children);
                    if(hasfiles != undefined && !hasfiles) {
                        object.state = {opened:false};
                    }
                    else{
                        object.state = {opened:true};
                    }
                }
            }

        }

        
        function traverse(o) {
            var ret = false;
            for (var i in o) {
                //func.apply(this,[i,o[i]]);
                if("type" in o[i] && o[i].type != "DIRECTORY"){
                    return true;
                }
                if (o[i] !== null && typeof(o[i])=="object") {
                    //going on step down in the object tree!!
                    ret = traverse(o[i].children);
                    if(ret == true){
                        return true;
                    }
                }
            }
            return ret;
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
                    disabled:true,
                    checkbox_disabled : true
                };
                object.a_attr = {
                    "valid":object.validation.valid,
                    "class":"invalid_node",
                    "title": "Validation Issues",
                    "data-content": errormessage,
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
                    if(node.state.disabled)
                        continue;
                    //Do NOT allow selection of directories
                    if (node.original.type == "DIRECTORY")
                        continue;
                    while (node.parent != "#") {
                        node = data.instance.get_node(node.parent);
                        var splits = node.text.split("/");
                        var localName = (splits[splits.length -1] != "")?splits[splits.length -1]:splits[splits.length - 2]
                        path = localName+"/"+path;

                    }
                    path = encodeURI(path);
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
                if(selection.models.length>0) {
                    $.blockUI({
                        css: {
                            border: 'none',
                            padding: '15px',
                            backgroundColor: '#000',
                            '-webkit-border-radius': '10px',
                            '-moz-border-radius': '10px',
                            opacity: .5,
                            color: '#fff'
                        }
                    });
                }
                selection.each(function(elem) {
                    pending += 1;
                    elem.fetch({"success":onFetched.bind(this,elem),
                    "error": function(err){
                        alert("Error fetching metadata");
                        return false;
                    }});
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
            $("#"+this.name).on("ready.jstree refresh.jstree open_node.jstree", function(event, data){
                $('[data-toggle="popover"]').popover({trigger: 'hover','placement': 'top',delay: { "show": 500, "hide": 100 }});
                
            });
            $("#"+this.name).on("open_node.jstree close_node.jstree", function(event, data){
                    console.log(data);
                var nset = false;
                for(var node in nodestates){
                    if(nodestates[node].path === data.node.original.path){
                        if(event.type === "open_node"){
                            nodestates[node].state = "open";
                        }
                        else{
                            nodestates[node].state = "closed";
                        }

                        nset = true;
                    }
                }
                if(!nset) {
                    nodestates.push({path: data.node.original.path, state: "open"})
                }
            });
            var that = this;
            $("#"+this.name).on('select_node.jstree', function(e, data) {
                console.log(e);
                console.log(data);
                console.log(data.instance._model.data);
                var model = data.instance._model.data;
                var children = data.node.children_d;
                var childrenID;
                for (var i = 0; i < children.length; i++) {
                    if (model[children[i]].state.disabled) {
                        childrenID = model[children[i]].id;
                        $("#"+that.name).jstree(true).deselect_node(childrenID);
                        model[children[i]].state.selected = false;
                    }
                }
            });
            this._updateSelection = getSelectionUpdater(this.selection,this);
            //Register view update on directory change
                this.s3directory.on("change:files", this.render, this);
                this.s3directory.on('change', this.render, this);
                this.directory.on("change:files", this.render, this);
                this.directory.on('change', this.render, this);

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
           // $("#"+this.name).jstree(true).open_all();
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
            if($("#treetype").find(":selected").text() === "S3") {
                var data = utils.deep(this.s3directory.get("files"), jsTreeAug);
            }
            else {
                var data = utils.deep(this.directory.get("files"), jsTreeAug);
            }
            if(data.type=="DIRECTORY"){
                opennodes = true;
            }
            $("#"+this.name).jstree(true).settings.core.data = data;
            $("#"+this.name).jstree(true).refresh();
        };

        function refresh_tree() {
            if($("#treetype").find(":selected").text() === "S3"){
                Models.refreshTree("S3");
            }
            else{
                Models.refreshTree("NFS");
            }

        };
        /**
         * Tree view object
         */
        return Backbone.View.extend({
            initialize: init,
            render: render,
            setUpdate: setUpdate,
            //Private functions
            gussy: gussy,
            refresh_tree: refresh_tree,

            events: {
                'click .refresh-tree' : 'refresh_tree',
                'change #treetype' : 'refresh_tree'
            }
        });
    }
);