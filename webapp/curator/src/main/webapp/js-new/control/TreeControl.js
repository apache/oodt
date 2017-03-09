/**
 * A controller mechanism for the tree view
 * @author starchmd
 */
define(["jquery", "js-new/utils/EventBus"],
    function($, EventBus) {
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

            EventBus.events.bind('ingest:execute', function(){
                _self.tree.refresh_tree();
            });
        }
});