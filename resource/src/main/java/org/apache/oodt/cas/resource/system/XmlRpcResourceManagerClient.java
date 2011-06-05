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

//APACHE imports
import org.apache.xmlrpc.CommonsXmlRpcTransportFactory;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;

//OODTimports
import org.apache.oodt.cas.resource.structs.Job;
import org.apache.oodt.cas.resource.structs.JobInput;
import org.apache.oodt.cas.resource.structs.JobStatus;
import org.apache.oodt.cas.resource.structs.ResourceNode;
import org.apache.oodt.cas.resource.structs.exceptions.JobExecutionException;
import org.apache.oodt.cas.resource.structs.exceptions.JobRepositoryException;
import org.apache.oodt.cas.resource.structs.exceptions.MonitorException;
import org.apache.oodt.cas.resource.structs.exceptions.QueueManagerException;
import org.apache.oodt.cas.resource.util.XmlRpcStructFactory;

//JDK imports
import java.net.URL;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * The XML RPC based resource manager client.
 * </p>
 * 
 */
public class XmlRpcResourceManagerClient {

    /* our xml rpc client */
    private XmlRpcClient client = null;

    /* our log stream */
    private static Logger LOG = Logger
            .getLogger(XmlRpcResourceManagerClient.class.getName());

    /* resource manager url */
    private URL resMgrUrl = null;

    /**
     * <p>
     * Constructs a new XmlRpcResourceManagerClient with the given
     * <code>url</code>.
     * </p>
     * 
     * @param url
     *            The url pointer to the xml rpc resource manager service.
     */
    public XmlRpcResourceManagerClient(URL url) {
        // set up the configuration, if there is any
        if (System.getProperty("org.apache.oodt.cas.resource.properties") != null) {
            String configFile = System
                    .getProperty("org.apache.oodt.cas.resource.properties");
            LOG.log(Level.INFO,
                    "Loading Resource Manager Configuration Properties from: ["
                            + configFile + "]");
            try {
                System.getProperties().load(
                        new FileInputStream(new File(configFile)));
            } catch (Exception e) {
                LOG.log(Level.INFO,
                        "Error loading configuration properties from: ["
                                + configFile + "]");
            }

        }

        CommonsXmlRpcTransportFactory transportFactory = new CommonsXmlRpcTransportFactory(
                url);
        int connectionTimeoutMins = Integer
                .getInteger(
                        "org.apache.oodt.cas.resource.system.xmlrpc.connectionTimeout.minutes",
                        20).intValue();
        int connectionTimeout = connectionTimeoutMins * 60 * 1000;
        int requestTimeoutMins = Integer
                .getInteger(
                        "org.apache.oodt.cas.resource.system.xmlrpc.requestTimeout.minutes",
                        60).intValue();
        int requestTimeout = requestTimeoutMins * 60 * 1000;
        transportFactory.setConnectionTimeout(connectionTimeout);
        transportFactory.setTimeout(requestTimeout);
        client = new XmlRpcClient(url, transportFactory);
        resMgrUrl = url;
    }

