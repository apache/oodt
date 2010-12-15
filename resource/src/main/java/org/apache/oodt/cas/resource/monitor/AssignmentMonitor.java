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
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

//OODT imports
import org.apache.oodt.cas.resource.structs.ResourceNode;
import org.apache.oodt.cas.resource.structs.exceptions.MonitorException;

/**
 * 
 * @author woollard
 * @author bfoster
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

    /* our log stream */
    private static Logger LOG = Logger.getLogger(AssignmentMonitor.class
            .getName());

    /* our nodes map */
    private static HashMap<String, ResourceNode> nodesMap;

    /* our load map */
    private static HashMap<String, Integer> loadMap;

    public AssignmentMonitor(List<ResourceNode> nodes) {
        nodesMap = new HashMap<String, ResourceNode>();
        loadMap = new HashMap<String, Integer>();
        
        for (ResourceNode node : nodes) {
            nodesMap.put(node.getNodeId(), node);
            loadMap.put(node.getNodeId(), new Integer(0));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.resource.monitor.Monitor#assignLoad(
     *      gov.nasa.jpl.oodt.cas.resource.structs.ResourceNode, int)
     */
    public boolean assignLoad(ResourceNode node, int loadValue)
            throws MonitorException {
        int loadCap = node.getCapacity();
        int curLoad = ((Integer) loadMap.get(node.getNodeId())).intValue();

        if (loadValue <= (loadCap - curLoad)) {
            loadMap.remove(node.getNodeId());
            loadMap.put(node.getNodeId(), new Integer(curLoad + loadValue));
            return true;
        } else {
            return false;
        }
    }

    public boolean reduceLoad(ResourceNode node, int loadValue)
            throws MonitorException {
        Integer load = (Integer) loadMap.get(node.getNodeId());
        int newVal = load.intValue() - loadValue;
        if (newVal < 0)
            newVal = 0; // should not happen but just in case
        loadMap.remove(node.getNodeId());
        loadMap.put(node.getNodeId(), new Integer(newVal));
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.resource.monitor.Monitor#getLoad(gov.nasa.jpl.oodt.cas.resource.structs.ResourceNode)
     */
    public int getLoad(ResourceNode node) throws MonitorException {
        ResourceNode resource = (ResourceNode) nodesMap.get(node.getNodeId());
        Integer i = (Integer) loadMap.get(node.getNodeId());
        return (resource.getCapacity() - i.intValue());
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
        return (ResourceNode) nodesMap.get(nodeId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.resource.monitor.Monitor#getNodeByURL(java.net.URL)
     */
    public ResourceNode getNodeByURL(URL ipAddr) throws MonitorException {
        ResourceNode targetResource = null;
        List<ResourceNode> nodes = this.getNodes();
        for (int i = 0; i < nodes.size(); i++) {
            if (((ResourceNode) nodes.get(i)).getIpAddr() == ipAddr) {
                targetResource = (ResourceNode) nodes.get(i);
                break;
            }
        }
        return targetResource;
    }

    public void addNode(ResourceNode node) throws MonitorException {
        nodesMap.put(node.getNodeId(), node);
        if (!loadMap.containsKey(node.getNodeId()))
            loadMap.put(node.getNodeId(), new Integer(0));
    }

    public void removeNodeById(String nodeId) throws MonitorException {
        nodesMap.remove(nodeId);    
        loadMap.remove(nodeId);
    }

}
