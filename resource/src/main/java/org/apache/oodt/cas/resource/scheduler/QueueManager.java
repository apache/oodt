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

//OODT imports

import org.apache.oodt.cas.resource.structs.exceptions.QueueManagerException;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;

//JDK imports

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * The Queue to Node Mapping Manager
 * </p>
 */
public class QueueManager {

	protected Map<String, LinkedHashSet<String>> queueToNodesMapping;
	
	public QueueManager() {
		this.queueToNodesMapping = new LinkedHashMap<String, LinkedHashSet<String>>();
	}
	
	public synchronized boolean containsQueue(String queueName) {
		return this.queueToNodesMapping.containsKey(queueName);
	}
	
	public synchronized void addNodeToQueue(String nodeId, String queueName) throws QueueManagerException {
		if (queueName == null || !this.queueToNodesMapping.containsKey(queueName)) {
		  throw new QueueManagerException("Queue '" + queueName + "' does not exist");
		}
		
		// add node to queue
		LinkedHashSet<String> nodes = this.queueToNodesMapping.get(queueName);
		if (nodes == null) {
		  nodes = new LinkedHashSet<String>();
		}
		nodes.add(nodeId);
		
		// put node list back into map
		this.queueToNodesMapping.put(queueName, nodes);
	}

	public synchronized void addQueue(String queueName) {
		if (queueName != null && !this.queueToNodesMapping.containsKey(queueName)) {
		  this.queueToNodesMapping.put(queueName, new LinkedHashSet<String>());
		}
	}

	public synchronized List<String> getNodes(String queueName) throws QueueManagerException {
		if (queueName != null && this.queueToNodesMapping.containsKey(queueName)) {
		  return new Vector<String>(this.queueToNodesMapping.get(queueName));
		} else {
		  throw new QueueManagerException("Queue '" + queueName + "' does not exist");
		}
	}
	
	public synchronized List<String> getQueues() {
		return new Vector<String>(this.queueToNodesMapping.keySet());
	}

	public synchronized Vector<String> getQueues(String nodeId) {
		Vector<String>
			queueNames = new Vector<String>();
		for (Map.Entry<String, LinkedHashSet<String>> queueName : this.queueToNodesMapping.entrySet()) {
		  if (queueName.getValue().contains(nodeId)) {
			queueNames.add(queueName.getKey());
		  }
		}
		return queueNames;
	}

	public synchronized void removeNodeFromQueue(String nodeId, String queueName) throws QueueManagerException {
		if (queueName != null && this.queueToNodesMapping.containsKey(queueName)) {
		  this.queueToNodesMapping.get(queueName).remove(nodeId);
		} else {
		  throw new QueueManagerException("Queue '" + queueName + "' does not exist");
		}
	}

	public synchronized void removeQueue(String queueName) {
		if (queueName != null) {
		  this.queueToNodesMapping.remove(queueName);
		}
	}
	
}

