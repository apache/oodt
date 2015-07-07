/**
 * A view that displays the metadata editing panel
 * @author starchmd
 */
define(["jquery",
        "underscore",
        "lib/backbone",
        "datatables"],
    function($,_,Backbone,DataTable) {
        return Backbone.View.extend({
            initialize: function(options) {
                var _self = this;
                this.headers = ["key","value"];
                this.metadata = options.metadata;
                this.extractors = options.extractors;
                this.metadata.on("change",_self.render,_self);
                this.extractors.on("change:extractors",_self.render,_self);
                this.render();
            },
            render: function() {
                var merged = {};
                var recurse = function recurseSelf(mer,met) {
                    if (mer.name != met.name)
                        return;
                    if (_.isEqual(mer.values,met.values))
                        mer.locked = true;
                    for (var key in met.children) {
                        if (key in mer.children)
                            recurseSelf(mer.children[key],met.children[key]);
                        else
                            mer.children[key] = met.children[key];
                    }
                };
                this.metadata.each(function(elem) {
                    elem.fetch({"success":function(){recurse(elem.get("root",merged));}});
                });
                var extracts = this.extractors.get("extractors");
                if (typeof(extracts) == "undefined")
                    extracts = [];
                var tmp = _.template($("script#template-table").html());
                this.$el.html(tmp({"headers":this.headers,"object":merged,"extractors":extracts}));
                var table = $(this.$el).find("table:first");
                table.DataTable({
                    "paging":   false
                });
            }
        });
    }
);