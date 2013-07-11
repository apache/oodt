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

import org.apache.oodt.cas.resource.structs.ResourceNode;

/**
 * @author rajith
 * @version $Revision$
 *
 * The ResourceMonitor interface.
 */
public interface ResourceMonitor {

    /**
     * Gets the load on a resource node.
     * @return A float representation of the load on a
     * {@link org.apache.oodt.cas.resource.structs.ResourceNode}.
     */
    public float getLoad(ResourceNode node);

    /**
     * update the load value of a node
     * {@link org.apache.oodt.cas.resource.structs.ResourceNode}  in the loadMap
     * @param nodeId nodeId of the node
     * @param loadValue load value to be assigned
     */
    public void updateLoad(String nodeId, float loadValue);

    /**
     * add a new node {@link org.apache.oodt.cas.resource.structs.ResourceNode}
     * to monitor
     * @param nodeId nodeId of the node
     * @param capacity capacity of the node
     */
    public void addNode(String nodeId, float capacity);

    /**
     * remove node {@link org.apache.oodt.cas.resource.structs.ResourceNode}
     * from the monitoring nodes set
     * @param nodeId nodeId of the node
     */
    public void removeNodeById(String nodeId);
}
