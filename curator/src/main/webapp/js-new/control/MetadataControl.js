/**
 * A controller mechanism
 * @author starchmd
 */
define(["jquery"],
    function($) {
        /**
         * Controller for matching datamodel, metadata model to metadataview.
         */
        return function(view,model,ingest) {
            var _self = this;
            _self.view = view;
            _self.model = model;
            _self.model.on("change",_self.view.render,_self.view);
            _self.ingest = ingest;
            _self.dataEntry = 
                /**
                 * What the controller does upon data entry.
                 * @param e
                 */
                function(e) {
                    var value = this.value;
                    var name = this.name;
                    if (name == "ProductType") {
                        _self.view.setProductType(value);
                    }
                    //Loop through all the selected files setting metadata
                    _self.model.each(
                            function(elem) {
                                var root = elem.get("root");
                                if (name in root.children) {
                                    root.children[name].values[0] = value;
                                } else {
                                    root.children[name] = {"name":name,"values":[value],"children":{}} 
                                }
                                //TODO: Remember validation
                                //TODO: Fix this to do a set and a save, not just a set
                                elem.save(null,{success:
                                    function() {
                                        elem.fetch();
                                    }});
                            }
                        );
                    _self.view.render();
                };
            _self.ingestClick =
                /**
                 * What to do on ingest click
                 * @param e - event (likely ignored)
                 */
                function(e) {
                    var selects = [];
                    _self.model.each(
                        function(elem) {
                            try {
                                var timestamp = new Date().getTime();
                                var id = elem.get("id");
                                var productName = elem.get("root")["children"]["ProductName"]["values"][0];
                                selects.push({"timestamp":timestamp,"file":id,"size":0,"pname":productName});
                            } catch(err) {
                                Console.log("Error: Failed to parse ingestibles"+err);
                            }
                        }
                    );
                    //Run ingest
                    if (selects.length > 0)
                        _self.ingest.save({"entries":selects});
                };
            _self.view.setOnEntryFunction(_self.dataEntry);
            _self.view.setIngestClick(_self.ingestClick);
        }
});