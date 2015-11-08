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


package org.apache.oodt.cas.resource.batchmgr;

//OODT imports
import org.apache.oodt.cas.resource.jobrepo.JobRepository;
import org.apache.oodt.cas.resource.monitor.Monitor;
import org.apache.oodt.cas.resource.structs.JobSpec;
import org.apache.oodt.cas.resource.structs.exceptions.JobExecutionException;
import org.apache.oodt.cas.resource.structs.ResourceNode;

//java imports
import java.util.List;

/**
 * @author woollard
 * @version $Revision$
 * 
 * <p>
 * A batchmgr interface.
 * </p>
 * 
 */
public interface Batchmgr {

    /**
     * Executes a job remotely on the specified <code>remoteHost</code>.
     * 
     * @param job
     *            The {@link JobSpec} to execute.
     * @return true if the job execution was successful, false otherwise.
     * @throws JobExecutionException
     *             If there is an exception executing the job on the remote
     *             host.
     */
    boolean executeRemotely(JobSpec job, ResourceNode resNode)
            throws JobExecutionException;

    /**
     * Sets the {@link Monitor} to be used by this Batchmgr.
     * 
     * @param monitor
     *            The {@link Monitor} to be used.
     */
    void setMonitor(Monitor monitor);

    /**
     * Sets the {@link JobRepository} that this Batchmgr will use to persist
     * {@link Job} information while {@link Job}s are executing.
     * 
     * @param repository
     */
    void setJobRepository(JobRepository repository);
    
    
    /**
     * 
     * @param jobId
     * @param node
     * @return
     */
    boolean killJob(String jobId, ResourceNode node);
    
    
    /**
     * 
     * @param jobId
     * @return
     */
    String getExecutionNode(String jobId);
    
    /**
     * Get a list of the ids of all jobs that are executing on the given node. 
     * @return A list of ids of jobs on the given node
     */
    List getJobsOnNode(String nodeId);

}
