/**
 * Module to load all the models and return them
 * @author starchmd
 */
define(["js-new/models/DirectoryModel",
        "js-new/models/MetadataModel",
        "js-new/models/ExtractorModel",
        "js-new/models/UploadModel",
        "js-new/models/IngestModel",
        "js-new/models/MetadataDataModel"],
    function(DirectoryModel,MetadataCollection,ExtractorCollection,UploadModel,IngestModel,MetadataDataModel) {
        /**
         * Return a set of happy models
         */
        var models = {
            "directory":new DirectoryModel({"id":"files"}),
            "extractor":new ExtractorCollection([],{"id":"extractor"}),
            "upload": UploadModel,
            "ingest": new IngestModel({"id":"ingest"}),
            "datamodel" : new MetadataDataModel({"id":"datamodel"}),
            "refresh" : function(exview,inview) {
                    models.directory.fetch();
                    if (exview != null) {
                        models.extractor.fetch({"success":function(){exview.render();}});
                    } else {
                        models.extractor.fetch();
                    }
                    models.ingest.fetch({"success":function(){inview.render();}});
                }
        };
        models.datamodel.fetch();
        
        models.metadata = new MetadataCollection([],{"id":"metadata","extractors":models.extractor,"datamodel":models.datamodel});
        return models;
    }
);