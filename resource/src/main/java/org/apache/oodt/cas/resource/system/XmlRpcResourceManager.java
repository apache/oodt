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


package org.apache.oodt.cas.resource.system;

import org.apache.oodt.cas.resource.scheduler.Scheduler;
import org.apache.oodt.cas.resource.structs.Job;
import org.apache.oodt.cas.resource.structs.JobInput;
import org.apache.oodt.cas.resource.structs.JobSpec;
import org.apache.oodt.cas.resource.structs.ResourceNode;
import org.apache.oodt.cas.resource.structs.exceptions.JobExecutionException;
import org.apache.oodt.cas.resource.structs.exceptions.JobQueueException;
import org.apache.oodt.cas.resource.structs.exceptions.JobRepositoryException;
import org.apache.oodt.cas.resource.structs.exceptions.MonitorException;
import org.apache.oodt.cas.resource.structs.exceptions.QueueManagerException;
import org.apache.oodt.cas.resource.structs.exceptions.SchedulerException;
import org.apache.oodt.cas.resource.util.GenericResourceManagerObjectFactory;
import org.apache.oodt.cas.resource.util.ResourceNodeComparator;
import org.apache.oodt.cas.resource.util.XmlRpcStructFactory;
import org.apache.oodt.config.Component;
import org.apache.oodt.config.ConfigurationManager;
import org.apache.oodt.config.ConfigurationManagerFactory;
import org.apache.xmlrpc.WebServer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author woollard
 * @version $Revision$
 * @deprecated soon be replaced by avro-rpc
 * <p>
 * An XML RPC-based Resource manager.
 * </p>
 *
 */
@Deprecated
public class XmlRpcResourceManager implements ResourceManager{

    /** our log stream */
    private static Logger LOG = Logger.getLogger(XmlRpcResourceManager.class.getName());

    private int port;
    /** our xml rpc web server */
    private WebServer webServer;
    /** our scheduler */
    private Scheduler scheduler;
    /** Configuration Manager instance of this instance */
    private ConfigurationManager configurationManager;

    public XmlRpcResourceManager(int port) throws IOException {
        this.port = port;
        List<String> propertiesFiles = new ArrayList<>();
        // set up the configuration, if there is any
        if (System.getProperty(ResourceManager.RESMGR_PROPERTIES_FILE_SYSTEM_PROPERTY) != null) {
            propertiesFiles.add(System.getProperty(ResourceManager.RESMGR_PROPERTIES_FILE_SYSTEM_PROPERTY));
        }

        configurationManager = ConfigurationManagerFactory.getConfigurationManager(Component.RESOURCE_MANAGER, propertiesFiles);
    }

    @Override
    public void startUp() throws Exception{
        try {
            configurationManager.loadConfiguration();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Unable to load configuration", e);
            throw new IOException("Unable to load configuration", e);
        }

        String schedulerClassStr = System.getProperty(
                "resource.scheduler.factory",
                "org.apache.oodt.cas.resource.scheduler.LRUSchedulerFactory");

        scheduler = GenericResourceManagerObjectFactory
                .getSchedulerServiceFromFactory(schedulerClassStr);

        // start up the scheduler
        new Thread(scheduler).start();

        // start up the web server
        webServer = new WebServer(port);
        webServer.addHandler("resourcemgr", this);
        webServer.start();

        LOG.log(Level.INFO, "Resource Manager started by "
                + System.getProperty("user.name", "unknown"));
    }

    public boolean isAlive() {
        return true;
    }

    /**
     * Gets the number of Jobs in JobQueue
     * @return Number of Jobs in JobQueue
     * @throws JobRepositoryException On Any Exception
     */
    public int getJobQueueSize() throws JobRepositoryException {
    	try {
    		return this.scheduler.getJobQueue().getSize();
    	}catch (Exception e) {
    		throw new JobRepositoryException("Failed to get size of JobQueue : " + e.getMessage(), e);
    	}
    }

    /**
     * Gets the max number of Jobs allowed in JobQueue
     * @return Max number of Jobs
     * @throws JobRepositoryException On Any Exception
     */
    public int getJobQueueCapacity() throws JobRepositoryException {
    	try {
    		return this.scheduler.getJobQueue().getCapacity();
    	}catch (Exception e) {
    		throw new JobRepositoryException("Failed to get capacity of JobQueue : " + e.getMessage(), e);
    	}
    }

