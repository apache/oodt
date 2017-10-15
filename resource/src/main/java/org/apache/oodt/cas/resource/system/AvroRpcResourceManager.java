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

import org.apache.avro.AvroRemoteException;
import org.apache.avro.ipc.NettyServer;
import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.specific.SpecificResponder;
import org.apache.oodt.cas.resource.scheduler.Scheduler;
import org.apache.oodt.cas.resource.structs.*;
import org.apache.oodt.cas.resource.structs.avrotypes.*;
import org.apache.oodt.cas.resource.structs.exceptions.*;
import org.apache.oodt.cas.resource.util.GenericResourceManagerObjectFactory;
import org.apache.oodt.cas.resource.util.XmlRpcStructFactory;
import org.apache.xmlrpc.WebServer;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AvroRpcResourceManager implements org.apache.oodt.cas.resource.structs.avrotypes.ResourceManager, ResourceManager{

    private int port = 2000;

    private Logger LOG = Logger
            .getLogger(XmlRpcResourceManager.class.getName());

    private Server server;

    /* our scheduler */
    private Scheduler scheduler = null;

    public AvroRpcResourceManager(int port)  throws Exception{
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

        this.port = port;

        // start up the web server
        server = new NettyServer(new SpecificResponder(org.apache.oodt.cas.resource.structs.avrotypes.ResourceManager.class,this),
                new InetSocketAddress(this.port));
        server.start();

        LOG.log(Level.INFO, "Resource Manager started by "
                + System.getProperty("user.name", "unknown"));

    }

    @Override
    public boolean isAlive() throws AvroRemoteException {
        return true;
    }

    @Override
    public int getJobQueueSize() throws AvroRemoteException {
        try {
            return this.scheduler.getJobQueue().getSize();
        }catch (Exception e) {
            throw new AvroRemoteException(new JobRepositoryException("Failed to get size of JobQueue : " + e.getMessage(), e));
        }
    }


    @Override
    public int getJobQueueCapacity() throws AvroRemoteException {
        try {
            return this.scheduler.getJobQueue().getCapacity();
        }catch (Exception e) {
            throw new AvroRemoteException(new JobRepositoryException("Failed to get capacity of JobQueue : " + e.getMessage(), e));
        }
    }

    @Override
    public boolean isJobComplete(String jobId) throws AvroRemoteException {
        try {
            JobSpec spec = scheduler.getJobQueue().getJobRepository().getJobById(
                    jobId);
            return scheduler.getJobQueue().getJobRepository().jobFinished(spec);

        } catch(JobRepositoryException e ){
            throw new AvroRemoteException(e);
        }
    }
    @Override
    public AvroJob getJobInfo(String jobId) throws AvroRemoteException {
        JobSpec spec = null;

        try {
            spec = scheduler.getJobQueue().getJobRepository()
                    .getJobById(jobId);
        } catch (JobRepositoryException e) {
            LOG.log(Level.WARNING,
                    "Exception communicating with job repository for job: ["
                            + jobId + "]: Message: " + e.getMessage());
            throw new AvroRemoteException(new JobRepositoryException("Unable to get job: [" + jobId
                    + "] from repository!"));
        }

        return AvroTypeFactory.getAvroJob(spec.getJob());

    }

    @Override
    public String handleJob(AvroJob exec, AvroJobInput into) throws AvroRemoteException {
        try {
            return genericHandleJob(exec, into);
        } catch (SchedulerException e) {
            throw new AvroRemoteException(e);
        }
    }

    @Override
    public boolean handleJobWithUrl(AvroJob exec, AvroJobInput in, String hostUrl) throws AvroRemoteException {
        try {
            return genericHandleJob(exec,in,hostUrl);
        } catch (JobExecutionException e) {
            throw new AvroRemoteException(e);
        }
    }

    @Override
    public List<AvroResourceNode> getNodes() throws AvroRemoteException {

        List resNodes = null;
        try {
            resNodes = scheduler.getMonitor().getNodes();
        } catch (MonitorException e) {
            throw new AvroRemoteException(e);
        }

        return AvroTypeFactory.getListAvroResourceNode(resNodes);
    }

    @Override
    public AvroResourceNode getNodeById(String nodeId) throws AvroRemoteException {
        ResourceNode node = null;
        try {
            node = scheduler.getMonitor().getNodeById(nodeId);
        } catch (MonitorException e) {
            throw new AvroRemoteException(e);
        }
        return AvroTypeFactory.getAvroResourceNode(node);
    }

    @Override
    public boolean killJob(String jobId) throws AvroRemoteException {
        String resNodeId = scheduler.getBatchmgr().getExecutionNode(jobId);
        if (resNodeId == null) {
            LOG.log(Level.WARNING, "Attempt to kill job: [" + jobId
                    + "]: cannot find execution node"
                    + " (has the job already finished?)");
            return false;
        }
        ResourceNode node = null;
        try {
            node = scheduler.getMonitor().getNodeById(resNodeId);
        } catch (MonitorException e) {
            throw new AvroRemoteException(e);
        }
        return scheduler.getBatchmgr().killJob(jobId, node);

    }

    @Override
    public String getExecutionNode(String jobId) throws AvroRemoteException {
        String execNode = scheduler.getBatchmgr().getExecutionNode(jobId);
        if (execNode == null) {
            LOG.log(Level.WARNING, "Job: [" + jobId
                    + "] not currently executing on any known node");
            return "";
        } else
            return execNode;
    }

    @Override
    public List<String> getQueues() throws AvroRemoteException {
        try {
            return this.scheduler.getQueueManager().getQueues();
        } catch (QueueManagerException e) {
            throw new AvroRemoteException(e);
        }
    }

    @Override
    public boolean addQueue(String queueName) throws AvroRemoteException {
        try {
            this.scheduler.getQueueManager().addQueue(queueName);
        } catch (QueueManagerException e) {
            e.printStackTrace();
        }
        return true;

    }

    @Override
    public boolean removeQueue(String queueName) throws AvroRemoteException {
        try {
            this.scheduler.getQueueManager().removeQueue(queueName);
        } catch (QueueManagerException e) {
            throw new AvroRemoteException(e);
        }
        return true;

    }

    @Override
    public boolean addNode(AvroResourceNode node) throws AvroRemoteException {
        try {
            this.scheduler.getMonitor().addNode(AvroTypeFactory.getResourceNode(node));
        } catch (MonitorException e) {
            throw new AvroRemoteException(e);
        }
        return true;
    }

    @Override
    public boolean removeNode(String nodeId) throws AvroRemoteException {
        try{
            for(String queueName: this.getQueuesWithNode(nodeId)){
                this.removeNodeFromQueue(nodeId, queueName);
            }
            this.scheduler.getMonitor().removeNodeById(nodeId);
        }catch(Exception e){
            throw new AvroRemoteException(new MonitorException(e.getMessage(), e));
        }

        return true;
    }

    @Override
    public boolean addNodeToQueue(String nodeId, String queueName) throws AvroRemoteException {
        try {
            this.scheduler.getQueueManager().addNodeToQueue(nodeId, queueName);
        } catch (QueueManagerException e) {
            throw new AvroRemoteException(e);
        }
        return true;

    }

    @Override
    public boolean removeNodeFromQueue(String nodeId, String queueName) throws AvroRemoteException {
        try {
            this.scheduler.getQueueManager().removeNodeFromQueue(nodeId, queueName);
        } catch (QueueManagerException e) {
            throw new AvroRemoteException(e);
        }
        return true;

    }

    @Override
    public List<String> getNodesInQueue(String queueName) throws AvroRemoteException {
        try {
            return this.scheduler.getQueueManager().getNodes(queueName);
        } catch (QueueManagerException e) {
            throw new AvroRemoteException(e);
        }
    }

    @Override
    public List<String> getQueuesWithNode(String nodeId) throws AvroRemoteException {
        try {
            return this.scheduler.getQueueManager().getQueues(nodeId);
        } catch (QueueManagerException e) {
            throw new AvroRemoteException(e);
        }
    }

    public boolean shutdown(){
        if (this.server != null) {
            this.server.close();
            this.server = null;
            return true;
        } else
            return false;
    }

    @Override
    public String getNodeLoad(String nodeId) throws AvroRemoteException {
        ResourceNode node = null;
        try {
            node = this.scheduler.getMonitor().getNodeById(nodeId);
            int capacity = node.getCapacity();
            int load = (this.scheduler.getMonitor().getLoad(node)) * -1 + capacity;
            return load + "/" + capacity;
        } catch (MonitorException e) {
            throw new AvroRemoteException(e);
        }
    }

    public static void main(String[] args) throws Exception {
        int portNum = -1;
        String usage = "AvroRpcResourceManager --portNum <port number for xml rpc service>\n";

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--portNum")) {
                portNum = Integer.parseInt(args[++i]);
            }
        }

        if (portNum == -1) {
            System.err.println(usage);
            System.exit(1);
        }

        AvroRpcResourceManager manager = new AvroRpcResourceManager(portNum);

        for (;;)
            try {
                Thread.currentThread().join();
            } catch (InterruptedException ignore) {
            }
    }


    @Override
    public boolean setNodeCapacity(String nodeId, int capacity) throws AvroRemoteException {
        try{
            this.scheduler.getMonitor().getNodeById(nodeId).setCapacity(capacity);
        }catch (MonitorException e){
            LOG.log(Level.WARNING, "Exception setting capacity on node "
                    + nodeId + ": " + e.getMessage());
            return false;
        }
        return true;

    }


    private String genericHandleJob(AvroJob avroJob, AvroJobInput avroJobInput)
            throws SchedulerException {

        Job exec = AvroTypeFactory.getJob(avroJob);
        JobInput in = AvroTypeFactory.getJobInput(avroJobInput);
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

    private boolean genericHandleJob(AvroJob avroJob, AvroJobInput avroJobInput,
                                     String urlStr) throws JobExecutionException {
        Job exec = AvroTypeFactory.getJob(avroJob);
        JobInput in = AvroTypeFactory.getJobInput(avroJobInput);

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
