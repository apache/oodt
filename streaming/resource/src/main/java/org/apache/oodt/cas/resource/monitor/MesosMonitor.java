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
package org.apache.oodt.cas.resource.monitor;

import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.oodt.cas.resource.structs.ResourceNode;
import org.apache.oodt.cas.resource.structs.exceptions.MonitorException;

/**
 * @author starchmd
 * @version $Revision$
 *
 * A monitor to monitor the mesos-cluster jobs.
 */
public class MesosMonitor implements Monitor {

    private static ConcurrentHashMap<String, ResourceNode> nodesMap = new ConcurrentHashMap<String, ResourceNode>();
    /* (non-Javadoc)
     * @see org.apache.oodt.cas.resource.monitor.Monitor#getLoad(org.apache.oodt.cas.resource.structs.ResourceNode)
     */
    @Override
    public int getLoad(ResourceNode node) throws MonitorException {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.apache.oodt.cas.resource.monitor.Monitor#getNodes()
     */
    @Override
    public List<ResourceNode> getNodes() throws MonitorException {
        return new LinkedList<ResourceNode>(nodesMap.values());
    }

    /* (non-Javadoc)
     * @see org.apache.oodt.cas.resource.monitor.Monitor#getNodeById(java.lang.String)
     */
    @Override
    public ResourceNode getNodeById(String nodeId) throws MonitorException {
        return nodesMap.get(nodeId);
    }

    /* (non-Javadoc)
     * @see org.apache.oodt.cas.resource.monitor.Monitor#getNodeByURL(java.net.URL)
     */
    @Override
    public ResourceNode getNodeByURL(URL ipAddr) throws MonitorException {
        for (ResourceNode node : nodesMap.values())
            if (node.getIpAddr().equals(ipAddr))
                return node;
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.oodt.cas.resource.monitor.Monitor#reduceLoad(org.apache.oodt.cas.resource.structs.ResourceNode, int)
     */
    @Override
    public boolean reduceLoad(ResourceNode node, int loadValue)
            throws MonitorException {
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.oodt.cas.resource.monitor.Monitor#assignLoad(org.apache.oodt.cas.resource.structs.ResourceNode, int)
     */
    @Override
    public boolean assignLoad(ResourceNode node, int loadValue)
            throws MonitorException {
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.oodt.cas.resource.monitor.Monitor#addNode(org.apache.oodt.cas.resource.structs.ResourceNode)
     */
    @Override
    public void addNode(ResourceNode node) throws MonitorException {
        nodesMap.put(node.getNodeId(), node);
    }

    /* (non-Javadoc)
     * @see org.apache.oodt.cas.resource.monitor.Monitor#removeNodeById(java.lang.String)
     */
    @Override
    public void removeNodeById(String nodeId) throws MonitorException {
        nodesMap.remove(nodeId);
    }

}
