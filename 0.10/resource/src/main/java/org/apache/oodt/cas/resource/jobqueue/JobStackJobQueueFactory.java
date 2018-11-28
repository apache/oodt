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

import org.apache.oodt.cas.resource.jobrepo.JobRepository;
import org.apache.oodt.cas.resource.util.GenericResourceManagerObjectFactory;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A factory for creating {@link JobStack} {@link JobQueue}s.
 * </p>.
 */
public class JobStackJobQueueFactory implements JobQueueFactory {

  /* the maximum size of the jobqueue */
  private int stackSize = -1;
  
  /* our job repository for persisting jobs */
  private JobRepository repo;

  public JobStackJobQueueFactory() {
    String stackSizeStr = System
        .getProperty("org.apache.oodt.cas.resource.jobqueue.jobstack.maxstacksize");

    if (stackSizeStr != null) {
      stackSize = Integer.parseInt(stackSizeStr);
    }
    
    String jobRepoFactoryClassStr = System.getProperty(
        "resource.jobrepo.factory",
        "org.apache.oodt.cas.resource.jobrepo.MemoryJobRepositoryFactory");
    this.repo = GenericResourceManagerObjectFactory
        .getJobRepositoryFromServiceFactory(jobRepoFactoryClassStr);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.oodt.cas.resource.jobqueue.JobQueueFactory#createQueue()
   */
  public JobQueue createQueue() {
    return new JobStack(stackSize, repo);
  }

}
