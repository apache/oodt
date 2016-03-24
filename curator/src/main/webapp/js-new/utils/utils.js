/**
 * Utility functions for use in the rest of the project
 * @author starchmd
 */
define(["underscore","js-new/config/Configuration"],
    function(_,Config) {
        /**
         * Function used to deep-clone an object with circular-reference detection
         * @param stack - stack to look for circular-reference
         * @param object - jsObject to deep clone
         * @param callback - (optional) callback to run on each child
         */     
        function deepHelper(stack,object,callback) {
            var tpe = typeof(object);
            if (["string","number","undefined"].indexOf(tpe) != -1)
                return object;
            else if (stack.indexOf(object) != -1)
                throw "Circular reference detected";
            stack.push(object);
            //Shallow clone this object
            object = _.clone(object);
            //Clone all children
            for (var key in object) {
                object[key] = deepHelper(stack,object[key],callback);
            }
            //Call the callback on the newly cloned object
            if (typeof(callback) != "undefined")
                callback(object);
            return object;
        }
        /**
         * Function used to deep-clone an object
         * @param object - jsObject to deep clone
         * @param callback - (optional) callback to run on each child
         */  
        function deep(object,callback) {
            return deepHelper([],object,callback);
        }
        /**
         * Harvest query parameters
         */
        function queryParameters() {
            var vars = {"hidden":[]};
            if (window.location.href.indexOf('?') >= 0)
            {
                var pairs = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
                for(var i = 0; i < pairs.length; i++)
                {
                    var key = pairs[i].split('=')[0];
                    var value = pairs[i].split('=')[1];
                    if (!(key in vars))
                        vars[key] = [];
                    vars[key].push(value);
                }
            }
            return vars;
        }
        /**
         * Returns a function-callback mediator 
         * @param context - context to bind to
         * @param functionName - name of function to call
         * @returns a function, bound to the context which will call the named function, if not null
         */
        function getMediator(context, functionName) {
            var mediator = function(e) {
                if (context[functionName] != null)
                    context[functionName].call(this,e);
            };
            return mediator;
        }
        
        
        /**
         * Get the hidden fields
         */
        function getHiddenConfigFields() {
            //Configuration must be well formed
            if (!("SETUP_CONFIG" in Config) || !("hidden" in Config["SETUP_CONFIG"]))
            {
                return [];
            }
            return Config["SETUP_CONFIG"]["hidden"];
            
        };
        /**
         * Handle query parameters from the above
         */
        function getPresetConfigFields() {
            var ret = {};
            if (("SETUP_CONFIG" in Config) && ("presets" in Config["SETUP_CONFIG"]))
            {
                ret = Config["SETUP_CONFIG"]["presets"];
            }
            return ret;
        };
        /**
         * Update presets into metadata
         * @param metadata - metadata model to fill with presets
         */
        function updateFromPresets(metadata) {
            var presets = getPresetConfigFields();
            for (var key in presets) {
                var pval = presets[key];
                if (typeof(pval) == "function") {
                    pval = pval(metadata);
                }
                if (pval != null) {
                    metadata.get("root").children[key] = {"children":{},"values":[pval],"name":key};
                }
            }
        };
        return {"deep":deep,"queryParameters":queryParameters,"getMediator":getMediator,"getHiddenConfigFields":getHiddenConfigFields,"getPresetConfigFields":getPresetConfigFields,"updateFromPresets":updateFromPresets};
    }
);