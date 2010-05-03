//Copyright (c) 2005, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.workflow.instrepo;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A factory interface for creating {@link WorkflowInstanceRepository} objects.
 * </p>
 * 
 */
public interface WorkflowInstanceRepositoryFactory {

    /**
     * @return An implementation of the 
     * {@link WorkflowInstanceRepository} interface.
     */
    public WorkflowInstanceRepository createInstanceRepository();
}