    public boolean isJobComplete(String jobId) throws JobRepositoryException {
        JobSpec spec = scheduler.getJobQueue().getJobRepository().getJobById(
                jobId);
        return scheduler.getJobQueue().getJobRepository().jobFinished(spec);
    }

    public Map getJobInfo(String jobId) throws JobRepositoryException {
        JobSpec spec;

        try {
            spec = scheduler.getJobQueue().getJobRepository()
                    .getJobById(jobId);
        } catch (JobRepositoryException e) {
            LOG.log(Level.WARNING,
                    "Exception communicating with job repository for job: ["
                            + jobId + "]: Message: " + e.getMessage());
            throw new JobRepositoryException("Unable to get job: [" + jobId
                    + "] from repository!");
        }

        return XmlRpcStructFactory.getXmlRpcJob(spec.getJob());
    }


    public String handleJob(Hashtable jobHash, Map jobIn)
            throws SchedulerException {
        return genericHandleJob(jobHash, jobIn);
    }

    public String handleJob(Hashtable jobHash, int jobIn)
            throws SchedulerException {
        return genericHandleJob(jobHash, jobIn);
    }

    public String handleJob(Hashtable jobHash, boolean jobIn)
            throws SchedulerException {
        return genericHandleJob(jobHash, jobIn);
    }

    public String handleJob(Hashtable jobHash, String jobIn)
            throws SchedulerException {
        return genericHandleJob(jobHash, jobIn);
    }

    public String handleJob(Hashtable jobHash, double jobIn)
            throws SchedulerException {
        return genericHandleJob(jobHash, jobIn);
    }

    public String handleJob(Hashtable jobHash, Date jobIn)
            throws SchedulerException {
        return genericHandleJob(jobHash, jobIn);
    }

    public String handleJob(Hashtable jobHash, Vector jobIn)
            throws SchedulerException {
        return genericHandleJob(jobHash, jobIn);
    }

    public String handleJob(Hashtable jobHash, byte[] jobIn)
            throws SchedulerException {
        return genericHandleJob(jobHash, jobIn);
    }

    public boolean handleJob(Hashtable jobHash, Hashtable jobIn, String urlStr)
            throws JobExecutionException {
        return genericHandleJob(jobHash, jobIn, urlStr);
    }

    public boolean handleJob(Hashtable jobHash, int jobIn, String urlStr)
            throws JobExecutionException {
        return genericHandleJob(jobHash, jobIn, urlStr);
    }

    public boolean handleJob(Hashtable jobHash, boolean jobIn, String urlStr)
            throws JobExecutionException {
        return genericHandleJob(jobHash, jobIn, urlStr);
    }

    public boolean handleJob(Hashtable jobHash, String jobIn, String urlStr)
            throws JobExecutionException {
        return genericHandleJob(jobHash, jobIn, urlStr);
    }

    public boolean handleJob(Hashtable jobHash, double jobIn, String urlStr)
            throws JobExecutionException {
        return genericHandleJob(jobHash, jobIn, urlStr);
    }

    public boolean handleJob(Hashtable jobHash, Date jobIn, String urlStr)
            throws JobExecutionException {
        return genericHandleJob(jobHash, jobIn, urlStr);
    }

    public boolean handleJob(Hashtable jobHash, Vector jobIn, String urlStr)
            throws JobExecutionException {
        return genericHandleJob(jobHash, jobIn, urlStr);
    }

    public boolean handleJob(Hashtable jobHash, byte[] jobIn, String urlStr)
            throws JobExecutionException {
        return genericHandleJob(jobHash, jobIn, urlStr);
    }

    public List getNodes() throws MonitorException {
        List resNodes = scheduler.getMonitor().getNodes();
        return XmlRpcStructFactory.getXmlRpcResourceNodeList(resNodes);
    }

    public Map getNodeById(String nodeId) throws MonitorException {
        ResourceNode node = scheduler.getMonitor().getNodeById(nodeId);
        return XmlRpcStructFactory.getXmlRpcResourceNode(node);

    }

    public boolean killJob(String jobId) throws MonitorException {
        String resNodeId = scheduler.getBatchmgr().getExecutionNode(jobId);
        if (resNodeId == null) {
            LOG.log(Level.WARNING, "Attempt to kill job: [" + jobId
                    + "]: cannot find execution node"
                    + " (has the job already finished?)");
            return false;
        }
        ResourceNode node = scheduler.getMonitor().getNodeById(resNodeId);
        return scheduler.getBatchmgr().killJob(jobId, node);
    }

