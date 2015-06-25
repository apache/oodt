define(["jquery","underscore","lib/backbone","lib/dropzone"],function($,_,Backbone,dropzone) {
    //Upload view
    return Backbone.View.extend({
        initialize: function(options) {
            var _self = this;
            this.name = options.name;
            this.notify = options.notify;
            var tmp = _.template($("script#template-upload").html());
            this.$el.append(tmp({"name":this.name}));
            var dz = $("#"+this.name).dropzone({"url":"/services/upload/file"});
            dz.on("success",function(){_self.notify.render(true);});
        }
    });
});