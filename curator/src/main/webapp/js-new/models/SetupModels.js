/**
 * Module to load all the models and return them
 * @author starchmd
 */
define(["js-new/models/DirectoryModel",
        "js-new/models/MetadataModel",
        "js-new/models/ExtractorModel",
        "js-new/models/UploadModel",
        "js-new/models/IngestModel"],
    function(DirectoryModel,MetadataCollection,ExtractorCollection,UploadModel,IngestModel) {
        /**
         * Return a set of happy models
         */
        var models = {
            "directory":new DirectoryModel({"id":"files"}),
            "extractor":new ExtractorCollection([],{"id":"extractor"}),
            "upload": UploadModel,
            "ingest": new IngestModel({"id":"ingest"})
        };
        models.metadata = new MetadataCollection([],{"id":"metadata","extractors":models.extractor});
        return models;
    }
);