/**
 * A model that represents a directory listing.
 * @author starchmd
 */
define(["jquery",
        "underscore",
        "lib/backbone",
        "js-new/config/Configuration"],
    function($,_,Backbone,Config) {
        
        function rmFile(path, children) {
            var o;
            if(children === undefined) {
                var o = this.get("files");
            }
            else{
                o = children;
            }
            if( o.path === path ){
                return o;
            }
            var result, p;
            for (p in o.children) {
                if( typeof o.children[p] === 'object' ) {
                    result = rmFile(path,o.children[p]);
                    if(result){
                        o.children.splice(p, 1);
                        return;
                    }
                }
            }
            return result;
        }
        /**
         * Parse the results of the REST-call from the file request
         * @param response - JSON response from directory listing
         */
        function parse(response) {
            if (!_.isEqual(response,this.get("files")))
                return {"files":response};
            return {};
        };
        /**
         * Backbone instance 
         */
        return Backbone.Model.extend({
            "urlRoot":Config.DIRECTORY_REST_SERVICE,
            "defaults":{"files":{}},
            "parse":parse,
            "rmFile":rmFile
            });
    }
);