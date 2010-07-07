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
import org.apache.xmlrpc.WebServer;

//OODT imports
import org.apache.oodt.cas.workflow.util.XmlRpcStructFactory;
import org.apache.oodt.cas.workflow.engine.WorkflowEngineFactory;
import org.apache.oodt.cas.workflow.engine.WorkflowEngine;
import org.apache.oodt.cas.workflow.repository.WorkflowRepository;
import org.apache.oodt.cas.workflow.repository.WorkflowRepositoryFactory;
import org.apache.oodt.cas.workflow.structs.Workflow;
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;
import org.apache.oodt.cas.workflow.structs.WorkflowInstancePage;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.oodt.cas.workflow.structs.WorkflowCondition;
import org.apache.oodt.cas.workflow.structs.exceptions.EngineException;
import org.apache.oodt.cas.workflow.structs.exceptions.InstanceRepositoryException;
import org.apache.oodt.cas.workflow.structs.exceptions.RepositoryException;
import org.apache.oodt.cas.metadata.Metadata;

//JDK imports
import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * An XML RPC-based Workflow manager.
 * </p>
 * 
 */
public class XmlRpcWorkflowManager {

    /* the port to run the XML RPC web server on, default is 2000 */
    private int webServerPort = 2000;

    /* our log stream */
    private Logger LOG = Logger
            .getLogger(XmlRpcWorkflowManager.class.getName());

    /* our xml rpc web server */
    private WebServer webServer = null;

    /* our workflow engine */
    private WorkflowEngine engine = null;

    /* our workflow repository */
    private WorkflowRepository repo = null;

    /**
     * 
     * @param port
     *            The web server port to run the XML Rpc server on, defaults to
     *            2000.
     */
    public XmlRpcWorkflowManager(int port) throws Exception {
        Class engineFactoryClass = null, repositoryFactoryClass = null;

        WorkflowEngineFactory engineFactory = null;
        WorkflowRepositoryFactory repoFactory = null;

        // load properties from workflow manager properties file, if specified
        if (System.getProperty("org.apache.oodt.cas.workflow.properties") != null) {
            String configFile = System
                    .getProperty("org.apache.oodt.cas.workflow.properties");
            LOG.log(Level.INFO,
                    "Loading Workflow Manager Configuration Properties from: ["
                            + configFile + "]");
            System.getProperties().load(
                    new FileInputStream(new File(configFile)));
        }

        String engineClassStr = System
                .getProperty("workflow.engine.factory",
                        "org.apache.oodt.cas.workflow.engine.DataSourceWorkflowEngineFactory");
        String repositoryClassStr = System
                .getProperty("workflow.repo.factory",
                        "org.apache.oodt.cas.workflow.repository.DataSourceWorkflowRepositoryFactory");

        try {
            engineFactoryClass = Class.forName(engineClassStr);
            engineFactory = (WorkflowEngineFactory) engineFactoryClass
                    .newInstance();
            engine = engineFactory.createWorkflowEngine();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new Exception("Unable to load workflow engine factory class "
                    + engineClassStr);
        }

        webServerPort = port;

        // set the corresponding workflow manager url
        engine.setWorkflowManagerUrl(safeGetUrlFromString("http://"
                + getHostname() + ":" + this.webServerPort));

        try {
            repositoryFactoryClass = Class.forName(repositoryClassStr);
            repoFactory = (WorkflowRepositoryFactory) repositoryFactoryClass
                    .newInstance();
            repo = repoFactory.createRepository();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new Exception(
                    "Unable to load workflow repository factory class "
                            + repositoryClassStr);
        }

        // start up the web server
        webServer = new WebServer(webServerPort);
        webServer.addHandler("workflowmgr", this);
        webServer.start();

        LOG.log(Level.INFO, "Workflow Manager started by "
                + System.getProperty("user.name", "unknown"));

    }

