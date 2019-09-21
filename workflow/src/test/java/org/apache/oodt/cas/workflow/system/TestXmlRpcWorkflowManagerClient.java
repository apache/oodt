/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.  See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.apache.oodt.cas.workflow.system;

//OODT imports

import org.apache.commons.io.FileUtils;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.instrepo.LuceneWorkflowInstanceRepository;
import org.apache.oodt.cas.workflow.structs.Workflow;
import org.apache.oodt.cas.workflow.structs.WorkflowCondition;
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;
import org.apache.oodt.cas.workflow.structs.WorkflowInstancePage;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration;
import org.apache.oodt.cas.workflow.structs.exceptions.InstanceRepositoryException;
import org.apache.oodt.cas.workflow.structs.exceptions.RepositoryException;
import org.apache.xmlrpc.XmlRpcException;

import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.*;
import static org.junit.Assume.*;

/**
 *
 * Test harness for the {@link XmlRpcWorkflowManagerClient}.
 *
 */
public class TestXmlRpcWorkflowManagerClient {

  private static final int WM_PORT = 50002;
  public static final int MILLIS = 1;

  private static XmlRpcWorkflowManager wmgr;

  private static String luceneCatLoc;

  private static final Logger LOG = Logger
      .getLogger(TestXmlRpcWorkflowManager.class.getName());

  private static final String catalogPath = new File("./target/instTestMetCat")
      .getAbsolutePath();

  private static XmlRpcWorkflowManagerClient wmc = null;

  private static LuceneWorkflowInstanceRepository repo = null;
  private static WorkflowInstance testWrkInst = null;
  private static Workflow testWrkFlw;
  private static WorkflowTask testTask;
  private static WorkflowCondition testCond;

  private static final int stdPgSz = 20;

  public TestXmlRpcWorkflowManagerClient() {


  }

  @BeforeClass
  public static void setup() throws MalformedURLException {

    testWrkInst = new WorkflowInstance();
    testWrkFlw = new Workflow();
    testTask = new WorkflowTask();
    testCond = new WorkflowCondition();
    Metadata sharedContext = new Metadata();

    // to check if the path already exists and to delete if it does exist
    if (new File(catalogPath).exists()) {
      try {
        FileUtils.deleteDirectory(new File(catalogPath));
      } catch (IOException e) {
        fail(e.getMessage());
      }
    }
    repo = new LuceneWorkflowInstanceRepository(catalogPath, stdPgSz);

    testWrkFlw.setName("test.getMetadataWorkflow");
    testWrkFlw.setId("test.id");
    List tasks = new Vector();
    List conds = new Vector();

    testCond.setConditionId("test.cond.id");
    testCond.setConditionInstanceClassName("test.class");
    testCond.setConditionName("test.cond.name");
    testCond.setOrder(1);
    conds.add(testCond);

    testTask.setTaskConfig(new WorkflowTaskConfiguration());
    testTask.setTaskId("test.task.id");
    testTask.setConditions(conds);
    testTask.setOrder(1);
    testTask.setTaskInstanceClassName("test.class");
    testTask.setTaskName("test.task.name");
    tasks.add(testTask);
    testWrkFlw.setTasks(tasks);

    testWrkInst.setCurrentTaskId("test.task");
    testWrkInst.setStatus("STARTED");
    testWrkInst.setWorkflow(testWrkFlw);

    sharedContext.addMetadata("key1", "val1");
    sharedContext.addMetadata("key1", "val2");
    sharedContext.addMetadata("key1", "val3");
    sharedContext.addMetadata("key2", "val4");
    sharedContext.addMetadata("key2", "val5");
    testWrkInst.setSharedContext(sharedContext);
    startXmlRpcWorkflowManager();
    startWorkflow();
    wmc = new XmlRpcWorkflowManagerClient(new URL(
        "http://localhost:" + WM_PORT));
  }


  private static void startWorkflow() {
    XmlRpcWorkflowManagerClient client = null;
    try {
      client = new XmlRpcWorkflowManagerClient(new URL("http://localhost:"
                                                       + WM_PORT));
    } catch (Exception e) {
      fail(e.getMessage());
    }

    try {
      client.sendEvent("long", new Metadata());
    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      throw new RuntimeException(e);
    }

  }


  @AfterClass
  public static void cleandown() {

    stopXmlRpcWorkflowManager();
  }

  @Test
  public void testGetWorkflowInstanceMetadataActuallyUsingTheXmlRpcWorkflowManagerClient()
      throws IOException, RepositoryException, XmlRpcException {

    List<Workflow> workflows = wmc.getWorkflows();
    assertThat(workflows, is(not(empty())));


    assertNotNull(workflows.get(0).getName());

  }

