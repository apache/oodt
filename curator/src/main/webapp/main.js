//Imports via require js
require.config({
    baseUrl: "",
    paths: {
        jquery: "lib/jquery-2.1.3",
        underscore: "lib/underscore",
        datatables: "lib/jquery.dataTables"
    }
});
require(["lib/domReady!","js-new/Startup"],
    function(doc,Startup) {
        //Pass in configuration
        var configuration = {};
        Startup(configuration);
    }
);


