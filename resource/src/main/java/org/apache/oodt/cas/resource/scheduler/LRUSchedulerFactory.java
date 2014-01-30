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

//JAVA imports
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports
import org.apache.oodt.cas.resource.queuerepo.XmlQueueRepositoryFactory;
import org.apache.oodt.cas.resource.util.GenericResourceManagerObjectFactory;
import org.apache.oodt.cas.resource.jobqueue.JobQueue;
import org.apache.oodt.cas.resource.monitor.Monitor;
import org.apache.oodt.cas.resource.batchmgr.Batchmgr;

/**
 * @author woollard
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * A Least recently used scheduler factory interface.
 * </p>
 * 
 */
public class LRUSchedulerFactory implements SchedulerFactory {

	private static final Logger LOG = Logger
			.getLogger(LRUSchedulerFactory.class.getName());

	/*
	 * a list of URIs pointing to directories that have the
	 * node-to-queue-mapping.xml files
	 */
	private LRUQueueManager queueManager;

	/* our monitor */
	private Monitor mon = null;

	/* our batchmgr */
	private Batchmgr batcher = null;

	/* our job queue */
	private JobQueue queue = null;

	public LRUSchedulerFactory() {
		String queueRepoFactoryClassStr = System.getProperty(
				"org.apache.oodt.cas.resource.queues.repo.factory",
				XmlQueueRepositoryFactory.class.getCanonicalName());
		String batchmgrClassStr = System
				.getProperty("resource.batchmgr.factory",
						"org.apache.oodt.cas.resource.batchmgr.XmlRpcBatchmgrFactory");
		String monitorClassStr = System
				.getProperty("resource.monitor.factory",
						"org.apache.oodt.cas.resource.monitor.AssignmentMonitorFactory");

		String jobQueueClassStr = System
				.getProperty("resource.jobqueue.factory",
						"org.apache.oodt.cas.resource.jobqueue.JobStackJobQueueFactory");

		try {
			queueManager = new LRUQueueManager(
					GenericResourceManagerObjectFactory
							.getQueueRepositoryFromFactory(
									queueRepoFactoryClassStr).loadQueues());
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to create queue manager : "
					+ e.getMessage(), e);
			queueManager = null;
		}
		batcher = GenericResourceManagerObjectFactory
				.getBatchmgrServiceFromFactory(batchmgrClassStr);
		mon = GenericResourceManagerObjectFactory
				.getMonitorServiceFromFactory(monitorClassStr);

		queue = GenericResourceManagerObjectFactory
				.getJobQueueServiceFromFactory(jobQueueClassStr);

		// set the monitor for this batcher
		batcher.setMonitor(mon);

		// set the job repo for this batcher
		batcher.setJobRepository(queue.getJobRepository());

	}

	public Scheduler createScheduler() {
		return new LRUScheduler(mon, batcher, queue, queueManager);
	}

}
