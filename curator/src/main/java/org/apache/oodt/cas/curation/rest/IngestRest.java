package org.apache.oodt.cas.curation.rest;


import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;

import org.apache.commons.lang.StringUtils;
import org.apache.oodt.cas.curation.ingest.IngestBackend;
import org.apache.oodt.cas.curation.ingest.IngestException;
import org.apache.oodt.cas.curation.ingest.InputStruct;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * A backend that uses put to initiate ingest
 *  
 * @author starchmd
 */
@Path("ingest")
public class IngestRest {
    private static final Logger LOG = Logger.getLogger(IngestRest.class.getName());
    //GSON serialization object using Google GSON
    private Gson gson = new GsonBuilder().create();
    private IngestBackend backend = null;
    /**
     * Construct a directory backend with hard-coded directories
     */
    public IngestRest() {}
    @PUT
    @Consumes("application/json")
    /**
     * Runs the ingest
     * @param user - user to isolate requests
     * @param extractor - optional extractor to run
     * @param json - new json for file
     */
    public Response ingest(@QueryParam("user") String user,@QueryParam("extractor") String extractor,String json) {
        try {
            setup();
            InputStruct input = gson.fromJson(json, InputStruct.class);
            List<String> files = new LinkedList<String>();
            for (InputStruct.InputEntry entry : input.entries) {
                files.add(entry.file);
            }
            LOG.log(Level.INFO, "Ingesting files: "+StringUtils.join(files,","));
            this.backend.ingest(input,user);
            return Response.ok().build();
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Exception occured calling ingest REST endpoint", e);
            return ExceptionResponseHandler.BuildExceptionResponse(e);
        }
    }
    @GET
    @Produces("application/json")
    /**
     * Get ingest status
     * @return list of statuses
     */
    public Response status(@QueryParam("user") String user) {
        try {
            setup();
            LOG.log(Level.INFO, "Reading current status");
            return Response.ok().entity(gson.toJson(this.backend.status(user))).build();
        } catch(Exception e) {
            LOG.log(Level.SEVERE,"Exception occured calling ingest REST endpoint", e);
            return ExceptionResponseHandler.BuildExceptionResponse(e);
        }
    }
    @DELETE
    /**
     * Delete errors
     * @return response object
     */
    public Response remove(@QueryParam("user") String user) {
        try {
            setup();
            LOG.log(Level.INFO, "Deleteing current errors");
            this.backend.clearErrors(user);
            return Response.ok().build();
        } catch(Exception e) {
            LOG.log(Level.SEVERE,"Exception occured calling ingest REST endpoint", e);
            return ExceptionResponseHandler.BuildExceptionResponse(e);
        }        
    }
    /**
     * Setup the backend
     * @throws IngestException
     */
    public synchronized void setup() throws IngestException {
        if (this.backend != null)
            return;
        LOG.log(Level.INFO, "Setting up ingest backend");
        this.backend = new IngestBackend();
    }

}
