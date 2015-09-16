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
package org.apache.oodt.cas.workflow.system;

//JDK imports
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Vector;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.repository.MockWorkflowRepository;
import org.apache.oodt.cas.workflow.structs.Workflow;
import org.apache.oodt.cas.workflow.structs.WorkflowCondition;
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;
import org.apache.oodt.cas.workflow.structs.WorkflowInstancePage;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;

//Google imports
import com.google.common.collect.Lists;

/**
 * A Mock {@link AvroRpcWorkflowManagerClient}.
 *
 * @author bfoster (Brian Foster)
 */
public class MockWorkflowManagerClient extends
      AvroRpcWorkflowManagerClient {

   private MethodCallDetails lastMethodCallDetails;
   private MockWorkflowRepository workflowRepo;

   public MockWorkflowManagerClient() throws MalformedURLException {
      super(new URL("http://localhost:9000"));
      workflowRepo = new MockWorkflowRepository();
   }

   public MethodCallDetails getLastMethodCallDetails() {
      return lastMethodCallDetails;
   }

   public String executeDynamicWorkflow(List<String> taskIds, Metadata metadata)
         throws Exception {
      lastMethodCallDetails = new MethodCallDetails("executeDynamicWorkflow",
            Lists.newArrayList(taskIds, metadata));
      return "TestId1";
   }

   public List<String> getRegisteredEvents() throws Exception {
      lastMethodCallDetails = new MethodCallDetails("getRegisteredEvents",
            Lists.newArrayList());
      return workflowRepo.getRegisteredEvents();
   }

   public WorkflowInstancePage getFirstPage() throws Exception {
      lastMethodCallDetails = new MethodCallDetails("getFirstPage",
            Lists.newArrayList());
      WorkflowInstancePage page = new WorkflowInstancePage();
      page.setPageNum(1);
      page.setPageSize(0);
      page.setTotalPages(0);
      page.setPageWorkflows(Lists.newArrayList());
      return page;
   }

   public WorkflowInstancePage getNextPage(WorkflowInstancePage currentPage)
         throws Exception {
      lastMethodCallDetails = new MethodCallDetails("getNextPage",
            Lists.newArrayList((Object) currentPage));
      WorkflowInstancePage page = new WorkflowInstancePage();
      page.setPageNum(1);
      page.setPageSize(0);
      page.setTotalPages(0);
      page.setPageWorkflows(Lists.newArrayList());
      return page;
   }

   public WorkflowInstancePage getPrevPage(WorkflowInstancePage currentPage)
         throws Exception {
      lastMethodCallDetails = new MethodCallDetails("getPrevPage",
            Lists.newArrayList((Object) currentPage));
      WorkflowInstancePage page = new WorkflowInstancePage();
      page.setPageNum(1);
      page.setPageSize(0);
      page.setTotalPages(0);
      page.setPageWorkflows(Lists.newArrayList());
      return page;
   }

   public WorkflowInstancePage getLastPage() throws Exception {
      lastMethodCallDetails = new MethodCallDetails("getLastPage", null);
      WorkflowInstancePage page = new WorkflowInstancePage();
      page.setPageNum(1);
      page.setPageSize(0);
      page.setTotalPages(0);
      page.setPageWorkflows(Lists.newArrayList());
      return page;
   }

   public WorkflowInstancePage paginateWorkflowInstances(int pageNum,
         String status) throws Exception {
      lastMethodCallDetails = new MethodCallDetails("paginateWorkflowInstances",
            Lists.newArrayList(pageNum, (Object) status));
      WorkflowInstancePage page = new WorkflowInstancePage();
      page.setPageNum(1);
      page.setPageSize(0);
      page.setTotalPages(0);
      page.setPageWorkflows(Lists.newArrayList());
      return page;
   }

   public WorkflowInstancePage paginateWorkflowInstances(int pageNum)
         throws Exception {
      lastMethodCallDetails = new MethodCallDetails("paginateWorkflowInstances",
            Lists.newArrayList((Object) pageNum));
      WorkflowInstancePage page = new WorkflowInstancePage();
      page.setPageNum(1);
      page.setPageSize(0);
      page.setTotalPages(0);
      page.setPageWorkflows(Lists.newArrayList());
      return page;
   }

   public List<Workflow> getWorkflowsByEvent(String eventName) throws Exception {
      lastMethodCallDetails = new MethodCallDetails("getWorkflowsByEvent",
            Lists.newArrayList((Object) eventName));
      return workflowRepo.getWorkflowsForEvent(eventName);
   }

   public Metadata getWorkflowInstanceMetadata(String wInstId) throws Exception {
      lastMethodCallDetails = new MethodCallDetails("getWorkflowInstanceMetadata",
            Lists.newArrayList((Object) wInstId));
      return new Metadata();
   }

   public synchronized boolean setWorkflowInstanceCurrentTaskStartDateTime(
         String wInstId, String startDateTimeIsoStr) throws Exception {
      lastMethodCallDetails = new MethodCallDetails(
            "setWorkflowInstanceCurrentTaskStartDateTime",
            Lists.newArrayList((Object) wInstId, startDateTimeIsoStr));
      return true;
   }

   public double getWorkflowCurrentTaskWallClockMinutes(String workflowInstId)
         throws Exception {
      lastMethodCallDetails = new MethodCallDetails(
            "getWorkflowCurrentTaskWallClockMinutes",
            Lists.newArrayList((Object) workflowInstId));
      return 0.0;
   }

   public double getWorkflowWallClockMinutes(String workflowInstId)
         throws Exception {
      lastMethodCallDetails = new MethodCallDetails(
            "getWorkflowWallClockMinutes",
            Lists.newArrayList((Object) workflowInstId));
      return 0.0;
   }

   public synchronized boolean stopWorkflowInstance(String workflowInstId)
         throws Exception {
      lastMethodCallDetails = new MethodCallDetails(
            "stopWorkflowInstance",
            Lists.newArrayList((Object) workflowInstId));
      return true;
   }

   public synchronized boolean pauseWorkflowInstance(String workflowInstId)
         throws Exception {
      lastMethodCallDetails = new MethodCallDetails(
            "pauseWorkflowInstance",
            Lists.newArrayList((Object) workflowInstId));
      return true;
   }

   public synchronized boolean resumeWorkflowInstance(String workflowInstId)
         throws Exception {
      lastMethodCallDetails = new MethodCallDetails(
            "resumeWorkflowInstance",
            Lists.newArrayList((Object) workflowInstId));
      return true;
   }

   public synchronized boolean setWorkflowInstanceCurrentTaskEndDateTime(
         String wInstId, String endDateTimeIsoStr) throws Exception {
      lastMethodCallDetails = new MethodCallDetails(
            "setWorkflowInstanceCurrentTaskEndDateTime",
            Lists.newArrayList((Object) wInstId, endDateTimeIsoStr));
      return true;
   }

   public synchronized boolean updateWorkflowInstanceStatus(
         String workflowInstId, String status) throws Exception {
      lastMethodCallDetails = new MethodCallDetails(
            "updateWorkflowInstanceStatus",
            Lists.newArrayList((Object) workflowInstId, status));
      return true;
   }

   public synchronized boolean updateWorkflowInstance(WorkflowInstance instance)
         throws Exception {
      lastMethodCallDetails = new MethodCallDetails(
            "updateWorkflowInstance",
            Lists.newArrayList((Object) instance));
      return true;
   }

   public synchronized boolean updateMetadataForWorkflow(String workflowInstId,
         Metadata metadata) throws Exception {
      lastMethodCallDetails = new MethodCallDetails(
            "updateMetadataForWorkflow",
            Lists.newArrayList((Object) workflowInstId, metadata));
      return true;
   }

   public boolean sendEvent(String eventName, Metadata metadata)
         throws Exception {
      lastMethodCallDetails = new MethodCallDetails(
            "sendEvent",
            Lists.newArrayList((Object) eventName, metadata));
      return true;
   }

   public WorkflowTask getTaskById(String taskId) throws Exception {
      lastMethodCallDetails = new MethodCallDetails(
            "getTaskById",
            Lists.newArrayList((Object) taskId));
      return workflowRepo.getWorkflowTaskById(taskId);
   }

   public WorkflowCondition getConditionById(String conditionId)
         throws Exception {
      lastMethodCallDetails = new MethodCallDetails(
            "getConditionById",
            Lists.newArrayList((Object) conditionId));
      return workflowRepo.getWorkflowConditionById(conditionId);
   }

   public WorkflowInstance getWorkflowInstanceById(String wInstId)
         throws Exception {
      lastMethodCallDetails = new MethodCallDetails(
            "getWorkflowInstanceById",
            Lists.newArrayList((Object) wInstId));
      WorkflowInstance wInst = new WorkflowInstance();
      wInst.setStatus("Running");
      wInst.setId("TestId");
      wInst.setWorkflow(workflowRepo.getWorkflowById(
            MockWorkflowRepository.WORKFLOW1_ID));
      wInst.setCurrentTaskId(MockWorkflowRepository.TASK1_ID);
      return wInst;
   }

   public Workflow getWorkflowById(String workflowId) throws Exception {
      lastMethodCallDetails = new MethodCallDetails(
            "getWorkflowById",
            Lists.newArrayList((Object) workflowId));
      return workflowRepo.getWorkflowById(workflowId);
   }

   public Vector<Workflow> getWorkflows() throws Exception {
      lastMethodCallDetails = new MethodCallDetails(
            "getWorkflows",
            Lists.newArrayList());
      return new Vector<Workflow>(workflowRepo.getWorkflows());
   }

   public int getNumWorkflowInstancesByStatus(String status) throws Exception {
      lastMethodCallDetails = new MethodCallDetails(
            "getNumWorkflowInstancesByStatus",
            Lists.newArrayList((Object) status));
      return 1;
   }

   public int getNumWorkflowInstances() throws Exception {
      lastMethodCallDetails = new MethodCallDetails(
            "getNumWorkflowInstances",
            Lists.newArrayList());
      return 1;
   }

   public Vector<WorkflowInstance> getWorkflowInstancesByStatus(String status) throws Exception {
      lastMethodCallDetails = new MethodCallDetails(
            "getWorkflowInstancesByStatus",
            Lists.newArrayList((Object) status));
      return new Vector<WorkflowInstance>();
   }

   public Vector<WorkflowInstance> getWorkflowInstances() throws Exception {
      lastMethodCallDetails = new MethodCallDetails(
            "getWorkflowInstances",
            Lists.newArrayList());
      return new Vector<WorkflowInstance>();
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
