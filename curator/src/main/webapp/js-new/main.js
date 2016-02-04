//Imports via require js
require.config({
    baseUrl: "",
    paths: {
        jquery: "lib/jquery-2.1.3",
        underscore: "lib/underscore",
        datatables: "lib/jquery.dataTables"
        //js: "../js"
    }
});
//"jquery","underscore","backbone","i18n!js/nls/ui",
require(["lib/domReady!",
         "jquery",
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
         "js-new/config/Configuration",
         "lib/text! template.html"
        ],
    function(doc,$,Models,TreeView,UploadView,MetadataEntryView,ExtractorView,MetadataButtonsView,IngestView,MetadataControl,ExtractorControl,IngestControl,Config,html) {
        //Load cookie, and set if nothing
        GLOBAL_USER = document.cookie.replace(/(?:(?:^|.*;\s*)user\s*\=\s*([^;]*).*$)|^.*$/, "$1");
        if (GLOBAL_USER == "") {
            GLOBAL_USER = String(new Date().getTime());
            document.cookie = "user="+GLOBAL_USER;
        }
        //Setup templates
        $("body").append(html);
        //Setup views
        var ingt = new IngestView({"el":$("#ingest"),"name":"ingest-view","ingest":Models.ingest});
        var meta = new MetadataEntryView({"el":$("#metadata"),"name":"metadata-view","datamodel":Models.datamodel,"model":Models.metadata,"working-set":Models.working});
        var extr = new ExtractorView({"el":$("#extractors"),"name":"extractor-view","extractors":Models.extractor});
        var upld = new UploadView({"el":$("#files"),"name":"upload-view","upload":Models.upload,"notify":Models.directory});
        var tree = new TreeView({"el":$("#files"),"name":"tree-view","directory":Models.directory,"selection":Models.metadata,"metview":meta});
        var btns = new MetadataButtonsView({"el":$("#met-buttons"),"name":"btns-view"});
        new MetadataControl(meta,tree,btns,Models.metadata,Models.ingest,Models.working);
        new ExtractorControl(extr,Models.extractor,Models.metadata);
        new IngestControl(ingt,Models.ingest,btns);
        Models.extractor.fetch({"success":extr.render.bind(extr)});
        Models.refresh(ingt);
        Models.datamodel.fetch({"success":meta.render.bind(meta,true)});
        setInterval(Models.refresh.bind(Models,ingt),Config.FILE_SYSTEM_REFRESH_INTERVAL);
    }
);


