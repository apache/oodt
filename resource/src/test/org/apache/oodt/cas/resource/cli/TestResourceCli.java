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
package org.apache.oodt.cas.resource.cli;

//JDK imports
import java.net.MalformedURLException;
import java.net.URL;

//OODT imports
import org.apache.oodt.cas.cli.CmdLineUtility;
import org.apache.oodt.cas.cli.util.OptionPropertyRegister;
import org.apache.oodt.cas.resource.structs.Job;
import org.apache.oodt.cas.resource.structs.JobInput;
import org.apache.oodt.cas.resource.structs.JobSpec;
import org.apache.oodt.cas.resource.structs.ResourceNode;
import org.apache.oodt.cas.resource.system.MockXmlRpcResourceManagerClient;
import org.apache.oodt.cas.resource.system.MockXmlRpcResourceManagerClient.MethodCallDetails;
import org.apache.oodt.cas.resource.util.JobBuilder;

//JUnit imports
import junit.framework.TestCase;

/**
 * Test class for cas-cli command line for resource manager.
 *
 * @author bfoster (Brian Foster)
 */
public class TestResourceCli extends TestCase {

   static {
      System.setProperty("org.apache.oodt.cas.cli.debug", "true");
      System.setProperty("org.apache.oodt.cas.cli.action.spring.config", "src/main/resources/cmd-line-actions.xml");
      System.setProperty("org.apache.oodt.cas.cli.option.spring.config", "src/main/resources/cmd-line-options.xml");
      System.setProperty("resource.scheduler.factory", "org.apache.oodt.cas.resource.MockScheduler");
   }

   private CmdLineUtility cmdLineUtility;
   private MockXmlRpcResourceManagerClient client;

   @Override
   public void setUp() throws Exception {
      cmdLineUtility = new CmdLineUtility();
      UseMockClientCmdLineActionStore actionStore = new UseMockClientCmdLineActionStore();
      client = actionStore.getClient();
      cmdLineUtility.setActionStore(actionStore);
   }

   @Override
   public void tearDown() throws Exception {
      OptionPropertyRegister.clearRegister();
   }

