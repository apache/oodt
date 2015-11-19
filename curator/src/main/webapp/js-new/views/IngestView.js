/**
 * View for ingesting files files
 * @author starchmd
 */
define(["jquery",
        "underscore",
        "lib/backbone",],
    function($,_,Backbone) {
        /**
         * Initialize function
         * @param options - options for initialization
         */
        function init(options) {
            var _self = this;
            //Replicate options locally
            for (var key in options)
                this[key] = options[key];
            this.ingest.on("change:status",this.render,this);
            //this.render();
        };
        /**
         * Render the view
         */
        function render() {
            var tmp = _.template($("script#template-ingesting").html());
            this.$el.html(tmp({"statuses":this.ingest.get("status")}));
        };
        /**
         * Return uploads views
         */
        return Backbone.View.extend({
            initialize: init,
            render: render
        });
    }
);
