package org.apache.oodt.cas.curation.rest;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.oodt.cas.curation.configuration.Configuration;
import org.apache.oodt.cas.curation.validation.ValidationBackend;
import org.apache.oodt.cas.curation.validation.ValidationException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * A backend that loads and prepares a validation layer from the 
 * filemanager.
 * 
 * @author starchmd
 */
@Path("validation")
public class ValidationRest {
    private static final Logger LOG = Logger.getLogger(ValidationBackend.class.getName());
    private Gson gson = new GsonBuilder().create();
    ValidationBackend backend = null;
    
    /**
     * Empty REST service constructor. (Configuration not available yet.
     */
    public ValidationRest(){}

    @GET
    @Produces("application/json")
    /**
     * Return information pertaining to validation
     * @return - OK response on success
     */
    public Response information() {
        try {
            if (this.backend == null)
                setup();
            return Response.ok().entity(gson.toJson(this.backend.getValidation())).build();
        } catch(Exception e) {
            LOG.log(Level.SEVERE, "Validation exception occured calling validation/info REST endpoint", e);
            return Response.serverError().entity(gson.toJson(e)).build();
        }
    }
    /**
     * Setup this service
     */
    public void setup() throws ValidationException {
        try {
            LOG.log(Level.INFO, "Setting up validation backend");
            this.backend = new ValidationBackend(Configuration.getWithReplacement(Configuration.FILEMANAGER_PROP_CONFIG));
        } catch(ValidationException ve) {
            LOG.log(Level.SEVERE, "Validation exception occured setting up validation REST service", ve);
            throw ve;
        }
    }
}
