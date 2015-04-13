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

import org.apache.oodt.cas.resource.util.GenericResourceManagerObjectFactory;
import org.apache.oodt.cas.resource.jobqueue.JobQueue;
import org.apache.oodt.cas.resource.jobqueue.JobStackJobQueueFactory;

/**
 * @author starchmd
 * @version $Revision$
 *
 * <p>
 * A factory for the SparkScheduler
 * </p>
 *
 */
public class SparkSchedulerFactory implements SchedulerFactory {

	private static final Logger LOG = Logger.getLogger(SparkSchedulerFactory.class.getName());


	private JobQueue queue = null;
	/**
	 * Setup factory
	 */
	public SparkSchedulerFactory() {
		String jobQueueClassStr = System.getProperty("resource.jobqueue.factory",
		        JobStackJobQueueFactory.class.getName());
		LOG.log(Level.INFO,"Using job-queue: "+jobQueueClassStr+ " with: "+SparkScheduler.class.getName());
		queue = GenericResourceManagerObjectFactory.getJobQueueServiceFromFactory(jobQueueClassStr);
	}
	/**
	 * Returns scheduler
	 */
	public Scheduler createScheduler() {
		return new SparkScheduler(queue);
	}
}