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
import org.apache.oodt.cas.workflow.util.DbStructFactory;
import org.apache.oodt.cas.workflow.structs.Workflow;
import org.apache.oodt.cas.workflow.structs.WorkflowConditionConfiguration;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration;
import org.apache.oodt.cas.workflow.structs.WorkflowCondition;
import org.apache.oodt.cas.workflow.structs.exceptions.RepositoryException;

//JDK imports
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A {@link DataSource}-based implementation of a workflow repository.
 * </p>
 * 
 */
public class DataSourceWorkflowRepository implements WorkflowRepository {

    /* our data source */
    private DataSource dataSource = null;

    /* our log stream */
    private Logger LOG = Logger.getLogger(DataSourceWorkflowRepository.class
            .getName());

    /**
     * <p>
     * Default Constructor
     * </p>.
     */
    public DataSourceWorkflowRepository(DataSource ds) {
        dataSource = ds;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.repository.WorkflowRepository#getWorkflowByName(java.lang.String)
     */
    public Workflow getWorkflowByName(String workflowName)
            throws RepositoryException {
        return getWorkflowByName(workflowName, true);
    }

    public Workflow getWorkflowByName(String workflowName, boolean getTasks)
            throws RepositoryException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        Workflow workflow = null;

        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement();

            String getWorkflowSql = "SELECT * from workflows WHERE workflow_name = '"
                    + workflowName + "'";

            LOG.log(Level.FINE, "getWorkflowByName: Executing: "
                    + getWorkflowSql);
            rs = statement.executeQuery(getWorkflowSql);

            while (rs.next()) {
                workflow = DbStructFactory.getWorkflow(rs);

                if (getTasks) {
                    workflow.setTasks(getTasksByWorkflowId(workflow.getId()));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.WARNING, "Exception getting workflow. Message: "
                    + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback getWorkflowByName transaction. Message: "
                                + e2.getMessage());
            }
            throw new RepositoryException(e.getMessage());
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }

                rs = null;
            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

                statement = null;
            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

