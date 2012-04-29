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

  public void testStages() {
    assertNotNull(this.lifecycle.getDefaultLifecycle());
    assertNotNull(this.lifecycle.getDefaultLifecycle().getStages());
    assertEquals(this.lifecycle.getDefaultLifecycle().getStages().size(), 5);
  }

  public void readNewStateFormat() {
    assertNotNull(this.lifecycle.getDefaultLifecycle());
    assertNotNull(this.lifecycle.getDefaultLifecycle().getStages());
    boolean gotNull = false, gotLoaded = false;
    WorkflowLifecycleStage category = this.lifecycle.getDefaultLifecycle()
        .getCategoryByName("Null");
    assertNotNull(category);
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

  public void XreadOldStateFormat() {
    assertTrue(false); // TODO: add test to verify back compat
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
