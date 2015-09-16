/**
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
package org.apache.oodt.cas.workflow.cli;

//OODT imports
import org.apache.oodt.cas.cli.CmdLineUtility;
import org.apache.oodt.cas.cli.util.OptionPropertyRegister;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.repository.MockWorkflowRepository;
import org.apache.oodt.cas.workflow.system.MockWorkflowManagerClient;
import org.apache.oodt.cas.workflow.system.MockWorkflowManagerClient.MethodCallDetails;

//Google imports
import com.google.common.collect.Lists;

//JUnit imports
import junit.framework.TestCase;

/**
 * Tests Workflow Manager Clients Command-line interface.
 *
 * @author bfoster (Brian Foster)
 */
public class TestWorkflowCli extends TestCase {

   static {
      System.setProperty("org.apache.oodt.cas.cli.debug", "true");
      System.setProperty("org.apache.oodt.cas.cli.action.spring.config", "src/main/resources/cmd-line-actions.xml");
      System.setProperty("org.apache.oodt.cas.cli.option.spring.config", "src/main/resources/cmd-line-options.xml");
      System.setProperty("workflow.engine.factory", "org.apache.oodt.cas.workflow.engine.MockWorkflowEngineFactory");
      System.setProperty("workflow.engine.instanceRep.factory", "org.apache.oodt.cas.workflow.instrepo.MemoryWorkflowInstanceRepositoryFactory");
      System.setProperty("workflow.repo.factory", "org.apache.oodt.cas.workflow.repository.MockWorkflowRepositoryFactory");
   }

   private CmdLineUtility cmdLineUtility;
   private MockWorkflowManagerClient client;

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

