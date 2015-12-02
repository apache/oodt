package org.apache.oodt.cas.curation.rest;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.oodt.cas.curation.configuration.Configuration;
import org.apache.oodt.cas.curation.metadata.FlatDirMetadataHandler;
import org.apache.oodt.cas.curation.metadata.MetadataHandler;
import org.apache.oodt.cas.curation.structs.ExtractorConfig;
import org.apache.oodt.cas.curation.util.ExtractorConfigReader;
import org.apache.oodt.cas.metadata.MetExtractor;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.exceptions.MetExtractionException;
import org.apache.oodt.cas.metadata.util.GenericMetadataObjectFactory;

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
public class MetadataBackend {

    private static final Logger LOG = Logger.getLogger(MetadataBackend.class.getName());
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
    private MetadataHandler handler;
    private final Map<String,ExtractorConfig> extractors = new HashMap<String,ExtractorConfig>();
    /**
     * Construct a directory backend with hard-coded directories
     */
    public MetadataBackend() {
        handler = new FlatDirMetadataHandler();
    }
    @GET
    @Produces("application/json")
    @Path("{file:.+}")
    /**
     * Gets the metadata as JSON, refreshes using an extractor
     * @param file - file to get metadata from
     * @param extractor - if specified, this extractor will be run and replace existing metadata
     */
    public Response getMetadata(@PathParam("file") String file,@QueryParam("extractor") String extractor) {
        try {
            LOG.info("File recieved:"+file);
            Metadata met = null;
            try {
                met = handler.get(file);
                //TODO: don't catch all exceptions, make it a specific excpetion
            } catch(Exception e) {
                met = new Metadata();
            }
            //If extractor is specified, then its metadata is considered "correct" and previous metadata is used only to fill filler
            if (extractors.containsKey(extractor)) {
                Metadata extracted = this.runExtractor(file, extractor);
                met = this.merge(extracted, met, extractors.get(extractor).getFiller());
                LOG.log(Level.INFO,"SAVING File: "+file+ " Metadata: "+met);
                for (String key : met.getAllKeys()) {
                    LOG.log(Level.INFO, "Metadat KEY: "+key+" IS:"+met.getMetadata(key));
                }
                handler.set(file, met);
            }
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
    public Response putMetadata(@PathParam("file") String file,@QueryParam("extractor") String extractor,String json) {
        try {
            System.out.println("File:"+ file);
            LOG.info("File recieved:"+file);
            //TODO: Sanitize this input
            Metadata met = gson.fromJson(json, Metadata.class);
            met.removeMetadata(Configuration.FILLER_METDATA_KEY);
            handler.set(file, met);
            if (extractors.containsKey(extractor)) {
                Metadata extracted = this.runExtractor(file, extractor);
                met = this.merge(extracted, met, extractors.get(extractor).getFiller());
                handler.set(file, met);
            }
            return Response.ok().entity(gson.toJson(met)).build();
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
            loadMetadataExtractors();
            return Response.ok().entity(gson.toJson(MetadataBackend.this.extractors.keySet())).build();
        } catch (Exception e) {
            return ExceptionResponseHandler.BuildExceptionResponse(e);
        }
    }
    /**
     * Loads the metadata extractors
     */
    protected void loadMetadataExtractors() {
        File directory = new File(Configuration.getWithReplacement(Configuration.EXTRACTOR_AREA_CONFIG));
        //Load only sub-directories of the extractor config area 
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
              return new File(current, name).isDirectory();
            }
        };
        String[] subdirs = directory.list(filter);
        for (String id : subdirs != null?subdirs:new String[]{}) {
            try {
                extractors.put(id,ExtractorConfigReader.readFromDirectory(directory, id));
            } catch (IOException e) {
                LOG.log(Level.WARNING, "Failed to load extractor confugred at:"+new File(directory,id).toString()+" Exception:", e);
            }
        }
    }
    /**
     * Runs a metadata extractor on given file
     * @param file - file to extract metadata from
     * @param config - configuration to run this met extractor
     * @return metadata extracted
     * @throws MetExtractionException - exception thrown when failure to extract metadata
     */
    protected Metadata runExtractor(String file, String id) throws MetExtractionException {
        ExtractorConfig config = extractors.get(id);
        MetExtractor metExtractor = GenericMetadataObjectFactory.getMetExtractorFromClassName(config.getClassName());
        metExtractor.setConfigFile(config.getConfigFiles().get(0));
        String parent = new File(Configuration.getWithReplacement(Configuration.STAGING_AREA_CONFIG)).getParent();
        File full = new File(parent,file);
        return metExtractor.extractMetadata(full.getAbsolutePath());
    }
    /**
     * Fill primary with secondary metadata where primary is filler
     * @param primary - primary metadata
     * @param secondary - secondary metadata
     * @return primary metadata object (which has been modified)
     */
    private Metadata merge(Metadata primary,Metadata secondary,String filler) {
        for (String key : primary.getAllKeys()) {
            List<String> values = primary.getAllMetadata(key);
            if (values != null && (values.size() == 0 || values.get(0).equals(filler))) {
                List<String> newVals = secondary.getAllMetadata(key);
                if (newVals != null && newVals.size() > 0 && !newVals.get(0).equals(filler))
                    primary.replaceMetadata(key, secondary.getValues(key));
            }
        }
        return primary;
    }
}
