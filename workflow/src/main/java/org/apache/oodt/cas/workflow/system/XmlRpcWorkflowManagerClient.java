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


package org.apache.oodt.cas.workflow.system;


import java.util.Collections;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.structs.Workflow;
import org.apache.oodt.cas.workflow.structs.WorkflowCondition;
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;
import org.apache.oodt.cas.workflow.structs.WorkflowInstancePage;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.oodt.cas.workflow.structs.exceptions.RepositoryException;
import org.apache.oodt.cas.workflow.util.XmlRpcStructFactory;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author mattmann
 * @version $Revision$
 * @deprecated replaced by avro-rpc
 * <p>
 * The XML RPC based workflow manager client.
 * </p>
 *
 */
@Deprecated
public class XmlRpcWorkflowManagerClient implements WorkflowManagerClient {

    /* our xml rpc client */
    private XmlRpcClient client = null;

    /* our log stream */
    private static Logger LOG = Logger
            .getLogger(XmlRpcWorkflowManagerClient.class.getName());

    /* workflow manager url */
    private URL workflowManagerUrl = null;

    /**
     * <p>
     * Constructs a new XmlRpcWorkflowManagerClient with the given
     * <code>url</code>.
     * </p>
     *
     * @param url
     *            The url pointer to the xml rpc workflow manager service.
     */
    public XmlRpcWorkflowManagerClient(URL url) {
        client = new XmlRpcClient(url);
        workflowManagerUrl = url;
    }

    @Override
    public boolean refreshRepository()
        throws XmlRpcException, IOException {
            return (Boolean) client.execute(
                "workflowmgr.refreshRepository", new Vector());

    }

  @Override
  public String executeDynamicWorkflow(List<String> taskIds, Metadata metadata)
      throws XmlRpcException, IOException {
    Vector argList = new Vector();
    Vector<String> taskIdVector = new Vector<String>();
    taskIdVector.addAll(taskIds);
    String instId;

    argList.add(taskIdVector);
    argList.add(metadata.getHashTable());

      instId = (String) client.execute("workflowmgr.executeDynamicWorkflow",
          argList);


    return instId;

  }

    public List getRegisteredEvents() throws XmlRpcException, IOException, RepositoryException {
        Vector argList = new Vector();

            return (List) client.execute("workflowmgr.getRegisteredEvents",
                    argList);

    }

    public WorkflowInstancePage getFirstPage() throws XmlRpcException, IOException {
        Vector argList = new Vector();
        Map pageHash;

            pageHash = (Map) client.execute("workflowmgr.getFirstPage",
                    argList);

        return XmlRpcStructFactory.getWorkflowInstancePageFromXmlRpc(pageHash);
    }

    @Override
    public WorkflowInstancePage getNextPage(WorkflowInstancePage currentPage)
        throws XmlRpcException, IOException {
        Vector argList = new Vector();
        argList.add(XmlRpcStructFactory
                .getXmlRpcWorkflowInstancePage(currentPage));
        Map pageHash;

            pageHash = (Map) client.execute("workflowmgr.getNextPage",
                    argList);


        return XmlRpcStructFactory.getWorkflowInstancePageFromXmlRpc(pageHash);
    }

    @Override
    public WorkflowInstancePage getPrevPage(WorkflowInstancePage currentPage)
            throws Exception {
        Vector argList = new Vector();
        argList.add(XmlRpcStructFactory
                .getXmlRpcWorkflowInstancePage(currentPage));
        Map pageHash;

        try {
            pageHash = (Map) client.execute("workflowmgr.getPrevPage",
                    argList);
        } catch (XmlRpcException e) {
            LOG.log(Level.SEVERE, e.getMessage());
            throw new Exception(e.getMessage());
        } catch (IOException e) {
            throw new Exception(e.getMessage());
        }

        return XmlRpcStructFactory.getWorkflowInstancePageFromXmlRpc(pageHash);
    }

    public WorkflowInstancePage getLastPage() throws XmlRpcException, IOException {
        Vector argList = new Vector();
        Map pageHash;

            pageHash = (Map) client.execute("workflowmgr.getLastPage",
                    argList);


        return XmlRpcStructFactory.getWorkflowInstancePageFromXmlRpc(pageHash);
    }

    @Override
    public WorkflowInstancePage paginateWorkflowInstances(int pageNum,
            String status) throws XmlRpcException, IOException {
        Vector argList = new Vector();
        argList.add(pageNum);
        argList.add(status);
        Map pageHash;

            pageHash = (Map) client.execute(
                    "workflowmgr.paginateWorkflowInstances", argList);


        return XmlRpcStructFactory.getWorkflowInstancePageFromXmlRpc(pageHash);
    }

    @Override
    public WorkflowInstancePage paginateWorkflowInstances(int pageNum)
        throws XmlRpcException, IOException {
        Vector argList = new Vector();
        argList.add(pageNum);
        Map pageHash;

            pageHash = (Map) client.execute(
                    "workflowmgr.paginateWorkflowInstances", argList);


        return XmlRpcStructFactory.getWorkflowInstancePageFromXmlRpc(pageHash);
    }

