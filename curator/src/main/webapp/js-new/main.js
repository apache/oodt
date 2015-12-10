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
         "js-new/views/IngestView",
         "js-new/control/MetadataControl",
         "js-new/control/ExtractorControl",
         "js-new/control/IngestControl",
         "js-new/config/Configuration",
         "lib/text! template.html"
        ],
    function(doc,$,Models,TreeView,UploadView,MetadataEntryView,ExtractorView,IngestView,MetadataControl,ExtractorControl,IngestControl,Config,html) {
        //Setup templates
        $("body").append(html);
        //Setup views
        var ingt = new IngestView({"el":$("#ingest"),"name":"ingest-view","ingest":Models.ingest});
        var meta = new MetadataEntryView({"el":$("#metadata"),"name":"metadata-view","datamodel":Models.datamodel,"model":Models.metadata});
        var extr = new ExtractorView({"el":$("#extractors"),"name":"extractor-view","extractors":Models.extractor});
        var upld = new UploadView({"el":$("#files"),"name":"upload-view","upload":Models.upload,"notify":Models.directory});
        var tree = new TreeView({"el":$("#files"),"name":"tree-view","directory":Models.directory,"selection":Models.metadata,"metview":meta});
        new MetadataControl(meta,Models.metadata,Models.ingest);
        new ExtractorControl(extr,Models.extractor,Models.metadata);
        new IngestControl(ingt,Models.ingest);
        Models.refresh(extr,ingt);
        setInterval(function() {Models.refresh(function(){},ingt);},Config.FILE_SYSTEM_REFRESH_INTERVAL);
    }
);


