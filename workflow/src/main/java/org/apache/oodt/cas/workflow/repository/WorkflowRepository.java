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


package org.apache.oodt.cas.workflow.repository;

//OODT imports
import org.apache.oodt.cas.workflow.structs.Workflow;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.oodt.cas.workflow.structs.WorkflowCondition;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration;
import org.apache.oodt.cas.workflow.structs.exceptions.RepositoryException;

//JDK imports
import java.util.List;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A repository interface for obtaining {@link Workflow}s, and managing the
 * information about their {@link WorkflowTask}s.
 * </p>
 * 
 */
public interface WorkflowRepository {

    /**
     * <p>
     * Returns a {@link Workflow} with the given <code>workflowName</code>.
     * </p>
     * 
     * @param workflowName
     *            The name of the {@link Workflow} to obtain.
     * @return A {@link Workflow} with the specified name.
     * @throws RepositoryException
     *             If any error occurs.
     */
    public Workflow getWorkflowByName(String workflowName)
            throws RepositoryException;

    /**
     * <p>
     * Returns a {@link Workflow} with the given <code>workflowId</code>.
     * </p>
     * 
     * @param workflowId
     *            The ID of the {@link Workflow} to obtain.
     * @return A {@link Workflow} with the given ID.
     * @throws RepositoryException
     *             If any error occurs.
     */
    public Workflow getWorkflowById(String workflowId)
            throws RepositoryException;

    /**
     * <p>
     * Gets all {@link Workflow}s from the repository.
     * </p>
     * 
     * @return A {@link List} of all {@link Workflow}s in the repository.
     * @throws RepositoryException
     *             If any error occurs.
     */
    public List getWorkflows() throws RepositoryException;

    /**
     * <p>
     * Returns a {@link List} of {@link WorkflowTask}s associated with the
     * specified {@link Workflow} identified by its <code>workflowId</code>.
     * </p>
     * 
     * @param workflowId
     *            The ID of the {@link Workflow} to obtain the {@link List} of
     *            {@link WorkflowTask}s for.
     * @return A {@link List} of {@link WorkflowTask}s associated with the
     *         specified Workflow.
     * @throws RepositoryException
     *             If any error occurs.
     */
    public List getTasksByWorkflowId(String workflowId)
            throws RepositoryException;

    /**
     * <p>
     * Returns a {@link List} of {@link WorkflowTask}s associated with the
     * specified {@link Workflow} identified by its <code>workflowName</code>.
     * </p>
     * 
     * @param workflowName
     *            The Name of the {@link Workflow} to obtain the {@link List} of
     *            {@link WorkflowTask}s for.
     * @return A {@link List} of {@link WorkflowTask}s associated with the
     *         specified Workflow.
     * @throws RepositoryException
     *             If any error occurs.
     */
    public List getTasksByWorkflowName(String workflowName)
            throws RepositoryException;

    /**
     * <p>
     * Returns a {@link List} of {@link Workflow}s associated with the
     * specified <code>eventName</code>.
     * </p>
     * 
     * @param eventName
     *            The name of the event to search for workflows for.
     *            </p>
     * @return A {@link List} of {@link Workflow}s associated with the
     *         specified event.
     * @throws RepositoryException
     *             If any error occurs.
     */
    public List getWorkflowsForEvent(String eventName)
            throws RepositoryException;
    
    
    /**
	 * <p>Returns an ordered {@link List} of {@link WorkflowCondition}s associated with the given <code>taskName</code>.</p>
	 * @param taskName The taskName to obtain the conditions for.
	 * @return Returns a {@link List} of {@link WorkflowCondition}s associated with the given <code>taskName</code>
	 * @throws RepositoryException If any error occurs.
	 */
	public List getConditionsByTaskName(String taskName)
			throws RepositoryException;

	/**
	 * <p>
	 * Returns an ordered {@link List} of {@link WorkflowCondition}s associated
	 * with the given <code>taskId</code>.
	 * </p>
	 * 
	 * @param taskId
	 *            The ID of the task to obtain the conditions for.
	 * @return Returns a {@link List} of {@link WorkflowCondition}s associated
	 *         with the given <code>taskId</code>
	 * @throws RepositoryException
	 *             If any error occurs.
	 */
	public List getConditionsByTaskId(String taskId) throws RepositoryException;

	/**
	 * <p>
	 * Gets the {@link WorkflowTaskConfiguration} metadata for the
	 * {@link WorkflowTask} with the given <code>taskId</code>.
	 * </p>
	 * 
	 * @param taskId
	 *            The ID of the {@link WorkflowTask} to get the
	 *            {@link WorkflowTaskConfiguration} metadata for.
	 * @return The {@link WorkflowTaskConfiguration} metadata for the
	 *         {@link WorkflowTask} with the given <code>taskId</code>.
	 * @throws RepositoryException
	 *             If any error occurs.
	 */
	public WorkflowTaskConfiguration getConfigurationByTaskId(String taskId)
			throws RepositoryException;
	
	/**
	 * <p>Returns the {@link WorkflowTask} with the given <code>taskId</code>.</p>
	 * 
	 * @param taskId The ID of the WorkflowTask to return.
	 * @return the {@link WorkflowTask} with the given <code>taskId</code>.
	 * @throws RepositoryException If any error occurs.
	 */
	public WorkflowTask getWorkflowTaskById(String taskId)
			throws RepositoryException;

	/**
	 * <p>Returns the {@link WorkflowCondition} with the given <code>conditionId</code>.</p>
	 * 
	 * @param conditionId The ID of the WorkflowCondition to return.
	 * @return the {@link WorkflowCondition} with the given <code>conditionId</code>.
	 * @throws RepositoryException If any error occurs.
	 */
	public WorkflowCondition getWorkflowConditionById(String conditionId)
			throws RepositoryException;
	
	
	/**
	 * <p>
	 * Gets a {@link List} of <code>Event</code>s that are registered in the
	 * Workflow Manager as having valid {@link Workflow}s mapped to their
	 * names.
	 * </p>
	 * 
	 * @return a {@link List} of <code>Event</code>s that are registered in
	 *         the Workflow Manager as having valid {@link Workflow}s mapped to
	 *         their names.
	 * @throws RepositoryException
	 *             If any error occurs.
	 */
	public List getRegisteredEvents() throws RepositoryException;

}
