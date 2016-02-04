/**
 * A controller mechanism
 * @author starchmd
 */
define(["jquery"],
    function($) {
        /**
         * Controller for matching datamodel, metadata model to metadataview.
         * @param view - metadat entry view
         * @param tree - file tree view
         * @param model - metadata model
         * @param ingest - ingesting view
         * @param buttons - metadata buttons view
         * @param working - working set metadata (instance of metadata model)
         */
        return function(view,tree,buttons,model,ingest,working) {
            var _self = this;
            _self.view = view;
            _self.tree = tree;
            _self.model = model;
            _self.model.on("add remove reset",_self.view.render.bind(_self.view,false));
            _self.ingest = ingest;
            _self.buttons = buttons;
            _self.working = working;
            
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
                    var updateModel = function(elem) {
                        var root = elem.get("root");
                        if (name in root.children) {
                            root.children[name].values[0] = value;
                        } else {
                            root.children[name] = {"name":name,"values":[value],"children":{}} 
                        }
                    };
                    //Save real models
                    _self.model.each(function(elem) {
                        updateModel(elem);
                        elem.save(null,{"success":
                            function(){
                                if ("collection" in elem) {
                                    elem.fetch({"success":_self.view.render.bind(_self.view,false)});
                                }
                            },"validate": false});                        
                        });
                    updateModel(_self.working);
                    //Render should be delayed, allowing focus to trigger
                    setTimeout(_self.view.render.bind(_self.view,false),1);
                };
            _self.ingestClick =
                /**
                 * What to do on ingest click
                 * @param e - event (likely ignored)
                 */
                function(e) {
                    var selects = [];
                    var torm = [];
                    var valid = true;
                    _self.model.each(
                        function(elem) {
                            try {
                                valid = valid && elem.isValid();
                                if (!valid) {
                                    return;
                                }
                                var timestamp = new Date().getTime();
                                var id = elem.get("id");
                                var productName = elem.get("root")["children"]["ProductName"]["values"][0];
                                selects.push({"timestamp":timestamp,"file":id,"size":0,"pname":productName});
                                torm.push(elem);
                            } catch(err) {
                                Console.log("Error: Failed to parse ingestibles"+err);
                            }
                        }
                    );
                    //Run ingest, removing selected files from collection
                    if (valid && selects.length > 0) {
                        _self.buttons.setIngesting(true);
                        _self.buttons.render();
                        _self.ingest.save({"entries":selects});
                        for (var i = 0; i < torm.length; i++) {
                            _self.model.remove(torm[i]);
                        }
                        _self.tree.render();
                    }
                    _self.view.render(true);
                };
                _self.metaClear =
                    /**
                     * What to do on metadata clear
                     * @param e - event (likely ignored)
                     */
                    function(e) {
                        var destroy = [];
                        //Clear working set
                        _self.working.get("root").children = {};
                        var ids = [];
                        _self.model.each(
                            function(elem) {
                                destroy.push(elem);
                                ids.push(elem.get("id"));
                            }
                        );
                        //Destroy first
                        for (var i = 0; i < destroy.length; i++) {
                            destroy[i].destroy();
                        }
                        //Then render
                        _self.view.render();
                        //Now add back, and refetch
                        for (var i = 0; i < ids.length; i++) {
                            _self.model.add({"id":ids[i]});
                        }
                        //Refresh and update view
                        _self.model.each(
                            function(elem) {
                                elem.fetch({"success":_self.view.render.bind(_self.view,false)});
                            });
                    };                
                
            _self.view.setOnEntryFunction(_self.dataEntry);
            _self.buttons.setIngestClick(_self.ingestClick);
            _self.buttons.setMetadataClear(_self.metaClear);
        }
});