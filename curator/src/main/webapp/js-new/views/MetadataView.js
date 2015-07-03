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
                this.extractors.on("change",_self.render,_self);
                this.render();
            },
            render: function() {
                var obj = this.metadata.get("yolo1").get("root");
                if (typeof(obj) == "undefined")
                    return;
                var tmp = _.template($("script#template-table").html());
                this.$el.html(tmp({"headers":this.headers,"object":obj,"extractors":this.extractors.models}));
                var table = $(this.$el).find("table:first");
                table.DataTable({
                    "paging":   false
                });
            }
        });
    }
);