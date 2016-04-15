package org.apache.oodt.cas.curation.rest;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import org.apache.oodt.cas.curation.configuration.Configuration;
import org.apache.oodt.cas.curation.directory.Directory;
import org.apache.oodt.cas.curation.directory.FileDirectory;
import org.apache.oodt.commons.validation.DirectoryValidator;

import com.google.gson.Gson;

/**
 * A directory backend that returns JSON object representing a directory structure.
 * Will also list available directory backends.
 *  
 * @author starchmd
 */
@Path("directory")
public class DirectoryBackend {
    private DirectoryValidator validator;
    @Context
    ServletContext context;
    Map<String,Directory> types = new HashMap<String,Directory>();
    Gson gson = new Gson();

    @Context
    public void setServletContext(ServletContext context) {
        this.context = context;
    }
        /**
         * Construct a directory backend with hard-coded directories
         */
    public DirectoryBackend() {

    }
    @GET
    @Produces("application/json")
    /**
     * Get the types of directory backends.
     */
    public String getDirectoryTypes() {
        //TODO: update this loading code to be user-configured and be fed off of "type"
        if (types.get("files") == null)
            bootstrapValidator();
            types.put("files", new FileDirectory(Configuration.getWithReplacement(Configuration.STAGING_AREA_CONFIG),
             validator));
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
            bootstrapValidator();
            types.put("files", new FileDirectory(Configuration.getWithReplacement(Configuration.STAGING_AREA_CONFIG),
             validator));
        return gson.toJson(types.get(type).list());
    }

  /**
   * Initialise the validator engine as defined in web.xml.
   */
  private void bootstrapValidator(){
        if(validator==null) {
            String vclass = context.getInitParameter("directory.validation");
            if (vclass != null && !vclass.equals("")) {
                Class<?> clazz = null;
                try {
                    clazz = Class.forName(vclass);

                    Constructor<?> constructor = clazz.getConstructor();
                    this.validator = (DirectoryValidator) constructor.newInstance();

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
