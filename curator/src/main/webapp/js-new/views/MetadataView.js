/**
 * A view that displays the metadata editing panel
 * @author starchmd
 */
define(["jquery",
        "underscore",
        "lib/backbone",
        "datatables",
        "js-new/utils/utils"],
    function($,_,Backbone,DataTable,utils) {
    
        /**
         * Get a callback to apply when cloning the metadata object
         * that masks the fill values, provides selections.
         * @param fill - fill valye to look for
         * @return - callback function enclosing "fill"
         */
        function getFillHandlerCallback(fill) {
            /**
             * Process the object's values eliminating fill and seting "choices" if it is a selection
             * @param object - metadata object to process
             */
            return function(object) {
                    /**
                     * A function to process element value for fill
                     * @param element - element of list
                     * @param index - index of the element in the list
                     * @param list - list iterated
                     */
                    function processForFill(element,index,list) {
                        //Ignore non-filled values
                        if (element.indexOf(fill) == -1) {
                            return;
                        }
                        var val = element.replace(fill,"");
                        //This is a "choices" fill
                        if (val.indexOf("__CHOICES__:") != -1) {
                            object.choices = val.replace("__CHOICES__:","").split(",");
                        }
                        //Always remove fill entry
                        list[index] = "";
                    };
                    _.each(object.values,processForFill);
                };
        };
        /**
         * Returns delimited list
         */
        function getDelimitedList(list1,list2) {
            var tmp = _.zip(list1,list2);
            for (var i = 0; i < tmp.length; i++) {
                tmp[i] = tmp[i].join("|");
            }
            return tmp;
        }
    
        /**
         * Recursively merge two metadata objects
         * @param mer - original metadata object (merge into)
         * @param met - new metadat object (merge from)
         */
        function recurseMerge(mer,met) {
            if (mer.name != met.name)
                return;
            //Lock if values don't match
            if (!_.isEqual(mer.values,met.values)) {
                mer.locked = true;
                mer.display = getDelimitedList(mer.values,met.values);
            } else {
                delete mer.display;
            }
            //Merge "choices"
            if ("choices" in met && !("choices" in mer))
                mer.choices = met.choices;
            //Merge the children
            for (var key in met.children) {
                if (!(key in mer.children))
                    mer.children[key] = {"name":met.children[key].name,"values":met.children[key].values,"children":{}};
                recurseMerge(mer.children[key],met.children[key]);
            }
        };
    
        /**
         * Initialization function for metadata view
         * @param options - options for this view
         */
        function init(options) {
            //Replicate options locally
            for (var key in options)
                this[key] = options[key];
            this.metadata.on("change",this.render,this);
            this.extractors.on("change:extractors",this.render,this);
            this.render();
        };
        /**
         * Render this view
         */
        function render() {
            var _self = this;
            console.log(_self.ingest);
            var merged = {"name":"root","values":[],"children":{}};
            //For each item in the collection, merge together
            this.metadata.each(function(elem) {
                if (typeof(elem.get("root")) == "undefined")
                    return;
                recurseMerge(merged,utils.deep(elem.get("root"),getFillHandlerCallback(elem.get("fill"))));
            });
            //Extractors
            var extracts = this.extractors.get("extractors");
            if (typeof(extracts) == "undefined")
                extracts = [];
            //Setup templates
            var tmp = _.template($("script#template-table").html());
            this.$el.html(tmp({"headers":this.headers,"object":merged,"extractors":extracts}));
            if (typeof(this.extractors.get("selected")) != "undefined")
                $(this.$el,"#extractors").find("select#extractors>option[value='"+this.extractors.get("selected")+"']").attr('selected', true);
            $(this.$el,"#extractors").on("change",function() {
                var selected = $(this).find("select#extractors>option:selected").val();
                _self.extractors.set("selected",selected);
            });
            //DataTables JS
            var table = $(this.$el).find("table:first");
            table.DataTable({"paging": false});
            //On change for edits
            $(this.$el).find("table:first").find("input,select").on("change",
                function(obj) {
                    var name = $(this).attr("name");
                    var value = $(this).val();
                    _self.metadata.each(
                        function(elem) {
                            var root = elem.get("root");
                            root.children[name].values[0] = value;
                            //Fix this to do a set and a save, not just a set
                            elem.save(null,{success:function() {
                                elem.fetch();
                            }});
                        }
                    );
                }
            );
            //On ingest click
            $(this.$el).find("button#ingest").on("click",
                function() {
                    var selects = [];
                    _self.metadata.each(
                        function(elem) {
                            try {
                            selects.push({"timestamp":new Date().getTime(),"file":elem.get("id"),"size":0,"pname":elem.get("root")["children"]["ProductName"]["values"][0]});
                            } catch(err) {}
                        }
                    );
                    if (selects.length > 0)
                        _self.ingest.save({"entries":selects});
                }
            );
        };
        /**
         * Metadata view
         */
        return Backbone.View.extend({
            initialize: init,
            render: render
        });
    }
);