    public Vector getRegisteredEvents() throws RepositoryException {

        List events = null;
        Vector eventsVector = new Vector();

        try {
            events = repo.getRegisteredEvents();

            if (events != null) {
                for (Iterator i = events.iterator(); i.hasNext();) {
                    eventsVector.add((String) i.next());
                }

            }

            return eventsVector;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RepositoryException(
                    "Exception getting registered events from repository: Message: "
                            + e.getMessage());
        }

    }

    public Hashtable getFirstPage() {
        WorkflowInstancePage page = engine.getInstanceRepository()
                .getFirstPage();
        if (page != null) {
            populateWorkflows(page.getPageWorkflows());
            return XmlRpcStructFactory.getXmlRpcWorkflowInstancePage(page);
        } else
            return XmlRpcStructFactory
                    .getXmlRpcWorkflowInstancePage(WorkflowInstancePage
                            .blankPage());

    }

    public Hashtable getNextPage(Hashtable currentPage) {
        // first unpack current page
        WorkflowInstancePage currPage = XmlRpcStructFactory
                .getWorkflowInstancePageFromXmlRpc(currentPage);
        WorkflowInstancePage page = engine.getInstanceRepository().getNextPage(
                currPage);
        if (page != null) {
            populateWorkflows(page.getPageWorkflows());
            return XmlRpcStructFactory.getXmlRpcWorkflowInstancePage(page);
        } else
            return XmlRpcStructFactory
                    .getXmlRpcWorkflowInstancePage(WorkflowInstancePage
                            .blankPage());
    }

    public Hashtable getPrevPage(Hashtable currentPage) {
        // first unpack current page
        WorkflowInstancePage currPage = XmlRpcStructFactory
                .getWorkflowInstancePageFromXmlRpc(currentPage);
        WorkflowInstancePage page = engine.getInstanceRepository().getPrevPage(
                currPage);
        if (page != null) {
            populateWorkflows(page.getPageWorkflows());
            return XmlRpcStructFactory.getXmlRpcWorkflowInstancePage(page);
        } else
            return XmlRpcStructFactory
                    .getXmlRpcWorkflowInstancePage(WorkflowInstancePage
                            .blankPage());
    }

    public Hashtable getLastPage() {
        WorkflowInstancePage page = engine.getInstanceRepository()
                .getLastPage();
        if (page != null) {
            populateWorkflows(page.getPageWorkflows());
            return XmlRpcStructFactory.getXmlRpcWorkflowInstancePage(page);
        } else
            return XmlRpcStructFactory
                    .getXmlRpcWorkflowInstancePage(WorkflowInstancePage
                            .blankPage());
    }

    public Hashtable paginateWorkflowInstances(int pageNum, String status)
            throws InstanceRepositoryException {
        WorkflowInstancePage page = engine.getInstanceRepository()
                .getPagedWorkflows(pageNum, status);
        if (page != null) {
            populateWorkflows(page.getPageWorkflows());
            return XmlRpcStructFactory.getXmlRpcWorkflowInstancePage(page);
        } else
            return XmlRpcStructFactory
                    .getXmlRpcWorkflowInstancePage(WorkflowInstancePage
                            .blankPage());

    }

    public Hashtable paginateWorkflowInstances(int pageNum)
            throws InstanceRepositoryException {
        WorkflowInstancePage page = engine.getInstanceRepository()
                .getPagedWorkflows(pageNum);
        if (page != null) {
            populateWorkflows(page.getPageWorkflows());
            return XmlRpcStructFactory.getXmlRpcWorkflowInstancePage(page);
        } else
            return XmlRpcStructFactory
                    .getXmlRpcWorkflowInstancePage(WorkflowInstancePage
                            .blankPage());
    }

    public Hashtable getWorkflowInstanceMetadata(String wInstId) {
        Metadata met = engine.getWorkflowInstanceMetadata(wInstId);
        return met.getHashtable();
    }

