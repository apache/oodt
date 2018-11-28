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

package org.apache.oodt.cas.workflow.lifecycle;

//JDK imports
import java.io.File;
import java.util.List;

import org.apache.oodt.cas.workflow.structs.Graph;
import org.apache.oodt.cas.workflow.structs.ParentChildWorkflow;
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;

//Junit imports
import junit.framework.TestCase;

/**
 * 
 * Test harness for {@link WorkflowLifecycleManager}.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class TestWorkflowLifecycleManager extends TestCase {

  private WorkflowLifecycleManager lifecycle;

  public void testPctCompleteWengineStages(){
    ParentChildWorkflow workflow = new ParentChildWorkflow(new Graph());
    WorkflowState successState = lifecycle.getDefaultLifecycle().createState("Success", "done", "All done.");
    WorkflowInstance instance = new WorkflowInstance();
    instance.setState(successState);
    instance.setParentChildWorkflow(workflow);
    double pct = lifecycle.getPercentageComplete(instance);
    assertNotNull(pct);
    assertEquals(1.0, pct);
  }
  
  public void testPctCompleteWorkflow1Stages() throws InstantiationException{
    this.lifecycle = new WorkflowLifecycleManager("./src/main/resources"
        + "/examples/workflow-lifecycle.xml");
    assertNotNull(this.lifecycle);
    assertNotNull(this.lifecycle.getDefaultLifecycle());
    assertNotNull(this.lifecycle.getDefaultLifecycle().getStages());
    ParentChildWorkflow workflow = new ParentChildWorkflow(new Graph());
    WorkflowState successState = lifecycle.getDefaultLifecycle().createState("FINISHED", "done", "All done.");
    WorkflowInstance instance = new WorkflowInstance();
    instance.setState(successState);
    instance.setParentChildWorkflow(workflow);
    double pct = lifecycle.getPercentageComplete(instance);
    assertNotNull(pct);
    assertEquals(1.0, pct);
    
    
  }
  
  
  public void testStages() {
    assertNotNull(this.lifecycle.getDefaultLifecycle());
    assertNotNull(this.lifecycle.getDefaultLifecycle().getStages());
    assertEquals(this.lifecycle.getDefaultLifecycle().getStages().size(), 7);
  }

  public void testReadNewStateFormat() {
    assertNotNull(this.lifecycle.getDefaultLifecycle());
    assertNotNull(this.lifecycle.getDefaultLifecycle().getStages());
    boolean gotNull = false, gotLoaded = false;
    WorkflowLifecycleStage category = this.lifecycle.getDefaultLifecycle()
        .getCategoryByName("initial");
    assertNotNull(category);
    assertEquals("initial", category.getName());
    for (WorkflowState state : (List<WorkflowState>) category.getStates()) {
      if (state.getName().equals("Null")) {
        gotNull = true;
        assertEquals(state.getDescription(), "Uninitialized State");
      }

      if (state.getName().equals("Loaded")) {
        gotLoaded = true;
        assertEquals(state.getDescription(), "Loading Complete");
      }
    }

    assertTrue(gotNull && gotLoaded);
  }

  public void testReadOldStateFormat() throws InstantiationException {
    this.lifecycle = new WorkflowLifecycleManager("./src/main/resources"
        + "/examples/workflow-lifecycle.xml");
    assertNotNull(this.lifecycle);
    assertNotNull(this.lifecycle.getDefaultLifecycle());
    assertNotNull(this.lifecycle.getDefaultLifecycle().getStages());
    assertEquals(this.lifecycle.getDefaultLifecycle().getStages().size(), 5);
    assertNotNull(this.lifecycle.getDefaultLifecycle().getCategoryByName(
        "workflow_start"));
    assertNotNull(this.lifecycle.getDefaultLifecycle()
        .getCategoryByName("workflow_start").getStates());
    assertEquals(
        this.lifecycle.getDefaultLifecycle()
            .getCategoryByName("workflow_start").getStates().size(), 2);
    boolean gotRsubmit = false, gotStarted = false;

    for (WorkflowState state : this.lifecycle.getDefaultLifecycle()
        .getCategoryByName("workflow_start").getStates()) {
      if (state.getName().equals("RSUBMIT")) {
        gotRsubmit = true;
      }

      if (state.getName().equals("STARTED")) {
        gotStarted = true;
      }
    }

    assertTrue(gotRsubmit && gotStarted);
  }

  /*
   * (non-Javadoc)
   * 
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    this.lifecycle = new WorkflowLifecycleManager(
        new File("./src/main/resources/examples/wengine/wengine-lifecycle.xml")
            .getAbsolutePath());
  }

  /*
   * (non-Javadoc)
   * 
   * @see junit.framework.TestCase#tearDown()
   */
  @Override
  protected void tearDown() throws Exception {
    this.lifecycle = null;
  }

}
