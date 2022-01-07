package org.apache.oodt.cas.wmservices.services;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.wmservices.enums.ErrorType;
import org.apache.oodt.cas.wmservices.exceptions.InternalServerErrorException;
import org.apache.oodt.cas.wmservices.exceptions.NotFoundException;
import org.apache.oodt.cas.wmservices.resources.*;
import org.apache.oodt.cas.workflow.exceptions.WorkflowException;
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;
import org.apache.oodt.cas.workflow.structs.WorkflowInstancePage;
import org.apache.oodt.cas.workflow.system.WorkflowManagerClient;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * Service class for Proposing Apache OODT-2.0 WorkflowManager REST-APIs This handles HTTP requests
 * and returns workflow manager entities JAX-RS resources converted to different formats.
 *
 * @author ngimhana (Nadeeshan Gimhana)
 */
@Path("workflow")
public class WMJaxrsServiceV2 {

  private static org.slf4j.Logger logger = LoggerFactory.getLogger(WMJaxrsServiceV2.class);

  /** The servlet context, which is used to retrieve context parameters. */
  @Context private ServletContext context;

  /**
   * Gets the workflow manager client instance from the servlet context.
   *
   * @return the workflow manager client instance from the servlet context attribute
   * @throws Exception if an object cannot be retrieved from the context attribute
   */
  public WorkflowManagerClient getContextClient() throws WorkflowException {
    // Get the workflow manager client from the servlet context.
    Object clientObject = context.getAttribute("client");
    if (clientObject instanceof WorkflowManagerClient) {
      return (WorkflowManagerClient) clientObject;
    }

    String message = ErrorType.CAS_PRODUCT_EXCEPTION_WORKFLOWMGR_CLIENT_UNAVILABLE.getErrorType();
    logger.debug("Warning Message: {}", message);
    throw new WorkflowException(message);
  }

  /**
   * Checks if workflow manager is alive
   *
   * @return status
   */
  @GET
  @Path("status")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public WorkflowManagerStatus getWorkflowManagerStatus() {
    try {
      WorkflowManagerClient client = getContextClient();
      WorkflowManagerStatus status = new WorkflowManagerStatus();
      status.setServerUp(client.isAlive());
      status.setUrl(client.getWorkflowManagerUrl().toString());
      return status;
    } catch (WorkflowException e) {
      logger.error("Error occurred when getting WM client", e);
      throw new InternalServerErrorException("Unable to get WM client");
    }
  }

  /**
   * returns all registered events
   *
   * @return events
   */
  @GET
  @Path("events")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public WorkflowEventListResource getRegisteredEvents() {
    try {
      WorkflowManagerClient wmclient = getContextClient();
      List events = wmclient.getRegisteredEvents();
      WorkflowEventListResource eventResource = new WorkflowEventListResource(events);
      return eventResource;
    } catch (Exception e) {
      logger.error("Error occurred when getting WM client", e);
      throw new InternalServerErrorException("Unable to get WM client");
    } 
  }

  /**
   * trigger workflow by event
   *
   * @return isOk
   */
  @POST
  @Path("event")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Response handleEvent(
          @QueryParam("eventName") String eventName
  ) throws Exception {
    try {
      WorkflowManagerClient wmclient = getContextClient();
      wmclient.sendEvent(eventName, new Metadata());
      return Response.ok(true,MediaType.TEXT_PLAIN).build();
    } catch (WorkflowException e) {
      logger.error("Error occurred when getting WM client", e);
      throw new InternalServerErrorException("Unable to get WM client");
    }
  }

  /**
   * Gets an HTTP response that represents a {@link WorkflowInstance} from the workflow manager.
   *
   * @param workflowInstId the ID of the workflow Instance
   * @return an HTTP response that represents a {@link WorkflowInstance} from the workflow manager
   */
  @GET
  @Path("instance/{ID}")
  @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
  public WorkflowInstanceResource getWorkflowInstanceById(@PathParam("ID") String workflowInstId)
      throws WebApplicationException {
    try {
      WorkflowManagerClient wmclient = getContextClient();
      WorkflowInstance workflowInstanceById = wmclient.getWorkflowInstanceById(workflowInstId);
      WorkflowInstanceResource workflowResource =
          new WorkflowInstanceResource(workflowInstanceById);
      logger.debug("WorkFlowInstance ID : {}" + workflowInstId);
      return workflowResource;
    } catch (Exception e) {
      throw new NotFoundException(e.getMessage());
    }
  }

  /**
   * Gets an HTTP response that represents a {@link WorkflowInstancePage} from the workflow manager.
   * Gives the First Page of WorkFlow Instances
   *
   * @return an HTTP response that represents a {@link WorkflowInstancePage} from the workflow
   *     manager
   */
  @GET
  @Path("firstpage")
  @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
  public WorkflowInstancePageResource getWorkflowInstancesAtFirstPage()
      throws WebApplicationException {
    try {
      WorkflowManagerClient wmclient = getContextClient();
      WorkflowInstancePage firstPage = wmclient.getFirstPage();
      int totalWorkflowCount = wmclient.getNumWorkflowInstances();
      WorkflowInstancePageResource firstPageResource = new WorkflowInstancePageResource(firstPage,totalWorkflowCount);
      return firstPageResource;
    } catch (Exception e) {
      throw new NotFoundException(e.getMessage());
    }
  }

