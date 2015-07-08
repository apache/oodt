package org.apache.oodt.cas.curation.rest;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.oodt.cas.curation.configuration.Configuration;
import org.apache.oodt.cas.filemgr.system.XmlRpcFileManagerClient;

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
    XmlRpcFileManagerClient client;

    /**
     * Construct a directory backend with hard-coded directories
     */
    public IngestBackend() {
        
    }
    @PUT
    @Consumes("application/json")
    /**
     * Runs the ingest
     * @param extractor - optional extractor to run
     * @param json - new json for file
     */
    public Response putMetadata(@QueryParam("extractor") String extractor,String json) throws Exception {
        //TODO: Sanitize this input
        IngestBackend.Input input = gson.fromJson(json, IngestBackend.Input.class);
        for (String file : input.files) {
            String parent = new File(Configuration.get(Configuration.STAGING_AREA_CONFIG)).getParent();
            File full = new File(parent,file);
            client.ingestProduct(product, metadata, clientTransfer);
            
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
