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


package gov.nasa.jpl.oodt.cas.resource.scheduler;

//JDKimports
import java.lang.Integer;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

//OODT imports
import gov.nasa.jpl.oodt.cas.resource.jobqueue.JobQueue;
import gov.nasa.jpl.oodt.cas.resource.monitor.Monitor;
import gov.nasa.jpl.oodt.cas.resource.batchmgr.Batchmgr;
import gov.nasa.jpl.oodt.cas.commons.xml.XMLUtils;
import gov.nasa.jpl.oodt.cas.resource.structs.JobSpec;
import gov.nasa.jpl.oodt.cas.resource.structs.ResourceNode;
import gov.nasa.jpl.oodt.cas.resource.structs.exceptions.JobExecutionException;
import gov.nasa.jpl.oodt.cas.resource.structs.exceptions.JobQueueException;
import gov.nasa.jpl.oodt.cas.resource.structs.exceptions.MonitorException;
import gov.nasa.jpl.oodt.cas.resource.structs.exceptions.SchedulerException;
import gov.nasa.jpl.oodt.cas.resource.util.XmlStructFactory;

/**
 * 
 * @author woollard
 * @version $Revision$
 * 
 * <p>
 * An implementation of a {@link Scheduler} that uses a <a
 * href="http://en.wikipedia.org/wiki/Cache_algorithms">least-recently-used</a>
 * algorithm for scheduling {@link Job}s.
 * </p>
 */
public class LRUScheduler implements Scheduler {

    /* our log stream */
    private static final Logger LOG = Logger.getLogger(LRUScheduler.class
            .getName());

    /* list of URI pointers to dirs containing node-to-queue-mapping.xml files */
    private List queuesHomeUris = null;

    /* a map of String queueId->List of nodeIds */
    private HashMap queues;

    /* the monitor we'll use to check the status of the resources */
    private Monitor myMonitor;

    /* the batch mgr we'll use to execute jobs */
    private Batchmgr myBatchmgr;

    /* our job queue */
    private JobQueue myJobQueue;

    /* our wait time between checking the queue */
    private int waitTime = -1;

    private static FileFilter queuesXmlFilter = new FileFilter() {
        public boolean accept(File pathname) {
            return pathname.isFile()
                    && pathname.toString()
                            .endsWith("node-to-queue-mapping.xml");
        }
    };