    public List getWorkflowsByEvent(String eventName) throws XmlRpcException, IOException, RepositoryException {
        List workflows = new Vector();
        Vector workflowVector;
        Vector argList = new Vector();
        argList.add(eventName);


            workflowVector = (Vector) client.execute(
                    "workflowmgr.getWorkflowsByEvent", argList);

            if (workflowVector != null) {
              for (Object aWorkflowVector : workflowVector) {
                Map workflowHash = (Map) aWorkflowVector;
                Workflow w = XmlRpcStructFactory
                    .getWorkflowFromXmlRpc(workflowHash);
                workflows.add(w);
              }
            }

            return workflows;

    }

    public Metadata getWorkflowInstanceMetadata(String wInstId) throws XmlRpcException, IOException {
        Vector argList = new Vector();
        argList.add(wInstId);
        Metadata met;

            Map instMetHash = (Map) client.execute(
                    "workflowmgr.getWorkflowInstanceMetadata", argList);
            met = new Metadata();
            met.addMetadata(instMetHash);


        return met;
    }

    @Override
    public synchronized boolean setWorkflowInstanceCurrentTaskStartDateTime(
            String wInstId, String startDateTimeIsoStr) throws XmlRpcException, IOException {
        Vector argList = new Vector();
        argList.add(wInstId);
        argList.add(startDateTimeIsoStr);

            return (Boolean) client.execute(
                "workflowmgr.setWorkflowInstanceCurrentTaskStartDateTime",
                argList);


    }

    @Override
    public double getWorkflowCurrentTaskWallClockMinutes(String workflowInstId)
        throws XmlRpcException, IOException {
        Vector argList = new Vector();
        argList.add(workflowInstId);

            return (Double) client.execute(
                "workflowmgr.getWorkflowCurrentTaskWallClockMinutes",
                argList);

    }

    @Override
    public double getWorkflowWallClockMinutes(String workflowInstId)
        throws XmlRpcException, IOException {
        Vector argList = new Vector();
        argList.add(workflowInstId);

            return (Double) client.execute(
                "workflowmgr.getWorkflowWallClockMinutes", argList);

    }

    @Override
    public synchronized boolean stopWorkflowInstance(String workflowInstId)
        throws XmlRpcException, IOException {
        Vector argList = new Vector();
        argList.add(workflowInstId);

            return (Boolean) client.execute(
                "workflowmgr.stopWorkflowInstance", argList);

    }

    @Override
    public synchronized boolean pauseWorkflowInstance(String workflowInstId)
        throws XmlRpcException, IOException {
        Vector argList = new Vector();
        argList.add(workflowInstId);

            return (Boolean) client.execute(
                "workflowmgr.pauseWorkflowInstance", argList);

    }

    @Override
    public synchronized boolean resumeWorkflowInstance(String workflowInstId)
        throws XmlRpcException, IOException {
        Vector argList = new Vector();
        argList.add(workflowInstId);

            return (Boolean) client.execute(
                "workflowmgr.resumeWorkflowInstance", argList);

    }

    @Override
    public synchronized boolean setWorkflowInstanceCurrentTaskEndDateTime(
            String wInstId, String endDateTimeIsoStr) throws XmlRpcException, IOException {
        Vector argList = new Vector();
        argList.add(wInstId);
        argList.add(endDateTimeIsoStr);

            return (Boolean) client.execute(
                "workflowmgr.setWorkflowInstanceCurrentTaskEndDateTime",
                argList);


    }

    @Override
    public synchronized boolean updateWorkflowInstanceStatus(
            String workflowInstId, String status) throws XmlRpcException, IOException {
        Vector argList = new Vector();
        argList.add(workflowInstId);
        argList.add(status);

            return (Boolean) client.execute(
                "workflowmgr.updateWorkflowInstanceStatus", argList);


    }

    @Override
    public synchronized boolean updateWorkflowInstance(WorkflowInstance instance)
        throws XmlRpcException, IOException {
        Vector argList = new Vector();
        argList.add(XmlRpcStructFactory.getXmlRpcWorkflowInstance(instance));

      return (Boolean) client.execute(
                "workflowmgr.updateWorkflowInstance", argList);

    }

    @Override
    public synchronized boolean updateMetadataForWorkflow(
            String workflowInstId, Metadata metadata) throws XmlRpcException, IOException {
        Vector argList = new Vector();
        argList.add(workflowInstId);
        argList.add(metadata.getHashTable());

            return (Boolean) client.execute(
                "workflowmgr.updateMetadataForWorkflow", argList);


    }

    @Override
    public boolean sendEvent(String eventName, Metadata metadata)
        throws XmlRpcException, IOException {
        Vector argList = new Vector();
        argList.add(eventName);
        argList.add(metadata.getHashTable());

            return (Boolean) client
                .execute("workflowmgr.handleEvent", argList);

    }

