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
            "refresh" : function() {
                    models.directory.fetch();
                    models.extractor.fetch();
                    models.ingest.fetch();
                }
        };
        models.datamodel.fetch();
        models.refresh();
        
        models.metadata = new MetadataCollection([],{"id":"metadata","extractors":models.extractor});
        return models;
    }
);