    public LRUScheduler(List uris, Monitor m, Batchmgr b, JobQueue q) {
        queues = new HashMap();

        queuesHomeUris = uris;
        loadNodeMappingInfo(queuesHomeUris);

        myMonitor = m;
        myBatchmgr = b;
        myJobQueue = q;

        String waitStr = System.getProperty(
                "gov.nasa.jpl.oodt.cas.resource.scheduler.wait.seconds", "20");
        waitTime = Integer.parseInt(waitStr);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    public void run() {
        for (;;) {

            try {
                Thread.currentThread().sleep((long) waitTime * 1000);
            } catch (InterruptedException ignore) {
            }

            if (!myJobQueue.isEmpty()) {
                JobSpec exec = null;

                try {
                    exec = myJobQueue.getNextJob();
                    LOG.log(Level.INFO, "Obtained Job: ["
                            + exec.getJob().getId()
                            + "] from Queue: Scheduling for execution");
                } catch (JobQueueException e) {
                    LOG.log(Level.WARNING,
                            "Error getting next job from JobQueue: Message: "
                                    + e.getMessage());
                    continue;
                }

                try {
                    schedule(exec);
                } catch (SchedulerException e) {
                    LOG.log(Level.WARNING, "Error scheduling job: ["
                            + exec.getJob().getId() + "]: Message: "
                            + e.getMessage());
                    // place the job spec back on the queue
                    try {
                        myJobQueue.addJob(exec);
                    } catch (Exception ignore) {
                    }
                }
            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.resource.scheduler.Scheduler#schedule(gov.nasa.jpl.oodt.cas.resource.structs.JobSpec)
     */
    public synchronized boolean schedule(JobSpec spec)
            throws SchedulerException {
        String queueName = spec.getJob().getQueueName();
        int load = spec.getJob().getLoadValue().intValue();

        Vector queueNodes = (Vector) queues.get(queueName);
        ResourceNode node = nodeAvailable(spec);

        if (node != null) {
            try {
                myMonitor.assignLoad(node, load);
                queueNodes.remove(node.getNodeId());
                queueNodes.add(node.getNodeId());
                // assign via batch system
                LOG.log(Level.INFO, "Assigning job: ["
                        + spec.getJob().getName() + "] to node: ["
                        + node.getNodeId() + "]");
                try {
                    myBatchmgr.executeRemotely(spec, node);
                } catch (JobExecutionException e) {
                    LOG.log(Level.WARNING, "Exception executing job: ["
                            + spec.getJob().getId() + "] to node: ["
                            + node.getIpAddr() + "]: Message: "
                            + e.getMessage());
                    try {
                        // queue the job back up
                        LOG.log(Level.INFO, "Requeueing job: ["
                                + spec.getJob().getId() + "]");
                        myJobQueue.addJob(spec);

                        // make sure to decrement the load
                        myMonitor.reduceLoad(node, load);
                    } catch (Exception ignore) {
                    }
                }
            } catch (MonitorException e) {
                LOG.log(Level.WARNING, "Exception assigning load to resource "
                        + "node: [" + node.getNodeId() + "]: load: [" + load
                        + "]: Message: " + e.getMessage());
                throw new SchedulerException(e.getMessage());
            }
        } else {
            // could not find resource, push onto JobQueue
            try {
                myJobQueue.addJob(spec);
            } catch (Exception ignore) {
            }
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.resource.scheduler.Scheduler#getBatchmgr()
     */
    public Batchmgr getBatchmgr() {
        return myBatchmgr;
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.resource.scheduler.Scheduler#getMonitor()
     */
    public Monitor getMonitor() {
        return myMonitor;
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.resource.scheduler.Scheduler#getJobQueue()
     */
    public JobQueue getJobQueue() {
        return myJobQueue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.resource.scheduler.Scheduler#nodeAvailable(gov.nasa.jpl.oodt.cas.resource.structs.JobSpec)
     */
    public synchronized ResourceNode nodeAvailable(JobSpec spec)
            throws SchedulerException {
        String queueName = spec.getJob().getQueueName();
        int load = spec.getJob().getLoadValue().intValue();

        Vector queueNodes = (Vector) queues.get(queueName);
        for (int i = 0; i < queueNodes.size(); i++) {
            String nodeId = (String) queueNodes.get(i);
            int nodeLoad = -1;
            ResourceNode resNode = null;

            try {
                resNode = myMonitor.getNodeById(nodeId);
                nodeLoad = myMonitor.getLoad(resNode);
            } catch (MonitorException e) {
                LOG
                        .log(Level.WARNING, "Exception getting load on "
                                + "node: [" + resNode.getNodeId()
                                + "]: Message: " + e.getMessage());
                throw new SchedulerException(e.getMessage());
            }

            if (load <= nodeLoad) {
                return resNode;
            }
        }

        return null;
    }

    private HashMap loadNodeMappingInfo(List dirUris) {

        HashMap resources = new HashMap();

        if (dirUris != null && dirUris.size() > 0) {
            for (Iterator i = dirUris.iterator(); i.hasNext();) {
                String dirUri = (String) i.next();

                try {
                    File nodesDir = new File(new URI(dirUri));
                    if (nodesDir.isDirectory()) {

                        String nodesDirStr = nodesDir.getAbsolutePath();

                        if (!nodesDirStr.endsWith("/")) {
                            nodesDirStr += "/";
                        }

                        // get all the workflow xml files
                        File[] nodesFiles = nodesDir.listFiles(queuesXmlFilter);

                        for (int j = 0; j < nodesFiles.length; j++) {

                            String nodesXmlFile = nodesFiles[j]
                                    .getAbsolutePath();
                            Document nodesRoot = null;
                            try {
                                nodesRoot = XMLUtils
                                        .getDocumentRoot(new FileInputStream(
                                                nodesFiles[j]));
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                                return null;
                            }

                            NodeList nodeList = nodesRoot
                                    .getElementsByTagName("node");

                            if (nodeList != null && nodeList.getLength() > 0) {
                                for (int k = 0; k < nodeList.getLength(); k++) {

                                    String nodeId = ((Element) nodeList.item(k))
                                            .getAttribute("id");
                                    Vector assignments = (Vector) XmlStructFactory
                                            .getQueueAssignment((Element) nodeList
                                                    .item(k));
                                    for (int l = 0; l < assignments.size(); l++) {
                                        if (!queues
                                                .containsKey((String) assignments
                                                        .get(l))) {
                                            queues.put((String) assignments
                                                    .get(l), new Vector());
                                        }
                                        ((Vector) queues
                                                .get((String) assignments
                                                        .get(l))).add(nodeId);
                                    }
                                }
                            }
                        }
                    }
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                    LOG
                            .log(
                                    Level.WARNING,
                                    "DirUri: "
                                            + dirUri
                                            + " is not a directory: skipping node loading for it.");
                }
            }
        }

        return resources;
    }

}
