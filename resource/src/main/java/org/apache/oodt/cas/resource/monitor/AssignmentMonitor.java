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

//JDK imports
import org.apache.oodt.cas.resource.structs.ResourceNode;
import org.apache.oodt.cas.resource.structs.exceptions.MonitorException;

import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Vector;

//OODT imports

/**
 * 
 * @author woollard
 * @author bfoster
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * An implementation of the {@link Monitor} interface that loads its information
 * about the underlying nodes from an XML file called <code>nodes.xml</code>.
 * This implementation additionally uses an in-memory hash map to monitor the
 * load on a given set of {@link ResourceNode}s.
 * </p>
 */
public class AssignmentMonitor implements Monitor {
	
    /* our nodes map */
    private ConcurrentHashMap<String, ResourceNode> nodesMap;

    /* our load map */
    private ConcurrentHashMap<String, Integer> loadMap;

    public AssignmentMonitor(List<ResourceNode> nodes) {
        nodesMap = new ConcurrentHashMap<String, ResourceNode>();
        loadMap = new ConcurrentHashMap<String, Integer>();
        
        for (ResourceNode node : nodes) {
            nodesMap.put(node.getNodeId(), node);
            loadMap.put(node.getNodeId(), 0);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.resource.monitor.Monitor#assignLoad(
     *      gov.nasa.jpl.oodt.cas.resource.structs.ResourceNode, int)
     */
    @Override
    public boolean assignLoad(ResourceNode node, int loadValue)
            throws MonitorException {
        int loadCap = node.getCapacity();
        int curLoad = loadMap.get(node.getNodeId());

        if (loadValue <= (loadCap - curLoad)) {
            loadMap.remove(node.getNodeId());
            loadMap.put(node.getNodeId(), curLoad + loadValue);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean reduceLoad(ResourceNode node, int loadValue)
            throws MonitorException {
        int load = loadMap.get(node.getNodeId());
        int newVal = load - loadValue;
        if (newVal < 0) {
            newVal = 0; // should not happen but just in case
        }
        loadMap.remove(node.getNodeId());
        loadMap.put(node.getNodeId(), newVal);
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.resource.monitor.Monitor#getLoad(gov.nasa.jpl.oodt.cas.resource.structs.ResourceNode)
     */
    public int getLoad(ResourceNode node) throws MonitorException {
        ResourceNode resource = nodesMap.get(node.getNodeId());
        int i = loadMap.get(node.getNodeId());
        return (resource.getCapacity() - i);
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.resource.monitor.Monitor#getNodes()
     */
    public List<ResourceNode> getNodes() throws MonitorException {
        return new Vector<ResourceNode>(nodesMap.values());
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.resource.monitor.Monitor#getNodeById(java.lang.String)
     */
    public ResourceNode getNodeById(String nodeId) throws MonitorException {
        return nodesMap.get(nodeId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.resource.monitor.Monitor#getNodeByURL(java.net.URL)
     */
    public ResourceNode getNodeByURL(URL ipAddr) throws MonitorException {
        ResourceNode targetResource = null;
        List<ResourceNode> nodes = this.getNodes();
        for (ResourceNode node : nodes) {
            if (node.getIpAddr() == ipAddr) {
                targetResource = node;
                break;
            }
        }
        return targetResource;
    }

    public void addNode(ResourceNode node) throws MonitorException {
        nodesMap.put(node.getNodeId(), node);
        if (!loadMap.containsKey(node.getNodeId())) {
            loadMap.put(node.getNodeId(), 0);
        }
    }

    public void removeNodeById(String nodeId) throws MonitorException {
        nodesMap.remove(nodeId);    
        loadMap.remove(nodeId);
    }
}