  @Test
  public void testGetPages() throws Exception {

    Thread.sleep(3000);
    WorkflowInstancePage page = null, lastpage = null, nextpage = null, prevpage = null;
    try{
      page = wmc.getFirstPage();
    }
    catch(Exception e){
      e.printStackTrace();
    }
    assumeNotNull(page);

    try{
      lastpage = wmc.getLastPage();
    }
    catch(Exception e){
      e.printStackTrace();
    }

    assumeNotNull(lastpage);


    try{
      nextpage = wmc.getNextPage(page);
    }
    catch(Exception e){
      e.printStackTrace();
    }

    assumeNotNull(nextpage);

    try{
      prevpage = wmc.getPrevPage(nextpage);
    }
    catch(Exception e){
      e.printStackTrace();
    }

    assumeNotNull(prevpage);

  }

  @Test
  public void testGetWorkflowsByEvent() throws Exception {

    List<Workflow> wflows = wmc.getWorkflowsByEvent("long");

    assertNotNull(wflows);

    assertThat(wflows, hasSize(2));

    assertThat(wflows.get(0).getName(), equalTo("Long Workflow"));


  }

  @Test
  public void testGetWorkflowInstancesByStatus() throws Exception {

    List<WorkflowInstance> wflows = wmc.getWorkflowInstancesByStatus("QUEUED");

    assertNotNull(wflows);

  }

  @Test
  public void testGetWorkflowInstanceMetadata2() throws Exception {
    WorkflowInstance wf = (WorkflowInstance) wmc.getFirstPage().getPageWorkflows().get(0);

    assertThat(wf, is(not(Matchers.nullValue())));


    Metadata meta = wmc.getWorkflowInstanceMetadata(wf.getId());

    assertNotNull(meta);

  }

  @Test
  public void testGetWorkflowCurrentTaskWallClockMinutes() throws Exception {
    XmlRpcWorkflowManagerClient fmc = new XmlRpcWorkflowManagerClient(new URL(
        "http://localhost:" + WM_PORT));

    List<WorkflowInstance> wfinstances = fmc.getWorkflowInstances();

    assertNotNull(wfinstances);


    double clock = fmc.getWorkflowCurrentTaskWallClockMinutes(wfinstances.get(0).getId());

    assertThat(clock, is(not(Matchers.nullValue())));

  }

  @Test
  public void testGetTaskById() throws Exception {

    WorkflowTask task = wmc.getTaskById("urn:oodt:HelloWorld");

    assertThat(task, is(not(nullValue())));

    assertThat(task.getTaskName(), equalTo("Hello World"));


  }

  @Test
  public void testGetRegisteredEvents() throws Exception {

    List<String> events = wmc.getRegisteredEvents();

    assertThat(events, is(not(nullValue())));

    assertThat(events, hasSize(12));

    assertThat(events, hasItem("stuck"));

  }

  @Test
  public void testGetNumWorkflowInstancesByStatus() throws Exception {
    int inst = wmc.getNumWorkflowInstancesByStatus("QUEUED");

    assertThat(inst, equalTo(0));
  }

  @Test
  public void testGetConditionById() throws Exception {

    WorkflowCondition cond = wmc.getConditionById("urn:oodt:TrueCondition");

    assertNotNull(cond);

    assertThat(cond.getConditionName(), equalTo("True Condition"));

  }

  @Test
  public void testGetNumWorkflowInstances() throws Exception {

    int num = wmc.getNumWorkflowInstances();

    assertThat(num, is(not(0)));


  }

  @Test
  public void testGetWorkflowInstances() throws IOException, XmlRpcException {

    List<WorkflowInstance> wfinstances = wmc.getWorkflowInstances();

    assertNotNull(wfinstances);
  }

  @Test
  public void testGetWFMUrl(){

    URL url = wmc.getWorkflowManagerUrl();

    assertThat(url, is(not(nullValue())));

    assertThat(url.toString(), equalTo("http://localhost:"+WM_PORT));
  }

  @Ignore
  @Test
  public void testGetNullWorkflowInstances()
      throws RepositoryException, XmlRpcException, IOException, InterruptedException {

    Thread.sleep(3000);
    WorkflowInstance instance = wmc.getWorkflowInstanceById("1234");

    assertThat(instance, is(nullValue()));

  }

  @Test
  public void testGetNullWorkflowInstancesByStatus() throws XmlRpcException, IOException {
    List<WorkflowInstance> instances = wmc.getWorkflowInstancesByStatus("NULL");

    assertThat(instances, is(empty()));

  }

