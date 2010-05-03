//Copyright (c) 2006, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.workflow.instrepo;

/**
 * @author mattmann
 * @version $Revsion$
 * 
 * <p>
 * A Factory for creating {@link MemoryWorkflowInstanceRepository}s.
 * </p>
 */
public class MemoryWorkflowInstanceRepositoryFactory implements
        WorkflowInstanceRepositoryFactory {
    
    private int pageSize = -1;

    /**
     * <p>
     * Default Constructor
     * </p>
     */
    public MemoryWorkflowInstanceRepositoryFactory() {
        pageSize = Integer.getInteger(
                "gov.nasa.jpl.oodt.cas.workflow.instanceRep.pageSize", 20)
                .intValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.workflow.instrepo.WorkflowInstanceRepositoryFactory#createInstanceRepository()
     */
    public WorkflowInstanceRepository createInstanceRepository() {
        return new MemoryWorkflowInstanceRepository(pageSize);
    }

}
