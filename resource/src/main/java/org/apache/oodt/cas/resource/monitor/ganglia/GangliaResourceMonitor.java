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

package org.apache.oodt.cas.resource.monitor.ganglia;

import org.apache.oodt.cas.resource.monitor.ResourceMonitor;
import org.apache.oodt.cas.resource.monitor.exceptions.GangliaMonitorException;
import org.apache.oodt.cas.resource.monitor.ganglia.loadcalc.LoadCalculator;
import org.apache.oodt.cas.resource.structs.ResourceNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author rajith
 * @version $Revision$
 */
public class GangliaResourceMonitor implements ResourceMonitor {

    private static final Logger LOG = Logger.getLogger(GangliaResourceMonitor.class.getName());

    /*loadMap will be updated after a UPDATE_INTERVAL milliseconds interval*/
    private static long UPDATE_INTERVAL = 60000;

    private static HashMap<String, Float> nodeCapacityMap = new HashMap<String, Float>();
    private static HashMap<String, Float> loadMap = new HashMap<String, Float>();

    private LoadCalculator loadCalculator;
    private long lastUpdatedTime;

    /**
     * Make a new GangliaResourceMonitor that reads information
     * from a ganglia meta daemon.
     * @param loadCalculator LoadCalculator {@link org.apache.oodt.cas.resource.monitor.ganglia.loadcalc.LoadCalculator} to calculate load
     * @param nodes resource nodes {@link org.apache.oodt.cas.resource.structs.ResourceNode} to be monitored.
     */
    public GangliaResourceMonitor(LoadCalculator loadCalculator, List<ResourceNode> nodes){
        this.loadCalculator = loadCalculator;

        for (ResourceNode node : nodes) {
            nodeCapacityMap.put(node.getNodeId(), (float) node.getCapacity());
        }
        // Initially set the value UPDATE_INTERVAL earlier.
        lastUpdatedTime = System.currentTimeMillis() - UPDATE_INTERVAL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getLoad(ResourceNode node) {
        try {
            if(lastUpdatedTime + UPDATE_INTERVAL <= (System.currentTimeMillis())){
                updateLoadMap();
            }
            return loadMap.get(node.getNodeId());
        } catch (GangliaMonitorException e) {
            LOG.log(Level.SEVERE, "Failed get status from the Ganglia meta daemon : "
                    + e.getMessage(), e);
            return nodeCapacityMap.get(node.getNodeId()); //return the capacity as the load of the node
        } catch (NullPointerException e){
            LOG.log(Level.SEVERE, "The required nodeId is not available: "
                    + node.getNodeId() + " :" + e.getMessage(), e);
            return (float) node.getCapacity(); //return node's if the nodeId is not available.
        } catch (Exception e){
            LOG.log(Level.SEVERE, "Failed get status from the Ganglia meta daemon for the nodeId: "
                    + node.getNodeId() + " :" + e.getMessage(), e);
            return nodeCapacityMap.get(node.getNodeId()); //return the capacity as the load of the node
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateLoad(String nodeId, float loadValue) {
        loadMap.put(nodeId, loadValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addNode(String nodeId, float capacity) {
        nodeCapacityMap.put(nodeId, capacity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeNodeById(String nodeId) {
        nodeCapacityMap.remove(nodeId);
        loadMap.remove(nodeId);
    }

    /**
     * Update the loadMap by calculating the load by the LoadCalculator
     * @throws org.apache.oodt.cas.resource.monitor.exceptions.GangliaMonitorException {@link org.apache.oodt.cas.resource.monitor.exceptions.GangliaMonitorException} if an error occurred
     */
    private void updateLoadMap() throws GangliaMonitorException {
        lastUpdatedTime = System.currentTimeMillis();
        HashMap<String, HashMap> resourceNodesMetrics = GangliaAdapter
                .getResourceNodeStatus(nodeCapacityMap.keySet());

        for (Map.Entry<String, HashMap> entry: resourceNodesMetrics.entrySet()){
            loadMap.put(entry.getKey(), loadCalculator.calculateLoad(
                    nodeCapacityMap.get(entry.getKey()), resourceNodesMetrics.get(entry.getKey())));
        }
    }

}
