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
import org.apache.avro.ipc.NettyTransceiver;
import org.apache.avro.ipc.Transceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.apache.oodt.cas.cli.CmdLineUtility;
import org.apache.oodt.cas.resource.structs.AvroTypeFactory;
import org.apache.oodt.cas.resource.structs.Job;
import org.apache.oodt.cas.resource.structs.JobInput;
import org.apache.oodt.cas.resource.structs.ResourceNode;
import org.apache.oodt.cas.resource.structs.avrotypes.ResourceManager;
import org.apache.oodt.cas.resource.structs.exceptions.JobExecutionException;
import org.apache.oodt.cas.resource.structs.exceptions.JobQueueException;
import org.apache.oodt.cas.resource.structs.exceptions.JobRepositoryException;
import org.apache.oodt.cas.resource.structs.exceptions.MonitorException;
import org.apache.oodt.cas.resource.structs.exceptions.QueueManagerException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AvroRpcResourceManagerClient implements ResourceManagerClient {

    /* our log stream */
    private static Logger LOG = Logger
            .getLogger(AvroRpcResourceManagerClient.class.getName());

    /* resource manager url */
    private URL resMgrUrl = null;

    transient Transceiver client;
    transient ResourceManager proxy;

    public AvroRpcResourceManagerClient(URL url) {
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

        try {
            this.client = new NettyTransceiver(new InetSocketAddress(url.getHost(), url.getPort()));
            proxy = (ResourceManager) SpecificRequestor.getClient(ResourceManager.class, client);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        CmdLineUtility cmdLineUtility = new CmdLineUtility();
        cmdLineUtility.run(args);
    }


    @Override
    public boolean isJobComplete(String jobId) throws JobRepositoryException {
        try {
            return proxy.isJobComplete(jobId);
        } catch (AvroRemoteException e) {
            throw new JobRepositoryException(e);
        }
    }

    @Override
    public Job getJobInfo(String jobId) throws JobRepositoryException {
        try {
            return AvroTypeFactory.getJob(proxy.getJobInfo(jobId));
        } catch (AvroRemoteException e) {
            throw new JobRepositoryException(e);
        }
    }

    @Override
    public boolean isAlive() {
        try {
            return proxy.isAlive();
        } catch (AvroRemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public int getJobQueueSize() throws JobRepositoryException {
        try {
            return proxy.getJobQueueSize();
        } catch (AvroRemoteException e) {
            throw new JobRepositoryException(e);
        }
    }

    @Override
    public int getJobQueueCapacity() throws JobRepositoryException {
        try {
            return proxy.getJobQueueCapacity();
        } catch (AvroRemoteException e) {
            throw new JobRepositoryException(e);
        }
    }

    @Override
    public boolean killJob(String jobId) {
        try {
            return proxy.killJob(jobId);
        } catch (AvroRemoteException e) {
            LOG.log(Level.SEVERE,
                    "Server error!");
        }
        return false;
    }

    @Override
    public String getExecutionNode(String jobId) {
        try {
            return proxy.getExecutionNode(jobId);
        } catch (AvroRemoteException e) {
            LOG.log(Level.SEVERE,
                    "Server error!");
        }
        return null;
    }

    @Override
    public String getNodeReport() throws MonitorException {
        try {
            return proxy.getNodeReport();
        } catch (AvroRemoteException e) {
            LOG.log(Level.SEVERE, "Server error!");
        }
        return null;
    }

    @Override
    public String getExecReport() throws JobRepositoryException {
        try {
            return proxy.getExecReport();
        } catch (AvroRemoteException e) {
            LOG.log(Level.SEVERE, "Server error!");
        }
        return null;
    }

    @Override
    public String submitJob(Job exec, JobInput in) throws JobExecutionException {
        try {
            return proxy.handleJob(AvroTypeFactory.getAvroJob(exec), AvroTypeFactory.getAvroJobInput(in));
        } catch (AvroRemoteException e) {
            LOG.log(Level.SEVERE,
                    "Server error!");

        }
        return null;
    }

    @Override
    public boolean submitJob(Job exec, JobInput in, URL hostUrl) throws JobExecutionException {
        try {
            return proxy.handleJobWithUrl(AvroTypeFactory.getAvroJob(exec), AvroTypeFactory.getAvroJobInput(in), hostUrl.toString());
        } catch (AvroRemoteException e) {
            throw new JobExecutionException(e);
        }
    }

    @Override
    public List getNodes() throws MonitorException {
        try {
            return AvroTypeFactory.getListResourceNode(proxy.getNodes());
        } catch (AvroRemoteException e) {
            throw new MonitorException(e);
        }
    }

    @Override
    public ResourceNode getNodeById(String nodeId) throws MonitorException {
        try {
            return AvroTypeFactory.getResourceNode(proxy.getNodeById(nodeId));
        } catch (AvroRemoteException e) {
            throw new MonitorException(e);
        }
    }

    @Override
    public URL getResMgrUrl() {
        return this.resMgrUrl;
    }

    @Override
    public void setResMgrUrl(URL resMgrUrl) {
        this.resMgrUrl = resMgrUrl;
    }

    @Override
    public void addQueue(String queueName) throws QueueManagerException {
        try {
            proxy.addQueue(queueName);
        } catch (AvroRemoteException e) {
            throw new QueueManagerException(e);
        }
    }

    @Override
    public void removeQueue(String queueName) throws QueueManagerException {
        try {
            proxy.removeQueue(queueName);
        } catch (AvroRemoteException e) {
            throw new QueueManagerException(e);
        }

    }

    @Override
    public void addNode(ResourceNode node) throws MonitorException {
        try {
            proxy.addNode(AvroTypeFactory.getAvroResourceNode(node));
        } catch (AvroRemoteException e) {
            throw new MonitorException(e);
        }
    }

    @Override
    public void removeNode(String nodeId) throws MonitorException {
        try {
            proxy.removeNode(nodeId);
        } catch (AvroRemoteException e) {
            throw new MonitorException(e);
        }
    }

    @Override
    public void setNodeCapacity(String nodeId, int capacity) throws MonitorException {
        try {
            proxy.setNodeCapacity(nodeId, capacity);
        } catch (AvroRemoteException e) {
            throw new MonitorException(e);
        }
    }

    @Override
    public void addNodeToQueue(String nodeId, String queueName) throws QueueManagerException {
        try {
            proxy.addNodeToQueue(nodeId, queueName);
        } catch (AvroRemoteException e) {
            throw new QueueManagerException(e);
        }
    }

    @Override
    public void removeNodeFromQueue(String nodeId, String queueName) throws QueueManagerException {
        try {
            proxy.removeNodeFromQueue(nodeId, queueName);
        } catch (AvroRemoteException e) {
            throw new QueueManagerException(e);
        }
    }

    @Override
    public List<String> getQueues() throws QueueManagerException {
        try {
            return proxy.getQueues();
        } catch (AvroRemoteException e) {
            throw new QueueManagerException(e);
        }
    }

    @Override
    public List<String> getNodesInQueue(String queueName) throws QueueManagerException {
        try {
            return proxy.getNodesInQueue(queueName);
        } catch (AvroRemoteException e) {
            throw new QueueManagerException(e);
        }
    }

    @Override
    public List<String> getQueuesWithNode(String nodeId) throws QueueManagerException {
        try {
            return proxy.getQueuesWithNode(nodeId);
        } catch (AvroRemoteException e) {
            throw new QueueManagerException(e);
        }
    }

    @Override
    public String getNodeLoad(String nodeId) throws MonitorException {
        try {
            return proxy.getNodeLoad(nodeId);
        } catch (AvroRemoteException e) {
            throw new MonitorException(e);
        }
    }

    @Override
    public List getQueuedJobs() throws JobQueueException {
        try {
            return proxy.getQueuedJobs();
        } catch (AvroRemoteException e) {
            throw new JobQueueException(e);
        }
    }
}
