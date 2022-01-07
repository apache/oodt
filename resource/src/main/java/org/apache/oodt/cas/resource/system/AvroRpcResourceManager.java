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
import org.apache.oodt.cas.resource.structs.AvroTypeFactory;
import org.apache.oodt.cas.resource.structs.Job;
import org.apache.oodt.cas.resource.structs.JobInput;
import org.apache.oodt.cas.resource.structs.JobSpec;
import org.apache.oodt.cas.resource.structs.ResourceNode;
import org.apache.oodt.cas.resource.structs.avrotypes.AvroJob;
import org.apache.oodt.cas.resource.structs.avrotypes.AvroJobInput;
import org.apache.oodt.cas.resource.structs.avrotypes.AvroResourceNode;
import org.apache.oodt.cas.resource.structs.exceptions.JobExecutionException;
import org.apache.oodt.cas.resource.structs.exceptions.JobQueueException;
import org.apache.oodt.cas.resource.structs.exceptions.JobRepositoryException;
import org.apache.oodt.cas.resource.structs.exceptions.MonitorException;
import org.apache.oodt.cas.resource.structs.exceptions.QueueManagerException;
import org.apache.oodt.cas.resource.structs.exceptions.SchedulerException;
import org.apache.oodt.cas.resource.util.GenericResourceManagerObjectFactory;
import org.apache.oodt.cas.resource.util.ResourceNodeComparator;
import org.apache.oodt.config.Component;
import org.apache.oodt.config.ConfigurationManager;
import org.apache.oodt.config.ConfigurationManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AvroRpcResourceManager implements org.apache.oodt.cas.resource.structs.avrotypes.ResourceManager, ResourceManager {

    private static final Logger logger = LoggerFactory.getLogger(AvroRpcResourceManager.class);

    private int port = 2000;
    private Server server;
    /** our scheduler */
    private Scheduler scheduler;
    /** Configuration Manager instance of this instance */
    private ConfigurationManager configurationManager;
    private ExecutorService executorService;

    public AvroRpcResourceManager(int port) {
        this.port = port;

        List<String> propertiesFiles = new ArrayList<>();
        // set up the configuration, if there is any
        if (System.getProperty(ResourceManager.RESMGR_PROPERTIES_FILE_SYSTEM_PROPERTY) != null) {
            propertiesFiles.add(System.getProperty(ResourceManager.RESMGR_PROPERTIES_FILE_SYSTEM_PROPERTY));
        }

        configurationManager = ConfigurationManagerFactory
                .getConfigurationManager(Component.RESOURCE_MANAGER, propertiesFiles);
    }

    @Override
    public void startUp() throws Exception {
        try {
            configurationManager.loadConfiguration();
        } catch (Exception e) {
            logger.error("Unable to load configuration", e);
            throw new IOException("Unable to load configuration", e);
        }

        String schedulerClassStr = System.getProperty("resource.scheduler.factory",
                "org.apache.oodt.cas.resource.scheduler.LRUSchedulerFactory");
        scheduler = GenericResourceManagerObjectFactory.getSchedulerServiceFromFactory(schedulerClassStr);

        // start up the scheduler
        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(scheduler);

        // start up the web server
        server = new NettyServer(new SpecificResponder(org.apache.oodt.cas.resource.structs.avrotypes.ResourceManager.class, this),
                new InetSocketAddress(this.port));
        server.start();

        logger.info("Resource Manager started by {}", System.getProperty("user.name", "unknown"));
    }

    @Override
    public boolean isAlive() {
        return true;
    }

    @Override
    public int getJobQueueSize() throws AvroRemoteException {
        try {
            return this.scheduler.getJobQueue().getSize();
        } catch (Exception e) {
            throw new AvroRemoteException(new JobRepositoryException("Failed to get size of JobQueue : " + e.getMessage(), e));
        }
    }


    @Override
    public int getJobQueueCapacity() throws AvroRemoteException {
        try {
            return this.scheduler.getJobQueue().getCapacity();
        } catch (Exception e) {
            throw new AvroRemoteException(new JobRepositoryException("Failed to get capacity of JobQueue : " + e.getMessage(), e));
        }
    }

    @Override
    public boolean isJobComplete(String jobId) throws AvroRemoteException {
        try {
            JobSpec spec = scheduler.getJobQueue().getJobRepository().getJobById(
                    jobId);
            return scheduler.getJobQueue().getJobRepository().jobFinished(spec);

        } catch (JobRepositoryException e) {
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
            logger.warn("Exception communicating with job repository for job: [{}]: Message: {}", jobId, e.getMessage());
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
            return genericHandleJob(exec, in, hostUrl);
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
            logger.warn("Attempt to kill job: [{}]: cannot find execution node (has the job already finished?)", jobId);
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
            logger.warn("Job: [{}] not currently executing on any known node", jobId);
            return "";
        } else
            return execNode;
    }

    @Override
    public String getNodeReport() {
        StringBuilder report = new StringBuilder();

        try {

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
        } catch (Exception e) {
            return null;
        }

        return report.toString();
    }

    public List<AvroJob> getQueuedJobs() {
        List<AvroJob> jobs = new ArrayList<>();
        List jobSpecs = this.scheduler.getJobQueue().getQueuedJobs();

        if (jobSpecs != null && jobSpecs.size() > 0) {
            for (Object jobSpec : jobSpecs) {
                Job job = ((JobSpec) jobSpec).getJob();
                jobs.add(AvroTypeFactory.getAvroJob(job));
            }
        }

        return jobs;
    }

    @Override
    public String getExecReport() {
        StringBuilder report = new StringBuilder();

        try {

            // get a sorted list of all nodes, since the report should be
            // alphabetically sorted by node
            List resNodes = scheduler.getMonitor().getNodes();
            if (resNodes.size() == 0) {
                throw new MonitorException(
                        "No jobs can be executing, as there are no nodes in the Monitor");
            }
            Vector<String> nodeIds = new Vector<String>();
            for (Object resNode : resNodes) {
                nodeIds.add(((ResourceNode) resNode).getNodeId());
            }
            Collections.sort(nodeIds);

            // generate the report string
            for (String nodeId : nodeIds) {
                List execJobIds = this.scheduler.getBatchmgr().getJobsOnNode(nodeId);
                if (execJobIds != null && execJobIds.size() > 0) {
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

        } catch (Exception e) {
            return null;
        }

        return report.toString();
    }

    @Override
    public List<String> getQueues() throws AvroRemoteException {
        try {
            return this.scheduler.getQueueManager().getQueues();
        } catch (Exception e) {
            throw new AvroRemoteException(e);
        }
    }

    @Override
    public boolean addQueue(String queueName) throws AvroRemoteException {
        try {
            this.scheduler.getQueueManager().addQueue(queueName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;

    }

    @Override
    public boolean removeQueue(String queueName) throws AvroRemoteException {
        try {
            this.scheduler.getQueueManager().removeQueue(queueName);
        } catch (Exception e) {
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
        try {
            for (String queueName : this.getQueuesWithNode(nodeId)) {
                this.removeNodeFromQueue(nodeId, queueName);
            }
            this.scheduler.getMonitor().removeNodeById(nodeId);
        } catch (Exception e) {
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
        } catch (Exception e) {
            throw new AvroRemoteException(e);
        }
    }

    @Override
    public boolean shutdown() {
        configurationManager.clearConfiguration();
        executorService.shutdownNow();

        if (this.server != null) {
            this.server.close();
            this.server = null;
            return true;
        } else {
            return false;
        }
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

        for (; ; )
            try {
                Thread.currentThread().join();
            } catch (InterruptedException ignore) {
            }
    }


    @Override
    public boolean setNodeCapacity(String nodeId, int capacity) throws AvroRemoteException {
        try {
            this.scheduler.getMonitor().getNodeById(nodeId).setCapacity(capacity);
        } catch (MonitorException e) {
            logger.warn("Exception setting capacity on node {}: {}", nodeId, e.getMessage());
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
            logger.warn("JobQueue exception adding job: Message: {}", e.getMessage());
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
            logger.warn("Error converting string: [{}] to URL object: Message: {}", urlStr, e.getMessage());
        }

        return url;
    }

}
