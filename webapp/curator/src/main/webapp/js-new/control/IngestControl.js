/**
 * A controller mechanism
 * @author starchmd
 */
define(["jquery", "js-new/utils/EventBus"],
    function($, EventBus) {
        /**
         * Controller for matching datamodel, metadata model to metadataview.
         */
        return function(view,model,buttons) {
            var _self = this;
            _self.view = view;
            _self.model = model;
            _self.buttons = buttons;

            _self.onclick = 
                /**
                 * What the controller does upon click of clear.
                 * @param e - item changed
                 */
                function(e) {
                    _self.model.sync("delete",_self.model);
                    _self.model.fetch();
                };
            _self.onRefresh = 
                /**
                 * What to do on refresh
                 */
                function() {
                    _self.buttons.setIngesting(false);
                    _self.buttons.render();
                };
            _self.view.setOnRefresh(_self.onRefresh);
            _self.view.setIngestClear(_self.onclick);

            EventBus.events.bind('ingest:execute', function(){
                _self.model.fetch({"success":_self.view.render.bind(_self.view)});
            });

        }
});