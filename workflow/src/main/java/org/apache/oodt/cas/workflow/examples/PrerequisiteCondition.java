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


package org.apache.oodt.cas.workflow.examples;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.resource.structs.Job;
import org.apache.oodt.cas.resource.structs.exceptions.JobQueueException;
import org.apache.oodt.cas.resource.system.XmlRpcResourceManagerClient;
import org.apache.oodt.cas.workflow.structs.WorkflowConditionConfiguration;
import org.apache.oodt.cas.workflow.structs.WorkflowConditionInstance;

/**
 * @author singhk
 * @version $Revision$
 *
 * <p>A Simple condition that evaluates to false as many times as the prerequisite job in Resource Manager
 * isn't completed. After that, the condition returns
 * true.</p>
 *
 */
public class PrerequisiteCondition implements WorkflowConditionInstance {

	public PrerequisiteCondition() {
		super();
	}
	
	/**
	 * Check if there is any job queued in Resource Manager with name equivalent to <code>jobName</code>
	 * @param client
	 * @param jobName
	 * @return true if job is present, otherwise false
	 */
	@SuppressWarnings("unchecked")
	private boolean isJobPresentInQueue(XmlRpcResourceManagerClient client, String jobName){
		Iterator<Job> iter;
		try {
			iter = (Iterator<Job>)client.getQueuedJobs().iterator();
			while(iter.hasNext()){
				Job job = iter.next();
				if(job.getName().equals(jobName))
					return true;
			}
		} catch (JobQueueException e) {
			e.printStackTrace();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.apache.oodt.cas.workflow.structs.WorkflowConditionInstance#evaluate(org.apache.oodt.cas.metadata.Metadata)
	 */
    public boolean evaluate(Metadata metadata, WorkflowConditionConfiguration config) {
		
		String[] jobs = (config.getProperty("JOBS")).split(",");
		String resourceManagerUrl = config.getProperty("RESMGR_URL");
		boolean flag = true;

		System.out.println(new Date() + " PrerequisiteCondition: Jobs: "+ Arrays.toString(jobs));
		System.out.println(new Date() + " PrerequisiteCondition: Resource Manager: "+ resourceManagerUrl);
		
		XmlRpcResourceManagerClient client = null;
		try {
			client = new XmlRpcResourceManagerClient(new URL(resourceManagerUrl));
			flag = true;
			for (String job : jobs)
				flag = flag && !isJobPresentInQueue(client, job);
			if(flag)
				return true;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return false;
	}
}
