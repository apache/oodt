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
            this._template = _.template($("script#template-ingesting").html());
            this.onRefresh = function(){};
            //this.render();
        };
        /**
         * Render the view
         */
        function render() {
            this.onRefresh();
            this.$el.html(this._template({"statuses":this.ingest.get("status")}));
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
         * A function to set "on refresh" function from controller
         * @param func - function to call
         */
        function setOnRefresh(func) {
            this.onRefresh = func;
        }
        /**
         * Return uploads views
         */
        return Backbone.View.extend({
            initialize: init,
            render: render,
            setIngestClear: setIngestClear,
            setOnRefresh: setOnRefresh
        });
    }
);
