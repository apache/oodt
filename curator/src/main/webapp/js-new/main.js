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
         "js-new/DirectoryModel",
         "js-new/MetadataModel",
         "js-new/TreeView",
         "js-new/UploadView",
         "js-new/MetadataView",
         "lib/text!templates/template.html"
        ],function(doc,$,DirectoryModel,MetadataCollection,TreeView,UploadView,MetadataView,html) {
    //Setup templates
    $("body").append(html);
    var coll = new MetadataCollection([],{"id":"metadata"});
    coll.add({"id":"yolo1"});
    var view = new TreeView({"el":$("#files"),"name":"tree-view","model":new DirectoryModel({"id":"files"})});
    var upvw = new UploadView({"el":$("#files"),"name":"upload-view","notify":view});
    var metv = new MetadataView({"el":$("#metadata"),"name":"metadata-view","collection":coll});

    coll.each(function(model) {
        model.fetch({"success":function() {
            metv.render();
        }});
    });
    setInterval(function() {view.render(true);},100000);
});


