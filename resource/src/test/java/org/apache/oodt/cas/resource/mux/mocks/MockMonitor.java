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
package org.apache.oodt.cas.resource.mux.mocks;

import org.apache.oodt.cas.resource.monitor.Monitor;
import org.apache.oodt.cas.resource.structs.ResourceNode;
import org.apache.oodt.cas.resource.structs.exceptions.MonitorException;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockMonitor implements Monitor {

    private Map<ResourceNode, Integer> loads;
    private List<ResourceNode> nodes;

    public MockMonitor(List<ResourceNode> nodes) {
        this.nodes = nodes;
        loads = new HashMap<>();
        for (ResourceNode node : nodes) {
            loads.put(node, 0);
        }
    }

    @Override
    public int getLoad(ResourceNode node) {
        return loads.get(node);
    }

    @Override
    public List getNodes() {
        return this.nodes;
    }

    @Override
    public ResourceNode getNodeById(String nodeId) {
        for (ResourceNode n : nodes) {
            if (n.getNodeId().equals(nodeId)) return n;
        }
        return null;
    }

    @Override
    public ResourceNode getNodeByURL(URL ipAddr) {
        for (ResourceNode n : nodes) {
            if (n.getIpAddr().toString().equals(ipAddr.toString())) {
                return n;
            }
        }
        return null;
    }

    @Override
    public boolean reduceLoad(ResourceNode node, int loadValue) throws MonitorException {
        if (nodes.contains(node)) {
            node.setCapacity(node.getCapacity() + loadValue);
            loads.put(node, loads.get(node) - loadValue);
            return true;
        }
        throw new MonitorException("Node Not Found");
    }

    @Override
    public boolean assignLoad(ResourceNode node, int loadValue) throws MonitorException {
        if (nodes.contains(node)) {
            int c = node.getCapacity() - loadValue;
            if (c > 0) {
                node.setCapacity(c);
                loads.put(node, loads.get(node) + loadValue);
                return true;
            }
            return false;
        }
        throw new MonitorException("Node Not Found");
    }

    @Override
    public void addNode(ResourceNode node) {
        nodes.add(node);
    }

    @Override
    public void removeNodeById(String nodeId) {
        nodes.remove(getNodeById(nodeId));
    }
}