    public boolean clearWorkflowInstances() throws XmlRpcException, IOException{
      Vector argList = new Vector();

      return (Boolean)client.execute("workflowmgr.clearWorkflowInstances", argList);
    }

    public WorkflowTask getTaskById(String taskId) throws XmlRpcException, IOException, RepositoryException {
        Vector argList = new Vector();
        argList.add(taskId);

            Map t = (Map) client.execute("workflowmgr.getTaskById",
                    argList);
            return XmlRpcStructFactory.getWorkflowTaskFromXmlRpc(t);

    }

    @Override
    public WorkflowCondition getConditionById(String conditionId)
        throws XmlRpcException, IOException, RepositoryException {
        Vector argList = new Vector();
        argList.add(conditionId);

            Map c = (Map) client.execute(
                    "workflowmgr.getConditionById", argList);
            return XmlRpcStructFactory.getWorkflowConditionFromXmlRpc(c);

    }

    @Override
    public WorkflowInstance getWorkflowInstanceById(String wInstId)
        throws XmlRpcException, IOException, RepositoryException {
        Vector argList = new Vector();
        argList.add(wInstId);

            Map workflowInstance = (Map) client.execute(
                    "workflowmgr.getWorkflowInstanceById", argList);
          return XmlRpcStructFactory
                  .getWorkflowInstanceFromXmlRpc(workflowInstance);

    }

    public Workflow getWorkflowById(String workflowId) throws XmlRpcException, IOException, RepositoryException {
        Vector argList = new Vector();
        argList.add(workflowId);

            Map workflow = (Map) client.execute(
                    "workflowmgr.getWorkflowById", argList);
          return XmlRpcStructFactory.getWorkflowFromXmlRpc(workflow);

    }

    public Vector getWorkflows() throws XmlRpcException, IOException, RepositoryException {
        Vector argList = new Vector();
        Vector works;
        Vector workflows;

            works = (Vector) client
                    .execute("workflowmgr.getWorkflows", argList);

            if (works != null) {
                workflows = new Vector(works.size());

              for (Object work : works) {
                Map workflw = (Map) work;
                Workflow w = XmlRpcStructFactory
                    .getWorkflowFromXmlRpc(workflw);
                workflows.add(w);
              }

                return workflows;
            } else {
              return null;
            }


    }

    public int getNumWorkflowInstancesByStatus(String status) throws XmlRpcException, IOException {
        Vector argList = new Vector();
        argList.add(status);
        int numInsts;

            numInsts = (Integer) client.execute(
                "workflowmgr.getNumWorkflowInstancesByStatus", argList);


        return numInsts;
    }

    public int getNumWorkflowInstances() throws XmlRpcException, IOException {
        Vector argList = new Vector();
        int numInsts;

            numInsts = (Integer) client.execute(
                "workflowmgr.getNumWorkflowInstances", argList);


        return numInsts;
    }

    public Vector getWorkflowInstancesByStatus(String status) throws XmlRpcException, IOException {
        Vector argList = new Vector();
        argList.add(status);
        Vector insts;
        Vector instsUnpacked;

            insts = (Vector) client.execute(
                    "workflowmgr.getWorkflowInstancesByStatus", argList);
            if (insts != null) {
                instsUnpacked = new Vector(insts.size());
              for (Object inst1 : insts) {
                Map hWinst = (Map) inst1;
                WorkflowInstance inst = XmlRpcStructFactory
                    .getWorkflowInstanceFromXmlRpc(hWinst);
                instsUnpacked.add(inst);
              }
                return instsUnpacked;
            } else {
              return null;
            }


    }

    public Vector getWorkflowInstances() throws XmlRpcException, IOException {
        Vector argList = new Vector();
        Vector insts;
        Vector instsUnpacked;

          insts = (Vector) client.execute("workflowmgr.getWorkflowInstances",
              argList);
          if (insts != null) {
            instsUnpacked = new Vector(insts.size());
            for (Object inst1 : insts) {
              Map hWinst = (Map) inst1;
              WorkflowInstance inst = XmlRpcStructFactory
                  .getWorkflowInstanceFromXmlRpc(hWinst);
              instsUnpacked.add(inst);
            }
            return instsUnpacked;
          } else {
            return null;
          }
    }

    /**
     * @return Returns the workflowManagerUrl.
     */
    @Override
    public URL getWorkflowManagerUrl() {
        return workflowManagerUrl;
    }

    /**
     * @param workflowManagerUrl
     *            The workflowManagerUrl to set.
     */
    @Override
    public void setWorkflowManagerUrl(URL workflowManagerUrl) {
        this.workflowManagerUrl = workflowManagerUrl;

        // reset the client
        client = new XmlRpcClient(workflowManagerUrl);
    }

    @Override
    public boolean isAlive() {
      try {
        return (boolean) client.execute("workflowmgr.isAlive", new Vector());
      } catch (XmlRpcException | IOException e) {
        LOG.log(Level.SEVERE, "An error occurred when checking WM status", e);
        return false;
      }
    }

    @Override
    public void close() throws IOException {

    }
}
