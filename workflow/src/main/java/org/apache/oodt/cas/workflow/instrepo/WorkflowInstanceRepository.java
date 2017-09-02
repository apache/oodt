/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.oodt.cas.workflow.instrepo;

//JDK imports
import java.util.List;

//OODT imports
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;
import org.apache.oodt.cas.workflow.structs.exceptions.InstanceRepositoryException;
import org.apache.oodt.cas.workflow.util.Pagination;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public interface WorkflowInstanceRepository extends Pagination {

    String X_POINT_ID = WorkflowInstanceRepository.class
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
    void addWorkflowInstance(WorkflowInstance wInst)
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
    void updateWorkflowInstance(WorkflowInstance wInst)
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
    void removeWorkflowInstance(WorkflowInstance wInst)
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
    WorkflowInstance getWorkflowInstanceById(String workflowInstId)
            throws InstanceRepositoryException;

    /**
     * @return A {@link List} of {@link WorkflowInstance}s that this
     *         {@link WorkflowEngine} is managing.
     * @throws InstanceRepositoryException
     *             If any error occurs.
     */
    List getWorkflowInstances() throws InstanceRepositoryException;

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
    List getWorkflowInstancesByStatus(String status)
            throws InstanceRepositoryException;

    /**
     * Gets the number of {@link WorkflowInstances} with any <code>status</code>
     * being managed by this WorkflowInstanceRepository.
     * 
     * @return The number of {@link WorkflowInstances} associated with any
     *         <code>status</code> being managed by this
     *         WorkflowInstanceRepository.
     */
    int getNumWorkflowInstances() throws InstanceRepositoryException;

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
    int getNumWorkflowInstancesByStatus(String status) throws InstanceRepositoryException;
    
    /**
     * Clears the instance repository of all workflows. 
     * @return False if there was any error (logged), and True otherwise.
     * @throws InstanceRepositoryException If there was some IO or other error deleting
     * workflow instances that was unrecoverable from.
     */
    public boolean clearWorkflowInstances() throws InstanceRepositoryException;

}