                conn = null;
            }
        }

        return workflow;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.repository.WorkflowRepository#getWorkflowById(java.lang.String)
     */
    public Workflow getWorkflowById(String workflowId)
            throws RepositoryException {
        return getWorkflowById(workflowId, true);
    }

    public Workflow getWorkflowById(String workflowId, boolean getTasks)
            throws RepositoryException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        Workflow workflow = null;

        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement();

            String getWorkflowSql = "SELECT * from workflows WHERE workflow_id = '"
                    + workflowId + "'";

            LOG
                    .log(Level.FINE, "getWorkflowById: Executing: "
                            + getWorkflowSql);
            rs = statement.executeQuery(getWorkflowSql);

            while (rs.next()) {
                workflow = DbStructFactory.getWorkflow(rs);

                if (getTasks) {
                    workflow.setTasks(getTasksByWorkflowId(workflow.getId()));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.WARNING, "Exception getting workflow. Message: "
                    + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback getWorkflowById transaction. Message: "
                                + e2.getMessage());
            }
            throw new RepositoryException(e.getMessage());
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }

                rs = null;
            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

                statement = null;
            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

                conn = null;
            }
        }

        return workflow;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.repository.WorkflowRepository#getWorkflows()
     */
    public List getWorkflows() throws RepositoryException {
        return getWorkflows(true);
    }

    public List getWorkflows(boolean getTasks) throws RepositoryException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        List workflows = null;

        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement();

            String getWorkflowSql = "SELECT * from workflows";

            LOG.log(Level.FINE, "getWorkflows: Executing: " + getWorkflowSql);
            rs = statement.executeQuery(getWorkflowSql);
            workflows = new Vector();

            while (rs.next()) {
                Workflow workflow = DbStructFactory.getWorkflow(rs);

                if (getTasks) {
                    workflow.setTasks(getTasksByWorkflowId(workflow.getId()));
                }

                workflows.add(workflow);
            }

            if (workflows.size() == 0) {
                workflows = null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.WARNING, "Exception getting workflows. Message: "
                    + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback getWorkflows transaction. Message: "
                                + e2.getMessage());
            }
            throw new RepositoryException(e.getMessage());
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }

                rs = null;
            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

                statement = null;
            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

                conn = null;
            }
        }

        return workflows;
    }

    public List getTasksByWorkflowId(String workflowId)
            throws RepositoryException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        List tasks = null;

        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement();

            String getTasksSql = "SELECT workflow_tasks.*, workflow_task_map.task_order "
                    + "FROM workflow_tasks, workflow_task_map "
                    + "WHERE workflow_task_map.workflow_task_id = workflow_tasks.workflow_task_id "
                    + "AND workflow_task_map.workflow_id = "
                    + workflowId
                    + " "
                    + "ORDER BY workflow_task_map.task_order";

            LOG.log(Level.FINE, "getTasksByWorkflowId: Executing: "
                    + getTasksSql);
            rs = statement.executeQuery(getTasksSql);
            tasks = new Vector();

            while (rs.next()) {
                // get an instance of the class name

                WorkflowTask task = DbStructFactory.getWorkflowTask(rs, true);

                if (task != null) {
                    task.setConditions(getConditionsByTaskId(task.getTaskId()));
                    task.setTaskConfig(getConfigurationByTaskId(task
                            .getTaskId()));
                    tasks.add(task);
                }
            }

            if (tasks.size() == 0) {
                tasks = null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.WARNING,
                    "Exception getting tasks for workflow. Message: "
                            + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback getTasksByWorkflowId transaction. Message: "
                                + e2.getMessage());
            }
            throw new RepositoryException(e.getMessage());
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }

                rs = null;
            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

                statement = null;
            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

                conn = null;
            }
        }

        return tasks;
    }

    public List getTasksByWorkflowName(String workflowName)
            throws RepositoryException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        List tasks = null;

        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement();

            String getTasksSql = "SELECT workflow_tasks.*, workflow_task_map.task_order "
                    + "FROM workflow_tasks, workflow_task_map, workflows "
                    + "WHERE workflow_task_map.workflow_task_id = workflow_tasks.workflow_task_id "
                    + "AND workflow_task_map.workflow_id = workflows.workflow_id "
                    + "AND workflows.workflow_name = '"
                    + workflowName
                    + "' "
                    + "ORDER BY workflow_task_map.task_order";

            LOG.log(Level.FINE, "getTasksByWorkflowName: Executing: "
                    + getTasksSql);
            rs = statement.executeQuery(getTasksSql);
            tasks = new Vector();

            while (rs.next()) {
                // get an instance of the class name
                WorkflowTask task = DbStructFactory.getWorkflowTask(rs, true);

                if (task != null) {
                    task.setConditions(getConditionsByTaskId(task.getTaskId()));
                    task.setTaskConfig(getConfigurationByTaskId(task
                            .getTaskId()));
                    tasks.add(task);
                }
            }

            if (tasks.size() == 0) {
                tasks = null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.WARNING,
                    "Exception getting tasks for workflow. Message: "
                            + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback getTasksByWorkflowName transaction. Message: "
                                + e2.getMessage());
            }
            throw new RepositoryException(e.getMessage());
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }

                rs = null;
            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

                statement = null;
            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

                conn = null;
            }
        }

        return tasks;
    }

    public List getWorkflowsForEvent(String eventName)
            throws RepositoryException {
        return getWorkflowsForEvent(eventName, true);
    }

    public List getWorkflowsForEvent(String eventName, boolean getTasks)
            throws RepositoryException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        List workflows = null;

        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement();

            String getWorkflowSql = "SELECT * from workflows, event_workflow_map WHERE event_workflow_map.workflow_id = workflows.workflow_id  "
                    + "AND event_workflow_map.event_name = '" + eventName + "'";

            LOG.log(Level.FINE, "getWorkflowsForEvent: Executing: "
                    + getWorkflowSql);
            rs = statement.executeQuery(getWorkflowSql);
            workflows = new Vector();

            while (rs.next()) {
                Workflow workflow = DbStructFactory.getWorkflow(rs);

                if (getTasks) {
                    workflow.setTasks(getTasksByWorkflowId(workflow.getId()));
                }
                workflows.add(workflow);
            }

            if (workflows.size() == 0) {
                workflows = null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.WARNING,
                    "Exception getting workflows for event. Message: "
                            + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback getWorkflowsForEvent transaction. Message: "
                                + e2.getMessage());
            }
            throw new RepositoryException(e.getMessage());
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }

                rs = null;
            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

                statement = null;
            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

                conn = null;
            }
        }

        return workflows;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.repository.WorkflowRepository#getConditionsByTaskName(java.lang.String)
     */
    public List getConditionsByTaskName(String taskName)
            throws RepositoryException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        List conditions = null;

        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement();

            String getConditionsSql = "SELECT workflow_conditions.*, workflow_condition_map.condition_order "
                    + "FROM workflow_conditions, workflow_condition_map, workflow_tasks "
                    + "WHERE workflow_condition_map.workflow_condition_id = workflow_conditions.workflow_condition_id "
                    + "AND workflow_condition_map.workflow_task_id = workflow_tasks.workflow_task_id "
                    + "AND workflow_tasks.workflow_task_name = '"
                    + taskName
                    + "' " + "ORDER BY workflow_condition_map.condition_order";

            LOG.log(Level.FINE, "getConditionsByTaskName: Executing: "
                    + getConditionsSql);
            rs = statement.executeQuery(getConditionsSql);
            conditions = new Vector();

            while (rs.next()) {
                // get an instance of the class name
                WorkflowCondition condition = DbStructFactory
                        .getWorkflowCondition(rs, true);
                conditions.add(condition);
            }

            if (conditions.size() == 0) {
                conditions = null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.WARNING,
                    "Exception getting conditions for task. Message: "
                            + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback getConditionsByTaskName transaction. Message: "
                                + e2.getMessage());
            }
            throw new RepositoryException(e.getMessage());
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }

                rs = null;
            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

                statement = null;
            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

                conn = null;
            }
        }

        return conditions;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.repository.WorkflowRepository#getConditionsByTaskId(java.lang.String)
     */
    public List getConditionsByTaskId(String taskId) throws RepositoryException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        List conditions = null;

        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement();

            String getConditionsSql = "SELECT workflow_conditions.*, workflow_condition_map.condition_order "
                    + "FROM workflow_conditions, workflow_condition_map "
                    + "WHERE workflow_condition_map.workflow_condition_id = workflow_conditions.workflow_condition_id "
                    + "AND workflow_condition_map.workflow_task_id = "
                    + taskId
                    + " " + "ORDER BY workflow_condition_map.condition_order";

            LOG.log(Level.FINE, "getConditionsByTaskId: Executing: "
                    + getConditionsSql);
            rs = statement.executeQuery(getConditionsSql);
            conditions = new Vector();

            while (rs.next()) {
                // get an instance of the class name
                WorkflowCondition condition = DbStructFactory
                        .getWorkflowCondition(rs, true);
                conditions.add(condition);
            }

            if (conditions.size() == 0) {
                conditions = null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.WARNING,
                    "Exception getting conditions for task. Message: "
                            + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback getConditionsByTaskId transaction. Message: "
                                + e2.getMessage());
            }
            throw new RepositoryException(e.getMessage());
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }

                rs = null;
            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

                statement = null;
            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

                conn = null;
            }
        }

        return conditions;
    }

    public WorkflowTaskConfiguration getConfigurationByTaskId(String taskId)
            throws RepositoryException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        WorkflowTaskConfiguration config = null;

        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement();

            String getConfigurationSql = "SELECT * from workflow_task_configuration WHERE workflow_task_id = "
                    + taskId;

            LOG.log(Level.FINE, "getConfigurationByTaskId: Executing: "
                    + getConfigurationSql);
            rs = statement.executeQuery(getConfigurationSql);

            config = new WorkflowTaskConfiguration();
            while (rs.next()) {
                config.getProperties().put(rs.getString("property_name"),
                        rs.getString("property_value"));
            }

            if (config.getProperties().keySet().size() == 0) {
                config = null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.WARNING,
                    "Exception getting task configuration for taskId: "
                            + taskId + " Message: " + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback getConfigurationBytaskId transaction. Message: "
                                + e2.getMessage());
            }
            throw new RepositoryException(e.getMessage());
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }

                rs = null;
            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

                statement = null;
            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

                conn = null;
            }
        }

        return config;
    }

    public WorkflowConditionConfiguration getConfigurationByConditionId(String condId)
            throws RepositoryException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        WorkflowConditionConfiguration config = null;

        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement();

            String getConfigurationSql = "SELECT * from workflow_condition_configuration WHERE workflow_condition_id = "
                    + condId;

            LOG.log(Level.FINE, "getConfigurationByConfigurationId: Executing: "
                    + getConfigurationSql);
            rs = statement.executeQuery(getConfigurationSql);

            config = new WorkflowConditionConfiguration();
            while (rs.next()) {
                config.getProperties().put(rs.getString("property_name"),
                        rs.getString("property_value"));
            }

            if (config.getProperties().keySet().size() == 0) {
                config = null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.WARNING,
                    "Exception getting task configuration for condId: "
                            + condId + " Message: " + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback getConfigurationByConfigurationId transaction. Message: "
                                + e2.getMessage());
            }
            throw new RepositoryException(e.getMessage());
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }

                rs = null;
            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

                statement = null;
            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

                conn = null;
            }
        }

        return config;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.repository.WorkflowRepository#getWorkflowTaskById(java.lang.String)
     */
    public WorkflowTask getWorkflowTaskById(String taskId)
            throws RepositoryException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        WorkflowTask task = null;

        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement();

            String getTaskSql = "SELECT * FROM workflow_tasks WHERE workflow_task_id = "
                    + taskId;

            LOG
                    .log(Level.FINE, "getWorkflowTaskById: Executing: "
                            + getTaskSql);
            rs = statement.executeQuery(getTaskSql);

            while (rs.next()) {
                // get an instance of the class name
                task = DbStructFactory.getWorkflowTask(rs, false);
                task.setConditions(getConditionsByTaskId(task.getTaskId()));
                task.setTaskConfig(getConfigurationByTaskId(task.getTaskId()));
            }

        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.WARNING, "Exception getting task by id. Message: "
                    + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback getWorkflowTaskById transaction. Message: "
                                + e2.getMessage());
            }
            throw new RepositoryException(e.getMessage());
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }

                rs = null;
            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

                statement = null;
            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

                conn = null;
            }
        }

        return task;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.repository.WorkflowRepository#getWorkflowConditionById(java.lang.String)
     */
    public WorkflowCondition getWorkflowConditionById(String conditionId)
            throws RepositoryException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        WorkflowCondition condition = null;

        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement();

            String getConditionsSql = "SELECT * FROM workflow_conditions WHERE workflow_condition_id = "
                    + conditionId;

            LOG.log(Level.FINE, "getWorkflowConditionById: Executing: "
                    + getConditionsSql);
            rs = statement.executeQuery(getConditionsSql);

            while (rs.next()) {
                // get an instance of the class name
                condition = DbStructFactory.getWorkflowCondition(rs, false);
                condition.setCondConfig(getConfigurationByConditionId(conditionId));
            }

        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.WARNING,
                    "Exception getting condition by id. Message: "
                            + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback getWorkflowConditionById transaction. Message: "
                                + e2.getMessage());
            }
            throw new RepositoryException(e.getMessage());
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }

                rs = null;
            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

                statement = null;
            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

                conn = null;
            }
        }

        return condition;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.repository.WorkflowRepository#getRegisteredEvents()
     */
    public List getRegisteredEvents() throws RepositoryException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        List events = null;

        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement();

            String getEventSql = "SELECT DISTINCT event_name FROM event_workflow_map ORDER BY event_name ASC";

            LOG.log(Level.FINE, "getRegisteredEvents: Executing: "
                    + getEventSql);
            rs = statement.executeQuery(getEventSql);
            events = new Vector();

            while (rs.next()) {
                // get an instance of the class name
                String event = DbStructFactory.getEvent(rs);
                events.add(event);
            }

            if (events.size() == 0) {
                events = null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.WARNING,
                    "Exception getting registered events. Message: "
                            + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback getRegisteredEvents transaction. Message: "
                                + e2.getMessage());
            }
            throw new RepositoryException(e.getMessage());
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }

                rs = null;
            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

                statement = null;
            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

                conn = null;
            }
        }

        return events;
    }

}
