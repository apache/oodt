package org.apache.oodt.cas.curation.rest;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.apache.oodt.cas.curation.configuration.Configuration;
import org.apache.oodt.cas.curation.directory.Directory;
import org.apache.oodt.cas.curation.directory.FileDirectory;

import com.google.gson.Gson;

/**
 * A directory backend that returns JSON object representing a directory structure.
 * Will also list available directory backends.
 *  
 * @author starchmd
 */
@Path("directory")
public class DirectoryBackend {
    Map<String,Directory> types = new HashMap<String,Directory>();
    Gson gson = new Gson();
    /**
     * Construct a directory backend with hard-coded directories
     */
    public DirectoryBackend() {}
    @GET
    @Produces("application/json")
    /**
     * Get the types of directory backends.
     */
    public String getDirectoryTypes() {
        //TODO: update this loading code to be user-configured and be fed off of "type"
        if (types.get("files") == null)
            types.put("files", new FileDirectory(Configuration.getWithReplacement(Configuration.STAGING_AREA_CONFIG)));
        return gson.toJson(types.keySet());
    }

    @GET
    @Produces("application/json")
    @Path("{type}")
    /**
     * Returns the listing of the given directory type
     * @param type - type of directory to list
     */
    public String list(@PathParam("type") String type) throws Exception {
        //TODO: update this loading code to be user-configured and be fed off of "type"
        if (types.get("files") == null)
            types.put("files", new FileDirectory(Configuration.getWithReplacement(Configuration.STAGING_AREA_CONFIG)));
        return gson.toJson(types.get(type).list());
    }
}
