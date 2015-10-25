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
package org.apache.oodt.cas.resource.mux;

import org.apache.oodt.cas.resource.monitor.Monitor;
import org.apache.oodt.cas.resource.scheduler.QueueManager;
import org.apache.oodt.cas.resource.structs.ResourceNode;
import org.apache.oodt.cas.resource.structs.exceptions.MonitorException;
import org.apache.oodt.cas.resource.structs.exceptions.QueueManagerException;

import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author starchmd
 * @version $Revision$
 *
 * A monitor to monitor the multiple monitors.
 */
public class QueueMuxMonitor implements Monitor {
    private static final Logger LOG = Logger.getLogger(QueueMuxMonitor.class.getName());
    private BackendManager backend;
    private QueueManager qManager;
    /**
     * ctor
     * @param backend - backend manager
     * @param qManager - queue manager
     */
    public QueueMuxMonitor(BackendManager backend, QueueManager qManager) {
        setBackendManager(backend,qManager);
    }
    /**
     * Set the backend manager.
     * @param backend - backend manager effectively mapping queue's to sets of backends.
     */
    public void setBackendManager(BackendManager backend, QueueManager qManager) {
        this.backend = backend;
        this.qManager = qManager;
    }

    /* (non-Javadoc)
     * @see org.apache.oodt.cas.resource.monitor.Monitor#getLoad(org.apache.oodt.cas.resource.structs.ResourceNode)
     */
    @Override
    public int getLoad(ResourceNode node) throws MonitorException {
        //Unclear what to do here.
        //Assuming we should never be more than "Max"
        List<String> queues = queuesForNode(node);
        int max = 0;
        for (String queue : queues) {
            try {
                max = Math.max(max,backend.getMonitor(queue).getLoad(node));
            } catch (QueueManagerException e) {
                LOG.log(Level.WARNING,"Queue '"+queue+"' has dissappeared.");
            }
        }
        return max;
    }

    /* (non-Javadoc)
     * @see org.apache.oodt.cas.resource.monitor.Monitor#getNodes()
     */
    @Override
    public List<ResourceNode> getNodes() throws MonitorException {
        Set<ResourceNode> set = new LinkedHashSet<ResourceNode>();
        for (Monitor mon:this.backend.getMonitors()) {
            for (Object res:mon.getNodes()) {
                set.add((ResourceNode)res);
            }
        }
        return new LinkedList<ResourceNode>(set);
    }

    /* (non-Javadoc)
     * @see org.apache.oodt.cas.resource.monitor.Monitor#getNodeById(java.lang.String)
     */
    @Override
    public ResourceNode getNodeById(String nodeId) throws MonitorException {
        ResourceNode node = null;
        Iterator<Monitor> imon = this.backend.getMonitors().iterator();
        while(imon.hasNext() && (node = imon.next().getNodeById(nodeId)) == null) {} 
        return node;
    }

    /* (non-Javadoc)
     * @see org.apache.oodt.cas.resource.monitor.Monitor#getNodeByURL(java.net.URL)
     */
    @Override
    public ResourceNode getNodeByURL(URL ipAddr) throws MonitorException {
        ResourceNode node = null;
        Iterator<Monitor> imon = this.backend.getMonitors().iterator();
        while(imon.hasNext() && (node = imon.next().getNodeByURL(ipAddr)) == null) {} 
        return node;
    }

    /* (non-Javadoc)
     * @see org.apache.oodt.cas.resource.monitor.Monitor#reduceLoad(org.apache.oodt.cas.resource.structs.ResourceNode, int)
     */
    @Override
    public boolean reduceLoad(ResourceNode node, int loadValue)
            throws MonitorException {
        List<String> queues = queuesForNode(node);
        boolean ret = true;
        for (String queue:queues) {
            try {
                ret &= backend.getMonitor(queue).reduceLoad(node, loadValue);
            } catch (QueueManagerException e) {
                LOG.log(Level.SEVERE,"Queue '"+queue+"' has dissappeared.");
                throw new MonitorException(e);
            }
        }
        return ret;
    }

    /* (non-Javadoc)
     * @see org.apache.oodt.cas.resource.monitor.Monitor#assignLoad(org.apache.oodt.cas.resource.structs.ResourceNode, int)
     */
    @Override
    public boolean assignLoad(ResourceNode node, int loadValue)
            throws MonitorException {
        List<String> queues = queuesForNode(node);
        boolean ret = true;
        for (String queue:queues) {
            try {
                ret &= backend.getMonitor(queue).assignLoad(node, loadValue);
            } catch (QueueManagerException e) {
                LOG.log(Level.SEVERE,"Queue '"+queue+"' has dissappeared.");
                throw new MonitorException(e);
            }
        }
        return ret;
    }

    /* (non-Javadoc)
     * @see org.apache.oodt.cas.resource.monitor.Monitor#addNode(org.apache.oodt.cas.resource.structs.ResourceNode)
     */
    @Override
    public void addNode(ResourceNode node) throws MonitorException {
        List<String> queues = queuesForNode(node);
        for (String queue:queues) {
            try {
                backend.getMonitor(queue).addNode(node);
            } catch (QueueManagerException e) {
                LOG.log(Level.SEVERE,"Queue '"+queue+"' has dissappeared.");
                throw new MonitorException(e);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.apache.oodt.cas.resource.monitor.Monitor#removeNodeById(java.lang.String)
     */
    @Override
    public void removeNodeById(String nodeId) throws MonitorException {
        for (Monitor mon:this.backend.getMonitors()) {
            mon.removeNodeById(nodeId);
        }
    }
    /**
     * Gets the queues that are associated with a particular node.
     * @param node - node which queues are needed for
     * @return list of queue names on that node
     */
    private List<String> queuesForNode(ResourceNode node) {
        List<String> ret = new LinkedList<String>();
        //Get list of queues
        List<String> queues = null;
        queues = qManager.getQueues();
        //Search each queu to see if it contains given node
        for (String queue : queues) {
            try
            {
                if (qManager.getNodes(queue).contains(node.getNodeId())) {
                    ret.add(queue);
                }
            } catch(QueueManagerException e) {
                LOG.log(Level.SEVERE, "Queue '"+queue+"' has dissappeared.");
            }
        }
        return ret;
    }
}