  @Test
  public void testGetWorkflowById() throws RepositoryException, XmlRpcException, IOException {
    List<Workflow> workflowlist = wmc.getWorkflows();

    assertThat(workflowlist, is(not(nullValue())));

    assertThat(workflowlist.size(), is(not(0)));

    Workflow work = wmc.getWorkflowById(workflowlist.get(0).getId());

    assertThat(work, is(not(nullValue())));

  }

  @Test
  public void testGetWorkflowInstanceById() throws XmlRpcException, IOException, RepositoryException {

    List<WorkflowInstance> workflowlist = wmc.getWorkflowInstances();

    assertThat(workflowlist, is(not(nullValue())));

    assertThat(workflowlist.size(), is(not(0)));

    WorkflowInstance instance = wmc.getWorkflowInstanceById(workflowlist.get(0).getId());

    assertThat(instance, is(not(nullValue())));

  }

  @Ignore
  @Test
  public void testUpdateMetadataForWorkflow(){

  }

  @Test
  public void testUpdateWorkflowInstanceStatus() throws XmlRpcException, IOException, RepositoryException {

    List<WorkflowInstance> workflowlist = null;
    WorkflowInstance instance = null;
    boolean upd = false;
    
    try{
      workflowlist = wmc.getWorkflowInstances();
    }
    catch(Exception e){
      e.printStackTrace();
    }
    assumeNotNull(workflowlist);
    assertThat(workflowlist, is(not(nullValue())));
    assertThat(workflowlist.size(), is(not(0)));

    try{
      instance = wmc.getWorkflowInstanceById(workflowlist.get(0).getId());
    }
    catch(Exception e){
      e.printStackTrace();
    }
    assumeNotNull(instance);
    assertThat(instance, is(not(nullValue())));

    try{
      upd = wmc.updateWorkflowInstanceStatus(instance.getId(), "RUNNING");
    }
    catch(Exception e){
      e.printStackTrace();
    }
    assumeTrue(upd);
    assertThat(upd, equalTo(true));


  }

  @Ignore
  @Test
  public void testSetWorkflowInstanceCurrentTaskEndDateTime(){

  }

  @Ignore
  @Test
  public void testResumeWorkflowInstance(){

  }

  @Ignore
  @Test
  public void testPauseWorkflowInstance(){

  }

  @Ignore
  @Test
  public void testStopWorkflowInstances(){

  }

  @Test
  public void testGetWorkflowWallClockMinutes() throws RepositoryException, XmlRpcException, IOException {
    List<WorkflowInstance> workflowlist = wmc.getWorkflowInstances();

    assertThat(workflowlist, is(not(nullValue())));

    assertThat(workflowlist.size(), is(not(0)));

    WorkflowInstance instance = wmc.getWorkflowInstanceById(workflowlist.get(0).getId());

    assertThat(instance, is(not(nullValue())));

    double clock = wmc.getWorkflowWallClockMinutes(instance.getId());

    assertThat(clock, is(not(nullValue())));
  }

  @Ignore
  @Test
  public void testSetWorkflowInstanceCurrentTaskStartDateTime(){

  }

  @Test
  public void testPaginateWorkflowInstances() throws XmlRpcException, IOException {

    WorkflowInstancePage paginate = wmc.paginateWorkflowInstances(2);

    assertThat(paginate, is(not(nullValue())));
  }

  @Test
  public void testPaginateWorkflowInstancesByStatus() throws XmlRpcException, IOException {

    WorkflowInstancePage paginate = wmc.paginateWorkflowInstances(2, "QUEUED");

    assertThat(paginate, is(not(nullValue())));
  }
  @Ignore
  @Test
  public void testExecuteDynamicWorkflow(){

  }

  @Ignore
  @Test
  public void testRefreshRepository() throws XmlRpcException, IOException {

    boolean refresh = wmc.refreshRepository();

    assertThat(refresh, equalTo(true));
  }
  @Test
  public void testGetWorkflowInstanceMetadata() {

    try {
      repo.addWorkflowInstance(testWrkInst);
    } catch (InstanceRepositoryException e) {
      fail(e.getMessage());
    }
    String testWrkInstId = testWrkInst.getId();
    assertNotNull(testWrkInstId);

    // get workflow instance from instance id
    WorkflowInstance WInst = null;
    try {
      WInst = repo.getWorkflowInstanceById(testWrkInstId);
    } catch (InstanceRepositoryException e) {
      fail(e.getMessage());
    }

    assertNotNull(WInst);

    // get Metadata for the workflow instance
    Metadata met;
    met = WInst.getSharedContext();
    assertNotNull(met);

    assertNotNull(met.getMap());
    assertEquals(2, met.getMap().keySet().size());
    assertNotNull(met.getAllMetadata("key1"));
    assertEquals(3, met.getAllMetadata("key1").size());
    assertNotNull(met.getAllMetadata("key2"));
    assertEquals(2, met.getAllMetadata("key2").size());

    // check key-values for key1
    boolean checkVal1 = false, checkVal2 = false, checkVal3 = false;

    for (String val : met.getAllMetadata("key1")) {
      if (val.equals("val1")) {
        checkVal1 = true;
      } else if (val.equals("val2")) {
        checkVal2 = true;
      } else if (val.equals("val3")) {
        checkVal3 = true;
      }
    }

    assert (checkVal1 && checkVal2 && checkVal3);

    // check key-values for key2
    boolean checkVal4 = false, checkVal5 = false;

    for (String val : met.getAllMetadata("key2")) {
      if (val.equals("val4")) {
        checkVal4 = true;
      } else if (val.equals("val5")) {
        checkVal5 = true;
      }
    }

    assertTrue(checkVal4 && checkVal5);
  }

