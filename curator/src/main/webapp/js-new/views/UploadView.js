/**
 * View for uploading files
 * @author starchmd
 */
define(["jquery",
        "underscore",
        "lib/backbone",
        "lib/dropzone"],
    function($,_,Backbone,dropzone) {
        return Backbone.View.extend({
            initialize: function(options) {
                var _self = this;
                this.name = options.name;
                this.notify = options.notify;
                var tmp = _.template($("script#template-upload").html());
                this.$el.append(tmp({"name":this.name}));
                //Configure dropzone
                var dz = $("#"+this.name).dropzone(options.model);
                dz.on("success",function(){
                        _self.notify.render(true);
                    });
            }
        });
    }
);