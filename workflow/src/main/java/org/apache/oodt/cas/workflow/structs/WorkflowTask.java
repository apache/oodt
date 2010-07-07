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


package org.apache.oodt.cas.workflow.structs;

//JDK imports
import java.util.List;
import java.util.Vector;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A Workflow task, or job, or process.
 * </p>
 * 
 */
public class WorkflowTask {

    /* a unique ID for the task */
    protected String taskId = null;

    /* the name of the task */
    protected String taskName = null;

    /* the static configuration parameters for the task */
    protected WorkflowTaskConfiguration taskConfig = null;

    /* the set of ordered conditions on this particular WorkflowTask */
    protected List conditions = null;

    /* the actual work performing portion of this WorkflowTask */
    protected String taskInstanceClassName = null;

    /* the order for this task */
    protected int order = -1;
    
    /* the list of required metadata fields that need to be passed into this task */
    protected List requiredMetFields = null;

    /**
     * <p>
     * Default Constructor.
     * </p>
     * 
     */
    public WorkflowTask() {
        conditions = new Vector();
        requiredMetFields = new Vector();
        taskConfig = new WorkflowTaskConfiguration();
    }

    /**
     * @param taskId
     *            The unique ID for this WorkflowTask.
     * @param taskName
     *            The display name for this WorkflowTask.
     * @param taskConfig
     *            The static configuration parameters for this WorkflowTask.
     * @param conditions
     *            The List of conditions attached to this WorkflowTask (if any).
     * @param taskInstanceClassName
     *            The instance class name for this WorkflowTask.
     * @param order
     *            The order in which this WorkflowTask is executed within a
     *            Workflow.
     */
    public WorkflowTask(String taskId, String taskName,
            WorkflowTaskConfiguration taskConfig, List conditions,
            String taskInstanceClassName, int order) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.taskConfig = taskConfig;
        this.conditions = conditions;
        this.taskInstanceClassName = taskInstanceClassName;
        this.order = order;
    }

    /**
     * @return Returns the taskConfig.
     */
    public WorkflowTaskConfiguration getTaskConfig() {
        return taskConfig;
    }

    /**
     * @param taskConfig
     *            The taskConfig to set.
     */
    public void setTaskConfig(WorkflowTaskConfiguration taskConfig) {
        this.taskConfig = taskConfig;
    }

    /**
     * @return Returns the taskId.
     */
    public String getTaskId() {
        return taskId;
    }

    /**
     * @param taskId
     *            The taskId to set.
     */
    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    /**
     * @return Returns the taskInstanceClassName.
     */
    public String getTaskInstanceClassName() {
        return taskInstanceClassName;
    }

    /**
     * @param taskInstanceClassName
     *            The taskInstanceClassName to set.
     */
    public void setTaskInstanceClassName(String taskInstanceClassName) {
        this.taskInstanceClassName = taskInstanceClassName;
    }

    /**
     * @return Returns the taskName.
     */
    public String getTaskName() {
        return taskName;
    }

    /**
     * @param taskName
     *            The taskName to set.
     */
    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    /**
     * 
     * @return A {@link List} of {@link WorkflowCondition}s associated with
     *         this task.
     */
    public List getConditions() {
        return conditions;
    }

    /**
     * <p>
     * Sets the {@link List} of {@link WorkflowCondition}s associated with this
     * task.
     * 
     * @param conditions
     *            The condition {@link List}.
     */
    public void setConditions(List conditions) {
        this.conditions = conditions;
    }

    /**
     * @return Returns the order.
     */
    public int getOrder() {
        return order;
    }

    /**
     * @param order
     *            The order to set.
     */
    public void setOrder(int order) {
        this.order = order;
    }

    /**
     * @return the requiredMetFields
     */
    public List getRequiredMetFields() {
        return requiredMetFields;
    }

    /**
     * @param requiredMetFields the requiredMetFields to set
     */
    public void setRequiredMetFields(List requiredMetFields) {
        this.requiredMetFields = requiredMetFields;
    }

}