   public void testAddNode() throws MalformedURLException {
      int capacity = 10;
      String nodeId = "TestNodeId";
      String ipAddr = "http://localhost:9999";
      cmdLineUtility.run(("--url http://localhost:9000"
            + " --operation --addNode --capacity " + capacity + " --nodeId "
            + nodeId + " --ipAddr " + ipAddr).split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("addNode", methodCallDetails.getMethodName());
      ResourceNode actualNode = (ResourceNode) methodCallDetails.getArgs().get(0);
      ResourceNode expectedNode = new ResourceNode(nodeId, new URL(ipAddr), capacity);
      assertEquals(expectedNode.getIpAddr(), actualNode.getIpAddr());
      assertEquals(expectedNode.getCapacity(), actualNode.getCapacity());
      assertEquals(expectedNode.getNodeId(), actualNode.getNodeId());
   }

   public void testAddNodeToQueue() {
      String queueName = "TestQueueName";
      String nodeId = "TestNodeId";
      cmdLineUtility.run(("--url http://localhost:9000"
            + " --operation --addNodeToQueue --queueName " + queueName + " --nodeId "
            + nodeId).split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("addNodeToQueue", methodCallDetails.getMethodName());
      assertEquals(nodeId, methodCallDetails.getArgs().get(0));
      assertEquals(queueName, methodCallDetails.getArgs().get(1));
   }

   public void testAddQueue() {
      String queueName = "TestQueueName";
      cmdLineUtility.run(("--url http://localhost:9000"
            + " --operation --addQueue --queueName " + queueName).split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("addQueue", methodCallDetails.getMethodName());
      assertEquals(queueName, methodCallDetails.getArgs().get(0));
   }

   public void testGetExecNode() {
      String jobId = "TestJobId";
      cmdLineUtility.run(("--url http://localhost:9000"
            + " --operation --getExecNode --jobId " + jobId).split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("getExecutionNode", methodCallDetails.getMethodName());
      assertEquals(jobId, methodCallDetails.getArgs().get(0));
   }

   public void testGetJobInfo() {
      String jobId ="TestJobId";
      cmdLineUtility.run(("--url http://localhost:9000"
            + " --operation --getJobInfo --jobId " + jobId).split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("getJobInfo", methodCallDetails.getMethodName());
      assertEquals(jobId, methodCallDetails.getArgs().get(0));
   }

   public void testGetNodeById() {
      String nodeId = "TestNodeId";
      cmdLineUtility.run(("--url http://localhost:9000"
            + " --operation --getNodeById --nodeId " + nodeId).split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("getNodeById", methodCallDetails.getMethodName());
      assertEquals(nodeId, methodCallDetails.getArgs().get(0));
   }

   public void testGetNodeLoad() {
      String nodeId = "TestNodeId";
      cmdLineUtility.run(("--url http://localhost:9000"
            + " --operation --getNodeLoad --nodeId " + nodeId).split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("getNodeLoad", methodCallDetails.getMethodName());
      assertEquals(nodeId, methodCallDetails.getArgs().get(0));
   }

   public void testGetNodes() {
      cmdLineUtility.run(("--url http://localhost:9000"
            + " --operation --getNodes").split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("getNodes", methodCallDetails.getMethodName());
      assertEquals(0, methodCallDetails.getArgs().size());
   }

   public void testGetNodesInQueue() {
      String queueName = "TestQueueName";
      cmdLineUtility.run(("--url http://localhost:9000"
            + " --operation --getNodesInQueue --queueName "
            + queueName).split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("getNodesInQueue", methodCallDetails.getMethodName());
      assertEquals(queueName, methodCallDetails.getArgs().get(0));
   }

   public void testGetQueues() {
      cmdLineUtility.run(("--url http://localhost:9000"
            + " --operation --getQueues").split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("getQueues", methodCallDetails.getMethodName());
      assertEquals(0, methodCallDetails.getArgs().size());
   }

   public void testGetQueuesWithNode() {
      String nodeId = "TestNodeId";
      cmdLineUtility.run(("--url http://localhost:9000"
            + " --operation --getQueuesWithNode --nodeId "
            + nodeId).split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("getQueuesWithNode", methodCallDetails.getMethodName());
      assertEquals(nodeId, methodCallDetails.getArgs().get(0));
   }

   public void testKill() {
      String jobId = "TestJobId";
      cmdLineUtility.run(("--url http://localhost:9000"
            + " --operation --kill --jobId "
            + jobId).split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("killJob", methodCallDetails.getMethodName());
      assertEquals(jobId, methodCallDetails.getArgs().get(0));
   }

   public void testRemoveNode() {
      String nodeId = "TestNodeId";
      cmdLineUtility.run(("--url http://localhost:9000"
            + " --operation --removeNode --nodeId "
            + nodeId).split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("removeNode", methodCallDetails.getMethodName());
      assertEquals(nodeId, methodCallDetails.getArgs().get(0));
   }

   public void testRemoveNodeFromQueue() {
      String nodeId = "TestNodeId";
      String queueName = "TestQueueNames";
      cmdLineUtility.run(("--url http://localhost:9000"
            + " --operation --removeNodeFromQueue --nodeId "
            + nodeId + " --queueName " + queueName).split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("removeNodeFromQueue", methodCallDetails.getMethodName());
      assertEquals(nodeId, methodCallDetails.getArgs().get(0));
      assertEquals(queueName, methodCallDetails.getArgs().get(1));
   }

   public void testRemoveQueue() {
      String queueName = "TestQueueName";
      cmdLineUtility.run(("--url http://localhost:9000"
            + " --operation --removeQueue --queueName "
            + queueName).split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("removeQueue", methodCallDetails.getMethodName());
      assertEquals(queueName, methodCallDetails.getArgs().get(0));
   }

   public void testSetNodeCapacity() {
      int capacity = 4;
      String nodeId = "TestNodeId";
      cmdLineUtility.run(("--url http://localhost:9000"
            + " --operation --setNodeCapacity --capacity "
            + capacity + " --nodeId " + nodeId).split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("setNodeCapacity", methodCallDetails.getMethodName());
      assertEquals(nodeId, methodCallDetails.getArgs().get(0));
      assertEquals(capacity, methodCallDetails.getArgs().get(1));
   }

   public void testSubmitJob() throws MalformedURLException {
      String jobDefinitionFile = "src/main/resources/examples/jobs/exJob.xml";
      cmdLineUtility.run(("--url http://localhost:9000"
            + " --operation --submitJob --def "
            + jobDefinitionFile).split(" "));
      JobSpec spec = JobBuilder.buildJobSpec(jobDefinitionFile);
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("submitJob", methodCallDetails.getMethodName());
      Job actualJob = (Job) methodCallDetails.getArgs().get(0);
      assertEquals(spec.getJob().getJobInstanceClassName(), actualJob.getJobInstanceClassName());
      assertEquals(spec.getJob().getJobInputClassName(), actualJob.getJobInputClassName());
      assertEquals(spec.getJob().getQueueName(), actualJob.getQueueName());
      assertEquals(spec.getJob().getLoadValue(), actualJob.getLoadValue());
      JobInput actualJobInput = (JobInput) methodCallDetails.getArgs().get(1);
      assertEquals(spec.getIn().getClass(), actualJobInput.getClass());
      assertEquals(2, methodCallDetails.getArgs().size());

      OptionPropertyRegister.clearRegister();

      String url = "http://localhost:9000";
      cmdLineUtility.run(("--url http://localhost:9000"
            + " --operation --submitJob --def "
            + jobDefinitionFile + " --nodeUrl " + url).split(" "));
      methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("submitJob", methodCallDetails.getMethodName());
      actualJob = (Job) methodCallDetails.getArgs().get(0);
      assertEquals(spec.getJob().getJobInstanceClassName(), actualJob.getJobInstanceClassName());
      assertEquals(spec.getJob().getJobInputClassName(), actualJob.getJobInputClassName());
      assertEquals(spec.getJob().getQueueName(), actualJob.getQueueName());
      assertEquals(spec.getJob().getLoadValue(), actualJob.getLoadValue());
      actualJobInput = (JobInput) methodCallDetails.getArgs().get(1);
      assertEquals(spec.getIn().getClass(), actualJobInput.getClass());
      assertEquals(new URL(url), methodCallDetails.getArgs().get(2));
      assertEquals(3, methodCallDetails.getArgs().size());
   }
}
