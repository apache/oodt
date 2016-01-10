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


package org.apache.oodt.cas.workflow.util;

//OODT imports
import org.apache.oodt.cas.workflow.structs.Priority;
import org.apache.oodt.cas.workflow.structs.Workflow;
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.oodt.cas.workflow.structs.WorkflowCondition;

//JDK imports
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A Factory for creating Workflow Manager objects from {@link ResultSet}s.
 * </p>
 * 
 */
public final class DbStructFactory {

    /* our log stream */
    private static Logger LOG = Logger.getLogger(DbStructFactory.class
            .getName());

    private DbStructFactory() throws InstantiationException {
        throw new InstantiationException("Don't construct DbStructFactories!");
    }

    public static String getEvent(ResultSet rs) throws SQLException {
        return rs.getString("event_name");
    }

    public static WorkflowInstance getWorkflowInstance(ResultSet rs)
            throws SQLException {
        WorkflowInstance workflowInst = new WorkflowInstance();
        workflowInst.setTimesBlocked(rs.getInt("times_blocked"));
        workflowInst.setStatus(rs.getString("workflow_instance_status"));
        workflowInst.setId(rs.getString("workflow_instance_id"));
        workflowInst.setCurrentTaskId(rs.getString("current_task_id"));
        workflowInst.setStartDateTimeIsoStr(rs.getString("start_date_time"));
        workflowInst.setEndDateTimeIsoStr(rs.getString("end_date_time"));
        workflowInst.setCurrentTaskStartDateTimeIsoStr(rs
                .getString("current_task_start_date_time"));
        workflowInst.setCurrentTaskEndDateTimeIsoStr(rs
                .getString("current_task_end_date_time"));
        workflowInst.setPriority(Priority.getPriority(rs.getDouble("priority")));
        Workflow workflow = new Workflow();
        workflow.setId(rs.getString("workflow_id"));
        workflowInst.setWorkflow(workflow);
        return workflowInst;
    }

    public static Workflow getWorkflow(ResultSet rs) throws SQLException {
        Workflow workflow = new Workflow();
        workflow.setName(rs.getString("workflow_name"));
        workflow.setId(String.valueOf(rs.getInt("workflow_id")));

        return workflow;
    }

    public static WorkflowTask getWorkflowTask(ResultSet rs, boolean setOrder)
            throws SQLException {
        String taskClassName = rs.getString("workflow_task_class");

        if (taskClassName != null) {
            WorkflowTask task = new WorkflowTask();
            task.setTaskInstanceClassName(taskClassName);
            task.setTaskId(String.valueOf(rs.getInt("workflow_task_id")));
            task.setTaskName(rs.getString("workflow_task_name"));
            return task;
        } else {
            return null;
        }
    }

    public static WorkflowCondition getWorkflowCondition(ResultSet rs,
            boolean setOrder) throws SQLException {

        String conditionClassName = rs.getString("workflow_condition_class");

        if (conditionClassName != null) {
            WorkflowCondition condition = new WorkflowCondition();
            condition.setConditionInstanceClassName(conditionClassName);
            condition.setConditionId(String.valueOf(rs
                    .getInt("workflow_condition_id")));
            condition.setConditionName(rs.getString("workflow_condition_name"));
            condition.setTimeoutSeconds(rs.getLong("workflow_condition_timeout"));
            condition.setOptional(rs.getBoolean("workflow_condition_optional"));
            return condition;
        } else {
            return null;
        }
    }

}
