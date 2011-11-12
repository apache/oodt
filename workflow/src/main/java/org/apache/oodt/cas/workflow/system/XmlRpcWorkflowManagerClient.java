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

//APACHE imports
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;

//JDK imports
import java.net.URL;
import java.util.Hashtable;
import java.util.Vector;
import java.util.List;
import java.util.Iterator;
import java.util.logging.Logger;
import java.io.IOException;

//OODT imports
import org.apache.oodt.cas.workflow.structs.WorkflowInstancePage;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.oodt.cas.workflow.structs.WorkflowCondition;
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;
import org.apache.oodt.cas.workflow.structs.Workflow;
import org.apache.oodt.cas.workflow.util.XmlRpcStructFactory;
import org.apache.oodt.cas.cli.CmdLineUtility;
import org.apache.oodt.cas.metadata.Metadata;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * The XML RPC based workflow manager client.
 * </p>
 * 
 */
public class XmlRpcWorkflowManagerClient {

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
    
  public String executeDynamicWorkflow(List<String> taskIds, Metadata metadata)
      throws Exception {
    Vector argList = new Vector();
    Vector<String> taskIdVector = new Vector<String>();
    taskIdVector.addAll(taskIds);
    String instId = null;
    
    argList.add(taskIdVector);
    argList.add(metadata.getHashtable());

    try {
      instId = (String) client.execute("workflowmgr.executeDynamicWorkflow",
          argList);
    } catch (XmlRpcException e) {
      e.printStackTrace();
      throw new Exception(e.getMessage());
    } catch (IOException e) {
      throw new Exception(e.getMessage());
    }

    return instId;

  }

    public List getRegisteredEvents() throws Exception {
        Vector argList = new Vector();

        try {
            return (List) client.execute("workflowmgr.getRegisteredEvents",
                    argList);
        } catch (XmlRpcException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        } catch (IOException e) {
            throw new Exception(e.getMessage());
        }
    }

    public WorkflowInstancePage getFirstPage() throws Exception {
        Vector argList = new Vector();
        Hashtable pageHash = null;

        try {
            pageHash = (Hashtable) client.execute("workflowmgr.getFirstPage",
                    argList);
        } catch (XmlRpcException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        } catch (IOException e) {
            throw new Exception(e.getMessage());
        }

        return XmlRpcStructFactory.getWorkflowInstancePageFromXmlRpc(pageHash);
    }

    public WorkflowInstancePage getNextPage(WorkflowInstancePage currentPage)
            throws Exception {
        Vector argList = new Vector();
        argList.add(XmlRpcStructFactory
                .getXmlRpcWorkflowInstancePage(currentPage));
        Hashtable pageHash = null;

        try {
            pageHash = (Hashtable) client.execute("workflowmgr.getNextPage",
                    argList);
        } catch (XmlRpcException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        } catch (IOException e) {
            throw new Exception(e.getMessage());
        }

        return XmlRpcStructFactory.getWorkflowInstancePageFromXmlRpc(pageHash);
    }

    public WorkflowInstancePage getPrevPage(WorkflowInstancePage currentPage)
            throws Exception {
        Vector argList = new Vector();
        argList.add(XmlRpcStructFactory
                .getXmlRpcWorkflowInstancePage(currentPage));
        Hashtable pageHash = null;

        try {
            pageHash = (Hashtable) client.execute("workflowmgr.getPrevPage",
                    argList);
        } catch (XmlRpcException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        } catch (IOException e) {
            throw new Exception(e.getMessage());
        }

        return XmlRpcStructFactory.getWorkflowInstancePageFromXmlRpc(pageHash);
    }

    public WorkflowInstancePage getLastPage() throws Exception {
        Vector argList = new Vector();
        Hashtable pageHash = null;

        try {
            pageHash = (Hashtable) client.execute("workflowmgr.getLastPage",
                    argList);
        } catch (XmlRpcException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        } catch (IOException e) {
            throw new Exception(e.getMessage());
        }

        return XmlRpcStructFactory.getWorkflowInstancePageFromXmlRpc(pageHash);
    }

