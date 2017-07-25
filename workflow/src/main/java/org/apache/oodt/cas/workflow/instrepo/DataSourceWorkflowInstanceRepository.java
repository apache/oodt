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

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;
import org.apache.oodt.cas.workflow.structs.exceptions.InstanceRepositoryException;
import org.apache.oodt.cas.workflow.util.DbStructFactory;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

//JDK imports

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A {@link WorkflowInstanceRepository} that persists {@link WorkflowInstance}s
 * to a JDBC-accessible DBMS.
 * </p>.
 */
public class DataSourceWorkflowInstanceRepository extends
        AbstractPaginatibleInstanceRepository {

    /* our data source */
    private DataSource dataSource = null;

    /* our log stream */
    private static final Logger LOG = Logger
            .getLogger(DataSourceWorkflowInstanceRepository.class.getName());

    /* should we quote fields or not */
    private boolean quoteFields = false;

    public DataSourceWorkflowInstanceRepository(DataSource ds,
            boolean quoteFields, int pageSize) {
        this.dataSource = ds;
        this.quoteFields = quoteFields;
        this.pageSize = pageSize;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.engine.WorkflowInstanceRepository#addWorkflowInstance(org.apache.oodt.cas.workflow.structs.WorkflowInstance)
     */
    public synchronized void addWorkflowInstance(WorkflowInstance wInst)
            throws InstanceRepositoryException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            statement = conn.createStatement();

            String startWorkflowSql;
            String taskIdField;
            String workflowIdField;

            if (quoteFields) {
                taskIdField = "'"
                        + wInst.getWorkflow().getTasks().get(0)
                                .getTaskId() + "'";
                workflowIdField = "'" + wInst.getWorkflow().getId() + "'";
            } else {
                taskIdField = wInst.getWorkflow().getTasks()
                        .get(0).getTaskId();
                workflowIdField = wInst.getWorkflow().getId();
            }

            startWorkflowSql = "INSERT INTO workflow_instances "
                    + "(workflow_instance_status, workflow_id, current_task_id,"
                    + "start_date_time, end_date_time, current_task_start_date_time,"
                    + "current_task_end_date_time, priority, times_blocked) " + "VALUES ('"
                    + wInst.getStatus() + "', " + workflowIdField + ","
                    + taskIdField + ", '" + wInst.getStartDateTimeIsoStr()
                    + "','" + wInst.getEndDateTimeIsoStr() + "','"
                    + wInst.getCurrentTaskStartDateTimeIsoStr() + "','"
                    + wInst.getCurrentTaskEndDateTimeIsoStr() + "', "+wInst.getPriority().getValue()+", "
                    + wInst.getTimesBlocked() + ")";

            LOG.log(Level.FINE, "sql: Executing: " + startWorkflowSql);
            statement.execute(startWorkflowSql);

            String workflowInstId = "";

            synchronized (workflowInstId) {
                String getWorkflowInstIdSql = "SELECT MAX(workflow_instance_id) "
                        + "AS max_id FROM workflow_instances";

                rs = statement.executeQuery(getWorkflowInstIdSql);

                while (rs.next()) {
                    workflowInstId = String.valueOf(rs.getInt("max_id"));
                }
            }

            conn.commit();
            wInst.setId(workflowInstId);

            // now add its metadata
            addWorkflowInstanceMetadata(wInst);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            LOG.log(Level.WARNING, "Exception starting workflow. Message: "
                    + e.getMessage());
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback startWorkflow transaction. Message: "
                                + e2.getMessage());
            }
            throw new InstanceRepositoryException(e.getMessage());
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }

            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

            }
        }

    }
    

    @Override
    public synchronized boolean clearWorkflowInstances() throws InstanceRepositoryException {
      Connection conn = null;
      Statement statement = null;

      try {
          conn = dataSource.getConnection();
          conn.setAutoCommit(false);
          statement = conn.createStatement();
          
          String deleteSql = "DELETE FROM workflow_instances";
          
          LOG.log(Level.FINE, "deleteSql: Executing: "
                  + deleteSql);
          statement.execute(deleteSql);
          conn.commit();

      } catch (Exception e) {
          LOG.log(Level.SEVERE, e.getMessage());
          LOG.log(Level.WARNING,
                  "Exception deleting all workflow instances. Message: "
                          + e.getMessage());
          try {
              if (conn != null) {
                  conn.rollback();
              }
          } catch (SQLException e2) {
              LOG.log(Level.SEVERE,
                      "Unable to rollback delete workflow instances "
                              + "transaction. Message: " + e2.getMessage());
          }
          throw new InstanceRepositoryException(e.getMessage());
      } finally {
          if (statement != null) {
              try {
                  statement.close();
              } catch (SQLException ignore) {
              }

          }

          if (conn != null) {
              try {
                  conn.close();

              } catch (SQLException ignore) {
              }

          }
      }
      return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.engine.WorkflowInstanceRepository#updateWorkflowInstance(org.apache.oodt.cas.workflow.structs.WorkflowInstance)
     */
    public synchronized void updateWorkflowInstance(WorkflowInstance wInst)
            throws InstanceRepositoryException {
        Connection conn = null;
        Statement statement = null;
        String taskIdField, workflowIdField;

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            statement = conn.createStatement();

            if (quoteFields) {
                taskIdField = "'" + wInst.getCurrentTaskId() + "'";
                workflowIdField = "'" + wInst.getWorkflow().getId() + "'";
            } else {
                taskIdField = wInst.getCurrentTaskId();
                workflowIdField = wInst.getWorkflow().getId();
            }

            String updateStatusSql = "UPDATE workflow_instances SET "
                    + "workflow_instance_status='" + wInst.getStatus()
                    + "', current_task_id=" + taskIdField + ", workflow_id = "
                    + workflowIdField + ",start_date_time='"
                    + wInst.getStartDateTimeIsoStr() + "'," + "end_date_time='"
                    + wInst.getEndDateTimeIsoStr()
                    + "',current_task_start_date_time='"
                    + wInst.getCurrentTaskStartDateTimeIsoStr()
                    + "',current_task_end_date_time='"
                    + wInst.getCurrentTaskEndDateTimeIsoStr()
                    + "',priority="
                    + wInst.getPriority().getValue()
                    + ",times_blocked="
                    + wInst.getTimesBlocked()
                    +" WHERE workflow_instance_id = " + wInst.getId();

            LOG.log(Level.FINE, "updateStatusSql: Executing: "
                    + updateStatusSql);
            statement.execute(updateStatusSql);
            conn.commit();

            // now update its metadata
            removeWorkflowInstanceMetadata(wInst.getId());
            addWorkflowInstanceMetadata(wInst);

        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            LOG.log(Level.WARNING,
                    "Exception updating workflow instance. Message: "
                            + e.getMessage());
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback updateWorkflowInstanceStatus "
                                + "transaction. Message: " + e2.getMessage());
            }
            throw new InstanceRepositoryException(e.getMessage());
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.engine.WorkflowInstanceRepository#removeWorkflowInstance(org.apache.oodt.cas.workflow.structs.WorkflowInstance)
     */
    public synchronized void removeWorkflowInstance(WorkflowInstance wInst)
            throws InstanceRepositoryException {
        Connection conn = null;
        Statement statement = null;

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            statement = conn.createStatement();

            String deleteSql = "DELETE FROM workflow_instances "
                    + "WHERE workflow_instance_id = " + wInst.getId();

            LOG.log(Level.FINE, "sql: Executing: " + deleteSql);
            statement.execute(deleteSql);
            conn.commit();

            // now remove its metadata
            removeWorkflowInstanceMetadata(wInst.getId());

        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            LOG.log(Level.WARNING,
                    "Exception removing workflow instance. Message: "
                            + e.getMessage());
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback removeWorkflowInstance "
                                + "transaction. Message: " + e2.getMessage());
            }
            throw new InstanceRepositoryException(e.getMessage());
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.engine.WorkflowInstanceRepository#getWorkflowInstanceById(java.lang.String)
     */
    public WorkflowInstance getWorkflowInstanceById(String workflowInstId)
            throws InstanceRepositoryException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        WorkflowInstance workflowInst = null;

        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement();

            String getWorkflowSql = "SELECT * from workflow_instances "
                    + "WHERE workflow_instance_id = " + workflowInstId;

            LOG.log(Level.FINE, "getWorkflowInstanceById: Executing: "
                    + getWorkflowSql);
            rs = statement.executeQuery(getWorkflowSql);

            while (rs.next()) {
                workflowInst = DbStructFactory.getWorkflowInstance(rs);
                // add its metadata
                workflowInst
                        .setSharedContext(getWorkflowInstanceMetadata(workflowInst
                                .getId()));
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            LOG.log(Level.WARNING,
                    "Exception getting workflow instance. Message: "
                            + e.getMessage());
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback getWorkflowInstanceById "
                                + "transaction. Message: " + e2.getMessage());
            }
            throw new InstanceRepositoryException(e.getMessage());
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }

            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

            }
        }

        return workflowInst;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.engine.WorkflowInstanceRepository#getWorkflowInstances()
     */
    public List getWorkflowInstances() throws InstanceRepositoryException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;
        List workflowInsts = null;

        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement();

            String getWorkflowSql = "SELECT * from workflow_instances "
                    + "ORDER BY workflow_instance_id DESC";

            LOG.log(Level.FINE, "getWorkflowInstances: Executing: "
                    + getWorkflowSql);
            rs = statement.executeQuery(getWorkflowSql);

            workflowInsts = new Vector();
            while (rs.next()) {
                WorkflowInstance workflowInst = DbStructFactory
                        .getWorkflowInstance(rs);
                // add its metadata
                workflowInst
                        .setSharedContext(getWorkflowInstanceMetadata(workflowInst
                                .getId()));
                workflowInsts.add(workflowInst);
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            LOG.log(Level.WARNING,
                    "Exception getting workflow instance. Message: "
                            + e.getMessage());
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback getWorkflowInstances "
                                + "transaction. Message: " + e2.getMessage());
            }
            throw new InstanceRepositoryException(e.getMessage());
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }

            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

            }
        }

        return workflowInsts;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.engine.WorkflowInstanceRepository#getWorkflowInstancesByStatus(java.lang.String)
     */
    public List getWorkflowInstancesByStatus(String status)
            throws InstanceRepositoryException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        List workflowInsts = null;

        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement();

            String getWorkflowSql = "SELECT * from workflow_instances "
                    + "WHERE workflow_instance_status = '" + status
                    + "' ORDER BY workflow_instance_id DESC";

            LOG.log(Level.FINE, "getWorkflowInstancesByStatus: Executing: "
                    + getWorkflowSql);
            rs = statement.executeQuery(getWorkflowSql);

            workflowInsts = new Vector();
            while (rs.next()) {
                WorkflowInstance workflowInst = DbStructFactory
                        .getWorkflowInstance(rs);
                // add its metadata
                workflowInst
                        .setSharedContext(getWorkflowInstanceMetadata(workflowInst
                                .getId()));
                workflowInsts.add(workflowInst);
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            LOG.log(Level.WARNING,
                    "Exception getting workflow instance. Message: "
                            + e.getMessage());
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback getWorkflowInstancesByStatus "
                                + "transaction. Message: " + e2.getMessage());
            }
            throw new InstanceRepositoryException(e.getMessage());
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }

            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

            }
        }

        return workflowInsts;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.instrepo.WorkflowInstanceRepository#getNumWorkflowInstances()
     */
    public int getNumWorkflowInstances() throws InstanceRepositoryException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;
        int numInsts = -1;

        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement();

            String getWorkflowSql = "SELECT COUNT(workflow_instance_id) AS num_insts from workflow_instances";

            LOG.log(Level.FINE, "getNumWorkflowInstances: Executing: "
                    + getWorkflowSql);
            rs = statement.executeQuery(getWorkflowSql);

            while (rs.next()) {
                numInsts = rs.getInt("num_insts");
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            LOG.log(Level.WARNING,
                    "Exception getting num workflow instances. Message: "
                            + e.getMessage());
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback getNumWorkflowInstances "
                                + "transaction. Message: " + e2.getMessage());
            }
            throw new InstanceRepositoryException(e.getMessage());
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }

            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

            }
        }

        return numInsts;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.instrepo.WorkflowInstanceRepository#getNumWorkflowInstancesByStatus(java.lang.String)
     */
    public int getNumWorkflowInstancesByStatus(String status)
            throws InstanceRepositoryException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;
        int numInsts = -1;

        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement();

            String getWorkflowSql = "SELECT COUNT(workflow_instance_id) AS num_insts from workflow_instances "
                    + "WHERE workflow_instance_status = '" + status + "'";

            LOG.log(Level.FINE, "getNumWorkflowInstancesByStatus: Executing: "
                    + getWorkflowSql);
            rs = statement.executeQuery(getWorkflowSql);

            while (rs.next()) {
                numInsts = rs.getInt("num_insts");
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            LOG.log(Level.WARNING,
                    "Exception getting num workflow instances by status. Message: "
                            + e.getMessage());
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback getNumWorkflowInstancesByStatus "
                                + "transaction. Message: " + e2.getMessage());
            }
            throw new InstanceRepositoryException(e.getMessage());
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }

            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

            }
        }

        return numInsts;
    }

    protected List paginateWorkflows(int pageNum, String status)
            throws InstanceRepositoryException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        List wInstIds = null;
        int numResults;

        if (status == null || (status.equals(""))) {
            numResults = getNumWorkflowInstances();
        } else {
            numResults = getNumWorkflowInstancesByStatus(status);
        }

        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);

            String getWorkflowSql = "SELECT workflow_instance_id FROM workflow_instances ";
            if (status != null && !status.equals("")) {
                getWorkflowSql += "WHERE workflow_instance_status = '" + status
                        + "'";

            }

            getWorkflowSql += "ORDER BY workflow_instance_id DESC ";

            LOG.log(Level.FINE, "workflow instance paged query: executing: "
                    + getWorkflowSql);

            rs = statement.executeQuery(getWorkflowSql);
            wInstIds = new Vector();

            int startNum = (pageNum - 1) * pageSize;

            if (startNum > numResults) {
                startNum = 0;
            }

            // must call next first, or else no relative cursor
            if (rs.next()) {
                // grab the first one
                int numGrabbed;
                if(pageNum == 1){
                    numGrabbed = 1;
                    wInstIds.add(rs.getString("workflow_instance_id"));                    
                }
                else{
                    numGrabbed = 0;
                }

                if(pageNum != 1){
                    // now move the cursor to the correct position
                    rs.relative(startNum);                    
                }

                // grab the rest
                while (rs.next() && numGrabbed < pageSize) {
                    String wInstId = rs.getString("workflow_instance_id");
                    wInstIds.add(wInstId);
                    numGrabbed++;
                }
            }

            if (wInstIds.size() == 0) {
                wInstIds = null;
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            LOG.log(Level.WARNING, "Exception performing query. Message: "
                    + e.getMessage());
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback query transaction. Message: "
                                + e2.getMessage());
            }
            throw new InstanceRepositoryException(e.getMessage());
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }

            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

            }
        }

        return wInstIds;
    }

    private Metadata getWorkflowInstanceMetadata(String workflowInstId)
            throws InstanceRepositoryException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        Metadata met = new Metadata();

        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement();

            String getWorkflowSql = "SELECT * from workflow_instance_metadata "
                    + "WHERE workflow_instance_id = " + workflowInstId;

            LOG.log(Level.FINE, "Executing: " + getWorkflowSql);
            rs = statement.executeQuery(getWorkflowSql);

            while (rs.next()) {
                met.addMetadata(rs.getString("workflow_met_key"), URLDecoder.decode(rs
                        .getString("workflow_met_val"), "UTF-8"));
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            LOG.log(Level.WARNING,
                    "Exception getting workflow instance metadata. Message: "
                            + e.getMessage());
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback getWorkflowInstancesMetadata "
                                + "transaction. Message: " + e2.getMessage());
            }
            throw new InstanceRepositoryException(e.getMessage());
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }

            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

            }
        }

        return met;
    }

    private synchronized void addWorkflowInstanceMetadata(WorkflowInstance inst)
            throws InstanceRepositoryException {

        if (inst.getSharedContext() != null
                && inst.getSharedContext().getMap().keySet().size() > 0) {
            for (String key : inst.getSharedContext().getMap().keySet()) {
                List vals = inst.getSharedContext().getAllMetadata(key);
                if (vals != null && vals.size() > 0) {
                    for (Object val1 : vals) {
                        String val = (String) val1;
                        if (val != null && !val.equals("")) {
                            addMetadataValue(inst.getId(), key, val);
                        }
                    }
                }

            }
        }

    }

    private synchronized void addMetadataValue(String wInstId, String key,
            String val) throws InstanceRepositoryException {
        Connection conn = null;
        Statement statement = null;

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            statement = conn.createStatement();
            String addMetSql = "INSERT INTO workflow_instance_metadata"
                    + " (workflow_instance_id,workflow_met_key,workflow_met_val) VALUES ("
                    + wInstId + ",'" + key + "','" + URLEncoder.encode(val, "UTF-8") + "')";

            LOG.log(Level.FINE, "sql: Executing: " + addMetSql);
            statement.execute(addMetSql);

            conn.commit();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            LOG.log(Level.WARNING, "Exception adding metadata [" + key + "=>"
                    + val + "] to workflow inst: [" + wInstId + "]. Message: "
                    + e.getMessage());
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback addMetadataValue transaction. Message: "
                                + e2.getMessage());
            }
            throw new InstanceRepositoryException(e.getMessage());
        } finally {

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

            }
        }

    }

    private synchronized void removeWorkflowInstanceMetadata(
            String workflowInstId) throws InstanceRepositoryException {
        Connection conn = null;
        Statement statement = null;

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            statement = conn.createStatement();

            String deleteSql = "DELETE FROM workflow_instance_metadata "
                    + "WHERE workflow_instance_id = " + workflowInstId;

            LOG.log(Level.FINE, "sql: Executing: " + deleteSql);
            statement.execute(deleteSql);
            conn.commit();

        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            LOG.log(Level.WARNING,
                    "Exception removing workflow instance metadata. Message: "
                            + e.getMessage());
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback removeWorkflowInstanceMetadata "
                                + "transaction. Message: " + e2.getMessage());
            }
            throw new InstanceRepositoryException(e.getMessage());
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

            }
        }
    }

}
