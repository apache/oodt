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
package org.apache.oodt.cas.workflow.engine;

//JDK imports
import java.net.URL;

//OODT imports
import org.apache.oodt.cas.workflow.instrepo.WorkflowInstanceRepository;
import org.apache.oodt.cas.workflow.structs.exceptions.EngineException;
import org.apache.oodt.cas.workflow.structs.Workflow;
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;
import org.apache.oodt.cas.metadata.Metadata;

/**
 * The engine that executes and monitors {@link WorkflowInstance}s, which are
 * the physical executing representation of the abtract {@link Workflow}s
 * provided.
 *
 * @author mattmann (Chris Mattmann)
 */
public interface WorkflowEngine {

    String X_POINT_ID = WorkflowEngine.class.getName();


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
    WorkflowInstance startWorkflow(Workflow workflow, Metadata metadata)
            throws EngineException;

    /**
     * Stops the {@link WorkflowInstance} identified by the given
     * <code>workflowInstId</code>.
     *
     * @param workflowInstId
     *            The identifier of the {@link WorkflowInstance} to stop.
     */
    void stopWorkflow(String workflowInstId);

    /**
     * <p>
     * Pauses the {@link WorkflowInstance} specified by its
     * <code>workflowInstId</code>.
     * </p>
     *
     * @param workflowInstId
     *            The ID of the Workflow Instance to pause.
     */
    void pauseWorkflowInstance(String workflowInstId);

    /**
     * <p>
     * Resumes Execution of the specified {@link WorkflowInstance} identified by
     * its <code>workflowInstId</code>.
     * </p>
     *
     * @param workflowInstId
     *            The ID of the {@link WorkflowInstance} to resume.
     */
    void resumeWorkflowInstance(String workflowInstId);

    /**
     * Gets the {@link WorkflowInstanceRepository} used by this
     * {@link WorkflowEngine}.
     *
     * @return The {@link WorkflowInstanceRepository} used by this
     *         {@link WorkflowEngine}.
     */
    WorkflowInstanceRepository getInstanceRepository();

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
    boolean updateMetadata(String workflowInstId, Metadata met);

    /**
     * Sets a pointer to the Workflow Manager that this {@link WorkflowEngine}
     * belongs to.
     *
     * @param url
     *            The {@link URL} pointer to the Workflow Manager that this
     *            {@link WorkflowEngine} belongs to.
     */
    void setWorkflowManagerUrl(URL url);

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
    double getWallClockMinutes(String workflowInstId);

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
    double getCurrentTaskWallClockMinutes(String workflowInstId);

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
    Metadata getWorkflowInstanceMetadata(String workflowInstId);

}