    public WorkflowInstancePage paginateWorkflowInstances(int pageNum,
            String status) throws Exception {
        Vector argList = new Vector();
        argList.add(new Integer(pageNum));
        argList.add(status);
        Hashtable pageHash = null;

        try {
            pageHash = (Hashtable) client.execute(
                    "workflowmgr.paginateWorkflowInstances", argList);
        } catch (XmlRpcException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        } catch (IOException e) {
            throw new Exception(e.getMessage());
        }

        return XmlRpcStructFactory.getWorkflowInstancePageFromXmlRpc(pageHash);
    }

    public WorkflowInstancePage paginateWorkflowInstances(int pageNum)
            throws Exception {
        Vector argList = new Vector();
        argList.add(new Integer(pageNum));
        Hashtable pageHash = null;

        try {
            pageHash = (Hashtable) client.execute(
                    "workflowmgr.paginateWorkflowInstances", argList);
        } catch (XmlRpcException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        } catch (IOException e) {
            throw new Exception(e.getMessage());
        }

        return XmlRpcStructFactory.getWorkflowInstancePageFromXmlRpc(pageHash);
    }

    public List getWorkflowsByEvent(String eventName) throws Exception {
        List workflows = new Vector();
        Vector workflowVector = new Vector();
        Vector argList = new Vector();
        argList.add(eventName);

        try {
            workflowVector = (Vector) client.execute(
                    "workflowmgr.getWorkflowsByEvent", argList);

            if (workflowVector != null) {
                for (Iterator i = workflowVector.iterator(); i.hasNext();) {
                    Hashtable workflowHash = (Hashtable) i.next();
                    Workflow w = XmlRpcStructFactory
                            .getWorkflowFromXmlRpc(workflowHash);
                    workflows.add(w);
                }
            }

            return workflows;
        } catch (XmlRpcException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        } catch (IOException e) {
            throw new Exception(e.getMessage());
        }
    }

    public Metadata getWorkflowInstanceMetadata(String wInstId) throws Exception {
        Vector argList = new Vector();
        argList.add(wInstId);
        Metadata met = null;

        try {
            Hashtable instMetHash = (Hashtable) client.execute(
                    "workflowmgr.getWorkflowInstanceMetadata", argList);
            met = new Metadata();
            met.addMetadata(instMetHash);
        } catch (XmlRpcException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        } catch (IOException e) {
            throw new Exception(e.getMessage());
        }

        return met;
    }

    public synchronized boolean setWorkflowInstanceCurrentTaskStartDateTime(
            String wInstId, String startDateTimeIsoStr) throws Exception {
        Vector argList = new Vector();
        argList.add(wInstId);
        argList.add(startDateTimeIsoStr);

        try {
            return ((Boolean) client.execute(
                    "workflowmgr.setWorkflowInstanceCurrentTaskStartDateTime",
                    argList)).booleanValue();
        } catch (XmlRpcException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        } catch (IOException e) {
            throw new Exception(e.getMessage());
        }

    }

    public double getWorkflowCurrentTaskWallClockMinutes(String workflowInstId)
            throws Exception {
        Vector argList = new Vector();
        argList.add(workflowInstId);

        try {
            return ((Double) client.execute(
                    "workflowmgr.getWorkflowCurrentTaskWallClockMinutes",
                    argList)).doubleValue();
        } catch (XmlRpcException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        } catch (IOException e) {
            throw new Exception(e.getMessage());
        }
    }

    public double getWorkflowWallClockMinutes(String workflowInstId)
            throws Exception {
        Vector argList = new Vector();
        argList.add(workflowInstId);

        try {
            return ((Double) client.execute(
                    "workflowmgr.getWorkflowWallClockMinutes", argList))
                    .doubleValue();
        } catch (XmlRpcException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        } catch (IOException e) {
            throw new Exception(e.getMessage());
        }
    }

    public synchronized boolean stopWorkflowInstance(String workflowInstId)
            throws Exception {
        Vector argList = new Vector();
        argList.add(workflowInstId);

        try {
            return ((Boolean) client.execute(
                    "workflowmgr.stopWorkflowInstance", argList))
                    .booleanValue();
        } catch (XmlRpcException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        } catch (IOException e) {
            throw new Exception(e.getMessage());
        }
    }

