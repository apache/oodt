/**
 * Module to load all the models and return them
 * @author starchmd
 */
define(["js-new/models/DirectoryModel",
        "js-new/models/MetadataModel",
        "js-new/models/ExtractorModel",
        "js-new/models/UploadModel"],
    function(DirectoryModel,MetadataCollection,ExtractorCollection,UploadModel) {
        return {
            "directory":new DirectoryModel({"id":"files"}),
            "metadata":new MetadataCollection([],{"id":"metadata"}),
            "extractor":new ExtractorCollection([],{"id":"extractor"}),
            "upload": UploadModel
        };
    }
);