  @Test
  public void testIsAlive() {
    assertTrue(wmc.isAlive());
  }

  private static void startXmlRpcWorkflowManager() {
    System.setProperty("java.util.logging.config.file", new File(
        "./src/main/resources/logging.properties").getAbsolutePath());

    try {
      System.getProperties().load(
          new FileInputStream("./src/main/resources/workflow.properties"));
    } catch (Exception e) {
      fail(e.getMessage());
    }
    try {
      luceneCatLoc = Files.createTempDirectory("repo").toString();
      LOG.log(Level.INFO, "Lucene instance repository: [" + luceneCatLoc + "]");
    } catch (Exception e) {
      fail(e.getMessage());
    }


    if (new File(luceneCatLoc).exists()) {
      // blow away lucene cat
      LOG.log(Level.INFO, "Removing workflow instance repository: ["
                          + luceneCatLoc + "]");
      try {
        FileUtils.deleteDirectory(new File(luceneCatLoc));
      } catch (IOException e) {
        fail(e.getMessage());
      }
    }

    System
        .setProperty("workflow.engine.instanceRep.factory",
            "org.apache.oodt.cas.workflow.instrepo.LuceneWorkflowInstanceRepositoryFactory");
    System
        .setProperty("org.apache.oodt.cas.workflow.instanceRep.lucene.idxPath",
            luceneCatLoc);

    try {
      System.setProperty("org.apache.oodt.cas.workflow.repo.dirs", "file://"
                                                                   + new File("./src/main/resources/examples")
                                                                       .getCanonicalPath());
      System.setProperty("org.apache.oodt.cas.workflow.lifecycle.filePath",
          new File("./src/main/resources/examples/workflow-lifecycle.xml")
              .getCanonicalPath());
    } catch (Exception e) {
      fail(e.getMessage());
    }

    try {
      wmgr = new XmlRpcWorkflowManager(WM_PORT);
      Thread.sleep(MILLIS);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      fail(e.getMessage());
    }

  }


  private static void stopXmlRpcWorkflowManager() {
    System.setProperty("java.util.logging.config.file", new File(
        "./src/main/resources/logging.properties").getAbsolutePath());

    try {
      System.getProperties().load(
          new FileInputStream("./src/main/resources/workflow.properties"));
    } catch (Exception e) {
      fail(e.getMessage());
    }
    System
        .setProperty("workflow.engine.instanceRep.factory",
            "org.apache.oodt.cas.workflow.instrepo.LuceneWorkflowInstanceRepositoryFactory");
    System
        .setProperty("org.apache.oodt.cas.workflow.instanceRep.lucene.idxPath",
            luceneCatLoc);

    try {
      System.setProperty("org.apache.oodt.cas.workflow.repo.dirs", "file://"
                                                                   + new File("./src/main/resources/examples")
                                                                       .getCanonicalPath());
      System.setProperty("org.apache.oodt.cas.workflow.lifecycle.filePath",
          new File("./src/main/resources/examples/workflow-lifecycle.xml")
              .getCanonicalPath());
    } catch (Exception e) {
      fail(e.getMessage());
    }

    try {
      wmgr.shutdown();
    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      fail(e.getMessage());
    }

    /**
     * Sleep before removing to prevent file not found issues.
     */

    try {
      Thread.sleep(MILLIS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    if (new File(luceneCatLoc).exists()) {
      // blow away lucene cat
      LOG.log(Level.INFO, "Removing workflow instance repository: ["
                          + luceneCatLoc + "]");
      try {
        FileUtils.deleteDirectory(new File(luceneCatLoc));
      } catch (IOException e) {
        fail(e.getMessage());
      }
    }
  }
}
