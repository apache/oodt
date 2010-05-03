//Copyright (c) 2005, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.workflow.engine;

//OODT imports
import gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowInstance;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A Threaded interface for processing a {@link WorkflowInstance}. The job of
 * this class is to actually take the WorkflowInstance and execute its jobs. The
 * class should maintain the state of the instance, such as the currentTaskId,
 * and so forth.
 * </p>
 * 
 */
public interface WorkflowProcessorThread extends Runnable {

    /**
     * @return The {@link WorkflowInstance} that this Thread is processing.
     */
    public WorkflowInstance getWorkflowInstance();

    /**
     * <p>
     * Stops once and for all the thread from processing the workflow. This
     * method should not maintain the state of the workflow, it should
     * gracefully shut down the WorkflowProcessorThread and any of its
     * subsequent resources.
     * </p>
     * 
     */
    public void stop();

    /**
     * <p>
     * Resumes execution of a {@link #pause}d {@link WorkflowInstace} by this
     * WorkflowProcessorThread.
     * </p>
     * 
     */
    public void resume();

    /**
     * <p>
     * Pauses exectuion of a {@link WorkflowInstace} being handled by this
     * WorkflowProcessorThread.
     * </p>
     * 
     */
    public void pause();

}
