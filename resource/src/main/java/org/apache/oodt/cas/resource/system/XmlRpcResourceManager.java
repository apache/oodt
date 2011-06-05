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

//OODT imports
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
import org.apache.oodt.cas.resource.util.XmlRpcStructFactory;

//APACHE imports
import org.apache.xmlrpc.WebServer;

//JDK imports
import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author woollard
 * @version $Revision$
 * 
 * <p>
 * An XML RPC-based Resource manager.
 * </p>
 * 
 */
public class XmlRpcResourceManager {

    /* the port to run the XML RPC web server on, default is 2000 */
    private int webServerPort = 2000;

    /* our log stream */
    private Logger LOG = Logger
            .getLogger(XmlRpcResourceManager.class.getName());

    /* our xml rpc web server */
    private WebServer webServer = null;

    /* our scheduler */
    private Scheduler scheduler = null;

    public XmlRpcResourceManager(int port) throws Exception {
        // load properties from workflow manager properties file, if specified
        if (System.getProperty("org.apache.oodt.cas.resource.properties") != null) {
            String configFile = System
                    .getProperty("org.apache.oodt.cas.resource.properties");
            LOG.log(Level.INFO,
                    "Loading Resource Manager Configuration Properties from: ["
                            + configFile + "]");
            System.getProperties().load(
                    new FileInputStream(new File(configFile)));
        }

        String schedulerClassStr = System.getProperty(
                "resource.scheduler.factory",
                "org.apache.oodt.cas.resource.scheduler.LRUSchedulerFactory");

        scheduler = GenericResourceManagerObjectFactory
                .getSchedulerServiceFromFactory(schedulerClassStr);

        // start up the scheduler
        new Thread(scheduler).start();

        webServerPort = port;

        // start up the web server
        webServer = new WebServer(webServerPort);
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

    public Hashtable getJobInfo(String jobId) throws JobRepositoryException {
        JobSpec spec = null;

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

    public String handleJob(Hashtable jobHash, Hashtable jobIn)
            throws SchedulerException {
        return genericHandleJob(jobHash, jobIn);
    }

    public String handleJob(Hashtable jobHash, int jobIn)
            throws SchedulerException {
        return genericHandleJob(jobHash, new Integer(jobIn));
    }

    public String handleJob(Hashtable jobHash, boolean jobIn)
            throws SchedulerException {
        return genericHandleJob(jobHash, new Boolean(jobIn));
    }

    public String handleJob(Hashtable jobHash, String jobIn)
            throws SchedulerException {
        return genericHandleJob(jobHash, jobIn);
    }

    public String handleJob(Hashtable jobHash, double jobIn)
            throws SchedulerException {
        return genericHandleJob(jobHash, new Double(jobIn));
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
        return genericHandleJob(jobHash, new Integer(jobIn), urlStr);
    }

    public boolean handleJob(Hashtable jobHash, boolean jobIn, String urlStr)
            throws JobExecutionException {
        return genericHandleJob(jobHash, new Boolean(jobIn), urlStr);
    }

    public boolean handleJob(Hashtable jobHash, String jobIn, String urlStr)
            throws JobExecutionException {
        return genericHandleJob(jobHash, jobIn, urlStr);
    }

    public boolean handleJob(Hashtable jobHash, double jobIn, String urlStr)
            throws JobExecutionException {
        return genericHandleJob(jobHash, new Double(jobIn), urlStr);
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

    public Hashtable getNodeById(String nodeId) throws MonitorException {
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
        } else
            return execNode;
    }

    public List<String> getQueues() throws QueueManagerException {
    	return new Vector<String>(this.scheduler.getQueueManager().getQueues());
    }
    
    public boolean addQueue(String queueName) throws QueueManagerException {
    	this.scheduler.getQueueManager().addQueue(queueName);
    	return true;
    }
    
    public boolean removeQueue(String queueName) throws QueueManagerException {
    	this.scheduler.getQueueManager().removeQueue(queueName);
    	return true;
    }
    
    public boolean addNode(Hashtable hashNode) throws MonitorException {
    	this.scheduler.getMonitor().addNode(XmlRpcStructFactory.getResourceNodeFromXmlRpc(hashNode));
    	return true;
    }
    
    public boolean removeNode(String nodeId) throws MonitorException {
    	this.scheduler.getMonitor().removeNodeById(nodeId);
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
    
    public List<String> getQueuesWithNode(String nodeId) throws QueueManagerException {
    	return new Vector<String>(this.scheduler.getQueueManager().getQueues(nodeId));
    }
    
    public boolean shutdown(){
      if (this.webServer != null) {
        this.webServer.shutdown();
        this.webServer = null;
        return true;
    } else
        return false;      
    }
    
    public static void main(String[] args) throws Exception {
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

        XmlRpcResourceManager manager = new XmlRpcResourceManager(portNum);

        for (;;)
            try {
                Thread.currentThread().join();
            } catch (InterruptedException ignore) {
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

        Job exec = XmlRpcStructFactory.getJobFromXmlRpc(jobHash);
        JobInput in = GenericResourceManagerObjectFactory
                .getJobInputFromClassName(exec.getJobInputClassName());
        in.read(jobIn);

        JobSpec spec = new JobSpec(in, exec);

        // queue the job up
        String jobId = null;

        try {
            jobId = scheduler.getJobQueue().addJob(spec);
        } catch (JobQueueException e) {
            LOG.log(Level.WARNING, "JobQueue exception adding job: Message: "
                    + e.getMessage());
            throw new SchedulerException(e.getMessage());
        }
        return jobId;
    }

    private boolean genericHandleJob(Hashtable jobHash, Object jobIn,
            String urlStr) throws JobExecutionException {
        Job exec = XmlRpcStructFactory.getJobFromXmlRpc(jobHash);
        JobInput in = GenericResourceManagerObjectFactory
                .getJobInputFromClassName(exec.getJobInputClassName());
        in.read(jobIn);

        JobSpec spec = new JobSpec(in, exec);

        URL remoteUrl = safeGetUrlFromString(urlStr);
        ResourceNode remoteNode = null;

        try {
            remoteNode = scheduler.getMonitor().getNodeByURL(remoteUrl);
        } catch (MonitorException e) {
        }

        if (remoteNode != null) {
            return scheduler.getBatchmgr().executeRemotely(spec, remoteNode);
        } else
            return false;
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
