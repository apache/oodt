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

package org.apache.oodt.cas.workflow.system;

//OODT imports
import org.apache.commons.io.FileUtils;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.instrepo.LuceneWorkflowInstanceRepository;
import org.apache.oodt.cas.workflow.structs.Workflow;
import org.apache.oodt.cas.workflow.structs.WorkflowCondition;
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration;
import org.apache.oodt.cas.workflow.structs.exceptions.InstanceRepositoryException;

//JDK imports
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

//Junit imports
import junit.framework.TestCase;

/**
 * 
 * Test harness for the {@link WorkflowManagerClient}.
 * 
 */
public class TestWorkflowManagerClient extends TestCase {

  private static final String catalogPath = new File("./target/instTestMetCat")
      .getAbsolutePath();

  private LuceneWorkflowInstanceRepository repo = null;
  private WorkflowInstance testWrkInst = null;
  private Workflow testWrkFlw;
  private WorkflowTask testTask;
  private WorkflowCondition testCond;

  private static final int stdPgSz = 20;

  public TestWorkflowManagerClient() {

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

  }

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
    Metadata met = null;
    met = WInst.getSharedContext();
    assertNotNull(met);

    assertNotNull(met.getHashTable());
    assertEquals(2, met.getHashTable().keySet().size());
    assertNotNull(met.getAllMetadata("key1"));
    assertEquals(3, met.getAllMetadata("key1").size());
    assertNotNull(met.getAllMetadata("key2"));
    assertEquals(2, met.getAllMetadata("key2").size());

    // check key-values for key1
    boolean checkVal1 = false, checkVal2 = false, checkVal3 = false;

    for (Iterator i = met.getAllMetadata("key1").iterator(); i.hasNext();) {
      String val = (String) i.next();
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

    for (Iterator i = met.getAllMetadata("key2").iterator(); i.hasNext();) {
      String val = (String) i.next();
      if (val.equals("val4")) {
        checkVal4 = true;
      } else if (val.equals("val5")) {
        checkVal5 = true;
      }
    }

    assertTrue(checkVal4 && checkVal5);
  }
}
