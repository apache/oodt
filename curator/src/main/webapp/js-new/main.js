//Imports via require js
require.config({
    baseUrl: "",
    paths: {
        jquery: "lib/jquery-2.1.3",
        underscore: "lib/underscore"
        //js: "../js"
    }
});
//"jquery","underscore","backbone","i18n!js/nls/ui",
require(["lib/domReady!","jquery","js-new/DirectoryModel","js-new/TreeView","js-new/UploadView","lib/text!templates/template.html"],function(doc,$,DirectoryModel,TreeView,UploadView,html) {
    //Setup templates
    $("body").append(html);
    var view = new TreeView({"el":$("#files"),"name":"tree-view","model":new DirectoryModel({"id":"files"})});
    var upvw = new UploadView({"el":$("#files"),"name":"upload-view","notify":view});
    setInterval(function() {view.render(true);},1000);
});


