//Copyright (c) 2006, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.workflow.instrepo;

//JDK imports
import java.util.List;

//OODT imports
import gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowInstance;
import gov.nasa.jpl.oodt.cas.workflow.structs.exceptions.InstanceRepositoryException;
import gov.nasa.jpl.oodt.cas.workflow.util.Pagination;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public interface WorkflowInstanceRepository extends Pagination {

    public static final String X_POINT_ID = WorkflowInstanceRepository.class
            .getName();

    /**
     * Persists the specified {@link WorkflowInstance} to the instance
     * repository.
     * 
     * @param wInst
     *            The workflow instance to persist.
     * @throws InstanceRepositoryException
     *             If any error occurs.
     */
    public void addWorkflowInstance(WorkflowInstance wInst)
            throws InstanceRepositoryException;

    /**
     * Updates and persists the specified {@link WorkflowInstance} to the
     * instance repository.
     * 
     * @param wInst
     *            The workflow instance to update and persist.
     * @throws InstanceRepositoryException
     *             If any error occurs.
     */
    public void updateWorkflowInstance(WorkflowInstance wInst)
            throws InstanceRepositoryException;

    /**
     * Removes the specified {@link WorkflowInstance} from the instance
     * repository.
     * 
     * @param wInst
     *            The workflow instance to remove.
     * @throws InstanceRepositoryException
     *             If any error occurs.
     */
    public void removeWorkflowInstance(WorkflowInstance wInst)
            throws InstanceRepositoryException;

    /**
     * <p>
     * Returns the {@link WorkflowInstance}s with the specified
     * <code>workflowInstId</code>.
     * </p>
     * 
     * @param workflowInstId
     *            The ID of the {@link WorkflowInstance} to return.
     * @return The specified {@link WorkflowInstance}.
     * @throws InstanceRepositoryException
     *             If any error occurs.
     */
    public WorkflowInstance getWorkflowInstanceById(String workflowInstId)
            throws InstanceRepositoryException;

    /**
     * @return A {@link List} of {@link WorkflowInstance}s that this
     *         {@link WorkflowEngine} is managing.
     * @throws InstanceRepositoryException
     *             If any error occurs.
     */
    public List getWorkflowInstances() throws InstanceRepositoryException;

    /**
     * <p>
     * Returns a {@link List} of {@link WorkflowInstance}s, with the specified
     * <code>status</code> String.
     * </p>
     * 
     * @param status
     *            A string representation of the status of the
     *            {@link WorkflowInstance}.
     * @return A {@link List} of {@link WorkflowInstance}s, with the specified
     *         <code>status</code> String.
     * @throws InstanceRepositoryException
     *             If there is any error that occurs.
     */
    public List getWorkflowInstancesByStatus(String status)
            throws InstanceRepositoryException;

    /**
     * Gets the number of {@link WorkflowInstances} with any <code>status</code>
     * being managed by this WorkflowInstanceRepository.
     * 
     * @return The number of {@link WorkflowInstances} associated with any
     *         <code>status</code> being managed by this
     *         WorkflowInstanceRepository.
     */
    public int getNumWorkflowInstances() throws InstanceRepositoryException;

    /**
     * Gets the number of {@link WorkflowInstances} with the given
     * <code>status</code> being managed by this WorkflowInstanceRepository.
     * 
     * @param status
     *            The status to obtain the number of {@link WorkflowInstance}s
     *            for.
     * @return The number of {@link WorkflowInstance}s with the given
     *         <code>status</code>.
     * @throws InstanceRepositoryException If there is any error that occurs.
     */
    public int getNumWorkflowInstancesByStatus(String status) throws InstanceRepositoryException;

}
