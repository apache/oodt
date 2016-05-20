/**
 * Start-Up module.
 * 
 * Allows the caller to pass in the Configuration and thus
 * change the behavior on stat-up while not needing to re-implement
 * the below start-up code.
 * 
 * @author starchmd
 */
define(["jquery",
        "js-new/models/SetupModels",
        "js-new/views/TreeView",
        "js-new/views/UploadView",
        "js-new/views/MetadataEntryView",
        "js-new/views/ExtractorView",
        "js-new/views/MetadataButtonsView",
        "js-new/views/IngestView",
        "js-new/control/MetadataControl",
        "js-new/control/ExtractorControl",
        "js-new/control/IngestControl",
        "js-new/control/TreeControl",
        "js-new/utils/utils",
        "js-new/config/Configuration",
        "lib/text! template.html"],
    function($,Models,TreeView,UploadView,MetadataEntryView,ExtractorView,MetadataButtonsView,IngestView,MetadataControl,ExtractorControl,IngestControl,TreeControl,utils,Config,html) {
        return function(entryCB,ingestCB) {
            //Load cookie, and set if nothing
            GLOBAL_USER = document.cookie.replace(/(?:(?:^|.*;\s*)user\s*\=\s*([^;]*).*$)|^.*$/, "$1");
            if (GLOBAL_USER == "") {
                GLOBAL_USER = String(new Date().getTime());
                document.cookie = "user="+GLOBAL_USER;
            }
            //Setup templates
            $("body").append(html);
            //Build views
            var views = [
                    {
                        "class" :   IngestView,
                        "params" :  {"name":"ingest-view","ingest":Models.ingest},
                        "element" : "ingest"
                    },
                    {
                        "class" :   MetadataEntryView,
                        "params" :  {"name":"metadata-view","datamodel":Models.datamodel,"model":Models.metadata,"working-set":Models.working},
                        "element" : "metadata"
                    },
                    {
                        "class" :   ExtractorView,
                        "params" :  {"name":"extractor-view","extractors":Models.extractor},
                        "element" : "extractors"
                    },
                    {
                        "class" :   UploadView,
                        "params" :  {"name":"upload-view","upload":Models.upload,"notify":Models.directory},
                        "element" : "files"
                    },
                    {
                        "class" :   TreeView,
                        "params" :  {"name":"tree-view","directory":Models.directory,"selection":Models.metadata},
                        "element" : "files"
                    },
                    {
                        "class" :   MetadataButtonsView,
                        "params" :  {"name":"btns-view","buttonText":Config.INGEST_BUTTON_TEXT},
                        "element" : "met-buttons"
                    },
                    
            ];
            var constructed = {};
            var view;
            var jqel;
            for (var i = 0; i < views.length; i++) {
                view = views[i];
                jqel = $("#"+view["element"]);
                if (jqel.length == 0) {
                    console.log("Could not find element with id: "+view["element"]);
                    continue;
                }
                view["params"]["el"] = jqel;
                constructed[view["params"]["name"]] = new view["class"](view["params"]);
            }
            //Build controllers
            var controls = [
                    {
                        "name" :    "metadata-control",
                        "class" :   MetadataControl,
                        "params" :  [constructed["metadata-view"],constructed["tree-view"],constructed["btns-view"],Models.metadata,Models.ingest,Models.working]
                    },
                    {
                        "name" :    "extractor-control",
                        "class" :   ExtractorControl,
                        "params" :  [constructed["extractor-view"],Models.extractor,Models.metadata]
                    },
                    {
                        "name" :    "ingest-control",
                        "class" :   IngestControl,
                        "params" :  [constructed["ingest-view"],Models.ingest,constructed["btns-view"]]
                    },
                    {
                        "name" :    "tree-control",
                        "class" :   TreeControl,
                        "params" :  [constructed["tree-view"],constructed["metadata-view"]]
                    }
            ];
            var control;
            for (var i = 0; i < controls.length; i++) {
                control = controls[i];
                if (typeof(control["params"][0]) == "undefined")
                    continue;
                control["params"].splice(0,0,null);
                constructed[control["name"]] = new (Function.prototype.bind.apply(control["class"],control["params"]));  
            }
            //Setup bindings and callbacks
            if (typeof(constructed["extractor-view"]) != "undefined") {
                Models.extractor.fetch({"success":constructed["extractor-view"].render.bind(constructed["extractor-view"])});
            }
            if (typeof(constructed["ingest-view"]) != "undefined") {
                Models.refresh(constructed["ingest-view"]);
                //setInterval(Models.refresh.bind(Models,constructed["ingest-view"]),Config.FILE_SYSTEM_REFRESH_INTERVAL);
                if (typeof(constructed["metadata-control"]) != "undefined") {
                    constructed["metadata-control"].enableIngestWait();
                }
            }
            if (typeof(constructed["metadata-view"]) != "undefined") {
                constructed["metadata-control"].setEntryCallback(entryCB);
                constructed["metadata-control"].setIngestCallback(ingestCB);
                Models.datamodel.fetch({"success":constructed["metadata-view"].render.bind(constructed["metadata-view"],true)});
            }
            //If we are not allowed to select files, use /dev/null 0 length system files
            if (typeof(constructed["tree-view"]) == "undefined") {
                Models.metadata.add({"id":"/dev/null"});
            }
            //Render upload view
            if (typeof(constructed["upload-view"]) != "undefined") {
                constructed["upload-view"].render();
            }            
            //Returns a function to allow external data entry
            return function(name,value) {
                constructed["metadata-control"].dataEntry.call({"name":name,"value":value});
            }
        }
    });