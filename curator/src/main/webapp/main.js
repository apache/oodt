//Imports via require js
require.config({
    baseUrl: "",
    shim : {
        "bootstrap" : { "deps" :['jquery'] },
        "popover" : { "deps" :['tooltip','jquery'] }
    },
    paths: {
        jquery: "lib/jquery-2.1.3",
        underscore: "lib/underscore",
        datatables: "lib/jquery.dataTables",
        bootstrap: "lib/bootstrap.min",
        tooltip: "lib/tooltip",
        popover: "lib/popover"
    }
});
require(["lib/domReady!","js-new/Startup"],
    function(doc,Startup) {
        //Pass in configuration
        var configuration = {};
        Startup(configuration);
    }
);


