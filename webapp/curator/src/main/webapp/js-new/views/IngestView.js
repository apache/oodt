/**
 * View for ingesting files files
 * @author starchmd
 */
define(["jquery",
        "underscore",
        "lib/backbone","js-new/utils/utils", "js-new/utils/EventBus"],
    function($,_,Backbone,utils,EventBus) {
        var initCall = false;
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
            var htmlText = $("script#template-ingesting").html();
            this._template = _.template(htmlText);
            this.onRefresh = function(){};
            this._ingestClear = null;
        };
        /**
         * Render the view
         */
        function render() {
            this.onRefresh();
            this.$el.html(this._template({"statuses":this.ingest.get("status")}));
            $(this.$el).find("button#ingest-clear-errors").on("click",utils.getMediator(this,"_ingestClear"));
            if(!initCall){
                EventBus.events.trigger('ingest:execute');
                initCall=true;
            }
        };
        /**
         * A function to set the "on click" for ingest clear button
         * @param func - function to call back (should come from controller)
         */
        function setIngestClear(func) {
            this._ingestClear = func;
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
