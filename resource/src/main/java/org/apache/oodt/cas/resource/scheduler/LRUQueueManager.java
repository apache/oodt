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

package org.apache.oodt.cas.resource.scheduler;

//JDK imports
import java.util.LinkedHashSet;
import java.util.Vector;

//OODT imports
import org.apache.oodt.cas.resource.structs.exceptions.QueueManagerException;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * The LRUs Queue to Node Mapping Manager
 * </p>
 */
public class LRUQueueManager extends QueueManager {
    
	public LRUQueueManager(QueueManager queueManager) throws QueueManagerException {
		for (String queue : queueManager.getQueues()) {
			LinkedHashSet<String> nodes = new LinkedHashSet<String>();
			nodes.addAll(queueManager.getNodes(queue));
			this.queueToNodesMapping.put(queue, nodes);
		}
	}
	
    public synchronized void usedNode(String queueName, String nodeId) {
    	Vector<String> nodes = new Vector<String>(this.queueToNodesMapping.get(queueName));
		nodes.remove(nodeId);
		nodes.add(nodeId);
		LinkedHashSet<String> nodeSet = new LinkedHashSet<String>();
		nodeSet.addAll(nodes);
		this.queueToNodesMapping.put(queueName, nodeSet);
    }
    
}