    public Vector getWorkflowsByEvent(String eventName)
            throws RepositoryException {
        List workflows = null;
        Vector workflowList = new Vector();

        try {
            workflows = repo.getWorkflowsForEvent(eventName);

            if (workflows != null) {
                for (Iterator i = workflows.iterator(); i.hasNext();) {
                    Workflow w = (Workflow) i.next();
                    Hashtable workflow = XmlRpcStructFactory
                            .getXmlRpcWorkflow(w);
                    workflowList.add(workflow);
                }
            }

            return workflowList;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RepositoryException(
                    "Exception getting workflows for event: " + eventName
                            + " from repository: Message: " + e.getMessage());
        }
    }

    public boolean handleEvent(String eventName, Hashtable metadata)
            throws RepositoryException, EngineException {
        LOG.log(Level.INFO, "WorkflowManager: Received event: " + eventName);

        List workflows = null;

        try {
            workflows = repo.getWorkflowsForEvent(eventName);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RepositoryException(
                    "Exception getting workflows associated with event: "
                            + eventName + ": Message: " + e.getMessage());
        }

        if (workflows != null) {
            for (Iterator i = workflows.iterator(); i.hasNext();) {
                Workflow w = (Workflow) i.next();
                LOG.log(Level.INFO, "WorkflowManager: Workflow " + w.getName()
                        + " retrieved for event " + eventName);

                Metadata m = new Metadata();
                m.addMetadata(metadata);

                try {
                    engine.startWorkflow(w, m);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new EngineException(
                            "Engine exception when starting workflow: "
                                    + w.getName() + ": Message: "
                                    + e.getMessage());
                }
            }
            return true;
        } else
            return false;
    }

    public Hashtable getWorkflowInstanceById(String wInstId)
            throws EngineException {
        WorkflowInstance inst = null;

        try {
            inst = engine.getInstanceRepository().getWorkflowInstanceById(
                    wInstId);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.WARNING,
                    "Error obtaining workflow instance with ID: [" + wInstId
                            + "]: Message: " + e.getMessage());
            inst = new WorkflowInstance();
        }

