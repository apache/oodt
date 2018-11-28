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

import java.net.URL;
import java.util.List;

import org.apache.oodt.cas.resource.monitor.Monitor;
import org.apache.oodt.cas.resource.structs.ResourceNode;
import org.apache.oodt.cas.resource.structs.exceptions.MonitorException;

public class MockMonitor implements Monitor {

    public int load = -1;
    List<ResourceNode> nodes;
    ResourceNode id;
    ResourceNode url;
    ResourceNode add;
    ResourceNode reduce;

    public MockMonitor(int load,List<ResourceNode> nodes, ResourceNode id, ResourceNode url, ResourceNode reduce) {
        this.load = load;
        this.nodes = nodes;
        this.id = id;
        this.url = url;
        this.reduce = reduce;
    }

    @Override
    public int getLoad(ResourceNode node) throws MonitorException {     
        return load;
    }
    @Override
    public List getNodes() throws MonitorException {
        return nodes;
    }

    @Override
    public ResourceNode getNodeById(String nodeId) throws MonitorException {
        return id.getNodeId().equals(nodeId)?id:null;
    }

    @Override
    public ResourceNode getNodeByURL(URL ipAddr) throws MonitorException {
        return url.getIpAddr().equals(ipAddr)?url:null;
    }

    @Override
    public boolean reduceLoad(ResourceNode node, int loadValue)
            throws MonitorException {
        reduce.setCapacity(reduce.getCapacity() - loadValue);
        return true;
    }

    @Override
    public boolean assignLoad(ResourceNode node, int loadValue)
            throws MonitorException {
        reduce.setCapacity(loadValue);
        return true;
    }

    @Override
    public void addNode(ResourceNode node) throws MonitorException {
        this.add = node;

    }

    @Override
    public void removeNodeById(String nodeId) throws MonitorException {
        if (this.add.getNodeId().equals(nodeId))
            this.add = null;
    }

    public ResourceNode getAdded() {
        return this.add;
    }
}
