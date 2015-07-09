package org.apache.oodt.cas.curation.rest;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.oodt.cas.curation.configuration.Configuration;
import org.apache.oodt.cas.curation.metadata.FlatDirMetadataHandler;
import org.apache.oodt.cas.filemgr.ingest.Ingester;
import org.apache.oodt.cas.filemgr.ingest.StdIngester;
import org.apache.oodt.cas.metadata.Metadata;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * A backend that uses put to initiate ingest
 *  
 * @author starchmd
 */
@Path("ingest")
public class IngestBackend {

    private static final Logger LOG = Logger.getLogger(IngestBackend.class.getName());
    //GSON serialization object using Google GSON
    private Gson gson = new GsonBuilder().create();
    private static final String DATA_TRANSFER_SERVICE = "org.apache.oodt.cas.filemgr.datatransfer.LocalDataTransferFactory";
    /**
     * Construct a directory backend with hard-coded directories
     */
    public IngestBackend() {}
    @PUT
    @Consumes("application/json")
    /**
     * Runs the ingest
     * @param extractor - optional extractor to run
     * @param json - new json for file
     */
    public Response igest(@QueryParam("extractor") String extractor,String json) throws Exception {
        //TODO: Clean this whole method up
        IngestBackend.Input input = gson.fromJson(json, IngestBackend.Input.class);;
        Ingester ingester = new StdIngester(DATA_TRANSFER_SERVICE);
        for (String file : input.files) {
            String parent = new File(Configuration.getWithReplacement(Configuration.STAGING_AREA_CONFIG)).getParent();
            File full = new File(parent,file);
            Metadata meta = new FlatDirMetadataHandler().get(file);
            ingester.ingest(new URL(Configuration.getWithReplacement(Configuration.FILEMANAGER_URL_CONFIG)), full.getAbsoluteFile(), meta);
        }
        return Response.ok().build();
    }
    /**
     * Class to represent input for ingest
     * 
     * @author starchmd
     */
    class Input {
        public String id;
        public List<String> files;
    }
}
