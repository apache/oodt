package org.apache.oodt.cas.curation.rest;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.oodt.cas.curation.metadata.MetadataBackend;
import org.apache.oodt.cas.metadata.Metadata;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * A backend that puts/gets the metadata of a given file.
 *  
 * @author starchmd
 */
@Path("metadata")
public class MetadataRest {

    private static final Logger LOG = Logger.getLogger(MetadataRest.class.getName());
    //GSON serialization object using Google GSON
    private Gson gson = new GsonBuilder().setExclusionStrategies(new ExclusionStrategy(){
        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return false;
        }
        @Override
        public boolean shouldSkipField(FieldAttributes field) {
            return field.getName().equals("parent");
        }}).create();
    private MetadataBackend backend;

    /**
     * Construct a directory backend with hard-coded directories
     */
    public MetadataRest() {}
    @GET
    @Produces("application/json")
    @Path("{file:.+}")
    /**
     * Gets the metadata as JSON, refreshes using an extractor
     * @param file - file to get metadata from
     * @param extractor - if specified, this extractor will be run and replace existing metadata
     */
    public Response getMetadata(@PathParam("file") String file,@QueryParam("user") String user,@QueryParam("extractor") String extractor) {
        try {
            if (file.equals("dev/null"))
            {
                file = "/dev/null";
            }
            this.setup();
            Metadata met = this.backend.getMetadata(file,(user==null)?"":user, extractor);
            return Response.ok().entity(gson.toJson(met)).build();
        } catch (Exception e) {
            return ExceptionResponseHandler.BuildExceptionResponse(e);
        }
    }

    @PUT
    @Consumes("application/json")
    @Produces("application/json")
    @Path("{file:.+}")
    /**
     * Sets the metadata for a given file
     * @param file - file to specify metadata for
     * @param extractor - optional extractor to run
     * @param json - new json for file
     */
    public Response putMetadata(@PathParam("file") String file,@QueryParam("user") String user,@QueryParam("extractor") String extractor,String json) {
        try {
            if (file.equals("dev/null"))
            {
                file = "/dev/null";
            }
            Metadata input = gson.fromJson(json, Metadata.class);
            this.setup();
            Metadata met = this.backend.putMetadata(file,(user==null)?"":user, extractor, input);
            return Response.ok().entity(gson.toJson(met)).build();
        } catch(Exception e) {
            return ExceptionResponseHandler.BuildExceptionResponse(e);
        
        }
    }
    @DELETE
    @Path("{file:.+}")
    /**
     * Deletes the metadata for a given file
     * @param file - file to specify metadata for
     */
    public Response deleteMetadata(@PathParam("file") String file,@QueryParam("user") String user) {
        try {
            if (file.equals("dev/null"))
            {
                file = "/dev/null";
            }
            this.setup();
            this.backend.deleteMetadata(file,(user==null)?"":user);
            return Response.ok().build();
        } catch(Exception e) {
            return ExceptionResponseHandler.BuildExceptionResponse(e);
        }
    }
    @GET
    @Produces("application/json")
    @Path("extractors")
    /**
     * Returns the list of extractors
     * @return - list of extractors
     */
    public Response getExtractors() {
        try {
            this.setup();
            return Response.ok().entity(gson.toJson(this.backend.getExtractors())).build();
        } catch (Exception e) {
            return ExceptionResponseHandler.BuildExceptionResponse(e);
        }
    }
    /**
     * Setup the backend to call
     */
    private synchronized void setup() {
        if (this.backend != null)
            return;
        LOG.info("Setting up metadata backend for metadata rest service");
        this.backend = new MetadataBackend();
    }
}
