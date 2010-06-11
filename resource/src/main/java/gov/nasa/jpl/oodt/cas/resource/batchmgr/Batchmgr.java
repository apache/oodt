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


package gov.nasa.jpl.oodt.cas.resource.batchmgr;

//OODT imports
import gov.nasa.jpl.oodt.cas.resource.jobrepo.JobRepository;
import gov.nasa.jpl.oodt.cas.resource.monitor.Monitor;
import gov.nasa.jpl.oodt.cas.resource.structs.JobSpec;
import gov.nasa.jpl.oodt.cas.resource.structs.exceptions.JobExecutionException;
import gov.nasa.jpl.oodt.cas.resource.structs.ResourceNode;

//java imports
import java.net.URL;

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
     * @param remoteHost
     *            A {@link URL} pointer to the remote host to execute the job
     *            on.
     * @return true if the job execution was successful, false otherwise.
     * @throws JobExecutionException
     *             If there is an exception executing the job on the remote
     *             host.
     */
    public boolean executeRemotely(JobSpec job, ResourceNode resNode)
            throws JobExecutionException;

    /**
     * Sets the {@link Monitor} to be used by this Batchmgr.
     * 
     * @param monitor
     *            The {@link Monitor} to be used.
     */
    public void setMonitor(Monitor monitor);

    /**
     * Sets the {@link JobRepository} that this Batchmgr will use to persist
     * {@link Job} information while {@link Job}s are executing.
     * 
     * @param repository
     */
    public void setJobRepository(JobRepository repository);
    
    
    /**
     * 
     * @param jobId
     * @param node
     * @return
     */
    public boolean killJob(String jobId, ResourceNode node);
    
    
    /**
     * 
     * @param jobId
     * @return
     */
    public String getExecutionNode(String jobId);

}