    public static void main(String[] args) throws Exception {

        String getNodeByIdOperation = "--getNodeById --nodeId <node id>\n";
        String getNodesOperation = "--getNodes\n";
        String getQueuesOperation = "--getQueues\n";
        String addNodeOperation = "--addNode --nodeId <node id> --ipAddr <url> --capacity <max load>\n";
        String removeNodeOperation = "--removeNode --nodeId <node id>\n";
        String setNodeCapacityOperation = "--setNodeCapacity --nodeId <node id> --capacity <max load>\n";
        String addQueueOperation = "--addQueue --queueName <queue name>\n";
        String removeQueueOperation = "--removeQueue --queueName <queue name>\n";
        String addNodeToQueueOperation = "--addNodeToQueue --nodeId <node id> --queueName <queue name>\n";
        String getNodesInQueueOperation = "--getNodesInQueue --queueName <queue name>\n";
        String getQueuesWithNodeOperation = "--getQueuesWithNode --nodeId <node id>\n";
        String removeNodeFromQueueOperation = "--removeNodeFromQueue --nodeId <node id> --queueName <queue name>\n";
        String submitJobOperation = "--submitJob --def <job def file> --input <job input constructor>\n";
        String submitJobRemoteOperation = "--submitJob --def <job def file> --input <job input constructor> --url <url>\n";
        String getJobInfoOperation = "--getJobInfo --id <job id>\n";
        String killOperation = "--kill --id <job id>\n";
        String getExecutionNodeOperation = "--getExecNode --id <job id>\n";

        String usage = "resmgr-client --url <url to xml rpc service> --operation "
                + "[<operation> [params]]\n"
                + "operations:\n"
                + getNodeByIdOperation
                + getNodesOperation
                + getQueuesOperation
                + addNodeOperation
                + removeNodeOperation
                + addQueueOperation
                + removeQueueOperation
                + addNodeToQueueOperation
                + getNodesInQueueOperation
                + getQueuesWithNodeOperation
                + removeNodeFromQueueOperation
                + submitJobOperation
                + submitJobRemoteOperation
                + getJobInfoOperation
                + killOperation + getExecutionNodeOperation;

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

        // create the client
        XmlRpcResourceManagerClient client = new XmlRpcResourceManagerClient(
                new URL(url));

        if (operation.equals("--getNodes")) {
            // no arguments to read, just call getNodes
            List resNodes = client.getNodes();

            if (resNodes != null && resNodes.size() > 0) {
                for (Iterator i = resNodes.iterator(); i.hasNext();) {
                    ResourceNode node = (ResourceNode) i.next();
                    System.out.println("node: [id=" + node.getNodeId()
                            + ",capacity=" + node.getCapacity() + ",url="
                            + node.getIpAddr() + "]");
                }
            }

        }else if (operation.equals("--getQueues")) {
            List<String> queueNames = client.getQueues();
            System.out.println("Queues:");
            for (String queueName : queueNames) 
                System.out.println(" - " + queueName);
            System.out.println();
            
        }else if (operation.equals("--addNode")) {
            String nodeId = null;
            String nodeUrl = null;
            String capacity = null;
            
            for (int i = 4; i < args.length; i++) {
                if (args[i].equals("--nodeId")) {
                    nodeId = args[++i];
                }else if (args[i].equals("--ipAddr")) {
                    nodeUrl = args[++i];
                }else if (args[i].equals("--capacity")) {
                    capacity = args[++i];
                }
            }
            
            if (nodeId == null || nodeUrl == null || capacity == null) {
                System.err.println(addNodeOperation);
                System.exit(1);
            }
                
            client.addNode(new ResourceNode(nodeId, new URL(nodeUrl), Integer.parseInt(capacity)));
            System.out.println("Successfully added node!");
            
        }else if (operation.equals("--removeNode")) {
            String nodeId = null;
            
            for (int i = 4; i < args.length; i++) {
                if (args[i].equals("--nodeId")) {
                    nodeId = args[++i];
                }
            }
            
            if (nodeId == null) {
                System.err.println(removeNodeOperation);
                System.exit(1);
            }
                
            client.removeNode(nodeId);
            System.out.println("Successfully removed node!");
            
        }else if (operation.equals("--setNodeCapacity")){
            String nodeId = null;
            String capacity = null;
            
            for (int i = 4; i < args.length; i++) {
                if (args[i].equals("--nodeId")) {
                    nodeId = args[++i];
                }else if (args[i].equals("--capacity")) {
                    capacity = args[++i];
                }
            }
            
            if (nodeId == null || capacity == null) {
                System.err.println(setNodeCapacityOperation);
                System.exit(1);
            }
            
            client.setNodeCapacity(nodeId, Integer.parseInt(capacity));
            System.out.println("Successfully set node capacity!");
        	
        }else if (operation.equals("--addQueue")) {
            String queueName = null;
            
            for (int i = 4; i < args.length; i++) {
                if (args[i].equals("--queueName")) {
                    queueName = args[++i];
                }
            }
            
            if (queueName == null) {
                System.err.println(addQueueOperation);
                System.exit(1);
            }
                
            client.addQueue(queueName);
            System.out.println("Successfully added queue!");
            
        }else if (operation.equals("--removeQueue")) {
            String queueName = null;
            
            for (int i = 4; i < args.length; i++) {
                if (args[i].equals("--queueName")) {
                    queueName = args[++i];
                }
            }
            
            if (queueName == null) {
                System.err.println(removeQueueOperation);
                System.exit(1);
            }
                
            client.removeQueue(queueName);
            System.out.println("Successfully removed queue!");
            
        }else if (operation.equals("--addNodeToQueue")) {
            String nodeId = null;
            String queueName = null;
            
            for (int i = 4; i < args.length; i++) {
                if (args[i].equals("--nodeId")) {
                    nodeId = args[++i];
                }else if (args[i].equals("--queueName")) {
                    queueName = args[++i];
                }
            }
            
            if (nodeId == null || queueName == null) {
                System.err.println(addNodeToQueueOperation);
                System.exit(1);
            }
                
            client.addNodeToQueue(nodeId, queueName);
            System.out.println("Successfully added node to queue!");
            
        }else if (operation.equals("--removeNodeFromQueue")) {
            String nodeId = null;
            String queueName = null;
            
            for (int i = 4; i < args.length; i++) {
                if (args[i].equals("--nodeId")) {
                    nodeId = args[++i];
                }else if (args[i].equals("--queueName")) {
                    queueName = args[++i];
                }
            }
            
            if (nodeId == null || queueName == null) {
                System.err.println(removeNodeFromQueueOperation);
                System.exit(1);
            }
                
            client.removeNodeFromQueue(nodeId, queueName);
            System.out.println("Successfully removed node from queue!");
            
        }else if (operation.equals("--getNodesInQueue")) {
            String queueName = null;
            
            for (int i = 4; i < args.length; i++) {
                if (args[i].equals("--queueName")) {
                    queueName = args[++i];
                }
            }
            
            if (queueName == null) {
                System.err.println(getNodesInQueueOperation);
                System.exit(1);
            }
                
            List<String> nodeIds = client.getNodesInQueue(queueName);
            System.out.println("Nodes in Queue '" + queueName + "':");
            for (String nodeId : nodeIds) 
                System.out.println(" - " + nodeId);
            System.out.println();
            
        }else if (operation.equals("--getQueuesWithNode")) {
            String nodeId = null;
            
            for (int i = 4; i < args.length; i++) {
                if (args[i].equals("--nodeId")) {
                    nodeId = args[++i];
                }
            }
            
            if (nodeId == null) {
                System.err.println(getQueuesWithNodeOperation);
                System.exit(1);
            }
                
            List<String> queueNames = client.getQueuesWithNode(nodeId);
            System.out.println("Queues with node '" + nodeId + "':");
            for (String queueName : queueNames) 
                System.out.println(" - " + queueName);
            System.out.println();
            
        } else if (operation.equals("--getExecNode")) {
            String jobId = null;

            for (int i = 4; i < args.length; i++) {
                if (args[i].equals("--id")) {
                    jobId = args[++i];
                }
            }

            if (jobId == null) {
                System.err.println(getExecutionNodeOperation);
                System.exit(1);

            }

            String execNode = client.getExecutionNode(jobId);
            if (execNode == null || (execNode != null && execNode.equals(""))) {
                System.out.println("Job: [" + jobId
                        + "] not executing on any known node!");
            } else {
                System.out.println(execNode);
            }
        } else if (operation.equals("--kill")) {
            String jobId = null;

            for (int i = 4; i < args.length; i++) {
                if (args[i].equals("--id")) {
                    jobId = args[++i];
                }
            }

            if (jobId == null) {
                System.err.println(killOperation);
                System.exit(1);

            }

            if (client.killJob(jobId)) {
                System.out.println("Job: [" + jobId + "] successfully killed.");
            } else {
                System.out.println("Unable to kill job: [" + jobId + "]");
            }

        } else if (operation.equals("--getNodeById")) {
            String nodeId = null;
            for (int i = 4; i < args.length; i++) {
                if (args[i].equals("--nodeId")) {
                    nodeId = args[++i];
                }
            }

            if (nodeId == null) {
                System.err.println(getNodeByIdOperation);
                System.exit(1);
            }

            ResourceNode node = client.getNodeById(nodeId);

            if (node != null) {
                System.out.println("node: [id=" + node.getNodeId()
                        + ",capacity=" + node.getCapacity() + ",url="
                        + node.getIpAddr() + "]");
            }
        } else if (operation.equals("--getJobInfo")) {
            String jobId = null;

            for (int i = 4; i < args.length; i++) {
                if (args[i].equals("--id")) {
                    jobId = args[++i];
                }
            }

            if (jobId == null) {
                System.err.println(getJobInfoOperation);
                System.exit(1);
            }

            Job jobInfo = client.getJobInfo(jobId);

            System.out.println("Job: [id=" + jobId + ", status="
                    + getReadableJobStatus(jobInfo.getStatus()) + ",name="
                    + jobInfo.getName() + ",queue=" + jobInfo.getQueueName()
                    + ",load=" + jobInfo.getLoadValue() + ",inputClass="
                    + jobInfo.getJobInputClassName() + ",instClass="
                    + jobInfo.getJobInstanceClassName() + "]");
        } else
            throw new IllegalArgumentException("Unknown Operation!");

    }

