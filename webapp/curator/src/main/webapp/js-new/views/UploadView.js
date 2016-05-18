/**
 * View for uploading files
 * @author starchmd
 */
define(["jquery",
        "underscore",
        "lib/backbone",
        "lib/dropzone"],
    function($,_,Backbone,dropzone) {
        /**
         * Initialize function
         * @param options - options for initialization
         */
        function init(options) {
            var _self = this;
            //Replicate options locally
            for (var key in options)
                this[key] = options[key];
            //Setup templates
            var tmp = _.template($("script#template-upload").html());
            this.$el.append(tmp({"name":this.name}));

        };
        /**
         * Render this view.
         */
        function render() {
            var _self = this;
            //Configure dropzone
            var dz = $("#"+_self.name).dropzone(_self.upload);
            dz.on("success",function(){_self.notify.fetch();});
        }
        /**
         * Return uploads views
         */
        return Backbone.View.extend({
            initialize: init,
            render: render
        });
    }
);