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
package org.apache.oodt.cas.resource.system;

//JDK imports
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

//OODT imports
import org.apache.oodt.cas.resource.examples.HelloWorldJob;
import org.apache.oodt.cas.resource.structs.Job;
import org.apache.oodt.cas.resource.structs.JobInput;
import org.apache.oodt.cas.resource.structs.NameValueJobInput;
import org.apache.oodt.cas.resource.structs.ResourceNode;
import org.apache.oodt.cas.resource.structs.exceptions.JobExecutionException;
import org.apache.oodt.cas.resource.structs.exceptions.JobRepositoryException;
import org.apache.oodt.cas.resource.structs.exceptions.MonitorException;
import org.apache.oodt.cas.resource.structs.exceptions.QueueManagerException;

//Google imports
import com.google.common.collect.Lists;

/**
 * Mock implementation of {@link XmlRpcResourceManagerClient}.
 * 
 * @author bfoster (Brian Foster)
 */
public class MockXmlRpcResourceManagerClient extends
      XmlRpcResourceManagerClient {

   private MethodCallDetails lastMethodCallDetails;

   public MockXmlRpcResourceManagerClient() throws MalformedURLException {
      super(new URL("http://localhost:9000"));
   }

   public MethodCallDetails getLastMethodCallDetails() {
      return lastMethodCallDetails;
   }

   public void addNode(ResourceNode node) throws MonitorException {
      lastMethodCallDetails = new MethodCallDetails("addNode",
            Lists.newArrayList((Object) node));
   }

   public void addNodeToQueue(String nodeId, String queueName)
         throws QueueManagerException {
      lastMethodCallDetails = new MethodCallDetails("addNodeToQueue",
            Lists.newArrayList((Object) nodeId, queueName));
   }

   public void addQueue(String queueName) throws QueueManagerException {
      lastMethodCallDetails = new MethodCallDetails("addQueue",
            Lists.newArrayList((Object) queueName));
   }

   public String getExecutionNode(String jobId) {
      lastMethodCallDetails = new MethodCallDetails("getExecutionNode",
            Lists.newArrayList((Object) jobId));
      return "TestNodeId";
   }

   public Job getJobInfo(String jobId) throws JobRepositoryException {
      lastMethodCallDetails = new MethodCallDetails("getJobInfo",
            Lists.newArrayList((Object) jobId));
      Job job = new Job();
      job.setId(jobId);
      job.setName("TestJobName");
      job.setJobInputClassName(NameValueJobInput.class.getCanonicalName());
      job.setJobInstanceClassName(HelloWorldJob.class.getCanonicalName());
      job.setLoadValue(4);
      job.setQueueName("TestQueueName");
      job.setStatus("DONE");
      return job;
   }

   public ResourceNode getNodeById(String nodeId) throws MonitorException {
      lastMethodCallDetails = new MethodCallDetails("getNodeById",
            Lists.newArrayList((Object) nodeId));
      try {
         return new ResourceNode(nodeId, new URL("http://localhost:9999"), 5);
      } catch (Exception e) {
         throw new MonitorException(e);
      }
   }

   public String getNodeLoad(String nodeId) throws MonitorException {
      lastMethodCallDetails = new MethodCallDetails("getNodeLoad",
            Lists.newArrayList((Object) nodeId));
      return "10";
   }

   public List<ResourceNode> getNodes() throws MonitorException {
      lastMethodCallDetails = new MethodCallDetails("getNodes",
            Lists.newArrayList());
      return Lists.newArrayList();
   }

   public List<String> getNodesInQueue(String queueName)
         throws QueueManagerException {
      lastMethodCallDetails = new MethodCallDetails("getNodesInQueue",
            Lists.newArrayList((Object) queueName));
      return Lists.newArrayList();
   }

   public List<String> getQueues() throws QueueManagerException {
      lastMethodCallDetails = new MethodCallDetails("getQueues",
            Lists.newArrayList());
      return Lists.newArrayList();
   }

   public List<String> getQueuesWithNode(String nodeId) throws QueueManagerException {
      lastMethodCallDetails = new MethodCallDetails("getQueuesWithNode",
            Lists.newArrayList((Object) nodeId));
      return Lists.newArrayList();
   }

   public boolean killJob(String jobId) {
      lastMethodCallDetails = new MethodCallDetails("killJob",
            Lists.newArrayList((Object) jobId));
      return true;
   }

   public void removeNode(String nodeId) throws MonitorException {
      lastMethodCallDetails = new MethodCallDetails("removeNode",
            Lists.newArrayList((Object) nodeId));
   }

   public void removeNodeFromQueue(String nodeId, String queueName) throws QueueManagerException {
      lastMethodCallDetails = new MethodCallDetails("removeNodeFromQueue",
            Lists.newArrayList((Object) nodeId, queueName));
   }

   public void removeQueue(String queueName) throws QueueManagerException {
      lastMethodCallDetails = new MethodCallDetails("removeQueue",
            Lists.newArrayList((Object) queueName));
   }

   public void setNodeCapacity(String nodeId, int capacity) throws MonitorException{
      lastMethodCallDetails = new MethodCallDetails("setNodeCapacity",
            Lists.newArrayList((Object) nodeId, capacity));
   }

   public String submitJob(Job exec, JobInput in) throws JobExecutionException {
      lastMethodCallDetails = new MethodCallDetails("submitJob",
            Lists.newArrayList((Object) exec, in));
      return "TestJobId";
   }

   public boolean submitJob(Job exec, JobInput in, URL url) throws JobExecutionException {
      lastMethodCallDetails = new MethodCallDetails("submitJob",
            Lists.newArrayList((Object) exec, in, url));
      return true;
   }

   public class MethodCallDetails {
      private String methodName;
      private List<Object> args;

      public MethodCallDetails(String methodName, List<Object> args) {
         this.methodName = methodName;
         this.args = args;
      }

      public String getMethodName() {
         return methodName;
      }

      public List<Object> getArgs() {
         return args;
      }
   }
}