    public boolean isJobComplete(String jobId) throws JobRepositoryException {
        Vector argList = new Vector();
        argList.add(jobId);

        boolean complete = false;

        try {
            complete = ((Boolean) client.execute("resourcemgr.isJobComplete",
                    argList)).booleanValue();
        } catch (XmlRpcException e) {
            throw new JobRepositoryException(e.getMessage());
        } catch (IOException e) {
            throw new JobRepositoryException(e.getMessage());
        }

        return complete;
    }

    public Job getJobInfo(String jobId) throws JobRepositoryException {
        Vector argList = new Vector();
        argList.add(jobId);

        Hashtable jobHash = null;

        try {
            jobHash = (Hashtable) client.execute("resourcemgr.getJobInfo",
                    argList);
        } catch (XmlRpcException e) {
            throw new JobRepositoryException(e.getMessage());
        } catch (IOException e) {
            throw new JobRepositoryException(e.getMessage());
        }

        return XmlRpcStructFactory.getJobFromXmlRpc(jobHash);
    }

    public boolean isAlive() {
        Vector argList = new Vector();

        try {
            return ((Boolean) client.execute("resourcemgr.isAlive", argList))
                    .booleanValue();
        } catch (XmlRpcException e) {
            return false;
        } catch (IOException e) {
            return false;
        }

    }

