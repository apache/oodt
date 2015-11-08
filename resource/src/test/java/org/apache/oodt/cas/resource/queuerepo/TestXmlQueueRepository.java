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

package org.apache.oodt.cas.resource.queuerepo;

//JDK imports
import org.apache.oodt.cas.resource.scheduler.QueueManager;
import org.apache.oodt.cas.resource.structs.exceptions.QueueManagerException;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import junit.framework.TestCase;

//OODT imports
//Junit imports

/**
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Test Suite for the {@link XmlQueueRepository} service
 * </p>.
 */
public class TestXmlQueueRepository extends TestCase {

	private QueueManager queueManager;
	
	protected void setUp() {
		System.setProperty("org.apache.oodt.cas.resource.nodetoqueues.dirs",
				"file:"
						+ new File("./src/main/resources/examples")
								.getAbsolutePath());
		this.queueManager = new XmlQueueRepositoryFactory().createQueueRepository().loadQueues();
	}
	
	public void testMapping() throws QueueManagerException {
	  assertTrue(this.queueManager.getQueues().containsAll(Arrays.asList("quick", "high", "long")));
		assertEquals(this.queueManager.getNodes("quick"), Collections.singletonList("localhost"));
		assertEquals(this.queueManager.getNodes("high"), Collections.singletonList("localhost"));
		assertEquals(this.queueManager.getNodes("long"), Collections.singletonList("localhost"));
		assertTrue(this.queueManager.getQueues("localhost").containsAll(Arrays.asList("quick", "high", "long")));
		
		this.queueManager.addQueue("test-queue-1");
		this.queueManager.addNodeToQueue("test-node-1", "test-queue-1");
		
		assertEquals(this.queueManager.getQueues("test-node-1"), Collections.singletonList("test-queue-1"));

		this.queueManager.addNodeToQueue("test-node-1","quick");
		assertEquals(this.queueManager.getQueues("test-node-1"), Arrays.asList("quick", "test-queue-1"));
		
		this.queueManager.removeQueue("quick");
		assertEquals(this.queueManager.getQueues("test-node-1"), Collections.singletonList("test-queue-1"));
		assertEquals(this.queueManager.getQueues("localhost"), Arrays.asList("high", "long"));
	}
	
}
