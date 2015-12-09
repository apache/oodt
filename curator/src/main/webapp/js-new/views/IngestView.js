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
            $(this.$el).find("button#ingest-clear-errors").on("click",this.ingestClear);
        };
        /**
         * A function to set the "on click" for ingest clear button
         * @param func - function to call back (should come from controller)
         */
        function setIngestClear(func) {
            this.ingestClear = func;
        };
        /**
         * Return uploads views
         */
        return Backbone.View.extend({
            initialize: init,
            render: render,
            setIngestClear: setIngestClear
        });
    }
);
