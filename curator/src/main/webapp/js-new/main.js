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
         "js-new/views/MetadataView",
         "js-new/views/IngestView",
         "js-new/config/Configuration",
         "lib/text! template.html"
        ],
    function(doc,$,Models,TreeView,UploadView,MetadataView,IngestView,Config,html) {
        //Setup templates
        $("body").append(html);
        //Setup views
        var ingt = new IngestView({"el":$("#ingest"),"name":"ingest-view","ingest":Models.ingest});
        var meta = new MetadataView({"el":$("#metadata"),"name":"metadata-view","metadata":Models.metadata,
                                     "extractors":Models.extractor,"ingest":Models.ingest});
        var upld = new UploadView({"el":$("#files"),"name":"upload-view","upload":Models.upload,"notify":Models.directory});
        var tree = new TreeView({"el":$("#files"),"name":"tree-view","directory":Models.directory,"selection":Models.metadata,"metview":meta});

        /*Models.metadata.each(function(model) {
            model.fetch({"success":function() {
                meta.render();
            }});
        });*/
        Models.directory.fetch();
        Models.extractor.fetch();
        Models.ingest.fetch();
        setInterval(function() {Models.directory.fetch();Models.extractor.fetch();Models.ingest.fetch();},Config.FILE_SYSTEM_REFRESH_INTERVAL);
    }
);


