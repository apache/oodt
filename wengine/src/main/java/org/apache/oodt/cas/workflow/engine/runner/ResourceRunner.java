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
package org.apache.oodt.cas.workflow.engine.runner;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.resource.structs.Job;
import org.apache.oodt.cas.resource.system.XmlRpcResourceManagerClient;
import org.apache.oodt.cas.workflow.instance.TaskInstance;
import static org.apache.oodt.cas.workflow.metadata.ResourceMetKeys.*;

/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 * CAS-Resource EngineRunner
 *
 */
public class ResourceRunner extends EngineRunner {

	private XmlRpcResourceManagerClient rsManagerClient;
	
	public ResourceRunner(XmlRpcResourceManagerClient rsManagerClient) {
		super();
		this.rsManagerClient = rsManagerClient;
	}
	
	@Override
	public void execute(TaskInstance workflowInstance) throws Exception {
		ResourceJobInput input = new ResourceJobInput();
		input.workflowInstance = workflowInstance;
		Job job = new Job();
//		job.setId(workflowInstance.getJobId());
		job.setName(workflowInstance.getInstanceId() + ":" + workflowInstance.getModelId());
		job.setJobInputClassName(ResourceJobInput.class.getCanonicalName());
		job.setJobInstanceClassName(ResourceJobInstance.class.getCanonicalName());
		Metadata m = workflowInstance.getMetadata();
		if (m.getMetadata(LOAD) != null)
			job.setLoadValue(Integer.parseInt(m.getMetadata(LOAD)));
		else 
			job.setLoadValue(2);
		if (m.getMetadata(QUEUE_NAME) != null)
			job.setQueueName(m.getMetadata(QUEUE_NAME));
		else
			throw new Exception("Must specify 'QueueName' for task [instanceId = '" + workflowInstance.getInstanceId() + "', modelId = '" + workflowInstance.getModelId() + "']");
		workflowInstance.setJobId(this.rsManagerClient.submitJob(job, input));
	}

	@Override
	/**
	 * Additional '-1' is a workaround of a bug in resource manager
	 */
	public int getOpenSlots(TaskInstance workflowInstance) throws Exception {
		return this.rsManagerClient.getJobQueueCapacity() - this.rsManagerClient.getJobQueueSize() - 1;
	}

	@Override
	public boolean hasOpenSlots(TaskInstance workflowInstance) throws Exception {
		return this.getOpenSlots(workflowInstance) > 0;
	}
	
	@Override
	public void shutdown() throws Exception {
		//do nothing
	}

}
