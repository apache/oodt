/**
 * View for ingest and clear buttons
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
            this.ingesting = false;
            this._template = _.template($("script#template-metadata-buttons").html());
            this._template_status = _.template($("script#template-ingest-status").html());
            this.first = true;
        };
        /**
         * Render the view
         */
        function render() {
            if (this.first) {
                this.first = false;
                this.$el.html(this._template());
                //Buttons
                $(this.$el).find("button#ingest").on("click",this.ingestClick);
                $(this.$el).find("button#clear-metadata").on("click",this.metaClear);
            }
            this.$el.find("span#ingest-status").html(this._template_status({"ingesting":this.ingesting}));
        };
        /**
         * A function to set the "on click" for ingest button
         * @param func - function to call back (should come from controller)
         */
        function setIngestClick(func) {
            this.ingestClick = func;
        };
        /**
         * A function to set the "on click" for metadata clear button
         * @param func - function to call back (should come from controller)
         */
        function setMetadataClear(func) {
            this.metaClear = func;
        };
        /**
         * Set ingesting status
         * @param ingesting - is the html ingesting?
         */
        function setIngesting(ingesting) {
            this.ingesting = ingesting;
        };
        /**
         * Return uploads views
         */
        return Backbone.View.extend({
            initialize: init,
            render: render,
            setIngestClick: setIngestClick,
            setMetadataClear: setMetadataClear,
            setIngesting: setIngesting
        });
    }
);
