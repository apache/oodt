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

package org.apache.oodt.cas.resource.jobrepo;

//JDK imports
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;

//CommonsIO imports
import org.apache.commons.io.FileUtils;

//XStream imports
import com.thoughtworks.xstream.XStream;

//OODT imports
import org.apache.oodt.cas.resource.structs.JobSpec;
import org.apache.oodt.cas.resource.structs.JobStatus;
import org.apache.oodt.cas.resource.structs.exceptions.JobRepositoryException;

/**
 * @author bfoster
 * @version $Revision$
 * 
 * XStream based JobRepository
 */
public class XStreamJobRepository implements JobRepository {

	private File workingDir;
	private final int maxHistory;
	private Map<String, String> jobMap;
	private List<String> jobPrecedence;
	
	public XStreamJobRepository(File workingDir, int maxHistory) {
		this.workingDir = workingDir;
		this.maxHistory = Math.max(maxHistory == -1 ? Integer.MAX_VALUE : maxHistory, 1);
		this.jobMap = Collections.synchronizedMap(new ConcurrentHashMap<String, String>());
		this.jobPrecedence = new Vector<String>();
	}
	
	public synchronized String addJob(JobSpec spec) throws JobRepositoryException {
	    XStream xstream = new XStream();
	    FileOutputStream os = null;
		try {
			if (this.jobMap.size() >= this.maxHistory) {
			  FileUtils.forceDelete(new File(jobMap.remove(jobPrecedence.remove(0))));
			}
			
			if (spec.getJob().getId() == null) {
			  spec.getJob().setId(UUID.randomUUID().toString());
			} else if (this.jobMap.containsKey(spec.getJob().getId())) {
			  throw new JobRepositoryException(
				  "JobId '" + spec.getJob().getId() + "' already in use -- must pick unique JobId");
			}
			
			File file = this.generateFilePath(spec.getJob().getId());
			os = new FileOutputStream(file);
		    xstream.toXML(spec, os);
		    jobMap.put(spec.getJob().getId(), file.getAbsolutePath());
		    jobPrecedence.add(spec.getJob().getId());
			return spec.getJob().getId();
		}catch (Exception e) {
			throw new JobRepositoryException("Failed to add job spec to repo : " + e.getMessage(), e);
		}finally {
			try {
				os.close();
			}catch (Exception ignored) {}
		}
	}

	public JobSpec getJobById(String jobId) throws JobRepositoryException {
	    XStream xstream = new XStream();
	    FileInputStream is = null;
		try {
			is = new FileInputStream(new File(this.jobMap.get(jobId)));
		    return (JobSpec) xstream.fromXML(is);
		}catch (Exception e) {
			throw new JobRepositoryException("Failed to load job spec from repo by id '" + jobId + "' : " + e.getMessage(), e);
		}finally {
			try {
				is.close();
			}catch (Exception ignored) {}
		}
	}

	public String getStatus(JobSpec spec) throws JobRepositoryException {
		return this.getJobById(spec.getJob().getId()).getJob().getStatus();
	}

	public boolean jobFinished(JobSpec spec) throws JobRepositoryException {
		String status = this.getStatus(spec);
	    return status.equals(JobStatus.SUCCESS);
	}

	public synchronized void removeJob(JobSpec spec) throws JobRepositoryException {
		try {
			FileUtils.forceDelete(new File(this.jobMap.get(spec.getJob().getId())));
		    jobMap.remove(spec.getJob().getId());
		    jobPrecedence.remove(spec.getJob().getId());
		}catch (Exception e) {
			throw new JobRepositoryException("Failed to delete job '" + spec.getJob().getId() + "' : " + e.getMessage(), e);
		}
	}

	public synchronized void updateJob(JobSpec spec) throws JobRepositoryException {
	    XStream xstream = new XStream();
	    FileOutputStream os = null;
		try {
			FileUtils.forceDelete(new File(this.jobMap.get(spec.getJob().getId())));
			os = new FileOutputStream(new File(this.jobMap.get(spec.getJob().getId())));
		    xstream.toXML(spec, os);
		}catch (Exception e) {
			throw new JobRepositoryException("Failed to add job spec to repo : " + e.getMessage(), e);
		}finally {
			try {
				os.close();
			}catch (Exception ignored) {}
		}
	}
	
	protected File generateFilePath(String jobId) {
		return new File(workingDir, jobId + ".xstream");
	}

}
