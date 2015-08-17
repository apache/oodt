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
import org.apache.oodt.cas.cli.CmdLineUtility;
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
public class XmlRpcResourceManagerClient implements ResourceManagerClient {

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

    public static void main(String[] args) {
       CmdLineUtility cmdLineUtility = new CmdLineUtility();
       cmdLineUtility.run(args);
    }

    @Override
    public boolean isJobComplete(String jobId) throws JobRepositoryException {
        Vector argList = new Vector();
        argList.add(jobId);

        boolean complete = false;

        try {
            complete = ((Boolean) client.execute("resourcemgr.isJobComplete",
                    argList)).booleanValue();
        } catch (XmlRpcException e) {
            throw new JobRepositoryException(e.getMessage(), e);
        } catch (IOException e) {
            throw new JobRepositoryException(e.getMessage(), e);
        }

        return complete;
    }

    @Override
    public Job getJobInfo(String jobId) throws JobRepositoryException {
        Vector argList = new Vector();
        argList.add(jobId);

        Hashtable jobHash = null;

        try {
            jobHash = (Hashtable) client.execute("resourcemgr.getJobInfo",
                    argList);
        } catch (XmlRpcException e) {
            throw new JobRepositoryException(e.getMessage(), e);
        } catch (IOException e) {
            throw new JobRepositoryException(e.getMessage(), e);
        }

        return XmlRpcStructFactory.getJobFromXmlRpc(jobHash);
    }

    @Override
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
    @Override
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
    @Override
    public int getJobQueueCapacity() throws JobRepositoryException {
        try {
            Vector argList = new Vector();
            return ((Integer) client.execute("resourcemgr.getJobQueueCapacity", argList));
        } catch (Exception e) {
            throw new JobRepositoryException("Failed to get JobQueue capacity from server : " + e.getMessage(), e);
        }
    }
    
    @Override
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

    @Override
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

    @Override
    public String submitJob(Job exec, JobInput in) throws JobExecutionException {
        Vector argList = new Vector();
        argList.add(XmlRpcStructFactory.getXmlRpcJob(exec));
        argList.add(in.write());

        LOG.log(Level.FINEST, argList.toString());

        String jobId = null;

        try {
            jobId = (String) client.execute("resourcemgr.handleJob", argList);
        } catch (XmlRpcException e) {
            throw new JobExecutionException(e.getMessage(), e);
        } catch (IOException e) {
            throw new JobExecutionException(e.getMessage(), e);
        }

        return jobId;

    }

    @Override
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
            throw new JobExecutionException(e.getMessage(), e);
        } catch (IOException e) {
            throw new JobExecutionException(e.getMessage(), e);
        }

        return success;

    }

    @Override
    public List getNodes() throws MonitorException {
        Vector argList = new Vector();

        Vector nodeVector = null;

        try {
            nodeVector = (Vector) client.execute("resourcemgr.getNodes",
                    argList);
        } catch (XmlRpcException e) {
            throw new MonitorException(e.getMessage(), e);
        } catch (IOException e) {
            throw new MonitorException(e.getMessage(), e);
        }

        return XmlRpcStructFactory.getResourceNodeListFromXmlRpc(nodeVector);

    }

    @Override
    public ResourceNode getNodeById(String nodeId) throws MonitorException {
        Vector argList = new Vector();
        argList.add(nodeId);

        Hashtable resNodeHash = null;

        try {
            resNodeHash = (Hashtable) client.execute("resourcemgr.getNodeById",
                    argList);
        } catch (XmlRpcException e) {
            throw new MonitorException(e.getMessage(), e);
        } catch (IOException e) {
            throw new MonitorException(e.getMessage(), e);
        }

        return XmlRpcStructFactory.getResourceNodeFromXmlRpc(resNodeHash);

    }

    /**
     * @return the resMgrUrl
     */
    @Override
    public URL getResMgrUrl() {
        return resMgrUrl;
    }

    /**
     * @param resMgrUrl
     *            the resMgrUrl to set
     */
    @Override
    public void setResMgrUrl(URL resMgrUrl) {
        this.resMgrUrl = resMgrUrl;
    }

    /**
     * Creates a queue with the given name
     * @param queueName The name of the queue to be created
     * @throws QueueManagerException on any error
     */
    @Override
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
    @Override
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
    @Override
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
    @Override
    public void removeNode(String nodeId) throws MonitorException {
        try {
            Vector<Object> argList = new Vector<Object>();
            argList.add(nodeId);
            client.execute("resourcemgr.removeNode", argList);
        }catch (Exception e) {
            throw new MonitorException(e.getMessage(), e);
        }
    }
    
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
    public List<String> getQueuesWithNode(String nodeId) throws QueueManagerException {
        try {
            Vector<Object> argList = new Vector<Object>();
            argList.add(nodeId);
            return (List<String>) client.execute("resourcemgr.getQueuesWithNode", argList);
        }catch (Exception e) {
            throw new QueueManagerException(e.getMessage(), e);
        }
    }
    
    /**
     * Report on the load of the requested node
     * @param nodeId The id of the node to be polled
     * @return A String showing a fraction of the loads node over its capacity
     * @throws MonitorException on any error
     */
    @Override
    public String getNodeLoad(String nodeId) throws MonitorException{
    	try{
	    	Vector<Object> argList = new Vector<Object>();
	    	argList.add(nodeId);
	    	return (String)client.execute("resourcemgr.getNodeLoad", argList);
    	}catch(Exception e){
    		throw new MonitorException(e.getMessage(), e);
    	}
    }

  private static String getReadableJobStatus(String status) {
    if (status.equals(JobStatus.SUCCESS)) {
      return "SUCCESS";
    } else if (status.equals(JobStatus.FAILURE)) {
      return "FAILURE";
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
