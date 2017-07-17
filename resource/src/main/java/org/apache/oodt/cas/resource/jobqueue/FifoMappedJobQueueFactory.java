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

package org.apache.oodt.cas.resource.jobqueue;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.oodt.cas.resource.jobqueue.JobQueue;
import org.apache.oodt.cas.resource.jobqueue.JobQueueFactory;
import org.apache.oodt.cas.resource.jobrepo.JobRepository;
import org.apache.oodt.cas.resource.util.GenericResourceManagerObjectFactory;


/**
 * This factory class reads in properties set in the resource.properties file
 * and read in via the command line and uses those properties to create a
 * {@link FifoMappedJobQueue}.
 * 
 * @author resneck
 *
 */
public class FifoMappedJobQueueFactory implements JobQueueFactory {

	private int stackSize = -1;
	private JobRepository repo;
	
	private static final Logger LOG =
			Logger.getLogger(FifoMappedJobQueueFactory.class.getName());
	
	public FifoMappedJobQueueFactory() {
		try{
			String stackSizeStr = System.getProperty(
					"gov.nasa.smap.spdm.resource.jobqueue.fifomappedjobqueue.maxstacksize");
	
			if (stackSizeStr != null) {
				stackSize = Integer.parseInt(stackSizeStr);
			}
		    
			String jobRepoFactoryClassStr = System.getProperty(
					"resource.jobrepo.factory",
					"gov.nasa.smap.spdm.resource.jobrepo.SmapMemoryJobRepositoryFactory");
			this.repo = GenericResourceManagerObjectFactory.
					getJobRepositoryFromServiceFactory(jobRepoFactoryClassStr);
		}catch(Exception e){
			LOG.log(Level.SEVERE, "An error occurred while creating a " +
					"FifoMappedJobQueue: " + e.getMessage());
		}

	}
	
	/**
	 * @see org.apache.oodt.cas.resource.jobqueue.JobQueueFactory#createQueue()
	 */
	public JobQueue createQueue() {
		return new FifoMappedJobQueue(stackSize, repo);
	}
	
}