    public String getExecutionNode(String jobId) {
        String execNode = scheduler.getBatchmgr().getExecutionNode(jobId);
        if (execNode == null) {
            LOG.log(Level.WARNING, "Job: [" + jobId
                    + "] not currently executing on any known node");
            return "";
        } else {
            return execNode;
        }
    }

    public List<String> getQueues() {
    	return new Vector<String>(this.scheduler.getQueueManager().getQueues());
    }

    public boolean addQueue(String queueName) {
    	this.scheduler.getQueueManager().addQueue(queueName);
    	return true;
    }

    public boolean removeQueue(String queueName) {
    	this.scheduler.getQueueManager().removeQueue(queueName);
    	return true;
    }

    public boolean addNode(Hashtable hashNode) throws MonitorException {
        return this.addNodeCore(hashNode);
    }
    public boolean addNodeCore(Map hashNode) throws MonitorException {
    	this.scheduler.getMonitor().addNode(XmlRpcStructFactory.getResourceNodeFromXmlRpc(hashNode));
    	return true;
    }

    public boolean removeNode(String nodeId) throws MonitorException {
    	try{
	    	for(String queueName: this.getQueuesWithNode(nodeId)){
	    		this.removeNodeFromQueue(nodeId, queueName);
	    	}
	    	this.scheduler.getMonitor().removeNodeById(nodeId);
    	}catch(Exception e){
    		throw new MonitorException(e.getMessage(), e);
    	}

    	return true;
    }

    public boolean addNodeToQueue(String nodeId, String queueName) throws QueueManagerException {
    	this.scheduler.getQueueManager().addNodeToQueue(nodeId, queueName);
    	return true;
    }

    public boolean removeNodeFromQueue(String nodeId, String queueName) throws QueueManagerException {
    	this.scheduler.getQueueManager().removeNodeFromQueue(nodeId, queueName);
    	return true;
    }

    public List<String> getNodesInQueue(String queueName) throws QueueManagerException {
    	return new Vector<String>(this.scheduler.getQueueManager().getNodes(queueName));
    }

    public List<String> getQueuesWithNode(String nodeId) {
    	return new Vector<String>(this.scheduler.getQueueManager().getQueues(nodeId));
    }

    @Override
    public boolean shutdown() {
        configurationManager.clearConfiguration();
        if (this.webServer != null) {
            this.webServer.shutdown();
            this.webServer = null;
            return true;
        } else {
            return false;
        }
    }

    public String getNodeLoad(String nodeId) throws MonitorException{
    	ResourceNode node = this.scheduler.getMonitor().getNodeById(nodeId);
    	int capacity = node.getCapacity();
    	int load = (this.scheduler.getMonitor().getLoad(node)) * -1 + capacity;
    	return load + "/" + capacity;
    }

    public List getQueuedJobs() {
    	Vector jobs = new Vector();
    	List jobSpecs = this.scheduler.getJobQueue().getQueuedJobs();

    	if(jobSpecs != null && jobSpecs.size() > 0){
            for (Object jobSpec : jobSpecs) {
                Job job = ((JobSpec) jobSpec).getJob();
                jobs.add(job);
            }
    	}

    	return XmlRpcStructFactory.getXmlRpcJobList(jobs);
    }

    public String getNodeReport() throws MonitorException{
    	StringBuilder report = new StringBuilder();

    	try{

    		// get a sorted list of nodes
    		List nodes = scheduler.getMonitor().getNodes();
    		Collections.sort(nodes, new ResourceNodeComparator());

    		// formulate the report string
            for (Object node1 : nodes) {
                ResourceNode node = (ResourceNode) node1;
                String nodeId = node.getNodeId();
                report.append(nodeId);
                report.append(" (").append(getNodeLoad(nodeId)).append("/").append(node.getCapacity()).append(")");
                List<String> nodeQueues = getQueuesWithNode(nodeId);
                if (nodeQueues != null && nodeQueues.size() > 0) {
                    report.append(" -- ").append(nodeQueues.get(0));
                    for (int j = 1; j < nodeQueues.size(); j++) {
                        report.append(", ").append(nodeQueues.get(j));
                    }
                }
                report.append("\n");
            }

    	}catch(Exception e){
    		throw new MonitorException(e.getMessage(), e);
    	}

    	return report.toString();
    }