   public void testDynWorkflow() throws Exception {
      cmdLineUtility
            .run(("--url http://localhost:9000"
                  + " --operation --dynWorkflow --taskIds "
                  + MockWorkflowRepository.TASK1_ID + " "
                  + MockWorkflowRepository.TASK2_ID + " "
                  + MockWorkflowRepository.TASK3_ID
                  + " --metaData --key Filename data.dat --key NominalDate 2001-02-20")
                  .split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("executeDynamicWorkflow", methodCallDetails.getMethodName());
      assertEquals(Lists.newArrayList(MockWorkflowRepository.TASK1_ID,
            MockWorkflowRepository.TASK2_ID, MockWorkflowRepository.TASK3_ID),
            methodCallDetails.getArgs().get(0));
      Metadata m = new Metadata();
      m.addMetadata("NominalDate", Lists.newArrayList("2001-02-20"));
      m.addMetadata("Filename", Lists.newArrayList("data.dat"));
      assertEquals(m, methodCallDetails.getArgs().get(1));
   }

   public void testGetConditionById() {
      cmdLineUtility
            .run(("--url http://localhost:9000"
                  + " --operation --getConditionById --id "
                  + MockWorkflowRepository.CONDITION1_ID)
                  .split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("getConditionById", methodCallDetails.getMethodName());
      assertEquals(MockWorkflowRepository.CONDITION1_ID, methodCallDetails.getArgs().get(0));
   }

   public void testGetFirstPage() {
      cmdLineUtility.run(("--url http://localhost:9000"
            + " --operation --getFirstPage").split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("getFirstPage", methodCallDetails.getMethodName());

      OptionPropertyRegister.clearRegister();

      String status = "DONE";
      cmdLineUtility.run(("--url http://localhost:9000"
            + " --operation --getFirstPage --status " + status).split(" "));
      methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("paginateWorkflowInstances", methodCallDetails.getMethodName());
      assertEquals(1, methodCallDetails.getArgs().get(0));
      assertEquals(status, methodCallDetails.getArgs().get(1));
   }
 
   public void testGetLastPage() {
      cmdLineUtility.run(("--url http://localhost:9000"
            + " --operation --getLastPage").split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("getLastPage", methodCallDetails.getMethodName());

      OptionPropertyRegister.clearRegister();

      String status = "DONE";
      cmdLineUtility.run(("--url http://localhost:9000"
            + " --operation --getLastPage --status " + status).split(" "));
      methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("paginateWorkflowInstances", methodCallDetails.getMethodName());
      assertEquals(0, methodCallDetails.getArgs().get(0));
      assertEquals(status, methodCallDetails.getArgs().get(1));
   }

   public void testGetNextPage() {
      cmdLineUtility.run(("--url http://localhost:9000"
            + " --operation --getNextPage --pageNum 1").split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("paginateWorkflowInstances", methodCallDetails.getMethodName());

      OptionPropertyRegister.clearRegister();

      String status = "DONE";
      cmdLineUtility.run(("--url http://localhost:9000"
            + " --operation --getNextPage --pageNum 1 --status " + status).split(" "));
      methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("paginateWorkflowInstances", methodCallDetails.getMethodName());
      assertEquals(2, methodCallDetails.getArgs().get(0));
      assertEquals(status, methodCallDetails.getArgs().get(1));
   }

   public void testGetPrevPage() {
      cmdLineUtility.run(("--url http://localhost:9000"
            + " --operation --getPrevPage --pageNum 1").split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("paginateWorkflowInstances", methodCallDetails.getMethodName());

      OptionPropertyRegister.clearRegister();

      String status = "DONE";
      cmdLineUtility.run(("--url http://localhost:9000"
            + " --operation --getPrevPage --pageNum 1 --status " + status).split(" "));
      methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("paginateWorkflowInstances", methodCallDetails.getMethodName());
      assertEquals(0, methodCallDetails.getArgs().get(0));
      assertEquals(status, methodCallDetails.getArgs().get(1));
   }

   public void testGetRegisteredEvents() {
      cmdLineUtility.run(("--url http://localhost:9000"
            + " --operation --getRegisteredEvents").split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("getRegisteredEvents", methodCallDetails.getMethodName());
      assertTrue(methodCallDetails.getArgs().isEmpty());
   }

   public void testGetTaskById() {
      String taskId = MockWorkflowRepository.TASK1_ID;
      cmdLineUtility.run(("--url http://localhost:9000"
            + " --operation --getTaskById --id " + taskId).split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("getTaskById", methodCallDetails.getMethodName());
      assertEquals(taskId, methodCallDetails.getArgs().get(0));
   }

   public void testGetTaskWallClockTime() {
      String taskId = MockWorkflowRepository.TASK1_ID;
      cmdLineUtility.run(("--url http://localhost:9000"
            + " --operation --getTaskWallClockTime --id " + taskId).split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("getWorkflowCurrentTaskWallClockMinutes", methodCallDetails.getMethodName());
      assertEquals(taskId, methodCallDetails.getArgs().get(0));
   }

   public void testGetWallClockTime() {
      String taskId = MockWorkflowRepository.TASK1_ID;
      cmdLineUtility.run(("--url http://localhost:9000"
            + " --operation --getWallClockTime --id " + taskId).split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("getWorkflowWallClockMinutes", methodCallDetails.getMethodName());
      assertEquals(taskId, methodCallDetails.getArgs().get(0));
   }

   public void testGetWorkflowById() {
      String workflowId = MockWorkflowRepository.WORKFLOW1_ID;
      cmdLineUtility.run(("--url http://localhost:9000"
            + " --operation --getWorkflowById --id " + workflowId).split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("getWorkflowById", methodCallDetails.getMethodName());
      assertEquals(workflowId, methodCallDetails.getArgs().get(0));
   }

   public void testGetWorkflowInst() {
      String instId = "TestId";
      cmdLineUtility.run(("--url http://localhost:9000"
            + " --operation --getWorkflowInst --id " + instId).split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("getWorkflowCurrentTaskWallClockMinutes", methodCallDetails.getMethodName());
      assertEquals(instId, methodCallDetails.getArgs().get(0));
   }

   public void testGetWorkflowInstMet() {
      String instId = "TestId";
      cmdLineUtility.run(("--url http://localhost:9000"
            + " --operation --getWorkflowInstMet --id " + instId).split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("getWorkflowInstanceMetadata", methodCallDetails.getMethodName());
      assertEquals(instId, methodCallDetails.getArgs().get(0));
   }

   public void testGetWorkflowInsts() {
      cmdLineUtility.run(("--url http://localhost:9000"
            + " --operation --getWorkflowInsts").split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("getWorkflowInstances", methodCallDetails.getMethodName());
      assertTrue(methodCallDetails.getArgs().isEmpty());
   }

   public void testGetWorkflowsByEvent() {
      String eventName = "TestEventName";
      cmdLineUtility.run(("--url http://localhost:9000"
            + " --operation --getWorkflowsByEvent --eventName " + eventName).split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("getWorkflowsByEvent", methodCallDetails.getMethodName());
      assertEquals(eventName, methodCallDetails.getArgs().get(0));
   }

   public void testGetWorkflows() {
      cmdLineUtility.run(("--url http://localhost:9000"
            + " --operation --getWorkflows").split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("getWorkflows", methodCallDetails.getMethodName());
      assertTrue(methodCallDetails.getArgs().isEmpty());
   }

   public void testPauseWorkflowInst() {
      String instId = "TestId";
      cmdLineUtility.run(("--url http://localhost:9000"
            + " --operation --pauseWorkflowInst --id " + instId).split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("pauseWorkflowInstance", methodCallDetails.getMethodName());
      assertEquals(instId, methodCallDetails.getArgs().get(0));
   }

   public void testResumeWorkflowInst() {
      String instId = "TestId";
      cmdLineUtility.run(("--url http://localhost:9000"
            + " --operation --resumeWorkflowInst --id " + instId).split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("resumeWorkflowInstance", methodCallDetails.getMethodName());
      assertEquals(instId, methodCallDetails.getArgs().get(0));
   }

   public void testSendEvent() {
      String eventName = "TestEventName";
      cmdLineUtility.run(("--url http://localhost:9000"
            + " --operation --sendEvent --eventName " + eventName).split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("sendEvent", methodCallDetails.getMethodName());
      assertEquals(eventName, methodCallDetails.getArgs().get(0));

      OptionPropertyRegister.clearRegister();

      cmdLineUtility.run(("--url http://localhost:9000"
           + " --operation --sendEvent --eventName " + eventName
           + " --metaData --key Filename data.dat --key NominalDate 2001-02-20").split(" "));
      methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("sendEvent", methodCallDetails.getMethodName());
      assertEquals(eventName, methodCallDetails.getArgs().get(0));
      Metadata m = new Metadata();
      m.addMetadata("Filename", Lists.newArrayList("data.dat"));
      m.addMetadata("NominalDate", Lists.newArrayList("2001-02-20"));
      assertEquals(m, methodCallDetails.getArgs().get(1));
   }

   public void testStopWorkflowInstance() {
      String instId = "TestId";
      cmdLineUtility.run(("--url http://localhost:9000"
            + " --operation --stopWorkflowInst --id " + instId).split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("stopWorkflowInstance", methodCallDetails.getMethodName());
      assertEquals(instId, methodCallDetails.getArgs().get(0));
   }
}
