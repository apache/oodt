/**
 * A controller mechanism
 * @author starchmd
 */
define(["jquery"],
    function($) {
        /**
         * Controller for matching datamodel, metadata model to metadataview.
         */
        return function(view,model,metadata) {
            var _self = this;
            _self.view = view;
            _self.model = model;
            _self.metadata = metadata;
            
            _self.model.on("change:extractors",_self.view.render,_self.view);
            _self.onchange = 
                /**
                 * What the controller does upon change in extractor.
                 * @param e - item changed
                 */
                function(e) {
                    var value = this.value;
                    _self.model.set("selected",value);
                    _self.view.render();
                    _self.metadata.each(
                        function(elem) {
                            elem.fetch();
                        });
                };
            _self.view.setOnChangeFunction(_self.onchange);
        }
});