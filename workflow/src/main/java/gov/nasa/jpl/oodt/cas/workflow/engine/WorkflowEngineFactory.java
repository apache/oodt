//Copyright (c) 2005, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.workflow.engine;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A factory interface for creating {@link WorkflowEngine} objects.
 * </p>
 * 
 */
public interface WorkflowEngineFactory {

    /**
     * @return An implementation of the {@link WorkflowEngine} interface.
     */
    public WorkflowEngine createWorkflowEngine();
}
