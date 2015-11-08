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

package org.apache.oodt.cas.resource.util;

//OODT imports

import org.apache.oodt.cas.resource.structs.Job;
import org.apache.oodt.cas.resource.structs.ResourceNode;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

//JDK imports

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A Factory for serializing and reading Resource Manager objects to and from
 * the XML-RPC wire.
 * </p>
 */
public final class XmlRpcStructFactory {

	private XmlRpcStructFactory() throws InstantiationException {
		throw new InstantiationException("Don't construct factory classes!");
	}

	public static Hashtable<String, Object> getXmlRpcJob(Job job) {
	  Hashtable<String, Object> jobHash = new Hashtable<String, Object>();
		jobHash.put("job.id", job.getId() != null ? job.getId():"");
		jobHash.put("job.name", job.getName());
		jobHash.put("job.instanceClassName", job.getJobInstanceClassName());
		jobHash.put("job.inputClassName", job.getJobInputClassName());
		jobHash.put("job.queueName", job.getQueueName());
		jobHash.put("job.load",job.getLoadValue());
  jobHash.put("job.status", job.getStatus() != null ? job.getStatus() : "");
    return jobHash;
	}

	public static Job getJobFromXmlRpc(Map jobHash) {
		Job job = new Job();
		job.setId((String) jobHash.get("job.id"));
		job.setName((String) jobHash.get("job.name"));
		job.setJobInputClassName((String) jobHash.get("job.inputClassName"));
		job.setJobInstanceClassName((String) jobHash
				.get("job.instanceClassName"));
		job.setQueueName((String)jobHash.get("job.queueName"));
		job.setLoadValue((Integer)jobHash.get("job.load"));
  job.setStatus((String)jobHash.get("job.status"));
		return job;
	}
	
	public static Vector getXmlRpcJobList(List jobs){
		Vector jobVector = new Vector();
		
		if(jobs != null && jobs.size() > 0){
		  for (Object job1 : jobs) {
			Job job = (Job) job1;
			jobVector.add(getXmlRpcJob(job));
		  }
		}
		
		return jobVector;
	}
	
	public static List getJobListFromXmlRpc(Vector<Map> jobVector){
		List jobs = new Vector();
		
		if(jobVector != null && jobVector.size() > 0){
		  for (Map aJobVector : jobVector) {
			jobs.add(getJobFromXmlRpc(aJobVector));
		  }
		}
		
		return jobs;
	}

 public static Vector getXmlRpcResourceNodeList(List<ResourceNode> resNodes) {
    Vector resNodeVector = new Vector();

    if (resNodes != null && resNodes.size() > 0) {
	  for (ResourceNode resNode : resNodes) {
		resNodeVector.add(getXmlRpcResourceNode(resNode));
	  }
    }

    return resNodeVector;
  }

  public static List getResourceNodeListFromXmlRpc(Vector<Map> resNodeVector) {
    List resNodes = new Vector();

    if (resNodeVector != null && resNodeVector.size() > 0) {
	  for (Map aResNodeVector : resNodeVector) {
		resNodes.add(getResourceNodeFromXmlRpc(aResNodeVector));
	  }
    }

    return resNodes;
  }

  public static Hashtable<String,String> getXmlRpcResourceNode(ResourceNode node) {
    Hashtable<String, String> resNodeHash = new Hashtable<String, String>();
    resNodeHash.put("node.id", node.getNodeId());
    resNodeHash.put("node.capacity", String.valueOf(node.getCapacity()));
    resNodeHash.put("node.url", node.getIpAddr().toExternalForm());

    return resNodeHash;
  }

  public static ResourceNode getResourceNodeFromXmlRpc(Map resNodeHash) {
    ResourceNode node = new ResourceNode();
    node.setId((String) resNodeHash.get("node.id"));
    node.setCapacity(Integer
        .parseInt((String) resNodeHash.get("node.capacity")));
    try {
      node.setIpAddr(new URL((String) resNodeHash.get("node.url")));
    } catch (MalformedURLException ignore) {
    }

    return node;
  }

}
