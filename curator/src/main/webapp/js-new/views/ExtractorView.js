define(["jquery",
        "underscore",
        "lib/backbone",
        "datatables",
        "js-new/utils/utils"],
    function($,_,Backbone,DataTable,utils) {
        /**
         * Init this view
         * @param options - map to
         *     {
         *         extractors - extractors model
         *     }
         */
        function init(options) {
            this.extractors = options["extractors"];
        };
        /**
         * Set the function to call "on-change"
         * @param func - function to change
         */
        function setOnChangeFunction(func) {
            this.onchange = func;
        };
        /**
         * Render the extractor list
         */
        function render() {
            var tmp = _.template($("script#template-extractor-list").html());
            this.$el.html(tmp({"model":this.extractors}));
            $(this.$el).find("select").on("change",this.onchange);
        };
        /**
         * Extractor view
         */
        return Backbone.View.extend({
            initialize: init,
            render: render,
            setOnChangeFunction: setOnChangeFunction
        });
    });