  /**
   * Gets an HTTP response that represents a {@link WorkflowInstancePage} from the workflow manager.
   * Gives the specified Page of WorkFlow Instances
   *
   * @return an HTTP response that represents a {@link WorkflowInstancePage} from the workflow
   *     manager
   */
  @GET
  @Path("page")
  @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
  public WorkflowInstancePageResource getWorkflowPage(
    @QueryParam("workflowPage") int pageNo
  ) throws WebApplicationException {
    try {
      WorkflowManagerClient wmclient = getContextClient();
      WorkflowInstancePage workflowPage = wmclient.paginateWorkflowInstances(pageNo);
      int totalWorkflowCount = wmclient.getNumWorkflowInstances();
      WorkflowInstancePageResource workflowPageResource = new WorkflowInstancePageResource(workflowPage,totalWorkflowCount);
      return workflowPageResource;
    } catch (Exception e) {
      throw new NotFoundException(e.getMessage());
    }
  }

  /**
   * This REST API stops a running {@link WorkflowInstance}.
   *
   * @param workflowInstanceId the ID of the workflow Instance
   * @return {@link Response}
   * @throws Exception if there occurred an error while executing the operation
   */
  @POST
  @Path("stop/{ID}")
  @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
  public Response stopWorkflowInstance(@PathParam("ID") String workflowInstanceId)
      throws WebApplicationException {
    try {
      WorkflowManagerClient wmclient = getContextClient();
      boolean workflowStatus = wmclient.stopWorkflowInstance(workflowInstanceId);
      WMRequestStatusResource status =
          new WMRequestStatusResource(
              wmclient.getWorkflowManagerUrl().toString(),
              "Sucessfully Stopped : "
                  + workflowInstanceId
                  + " "
                  + getWorkflowInstanceById(workflowInstanceId).getWorkflowState().getName());
      return Response.status(Status.OK).entity(status).type(MediaType.APPLICATION_JSON).build();
    } catch (Exception e) {
      throw new NotFoundException(e.getMessage());
    }
  }

  /**
   * This REST API pauses a running {@link WorkflowInstance}.
   *
   * @param workflowInstanceId the ID of the workflow Instance
   * @return {@link Response}
   * @throws Exception if there occurred an error while executing the operation
   */
  @POST
  @Path("pause/{ID}")
  @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
  public Response pauseWorkflowInstanc(@PathParam("ID") String workflowInstanceId)
      throws WebApplicationException {
    try {
      WorkflowManagerClient wmclient = getContextClient();
      boolean workflowStatus = wmclient.stopWorkflowInstance(workflowInstanceId);
      WMRequestStatusResource status =
          new WMRequestStatusResource(
              wmclient.getWorkflowManagerUrl().toString(),
              "Sucessfully Paused : "
                  + workflowInstanceId
                  + " "
                  + getWorkflowInstanceById(workflowInstanceId).getWorkflowState().getName());
      return Response.status(Status.OK).entity(status).type(MediaType.APPLICATION_JSON).build();
    } catch (Exception e) {
      throw new NotFoundException(e.getMessage());
    }
  }

  /**
   * This REST API resumes a paused {@link WorkflowInstance}.
   *
   * @param workflowInstanceId the ID of the workflow Instance
   * @return {@link Response}
   * @throws Exception if there occurred an error while executing the operation
   */
  @POST
  @Path("resume/{ID}")
  @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
  public Response resumeWorkflowInstance(@PathParam("ID") String workflowInstanceId)
      throws WebApplicationException {
    try {
      WorkflowManagerClient wmclient = getContextClient();
      boolean workflowStatus = wmclient.stopWorkflowInstance(workflowInstanceId);

      WMRequestStatusResource status =
          new WMRequestStatusResource(
              wmclient.getWorkflowManagerUrl().toString(),
              "Sucessfully resumed : "
                  + workflowInstanceId
                  + " "
                  + getWorkflowInstanceById(workflowInstanceId).getWorkflowState().getName());
      return Response.status(Status.OK).entity(status).type(MediaType.APPLICATION_JSON).build();

    } catch (Exception e) {
      throw new NotFoundException(e.getMessage());
    }
  }

  /**
   * This REST API updates the state of {@link WorkflowInstance}.
   *
   * @param workflowInstanceId the ID of the workflow Instance
   * @param wmInstanceStatus state of the workflowState(etc.FINISHED,Running...)
   * @return {@link Response}
   * @throws Exception if there occurred an error while executing the operation
   */
  @POST
  @Path("updatestatus")
  @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
  public Response resumeWorkflowInstance(
      @QueryParam("workflowInstanceId") String workflowInstanceId,
      @QueryParam("status") String wmInstanceStatus)
      throws WebApplicationException {
    try {
      WorkflowManagerClient wmclient = getContextClient();
      String previousStatus =
          wmclient.getWorkflowInstanceById(workflowInstanceId).getState().getName();
      boolean workflowStatus =
          wmclient.updateWorkflowInstanceStatus(workflowInstanceId, wmInstanceStatus);

      WMRequestStatusResource status =
          new WMRequestStatusResource(
              wmclient.getWorkflowManagerUrl().toString(),
              "Sucessfully Updated Status of workflow : "
                  + workflowInstanceId
                  + " from "
                  + previousStatus
                  + " to "
                  + getWorkflowInstanceById(workflowInstanceId).getWorkflowState().getName());
      return Response.status(Status.OK).entity(status).type(MediaType.APPLICATION_JSON).build();

    } catch (Exception e) {
      throw new NotFoundException(e.getMessage());
    }
  }
}
