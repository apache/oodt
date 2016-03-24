/**
 * A controller mechanism for the tree view
 * @author starchmd
 */
define(["jquery"],
    function($) {
        /**
         * 
         */
        return function(tree,metadata) {
            var _self = this;
            _self.tree = tree;
            _self.metadata = metadata;
            
            _self.onupdate = 
                /**
                 * What the controller does upon update of the tree view
                 */
                function() {
                    _self.metadata.render(true);
                };
           
            _self.tree.setUpdate(_self.onupdate);
        }
});