    /**
     * Gets the number of Jobs in JobQueue
     * @return Number of Jobs in JobQueue
     * @throws JobRepositoryException On Any Exception
     */
    public int getJobQueueSize() throws JobRepositoryException {
        try {
            Vector argList = new Vector();
            return ((Integer) client.execute("resourcemgr.getJobQueueSize", argList));
        } catch (Exception e) {
            throw new JobRepositoryException("Failed to get JobQueue from server : " + e.getMessage(), e);
        }
    }
    
    /**
     * Gets the max number of Jobs allowed in JobQueue
     * @return Max number of Jobs
     * @throws JobRepositoryException On Any Exception
     */
    public int getJobQueueCapacity() throws JobRepositoryException {
        try {
            Vector argList = new Vector();
            return ((Integer) client.execute("resourcemgr.getJobQueueCapacity", argList));
        } catch (Exception e) {
            throw new JobRepositoryException("Failed to get JobQueue capacity from server : " + e.getMessage(), e);
        }
    }
    
    public boolean killJob(String jobId) {
        Vector argList = new Vector();
        argList.add(jobId);

        try {
            return ((Boolean) client.execute("resourcemgr.killJob", argList))
                    .booleanValue();
        } catch (XmlRpcException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    public String getExecutionNode(String jobId) {
        Vector argList = new Vector();
        argList.add(jobId);

        try {
            return (String) client.execute("resourcemgr.getExecutionNode", argList);
        } catch (XmlRpcException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    public String submitJob(Job exec, JobInput in) throws JobExecutionException {
        Vector argList = new Vector();
        argList.add(XmlRpcStructFactory.getXmlRpcJob(exec));
        argList.add(in.write());

        LOG.log(Level.FINEST, argList.toString());

        String jobId = null;

        try {
            jobId = (String) client.execute("resourcemgr.handleJob", argList);
        } catch (XmlRpcException e) {
            throw new JobExecutionException(e.getMessage());
        } catch (IOException e) {
            throw new JobExecutionException(e.getMessage());
        }

        return jobId;

    }

    public boolean submitJob(Job exec, JobInput in, URL hostUrl)
            throws JobExecutionException {
        Vector argList = new Vector();
        argList.add(XmlRpcStructFactory.getXmlRpcJob(exec));
        argList.add(in.write());
        argList.add(hostUrl.toString());

        boolean success = false;

        try {
            success = ((Boolean) client.execute("resourcemgr.handleJob",
                    argList)).booleanValue();
        } catch (XmlRpcException e) {
            throw new JobExecutionException(e.getMessage());
        } catch (IOException e) {
            throw new JobExecutionException(e.getMessage());
        }

        return success;

    }

    public List getNodes() throws MonitorException {
        Vector argList = new Vector();

        Vector nodeVector = null;

        try {
            nodeVector = (Vector) client.execute("resourcemgr.getNodes",
                    argList);
        } catch (XmlRpcException e) {
            throw new MonitorException(e.getMessage());
        } catch (IOException e) {
            throw new MonitorException(e.getMessage());
        }

        return XmlRpcStructFactory.getResourceNodeListFromXmlRpc(nodeVector);

    }

    public ResourceNode getNodeById(String nodeId) throws MonitorException {
        Vector argList = new Vector();
        argList.add(nodeId);

        Hashtable resNodeHash = null;

        try {
            resNodeHash = (Hashtable) client.execute("resourcemgr.getNodeById",
                    argList);
        } catch (XmlRpcException e) {
            throw new MonitorException(e.getMessage());
        } catch (IOException e) {
            throw new MonitorException(e.getMessage());
        }

        return XmlRpcStructFactory.getResourceNodeFromXmlRpc(resNodeHash);

    }

    /**
     * @return the resMgrUrl
     */
    public URL getResMgrUrl() {
        return resMgrUrl;
    }

    /**
     * @param resMgrUrl
     *            the resMgrUrl to set
     */
    public void setResMgrUrl(URL resMgrUrl) {
        this.resMgrUrl = resMgrUrl;
    }

    /**
     * Creates a queue with the given name
     * @param queueName The name of the queue to be created
     * @throws QueueManagerException on any error
     */
    public void addQueue(String queueName) throws QueueManagerException {
        try {
            Vector<Object> argList = new Vector<Object>();
            argList.add(queueName);
            client.execute("resourcemgr.addQueue", argList);
        }catch (Exception e) {
            throw new QueueManagerException(e.getMessage(), e);
        }
    }
    
    /**
     * Removes the queue with the given name
     * @param queueName The name of the queue to be removed
     * @throws QueueManagerException on any error
     */
    public void removeQueue(String queueName) throws QueueManagerException {
        try {
            Vector<Object> argList = new Vector<Object>();
            argList.add(queueName);
            client.execute("resourcemgr.removeQueue", argList);
        }catch (Exception e) {
            throw new QueueManagerException(e.getMessage(), e);
        }
    }
    
    /**
     * Adds a node
     * @param node The node to be added
     * @throws MonitorException on any error
     */
    public void addNode(ResourceNode node) throws MonitorException {
        try {
            Vector<Object> argList = new Vector<Object>();
            argList.add(XmlRpcStructFactory.getXmlRpcResourceNode(node));
            client.execute("resourcemgr.addNode", argList);
        }catch (Exception e) {
            throw new MonitorException(e.getMessage(), e);
        }
    }
    
    /**
     * Removes the node with the given id
     * @param nodeId The id of the node to be removed
     * @throws MonitorException on any error
     */
    public void removeNode(String nodeId) throws MonitorException {
        try {
            Vector<Object> argList = new Vector<Object>();
            argList.add(nodeId);
            client.execute("resourcemgr.removeNode", argList);
        }catch (Exception e) {
            throw new MonitorException(e.getMessage(), e);
        }
    }
    
    public void setNodeCapacity(String nodeId, int capacity) throws MonitorException{
    	try{
    		Vector<Object> argList = new Vector<Object>();
            argList.add(nodeId);
            argList.add(new Integer(capacity));
            client.execute("resourcemgr.setNodeCapacity", argList);
    	}catch (Exception e){
    		throw new MonitorException(e.getMessage(), e);
    	}
    }
    
    /**
     * Addes the node with given id to the queue with the given name
     * @param nodeId The id of the node to be added to the given queueName
     * @param queueName The name of the queue to add the given node
     * @throws QueueManagerException on any error
     */
    public void addNodeToQueue(String nodeId, String queueName) throws QueueManagerException {
        try {
            Vector<Object> argList = new Vector<Object>();
            argList.add(nodeId);
            argList.add(queueName);
            client.execute("resourcemgr.addNodeToQueue", argList);
        }catch (Exception e) {
            throw new QueueManagerException(e.getMessage(), e);
        }
    }
    
    /**
     * Remove the node with the given id from the queue with the given name
     * @param nodeId The id of the node to be remove from the given queueName
     * @param queueName The name of the queue from which to remove the given node
     * @throws QueueManagerException on any error
     */
    public void removeNodeFromQueue(String nodeId, String queueName) throws QueueManagerException {
        try {
            Vector<Object> argList = new Vector<Object>();
            argList.add(nodeId);
            argList.add(queueName);
            client.execute("resourcemgr.removeNodeFromQueue", argList);
        }catch (Exception e) {
            throw new QueueManagerException(e.getMessage(), e);
        }
    }
    
    /**
     * Gets a list of currently supported queue names
     * @return A list of currently supported queue names
     * @throws QueueManagerException on any error
     */
    public List<String> getQueues() throws QueueManagerException {
        try {
            Vector<Object> argList = new Vector<Object>();
            return (List<String>) client.execute("resourcemgr.getQueues", argList);
        }catch (Exception e) {
            throw new QueueManagerException(e.getMessage(), e);
        }
    }
    
    /**
     * Gets a list of ids of the nodes in the given queue
     * @param queueName The name of the queue to get node ids from
     * @return List of node ids in the given queueName
     * @throws QueueManagerException on any error
     */
    public List<String> getNodesInQueue(String queueName) throws QueueManagerException {
        try {
            Vector<Object> argList = new Vector<Object>();
            argList.add(queueName);
            return (List<String>) client.execute("resourcemgr.getNodesInQueue", argList);
        }catch (Exception e) {
            throw new QueueManagerException(e.getMessage(), e);
        }
    }
    
    /**
     * Gets a list of queues which contain the node with the given nodeId
     * @param nodeId The id of the node to get queues it belongs to
     * @return List of queues which contain the give node
     * @throws QueueManagerException on any error
     */
    public List<String> getQueuesWithNode(String nodeId) throws QueueManagerException {
        try {
            Vector<Object> argList = new Vector<Object>();
            argList.add(nodeId);
            return (List<String>) client.execute("resourcemgr.getQueuesWithNode", argList);
        }catch (Exception e) {
            throw new QueueManagerException(e.getMessage(), e);
        }
    }
    
    private static String getReadableJobStatus(String status) {
        if (status.equals(JobStatus.COMPLETE)) {
            return "COMPLETE";
        } else if (status.equals(JobStatus.EXECUTED)) {
            return "EXECUTED";
        } else if (status.equals(JobStatus.QUEUED)) {
            return "QUEUED";
        } else if (status.equals(JobStatus.SCHEDULED)) {
            return "SCHEDULED";
        } else if (status.equals(JobStatus.KILLED)) {
            return "KILLED";
        } else
            return null;
    }
}
