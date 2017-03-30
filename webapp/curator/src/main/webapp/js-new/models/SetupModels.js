/**
 * Module to load all the models and return them
 * @author starchmd
 */
define(["js-new/models/DirectoryModel",
        "js-new/models/MetadataModel",
        "js-new/models/MetadataCollection",
        "js-new/models/ExtractorModel",
        "js-new/models/UploadModel",
        "js-new/models/IngestModel",
        "js-new/models/MetadataDataModel",
        "js-new/utils/utils"],
    function(DirectoryModel,Metadata,MetadataCollection,ExtractorCollection,UploadModel,IngestModel,MetadataDataModel,utils) {
        /**
         * Return a set of happy models
         */
        var models = {
            "directory":new DirectoryModel({"id":"files"}),
            "s3directory":new DirectoryModel({"id":"s3"}),
            "extractor":new ExtractorCollection([],{"id":"extractor"}),
            "upload": UploadModel,
            "ingest": new IngestModel({"id":"ingest"}),
            "datamodel" : new MetadataDataModel({"id":"datamodel"}),
            "refresh" : function(inview, torefresh) {
                    if(torefresh==="S3"){
                      models.s3directory.set({s3user: $('#s3name').val()})
                      models.s3directory.fetch();
                    }
                    else {
                      models.directory.fetch();
                    }
                    models.ingest.fetch({"success":inview.render.bind(inview)});
                },
            "refreshTree": function(torefresh){
              if(torefresh === "S3"){
                models.s3directory.set({s3user: $('#s3name').val()})
                models.s3directory.fetch();
              }
              else {
                models.directory.fetch();
              }
            },
            "working": new Metadata({"id":"working-set"})
        };
        //models.datamodel.fetch();
        models.metadata = new MetadataCollection([],{"id":"metadata","extractors":models.extractor,"datamodel":models.datamodel});
        return models;
    }
);