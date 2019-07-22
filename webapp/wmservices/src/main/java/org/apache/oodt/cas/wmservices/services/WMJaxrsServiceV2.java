package org.apache.oodt.cas.wmservices.services;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import org.apache.oodt.cas.wmservices.enums.ErrorType;
import org.apache.oodt.cas.wmservices.exceptions.NotFoundException;
import org.apache.oodt.cas.wmservices.resources.WorkflowInstanceResource;
import org.apache.oodt.cas.workflow.exceptions.WorkflowException;
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;
import org.apache.oodt.cas.workflow.system.WorkflowManagerClient;

/**
 * Service class for Proposing Apache OODT-2.0 WorkflowManager REST-APIs This handles HTTP requests
 * and returns workflow manager entities JAX-RS resources converted to different formats.
 *
 * @author ngimhana (Nadeeshan Gimhana)
 */
public class WMJaxrsServiceV2 {

  private static final Logger LOGGER = Logger.getLogger(WMJaxrsServiceV2.class.getName());

  // The servlet context, which is used to retrieve context parameters.
  @Context private ServletContext context;

  /**
   * Gets the workflow manager client instance from the servlet context.
   *
   * @return the workflow manager client instance from the servlet context attribute
   * @throws Exception if an object cannot be retrieved from the context attribute
   */
  public WorkflowManagerClient getContextClient() throws WorkflowException {
    /** Get the workflow manager client from the servlet context.* */
    Object clientObject = context.getAttribute("client");
    if (clientObject != null && clientObject instanceof WorkflowManagerClient) {
      return (WorkflowManagerClient) clientObject;
    }

    String message = ErrorType.CAS_PRODUCT_EXCEPTION_WORKFLOWMGR_CLIENT_UNAVILABLE.getErrorType();
    LOGGER.log(Level.WARNING, message);
    throw new WorkflowException(message);
  }

  /**
   * Gets an HTTP response that represents a {@link WorkflowInstance} from the workflow manager.
   *
   * @param workflowInstId the ID of the workflow Instance
   * @return an HTTP response that represents a {@link WorkflowInstance} from the workflow manager
   */
  @GET
  @Path("workflowInst")
  @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
  public WorkflowInstanceResource getWorkflowInstanceById(
      @QueryParam("workflowInstId") String workflowInstId) throws WebApplicationException {
    try {
      WorkflowManagerClient wmclient = getContextClient();
      WorkflowInstance workflowInstanceById = wmclient.getWorkflowInstanceById(workflowInstId);
      WorkflowInstanceResource workflowResource =
          new WorkflowInstanceResource(workflowInstanceById);
      LOGGER.log(Level.INFO, workflowResource.getCurrentTaskId());
      return workflowResource;
    } catch (Exception e) {
      throw new NotFoundException(e.getMessage());
    }
  }
}
