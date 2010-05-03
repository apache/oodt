//Copyright (c) 2006, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.workflow.structs;

//OODT imports
import gov.nasa.jpl.oodt.cas.metadata.Metadata;
import gov.nasa.jpl.oodt.cas.workflow.structs.exceptions.WorkflowTaskInstanceException;

/**
 * @author mattmann
 * @version $Revsion$
 * 
 * <p>The part of a WorkflowTask that is responsible
 * for actually doing the work.</p>
 */
public interface WorkflowTaskInstance {

    /**
     * <p>
     * Runs the Task with the specified metadata context.
     * </p>
     * 
     * @param metadata
     *            The TaskContext of metadata that is shared between the tasks.
     *            
     * @param config The static configuration metadata for this task.
     */
    public void run(Metadata metadata, WorkflowTaskConfiguration config) throws WorkflowTaskInstanceException;
}