    public synchronized boolean pauseWorkflowInstance(String workflowInstId)
            throws Exception {
        Vector argList = new Vector();
        argList.add(workflowInstId);

        try {
            return ((Boolean) client.execute(
                    "workflowmgr.pauseWorkflowInstance", argList))
                    .booleanValue();
        } catch (XmlRpcException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        } catch (IOException e) {
            throw new Exception(e.getMessage());
        }
    }

    public synchronized boolean resumeWorkflowInstance(String workflowInstId)
            throws Exception {
        Vector argList = new Vector();
        argList.add(workflowInstId);

        try {
            return ((Boolean) client.execute(
                    "workflowmgr.resumeWorkflowInstance", argList))
                    .booleanValue();
        } catch (XmlRpcException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        } catch (IOException e) {
            throw new Exception(e.getMessage());
        }
    }

    public synchronized boolean setWorkflowInstanceCurrentTaskEndDateTime(
            String wInstId, String endDateTimeIsoStr) throws Exception {
        Vector argList = new Vector();
        argList.add(wInstId);
        argList.add(endDateTimeIsoStr);

        try {
            return ((Boolean) client.execute(
                    "workflowmgr.setWorkflowInstanceCurrentTaskEndDateTime",
                    argList)).booleanValue();
        } catch (XmlRpcException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        } catch (IOException e) {
            throw new Exception(e.getMessage());
        }

    }

    public synchronized boolean updateWorkflowInstanceStatus(
            String workflowInstId, String status) throws Exception {
        Vector argList = new Vector();
        argList.add(workflowInstId);
        argList.add(status);

        try {
            return ((Boolean) client.execute(
                    "workflowmgr.updateWorkflowInstanceStatus", argList))
                    .booleanValue();
        } catch (XmlRpcException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        } catch (IOException e) {
            throw new Exception(e.getMessage());
        }

    }

    public synchronized boolean updateWorkflowInstance(WorkflowInstance instance)
            throws Exception {
        Vector argList = new Vector();
        argList.add(XmlRpcStructFactory.getXmlRpcWorkflowInstance(instance));

        try {
            return ((Boolean) client.execute(
                    "workflowmgr.updateWorkflowInstance", argList))
                    .booleanValue();
        } catch (XmlRpcException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        } catch (IOException e) {
            throw new Exception(e.getMessage());
        }
    }

    public synchronized boolean updateMetadataForWorkflow(
            String workflowInstId, Metadata metadata) throws Exception {
        Vector argList = new Vector();
        argList.add(workflowInstId);
        argList.add(metadata.getHashtable());

        try {
            return ((Boolean) client.execute(
                    "workflowmgr.updateMetadataForWorkflow", argList))
                    .booleanValue();
        } catch (XmlRpcException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        } catch (IOException e) {
            throw new Exception(e.getMessage());
        }

    }

    public boolean sendEvent(String eventName, Metadata metadata)
            throws Exception {
        Vector argList = new Vector();
        argList.add(eventName);
        argList.add(metadata.getHashtable());

        try {
            return ((Boolean) client
                    .execute("workflowmgr.handleEvent", argList))
                    .booleanValue();
        } catch (XmlRpcException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        } catch (IOException e) {
            throw new Exception(e.getMessage());
        }
    }

