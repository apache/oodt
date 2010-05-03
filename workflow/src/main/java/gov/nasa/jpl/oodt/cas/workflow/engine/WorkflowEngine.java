//Copyright (c) 2005, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.workflow.engine;

//OODT imports
import java.net.URL;

import gov.nasa.jpl.oodt.cas.workflow.instrepo.WorkflowInstanceRepository;
import gov.nasa.jpl.oodt.cas.workflow.structs.exceptions.EngineException;
import gov.nasa.jpl.oodt.cas.workflow.structs.Workflow;
import gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowInstance;
import gov.nasa.jpl.oodt.cas.metadata.Metadata;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * The engine that executes and monitors {@link WorkflowInstance}s, which are
 * the physical executing representation of the abtract {@link Workflow}s
 * provided.
 * </p>
 * 
 */
public interface WorkflowEngine {

    public static final String X_POINT_ID = WorkflowEngine.class.getName();

    /**
     * <p>
     * Starts the specified {@link Workflow} by creating a
     * {@link WorkflowInstance}, and then running that instance. The started
     * {@link WorkflowInstance} which is returned, will have its status updated
     * by the workflow engine, including its status field, and its
     * currentTaskId.
     * </p>
     * 
     * @param workflow
     *            The abstract representation of the {@link Workflow} to start.
     * @param metadata
     *            Any metadata that needs to be shared between the tasks in the
     *            {@link WorkflowInstance}.
     * @return A {@link  WorkflowInstance} which can be used to monitor the
     *         execution of the {@link Workflow} through the Engine.
     * @throws EngineException
     *             If any error occurs.
     */
    public WorkflowInstance startWorkflow(Workflow workflow, Metadata metadata)
            throws EngineException;

    /**
     * Stops the {@link WorkflowInstance} identified by the given
     * <code>workflowInstId</code>.
     * 
     * @param workflowInstId
     *            The identifier of the {@link WorkflowInstance} to stop.
     */
    public void stopWorkflow(String workflowInstId);

    /**
     * <p>
     * Pauses the {@link WorkflowInstance} specified by its
     * <code>workflowInstId</code>.
     * </p>
     * 
     * @param workflowInstId
     *            The ID of the Workflow Instance to pause.
     */
    public void pauseWorkflowInstance(String workflowInstId);

    /**
     * <p>
     * Resumes Execution of the specified {@link WorkflowInstance} identified by
     * its <code>workflowInstId</code>.
     * </p>
     * 
     * @param workflowInstId
     *            The ID of the {@link WorkflowInstance} to resume.
     */
    public void resumeWorkflowInstance(String workflowInstId);

    /**
     * Gets the {@link WorkflowInstanceRepository} used by this
     * {@link WorkflowEngine}.
     * 
     * @return The {@link WorkflowInstanceRepository} used by this
     *         {@link WorkflowEngine}.
     */
    public WorkflowInstanceRepository getInstanceRepository();

    /**
     * Updates the {@link Metadata} context for the {@link WorkflowInstance}
     * identified by the given <code>workflowInstId</code>
     * 
     * @param workflowInstId
     *            Identifies the {@link WorkflowInstance} whose {@link Metadata}
     *            context will be updated.
     * @param met
     *            The new {@link Metadata} context.
     * @return true if the update was successful, false otherwise.
     */
    public boolean updateMetadata(String workflowInstId, Metadata met);

    /**
     * Sets a pointer to the Workflow Manager that this {@link WorkflowEngine}
     * belongs to.
     * 
     * @param url
     *            The {@link URL} pointer to the Workflow Manager that this
     *            {@link WorkflowEngine} belongs to.
     */
    public void setWorkflowManagerUrl(URL url);

    /**
     * Gets the amount of wall clock minutes that a particular
     * {@link WorkflowInstance} (identified by its <code>workflowInst</code>)
     * has been executing. This includes time spent <code>QUEUED</code>, time
     * spent <code>WAITING</code>, throughout its entire lifecycle.
     * 
     * @param workflowInstId
     *            The identifier of the {@link WorkflowInstance} to measure wall
     *            clock time for.
     * 
     * @return The amount of wall clock minutes that a particular
     *         {@link WorkflowInstance} has been executing for.
     */
    public double getWallClockMinutes(String workflowInstId);

    /**
     * Gets the amount of wall clock minutes that the particular
     * {@link WorkflowTask} within a {@link WorkflowInstance} has been executing
     * for.
     * 
     * @param workflowInstId
     *            The identifier of the {@link WorkflowInstance} to measure wall
     *            clock time for its current {@link WorkflowTask}.
     * @return The amount of wall clock minutes that a particular
     *         {@link WorkflowInstance}'s current {@link WorkflowTask} has been
     *         executing for.
     */
    public double getCurrentTaskWallClockMinutes(String workflowInstId);

    /**
     * Gets the {@link Metadata} associated with the {@link WorkflowInstance}
     * identified by the given identifier.
     * 
     * @param workflowInstId
     *            The identifier of the {@link WorkflowInstance} to obtain the
     *            {@link Metadata} for.
     * @return The {@link Metadata} shared context of the
     *         {@link WorkflowInstance} with the given identifier.
     * 
     */
    public Metadata getWorkflowInstanceMetadata(String workflowInstId);

}