        return XmlRpcStructFactory.getXmlRpcWorkflowInstance(inst);

    }

    public synchronized boolean stopWorkflowInstance(String workflowInstId) {
        engine.stopWorkflow(workflowInstId);
        return true;
    }

    public synchronized boolean pauseWorkflowInstance(String workflowInstId) {
        engine.pauseWorkflowInstance(workflowInstId);
        return true;
    }

    public synchronized boolean resumeWorkflowInstance(String workflowInstId) {
        engine.resumeWorkflowInstance(workflowInstId);
        return true;
    }

    public double getWorkflowWallClockMinutes(String workflowInstId) {
        return engine.getWallClockMinutes(workflowInstId);
    }

    public double getWorkflowCurrentTaskWallClockMinutes(String workflowInstId) {
        return engine.getCurrentTaskWallClockMinutes(workflowInstId);
    }

    public int getNumWorkflowInstancesByStatus(String status) throws InstanceRepositoryException {
        return engine.getInstanceRepository().getNumWorkflowInstancesByStatus(
                status);
    }

    public int getNumWorkflowInstances() throws InstanceRepositoryException {
        return engine.getInstanceRepository().getNumWorkflowInstances();
    }

    public Vector getWorkflowInstancesByStatus(String status)
            throws EngineException {
        List workflowInsts = null;

        Vector workflowInstances = new Vector();

        try {
            workflowInsts = engine.getInstanceRepository()
                    .getWorkflowInstancesByStatus(status);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.WARNING,
                    "Exception getting workflow instances by status: Message: ["
                            + e.getMessage() + "]");
            return workflowInstances;
        }

        if (workflowInsts != null) {
            LOG.log(Level.INFO,
                    "Getting workflow instances by status: retrieved: "
                            + workflowInsts.size() + " instances");

            try {
                for (Iterator i = workflowInsts.iterator(); i.hasNext();) {
                    WorkflowInstance wInst = (WorkflowInstance) i.next();
                    // pick up the description of the workflow
                    Workflow wDesc = repo.getWorkflowById(wInst.getWorkflow()
                            .getId());
                    // TODO: hack for now, fix this, we shouldn't have to cast
                    // here, bad
                    // design
                    wInst.setWorkflow(wDesc);
                    Hashtable workflowInstance = XmlRpcStructFactory
                            .getXmlRpcWorkflowInstance(wInst);
                    workflowInstances.add(workflowInstance);
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new EngineException(
                        "Exception getting workflow instances by statusfrom workflow engine: Message: "
                                + e.getMessage());
            }
        }

        return workflowInstances;
    }

    public Vector getWorkflowInstances() throws EngineException {
        List workflowInsts = null;

        Vector workflowInstances = new Vector();

        try {
            workflowInsts = engine.getInstanceRepository()
                    .getWorkflowInstances();
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.WARNING,
                    "Exception getting workflow instances: Message: ["
                            + e.getMessage() + "]");
            return workflowInstances;
        }

        if (workflowInsts != null) {
            LOG.log(Level.INFO, "Getting workflow instances: retrieved: "
                    + workflowInsts.size() + " instances");

            try {
                for (Iterator i = workflowInsts.iterator(); i.hasNext();) {
                    WorkflowInstance wInst = (WorkflowInstance) i.next();
                    // pick up the description of the workflow
                    Workflow wDesc = repo.getWorkflowById(wInst.getWorkflow()
                            .getId());
                    // TODO: hack for now, fix this, we shouldn't have to cast
                    // here, bad
                    // design
                    wInst.setWorkflow(wDesc);
                    Hashtable workflowInstance = XmlRpcStructFactory
                            .getXmlRpcWorkflowInstance(wInst);
                    workflowInstances.add(workflowInstance);
                }
                return workflowInstances;
            } catch (Exception e) {
                e.printStackTrace();
                throw new EngineException(
                        "Exception getting workflow instances from workflow engine: Message: "
                                + e.getMessage());
            }
        } else
            return null;
    }

    public Vector getWorkflows() throws RepositoryException {
        List workflowList = repo.getWorkflows();
        Vector workflows = new Vector();

        if (workflowList != null) {
            LOG.log(Level.INFO, "Getting workflows: retrieved: "
                    + workflowList.size() + " workflows");

            try {
                for (Iterator i = workflowList.iterator(); i.hasNext();) {
                    Workflow w = (Workflow) i.next();
                    Hashtable workflow = XmlRpcStructFactory
                            .getXmlRpcWorkflow(w);
                    workflows.add(workflow);
                }

                return workflows;
            } catch (Exception e) {
                e.printStackTrace();
                throw new RepositoryException(
                        "Exception getting workflows from repository: Message: "
                                + e.getMessage());
            }

        } else
            return null;

    }

    public Hashtable getTaskById(String taskId) throws RepositoryException {
        try {
            WorkflowTask t = repo.getWorkflowTaskById(taskId);
            return XmlRpcStructFactory.getXmlRpcWorkflowTask(t);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RepositoryException(
                    "Exception getting task by id: Message: " + e.getMessage());

        }
    }

    public Hashtable getConditionById(String conditionId)
            throws RepositoryException {
        try {
            WorkflowCondition c = repo.getWorkflowConditionById(conditionId);
            return XmlRpcStructFactory.getXmlRpcWorkflowCondition(c);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RepositoryException(
                    "Exception getting condition by id: Message: "
                            + e.getMessage());
        }
    }

    public Hashtable getWorkflowById(String workflowId)
            throws RepositoryException {
        try {
            Workflow workflow = repo.getWorkflowById(workflowId);
            return XmlRpcStructFactory.getXmlRpcWorkflow(workflow);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RepositoryException(
                    "Exception getting workflow by id from the repository: Message: "
                            + e.getMessage());
        }
    }

    public synchronized boolean updateMetadataForWorkflow(
            String workflowInstId, Hashtable metadata) {
        Metadata met = new Metadata();
        met.addMetadata(metadata);
        return this.engine.updateMetadata(workflowInstId, met);
    }

    public synchronized boolean updateWorkflowInstance(Hashtable workflowInst) {
        WorkflowInstance wInst = XmlRpcStructFactory
                .getWorkflowInstanceFromXmlRpc(workflowInst);
        return doUpdateWorkflowInstance(wInst);

    }

    public synchronized boolean setWorkflowInstanceCurrentTaskStartDateTime(
            String wInstId, String startDateTimeIsoStr) {
        WorkflowInstance wInst = null;
        try {
            wInst = this.engine.getInstanceRepository()
                    .getWorkflowInstanceById(wInstId);
        } catch (InstanceRepositoryException e) {
            e.printStackTrace();
            return false;
        }
        wInst.setCurrentTaskStartDateTimeIsoStr(startDateTimeIsoStr);
        return doUpdateWorkflowInstance(wInst);
    }

    public synchronized boolean setWorkflowInstanceCurrentTaskEndDateTime(
            String wInstId, String endDateTimeIsoStr) {
        WorkflowInstance wInst = null;
        try {
            wInst = this.engine.getInstanceRepository()
                    .getWorkflowInstanceById(wInstId);
        } catch (InstanceRepositoryException e) {
            e.printStackTrace();
            return false;
        }
        wInst.setCurrentTaskEndDateTimeIsoStr(endDateTimeIsoStr);
        return doUpdateWorkflowInstance(wInst);
    }

    public synchronized boolean updateWorkflowInstanceStatus(
            String workflowInstanceId, String status) throws Exception {
        WorkflowInstance wInst = null;
        try {
            wInst = engine.getInstanceRepository().getWorkflowInstanceById(
                    workflowInstanceId);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        wInst.setStatus(status);
        return doUpdateWorkflowInstance(wInst);
    }

    public static void main(String[] args) throws Exception {
        int portNum = -1;
        String usage = "XmlRpcWorkflowManager --portNum <port number for xml rpc service>\n";

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--portNum")) {
                portNum = Integer.parseInt(args[++i]);
            }
        }

        if (portNum == -1) {
            System.err.println(usage);
            System.exit(1);
        }

        XmlRpcWorkflowManager manager = new XmlRpcWorkflowManager(portNum);

        for (;;)
            try {
                Thread.currentThread().join();
            } catch (InterruptedException ignore) {
            }
    }

    private String getHostname() {
        try {
            // Get hostname by textual representation of IP address
            InetAddress addr = InetAddress.getLocalHost();
            // Get the host name
            String hostname = addr.getHostName();
            return hostname;
        } catch (UnknownHostException e) {
        }
        return null;
    }

    private URL safeGetUrlFromString(String urlStr) {
        try {
            return new URL(urlStr);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    private boolean doUpdateWorkflowInstance(WorkflowInstance wInst) {
        try {
            engine.getInstanceRepository().updateWorkflowInstance(wInst);
            return true;
        } catch (InstanceRepositoryException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void populateWorkflows(List wInsts) {
        if (wInsts != null && wInsts.size() > 0) {
            for (Iterator i = wInsts.iterator(); i.hasNext();) {
                WorkflowInstance wInst = (WorkflowInstance) i.next();
                wInst.setWorkflow(safeGetWorkflowById(wInst.getWorkflow()
                        .getId()));
            }
        }
    }

    private Workflow safeGetWorkflowById(String workflowId) {
        try {
            return repo.getWorkflowById(workflowId);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.WARNING, "Error getting workflow by its id: ["
                    + workflowId + "]: Message: " + e.getMessage());
            return new Workflow();
        }
    }

}