    public WorkflowTask getTaskById(String taskId) throws Exception {
        Vector argList = new Vector();
        argList.add(taskId);

        try {
            Hashtable t = (Hashtable) client.execute("workflowmgr.getTaskById",
                    argList);
            return XmlRpcStructFactory.getWorkflowTaskFromXmlRpc(t);
        } catch (XmlRpcException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }

    public WorkflowCondition getConditionById(String conditionId)
            throws Exception {
        Vector argList = new Vector();
        argList.add(conditionId);

        try {
            Hashtable c = (Hashtable) client.execute(
                    "workflowmgr.getConditionById", argList);
            return XmlRpcStructFactory.getWorkflowConditionFromXmlRpc(c);
        } catch (XmlRpcException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }

    public WorkflowInstance getWorkflowInstanceById(String wInstId)
            throws Exception {
        Vector argList = new Vector();
        argList.add(wInstId);

        try {
            Hashtable workflowInstance = (Hashtable) client.execute(
                    "workflowmgr.getWorkflowInstanceById", argList);
            WorkflowInstance wInst = XmlRpcStructFactory
                    .getWorkflowInstanceFromXmlRpc(workflowInstance);
            return wInst;
        } catch (XmlRpcException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }

    }

    public Workflow getWorkflowById(String workflowId) throws Exception {
        Vector argList = new Vector();
        argList.add(workflowId);

        try {
            Hashtable workflow = (Hashtable) client.execute(
                    "workflowmgr.getWorkflowById", argList);
            Workflow w = XmlRpcStructFactory.getWorkflowFromXmlRpc(workflow);
            return w;
        } catch (XmlRpcException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }

    public Vector getWorkflows() throws Exception {
        Vector argList = new Vector();
        Vector works = null;
        Vector workflows = null;

        try {
            works = (Vector) client
                    .execute("workflowmgr.getWorkflows", argList);

            if (works != null) {
                workflows = new Vector(works.size());

                for (Iterator i = works.iterator(); i.hasNext();) {
                    Hashtable workflw = (Hashtable) i.next();
                    Workflow w = XmlRpcStructFactory
                            .getWorkflowFromXmlRpc(workflw);
                    workflows.add(w);
                }

                return workflows;
            } else
                return null;
        } catch (XmlRpcException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }

    }

    public int getNumWorkflowInstancesByStatus(String status) throws Exception{
        Vector argList = new Vector();
        argList.add(status);
        int numInsts = -1;

        try {
            numInsts = ((Integer)client.execute(
                    "workflowmgr.getNumWorkflowInstancesByStatus", argList)).intValue();
        } catch (XmlRpcException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }

        return numInsts;
    }

    public int getNumWorkflowInstances() throws Exception{
        Vector argList = new Vector();
        int numInsts = -1;

        try {
            numInsts = ((Integer)client.execute(
                    "workflowmgr.getNumWorkflowInstances", argList)).intValue();
        } catch (XmlRpcException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }

        return numInsts;
    }

    public Vector getWorkflowInstancesByStatus(String status) throws Exception {
        Vector argList = new Vector();
        argList.add(status);
        Vector insts = null;
        Vector instsUnpacked = null;

        try {
            insts = (Vector) client.execute(
                    "workflowmgr.getWorkflowInstancesByStatus", argList);
            if (insts != null) {
                instsUnpacked = new Vector(insts.size());
                for (Iterator i = insts.iterator(); i.hasNext();) {
                    Hashtable hWinst = (Hashtable) i.next();
                    WorkflowInstance inst = XmlRpcStructFactory
                            .getWorkflowInstanceFromXmlRpc(hWinst);
                    instsUnpacked.add(inst);
                }
                return instsUnpacked;
            } else
                return null;

        } catch (XmlRpcException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }

    public Vector getWorkflowInstances() throws Exception {
        Vector argList = new Vector();
        Vector insts = null;
        Vector instsUnpacked = null;

        try {
            insts = (Vector) client.execute("workflowmgr.getWorkflowInstances",
                    argList);
            if (insts != null) {
                instsUnpacked = new Vector(insts.size());
                for (Iterator i = insts.iterator(); i.hasNext();) {
                    Hashtable hWinst = (Hashtable) i.next();
                    WorkflowInstance inst = XmlRpcStructFactory
                            .getWorkflowInstanceFromXmlRpc(hWinst);
                    instsUnpacked.add(inst);
                }
                return instsUnpacked;
            } else
                return null;

        } catch (XmlRpcException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }

    public static void main(String[] args) {
       CmdLineUtility cmdLineUtility = new CmdLineUtility();
       cmdLineUtility.run(args);
    }

    /**
     * @return Returns the workflowManagerUrl.
     */
    public URL getWorkflowManagerUrl() {
        return workflowManagerUrl;
    }

    /**
     * @param workflowManagerUrl
     *            The workflowManagerUrl to set.
     */
    public void setWorkflowManagerUrl(URL workflowManagerUrl) {
        this.workflowManagerUrl = workflowManagerUrl;

        // reset the client
        client = new XmlRpcClient(workflowManagerUrl);
    }

}
