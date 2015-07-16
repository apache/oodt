/**
 * Utility functions for use in the rest of the project
 * @author starchmd
 */
define(["underscore"],
    function(_) {
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
        
        return {"deep":deep};
    }
);