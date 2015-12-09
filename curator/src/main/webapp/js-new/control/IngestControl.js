/**
 * A controller mechanism
 * @author starchmd
 */
define(["jquery"],
    function($) {
        /**
         * Controller for matching datamodel, metadata model to metadataview.
         */
        return function(view,model) {
            var _self = this;
            _self.view = view;
            _self.model = model;
            
            _self.onclick = 
                /**
                 * What the controller does upon click of clear.
                 * @param e - item changed
                 */
                function(e) {
                    _self.model.destroy();
                    _self.view.render();
                };
            _self.view.setIngestClear(_self.onclick);
        }
});