    public String getExecutionReport() throws JobRepositoryException{
    	StringBuilder report = new StringBuilder();

    	try{

	    	// get a sorted list of all nodes, since the report should be
	    	// alphabetically sorted by node
	    	List resNodes = scheduler.getMonitor().getNodes();
	    	if(resNodes.size() == 0){
	    		throw new MonitorException(
	    				"No jobs can be executing, as there are no nodes in the Monitor");
	    	}
	    	Vector<String> nodeIds = new Vector<String>();
            for (Object resNode : resNodes) {
                nodeIds.add(((ResourceNode) resNode).getNodeId());
            }
	    	Collections.sort(nodeIds);

	    	// generate the report string
	    	for(String nodeId: nodeIds){
	    		List execJobIds = this.scheduler.getBatchmgr().getJobsOnNode(nodeId);
	    		if(execJobIds != null && execJobIds.size() > 0){
                    for (Object execJobId : execJobIds) {
                        String jobId = (String) execJobId;
                        Job job = scheduler.getJobQueue().getJobRepository()
                                           .getJobById(jobId).getJob();
                        report.append("job id=").append(jobId);
                        report.append(", load=").append(job.getLoadValue());
                        report.append(", node=").append(nodeId);
                        report.append(", queue=").append(job.getQueueName()).append("\n");
                    }
	    		}
	    	}

    	}catch(Exception e){
    		throw new JobRepositoryException(e.getMessage(), e);
    	}

    	return report.toString();
    }

    public static void main(String[] args) throws IOException {
        int portNum = -1;
        String usage = "XmlRpcResourceManager --portNum <port number for xml rpc service>\n";

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--portNum")) {
                portNum = Integer.parseInt(args[++i]);
            }
        }

        if (portNum == -1) {
            System.err.println(usage);
            System.exit(1);
        }
	
		XmlRpcResourceManager resourceManager = new XmlRpcResourceManager(portNum);
		try {
			resourceManager.startUp();
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "An error occurred while starting resource manager", e);
			return;
		}
	
		for (;;) {
            try {
                Thread.currentThread().join();
            } catch (InterruptedException ignore) {
            }
        }
    }

    public boolean setNodeCapacity(String nodeId, int capacity){
    	try{
    		this.scheduler.getMonitor().getNodeById(nodeId).setCapacity(capacity);
    	}catch (MonitorException e){
    		LOG.log(Level.WARNING, "Exception setting capacity on node "
    				+ nodeId + ": " + e.getMessage());
    		return false;
    	}
    	return true;
    }

    private String genericHandleJob(Hashtable jobHash, Object jobIn)
        throws SchedulerException {
        return this.genericHandleJobCore(jobHash, jobIn);
    }
        private String genericHandleJobCore(Map jobHash, Object jobIn)
            throws SchedulerException {

        Job exec = XmlRpcStructFactory.getJobFromXmlRpc(jobHash);
        JobInput in = GenericResourceManagerObjectFactory
                .getJobInputFromClassName(exec.getJobInputClassName());
        if (in != null) {
            in.read(jobIn);
        }

        JobSpec spec = new JobSpec(in, exec);

        // queue the job up
        String jobId;

        try {
            jobId = scheduler.getJobQueue().addJob(spec);
        } catch (JobQueueException e) {
            LOG.log(Level.WARNING, "JobQueue exception adding job: Message: "
                    + e.getMessage());
            throw new SchedulerException(e.getMessage());
        }
        return jobId;
    }

    private boolean genericHandleJob(Map jobHash, Object jobIn,
                                         String urlStr) throws JobExecutionException {
        return this.genericHandleJobCore(jobHash,jobIn,urlStr);
    }
    private boolean genericHandleJobCore(Map jobHash, Object jobIn,
            String urlStr) throws JobExecutionException {
        Job exec = XmlRpcStructFactory.getJobFromXmlRpc(jobHash);
        JobInput in = GenericResourceManagerObjectFactory
            .getJobInputFromClassName(exec.getJobInputClassName());
        if (in != null) {
            in.read(jobIn);
        }

        JobSpec spec = new JobSpec(in, exec);

        URL remoteUrl = safeGetUrlFromString(urlStr);
        ResourceNode remoteNode = null;

        try {
            remoteNode = scheduler.getMonitor().getNodeByURL(remoteUrl);
        } catch (MonitorException ignored) {
        }

        return remoteNode != null && scheduler.getBatchmgr().executeRemotely(spec, remoteNode);
    }

    private URL safeGetUrlFromString(String urlStr) {
        URL url = null;

        try {
            url = new URL(urlStr);
        } catch (MalformedURLException e) {
            LOG.log(Level.WARNING, "Error converting string: [" + urlStr
                    + "] to URL object: Message: " + e.getMessage());
        }

        return url;
    }
}
