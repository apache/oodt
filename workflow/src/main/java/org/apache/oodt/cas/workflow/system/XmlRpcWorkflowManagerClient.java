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
import java.net.MalformedURLException;
import java.util.Hashtable;
import java.util.Vector;
import java.util.List;
import java.util.Iterator;
import java.util.logging.Logger;
import java.io.IOException;

//OODT imports
import org.apache.oodt.cas.workflow.structs.exceptions.RepositoryException;
import org.apache.oodt.cas.workflow.structs.exceptions.EngineException;
import org.apache.oodt.cas.workflow.structs.WorkflowInstancePage;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.oodt.cas.workflow.structs.WorkflowCondition;
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;
import org.apache.oodt.cas.workflow.structs.Workflow;
import org.apache.oodt.cas.workflow.util.XmlRpcStructFactory;
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

    public Metadata getWorkflowInstanceMetadata(String wInstId)
            throws Exception {
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

    public static void main(String[] args) throws MalformedURLException,
            EngineException, RepositoryException {

        String sendEventOperation = "--sendEvent --eventName <name> [--metaData --key <key1> <val1> <val2>...<valn>...--keyn <keyn> <val1> <val2>...<valn>]\n";
        String getWorkflowInstOperation = "--getWorkflowInsts\n";
        String getWorkflowsOperation = "--getWorkflows\n";
        String getTaskByIdOperation = "--getTaskById --id <taskId>\n";
        String getConditionByIdOperation = "--getConditionById --id <conditionId>\n";
        String getWorkflowByIdOperation = "--getWorkflowById --id <workflowId>\n";
        String getWorkflowsByEventOperation = "--getWorkflowsByEvent --eventName <name>\n";
        String getRegisteredEventsOperation = "--getRegisteredEvents\n";
        String getWorkflowInstByIdOperation = "--getWorkflowInst --id <workflowInstId>\n";
        String getWorkflowWallClockTimeOperation = "--getWallClockTime --id <workflowInstId>\n";
        String getWorkflowTaskWallClockTimeOperation = "--getTaskWallClockTime --id <workflowInstId>\n";
        String stopWorkflowInstanceOperation = "--stopWorkflowInst --id <workflowInstId>\n";
        String pauseWorkflowInstanceOperation = "--pauseWorkflowInst --id <workflowInstId>\n";
        String resumeWorkflowInstanceOperation = "--resumeWorkflowInst --id <workflowInstId>\n";
        String getFirstPageOperation = "--getFirstPage [--status <status>]\n";
        String getNextPageOperation = "--getNextPage --pageNum <num> [--status <status>]\n";
        String getPrevPageOperation = "--getPrevPage --pageNum <num> [--status <status>]\n";
        String getLastPageOperation = "--getLastPage [--status <status>]\n";

        String usage = "wmgr-client --url <url to xml rpc service> --operation [<operation> [params]]\n"
                + "operations:\n"
                + sendEventOperation
                + getWorkflowInstOperation
                + getWorkflowsOperation
                + getTaskByIdOperation
                + getConditionByIdOperation
                + getWorkflowByIdOperation
                + getWorkflowsByEventOperation
                + getRegisteredEventsOperation
                + getWorkflowInstByIdOperation
                + getWorkflowWallClockTimeOperation
                + getWorkflowTaskWallClockTimeOperation
                + stopWorkflowInstanceOperation
                + pauseWorkflowInstanceOperation
                + resumeWorkflowInstanceOperation
                + getFirstPageOperation
                + getNextPageOperation
                + getPrevPageOperation
                + getLastPageOperation;

        String operation = null, url = null;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--operation")) {
                operation = args[++i];
            } else if (args[i].equals("--url")) {
                url = args[++i];
            }
        }

        if (operation == null) {
            System.err.println(usage);
            System.exit(1);
        }

        if (operation.equals("--sendEvent")) {
            // get the event name, and the metadata
            String eventName = null;
            Metadata metadata = new Metadata();

            for (int i = 4; i < args.length; i++) {
                if (args[i].equals("--eventName")) {
                    eventName = args[++i];
                } else if (args[i].equals("--metaData")) {
                    for (int j = i + 1; j < args.length; j++) {

                        if (args[j].equals("--key")) {
                            String key = args[++j];
                            List values = new Vector();

                            boolean endOfList = true;
                            for (j++; j < args.length; j++) {
                                if (!args[j].equals("--key")) {
                                    values.add(args[j]);
                                } else {
                                    endOfList = false;
                                    break;
                                }
                            }

                            if (!endOfList) {
                                j--;
                            }

                            System.out.println("Picked up metadata: [key="
                                    + key + ", values=" + values + "]");
                            metadata.addMetadata(key, values);

                        }
                    }
                    break;
                }
            }

            if (eventName == null) {
                System.err.println(sendEventOperation);
                System.exit(1);
            }

            // create the client
            XmlRpcWorkflowManagerClient client = new XmlRpcWorkflowManagerClient(
                    new URL(url));

            try {
                client.sendEvent(eventName, metadata);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

        } else if (operation.equals("--getWorkflowInsts")) {
            XmlRpcWorkflowManagerClient client = new XmlRpcWorkflowManagerClient(
                    new URL(url));

            List insts = null;

            try {
                insts = client.getWorkflowInstances();

                if (insts != null) {
                    for (Iterator i = insts.iterator(); i.hasNext();) {
                        WorkflowInstance inst = (WorkflowInstance) i.next();
                        System.out
                                .println("Instance: [id="
                                        + inst.getId()
                                        + ", status="
                                        + inst.getStatus()
                                        + ", currentTask="
                                        + inst.getCurrentTaskId()
                                        + ", workflow="
                                        + inst.getWorkflow().getName()
                                        + ",wallClockTime="
                                        + client
                                                .getWorkflowWallClockMinutes(inst
                                                        .getId())
                                        + ",currentTaskWallClockTime="
                                        + client
                                                .getWorkflowCurrentTaskWallClockMinutes(inst
                                                        .getId()) + "]");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        } else if (operation.equals("--stopWorkflowInst")) {
            XmlRpcWorkflowManagerClient client = new XmlRpcWorkflowManagerClient(
                    new URL(url));

            String workflowInstId = null;

            for (int i = 4; i < args.length; i++) {
                if (args[i].equals("--id")) {
                    workflowInstId = args[++i];
                }
            }

            try {
                boolean stopped = client.stopWorkflowInstance(workflowInstId);
                if (stopped) {
                    System.out.println("Successfully stopped workflow: ["
                            + workflowInstId + "]");
                }
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }

        } else if (operation.equals("--pauseWorkflowInst")) {
            XmlRpcWorkflowManagerClient client = new XmlRpcWorkflowManagerClient(
                    new URL(url));

            String workflowInstId = null;

            for (int i = 4; i < args.length; i++) {
                if (args[i].equals("--id")) {
                    workflowInstId = args[++i];
                }
            }

            try {
                boolean paused = client.pauseWorkflowInstance(workflowInstId);
                if (paused) {
                    System.out.println("Successfully paused workflow: ["
                            + workflowInstId + "]");
                }
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }

        } else if (operation.equals("--resumeWorkflowInst")) {
            XmlRpcWorkflowManagerClient client = new XmlRpcWorkflowManagerClient(
                    new URL(url));

            String workflowInstId = null;

            for (int i = 4; i < args.length; i++) {
                if (args[i].equals("--id")) {
                    workflowInstId = args[++i];
                }
            }

            try {
                boolean resumed = client.resumeWorkflowInstance(workflowInstId);
                if (resumed) {
                    System.out.println("Successfully resumed workflow: ["
                            + workflowInstId + "]");
                }
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }

        }

        else if (operation.equals("--getTaskWallClockTime")) {
            XmlRpcWorkflowManagerClient client = new XmlRpcWorkflowManagerClient(
                    new URL(url));

            String workflowInstId = null;

            for (int i = 4; i < args.length; i++) {
                if (args[i].equals("--id")) {
                    workflowInstId = args[++i];
                }
            }

            try {
                double wallClockTime = client
                        .getWorkflowCurrentTaskWallClockMinutes(workflowInstId);
                System.out.println(wallClockTime + " minutes");
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }

        } else if (operation.equals("--getWallClockTime")) {
            XmlRpcWorkflowManagerClient client = new XmlRpcWorkflowManagerClient(
                    new URL(url));

            String workflowInstId = null;

            for (int i = 4; i < args.length; i++) {
                if (args[i].equals("--id")) {
                    workflowInstId = args[++i];
                }
            }

            try {
                double wallClockTime = client
                        .getWorkflowWallClockMinutes(workflowInstId);
                System.out.println(wallClockTime + " minutes");
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }

        }

        else if (operation.equals("--getFirstPage")) {
            XmlRpcWorkflowManagerClient client = new XmlRpcWorkflowManagerClient(
                    new URL(url));

            WorkflowInstancePage page = null;
            String status = null;

            for (int i = 4; i < args.length; i++) {
                if (args[i].equals("--status")) {
                    status = args[++i];
                }
            }

            try {
                if (status != null && !status.equals("")) {
                    page = client.paginateWorkflowInstances(1, status);
                } else {
                    page = client.getFirstPage();
                }

                System.out.println("Page: [num=" + page.getPageNum() + ","
                        + "pageSize=" + page.getPageSize() + ",totalPages="
                        + page.getTotalPages() + "]");
                if (page.getPageWorkflows() != null
                        && page.getPageWorkflows().size() > 0) {
                    for (Iterator i = page.getPageWorkflows().iterator(); i
                            .hasNext();) {
                        WorkflowInstance inst = (WorkflowInstance) i.next();
                        System.out
                                .println("Instance: [id="
                                        + inst.getId()
                                        + ", status="
                                        + inst.getStatus()
                                        + ", currentTask="
                                        + inst.getCurrentTaskId()
                                        + ", workflow="
                                        + inst.getWorkflow().getName()
                                        + ",wallClockTime="
                                        + client
                                                .getWorkflowWallClockMinutes(inst
                                                        .getId())
                                        + ",currentTaskWallClockTime="
                                        + client
                                                .getWorkflowCurrentTaskWallClockMinutes(inst
                                                        .getId()) + "]");
                    }
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        } else if (operation.equals("--getLastPage")) {
            XmlRpcWorkflowManagerClient client = new XmlRpcWorkflowManagerClient(
                    new URL(url));

            WorkflowInstancePage page = null;
            String status = null;

            for (int i = 4; i < args.length; i++) {
                if (args[i].equals("--status")) {
                    status = args[++i];
                }
            }

            try {
                if (status != null && !status.equals("")) {
                    WorkflowInstancePage firstPage = client
                            .paginateWorkflowInstances(1, status);
                    page = client.paginateWorkflowInstances(firstPage
                            .getTotalPages(), status);
                } else {
                    page = client.getLastPage();
                }

                System.out.println("Page: [num=" + page.getPageNum() + ","
                        + "pageSize=" + page.getPageSize() + ",totalPages="
                        + page.getTotalPages() + "]");
                if (page.getPageWorkflows() != null
                        && page.getPageWorkflows().size() > 0) {
                    for (Iterator i = page.getPageWorkflows().iterator(); i
                            .hasNext();) {
                        WorkflowInstance inst = (WorkflowInstance) i.next();
                        System.out
                                .println("Instance: [id="
                                        + inst.getId()
                                        + ", status="
                                        + inst.getStatus()
                                        + ", currentTask="
                                        + inst.getCurrentTaskId()
                                        + ", workflow="
                                        + inst.getWorkflow().getName()
                                        + ",wallClockTime="
                                        + client
                                                .getWorkflowWallClockMinutes(inst
                                                        .getId())
                                        + ",currentTaskWallClockTime="
                                        + client
                                                .getWorkflowCurrentTaskWallClockMinutes(inst
                                                        .getId()) + "]");
                    }
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        } else if (operation.equals("--getNextPage")) {
            XmlRpcWorkflowManagerClient client = new XmlRpcWorkflowManagerClient(
                    new URL(url));

            WorkflowInstancePage page = null;
            String status = null;
            int pageNum = -1;

            for (int i = 4; i < args.length; i++) {
                if (args[i].equals("--status")) {
                    status = args[++i];
                } else if (args[i].equals("--pageNum")) {
                    pageNum = Integer.parseInt(args[++i]);
                }
            }

            if (pageNum == -1) {
                System.err.println(getNextPageOperation);
                System.exit(1);
            }

            try {
                if (status != null && !status.equals("")) {
                    page = client
                            .paginateWorkflowInstances(pageNum + 1, status);
                } else {
                    page = client.paginateWorkflowInstances(pageNum + 1);
                }

                System.out.println("Page: [num=" + page.getPageNum() + ","
                        + "pageSize=" + page.getPageSize() + ",totalPages="
                        + page.getTotalPages() + "]");
                if (page.getPageWorkflows() != null
                        && page.getPageWorkflows().size() > 0) {
                    for (Iterator i = page.getPageWorkflows().iterator(); i
                            .hasNext();) {
                        WorkflowInstance inst = (WorkflowInstance) i.next();
                        System.out
                                .println("Instance: [id="
                                        + inst.getId()
                                        + ", status="
                                        + inst.getStatus()
                                        + ", currentTask="
                                        + inst.getCurrentTaskId()
                                        + ", workflow="
                                        + inst.getWorkflow().getName()
                                        + ",wallClockTime="
                                        + client
                                                .getWorkflowWallClockMinutes(inst
                                                        .getId())
                                        + ",currentTaskWallClockTime="
                                        + client
                                                .getWorkflowCurrentTaskWallClockMinutes(inst
                                                        .getId()) + "]");
                    }
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if (operation.equals("--getPrevPage")) {
            XmlRpcWorkflowManagerClient client = new XmlRpcWorkflowManagerClient(
                    new URL(url));

            WorkflowInstancePage page = null;
            String status = null;
            int pageNum = -1;

            for (int i = 4; i < args.length; i++) {
                if (args[i].equals("--status")) {
                    status = args[++i];
                } else if (args[i].equals("--pageNum")) {
                    pageNum = Integer.parseInt(args[++i]);
                }
            }

            if (pageNum == -1) {
                System.err.println(getPrevPageOperation);
                System.exit(1);
            }

            try {
                if (status != null && !status.equals("")) {
                    page = client
                            .paginateWorkflowInstances(pageNum - 1, status);
                } else {
                    page = client.paginateWorkflowInstances(pageNum - 1);
                }

                System.out.println("Page: [num=" + page.getPageNum() + ","
                        + "pageSize=" + page.getPageSize() + ",totalPages="
                        + page.getTotalPages() + "]");
                if (page.getPageWorkflows() != null
                        && page.getPageWorkflows().size() > 0) {
                    for (Iterator i = page.getPageWorkflows().iterator(); i
                            .hasNext();) {
                        WorkflowInstance inst = (WorkflowInstance) i.next();
                        System.out
                                .println("Instance: [id="
                                        + inst.getId()
                                        + ", status="
                                        + inst.getStatus()
                                        + ", currentTask="
                                        + inst.getCurrentTaskId()
                                        + ", workflow="
                                        + inst.getWorkflow().getName()
                                        + ",wallClockTime="
                                        + client
                                                .getWorkflowWallClockMinutes(inst
                                                        .getId())
                                        + ",currentTaskWallClockTime="
                                        + client
                                                .getWorkflowCurrentTaskWallClockMinutes(inst
                                                        .getId()) + "]");
                    }
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        else if (operation.equals("--getWorkflowInst")) {
            XmlRpcWorkflowManagerClient client = new XmlRpcWorkflowManagerClient(
                    new URL(url));

            WorkflowInstance inst = null;

            String wInstId = null;

            for (int i = 4; i < args.length; i++) {
                if (args[i].equals("--id")) {
                    wInstId = args[++i];
                }
            }

            try {
                inst = client.getWorkflowInstanceById(wInstId);
                if (inst != null) {
                    System.out
                            .println("Instance: [id="
                                    + inst.getId()
                                    + ", status="
                                    + inst.getStatus()
                                    + ", currentTask="
                                    + inst.getCurrentTaskId()
                                    + ", workflow="
                                    + inst.getWorkflow().getName()
                                    + ",wallClockTime="
                                    + client.getWorkflowWallClockMinutes(inst
                                            .getId())
                                    + ",currentTaskWallClockTime="
                                    + client
                                            .getWorkflowCurrentTaskWallClockMinutes(inst
                                                    .getId()) + "]");
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }
        } else if (operation.equals("--getWorkflows")) {
            XmlRpcWorkflowManagerClient client = new XmlRpcWorkflowManagerClient(
                    new URL(url));

            List workflows = null;

            try {
                workflows = client.getWorkflows();

                if (workflows != null) {
                    for (Iterator i = workflows.iterator(); i.hasNext();) {
                        Workflow w = (Workflow) i.next();
                        System.out.println("Workflow: [id=" + w.getId()
                                + ", name=" + w.getName() + ", numTasks="
                                + w.getTasks().size() + "]");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        } else if (operation.equals("--getTaskById")) {
            String taskId = null;

            for (int i = 4; i < args.length; i++) {
                if (args[i].equals("--id")) {
                    taskId = args[++i];
                }
            }

            if (taskId == null) {
                System.err.println(getTaskByIdOperation);
                System.exit(1);
            }

            // create the client
            XmlRpcWorkflowManagerClient client = new XmlRpcWorkflowManagerClient(
                    new URL(url));

            WorkflowTask task = null;

            try {
                task = client.getTaskById(taskId);
                System.out.println("Task: [id=" + task.getTaskId() + ", name="
                        + task.getTaskName() + ", order=" + task.getOrder()
                        + ", class=" + task.getClass().getName()
                        + ", numConditions=" + task.getConditions().size()
                        + ", configuration="
                        + task.getTaskConfig().getProperties() + "]");
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

        } else if (operation.equals("--getConditionById")) {
            String conditionId = null;

            for (int i = 4; i < args.length; i++) {
                if (args[i].equals("--id")) {
                    conditionId = args[++i];
                }
            }

            if (conditionId == null) {
                System.err.println(getConditionByIdOperation);
                System.exit(1);
            }

            // create the client
            XmlRpcWorkflowManagerClient client = new XmlRpcWorkflowManagerClient(
                    new URL(url));

            WorkflowCondition condition = null;

            try {
                condition = client.getConditionById(conditionId);
                System.out.println("Condition: [id="
                        + condition.getConditionId() + ", name="
                        + condition.getConditionName() + ", order="
                        + condition.getOrder() + ", class="
                        + condition.getClass().getName() + "]");
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        } else if (operation.equals("--getWorkflowById")) {
            String workflowId = null;

            for (int i = 4; i < args.length; i++) {
                if (args[i].equals("--id")) {
                    workflowId = args[++i];
                }
            }

            if (workflowId == null) {
                System.err.println(getWorkflowByIdOperation);
                System.exit(1);
            }

            // create the client
            XmlRpcWorkflowManagerClient client = new XmlRpcWorkflowManagerClient(
                    new URL(url));

            Workflow workflow = null;

            try {
                workflow = client.getWorkflowById(workflowId);
                System.out.println("Workflow: [id=" + workflow.getId()
                        + ", name=" + workflow.getName() + ", numTasks="
                        + workflow.getTasks().size() + "]");
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        } else if (operation.equals("--getRegisteredEvents")) {
            XmlRpcWorkflowManagerClient client = new XmlRpcWorkflowManagerClient(
                    new URL(url));

            List events = null;

            try {
                events = client.getRegisteredEvents();

                if (events != null) {
                    for (Iterator i = events.iterator(); i.hasNext();) {
                        String event = (String) i.next();
                        System.out.println("Event: [name=" + event + "]");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        } else if (operation.equals("--getWorkflowsByEvent")) {
            XmlRpcWorkflowManagerClient client = new XmlRpcWorkflowManagerClient(
                    new URL(url));

            List workflows = null;

            String eventName = null;

            for (int i = 4; i < args.length; i++) {
                if (args[i].equals("--eventName")) {
                    eventName = args[++i];
                }
            }

            if (eventName == null) {
                System.err.println(getWorkflowsByEventOperation);
                System.exit(1);
            }

            try {
                workflows = client.getWorkflowsByEvent(eventName);

                if (workflows != null) {
                    for (Iterator i = workflows.iterator(); i.hasNext();) {
                        Workflow w = (Workflow) i.next();
                        System.out.println("Workflow: [id=" + w.getId()
                                + ", name=" + w.getName() + ", numTasks="
                                + w.getTasks().size() + "]");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

        } else
            throw new IllegalArgumentException("Unknown operation: "
                    + operation);

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
