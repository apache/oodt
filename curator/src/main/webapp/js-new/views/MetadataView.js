/**
 * A view that displays the metadata editing panel
 * @author starchmd
 */
define(["jquery",
        "underscore",
        "lib/backbone",
        "datatables"],
    function($,_,Backbone,DataTable) {
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
         * Recursively merge two metadata objects
         * @param mer - original metadata object (merge into)
         * @param met - new metadat object (merge from)
         */
        function recurseMerge(mer,met,fill) {
            //TODO: clone this object so "fill me" does not disappear
            if (mer.name != met.name)
                return;
            //Remove fill values
            var ind = mer.values.indexOf(fill);
            if (ind != -1)
                mer.values[ind] = "";
            var ind = met.values.indexOf(fill);
            if (ind != -1)
                met.values[ind] = "";          
            //Lock if values don't match
            if (!_.isEqual(mer.values,met.values)) {
                mer.locked = true;
                var max = Math.max(mer.values.length,met.values.length);
                var end = []
                for (var i = 0; i < max; i++) {
                    var tot = [];
                    if (i < mer.values.length)
                        tot.push(mer.values[i]);
                    if (i < met.values.length)
                        tot.push(met.values[i]);
                    end.push(tot.join("|"));
                }
                mer.values = end;
            }
            //Merge the children
            for (var key in met.children) {
                if (!(key in mer.children))
                    mer.children[key] = {"name":met.children[key].name,"values":met.children[key].values,"children":{}};
                recurseMerge(mer.children[key],met.children[key],fill);
                
            }
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
                recurseMerge(merged,elem.get("root"),elem.get("fill"));
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
            $(this.$el).find("table:first").find("input").on("change",
                function(obj) {
                    var name = $(this).attr("name");
                    var value = $(this).val();
                    _self.metadata.each(
                        function(elem) {
                            var root = elem.get("root");
                            root.children[name].values[0] = value;
                            elem.save();
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
                            selects.push(elem.get("id"));
                        }
                    );
                    console.log(">>>"+_self.ingest);
                    if (selects.length > 0)
                        _self.ingest.save({"files":selects});
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