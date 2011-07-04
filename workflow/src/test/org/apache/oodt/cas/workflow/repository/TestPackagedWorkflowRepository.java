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

package org.apache.oodt.cas.workflow.repository;

//JDK imports
import java.io.File;
import java.util.Collections;
import java.util.List;

import org.apache.oodt.cas.workflow.structs.Workflow;

//Junit imports
import junit.framework.TestCase;

/**
 * 
 * Test harness for the {@link PackagedWorkflowRepository}.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class TestPackagedWorkflowRepository extends TestCase {

  private PackagedWorkflowRepository repo;

  public TestPackagedWorkflowRepository() {
  }

  public void testDetectOuterLevelWorkflows() {
    assertNotNull(this.repo);
    List<Workflow> workflows = null;
    try {
      workflows = this.repo.getWorkflows();
    } catch (Exception e) {
      fail(e.getMessage());
    }

    boolean foundGranuleMaps = false;
    for (Workflow w : workflows) {
      if (w.getId().equals("urn:npp:GranuleMaps")) {
        foundGranuleMaps = true;
      }
    }

    assertTrue(foundGranuleMaps);
  }

  public void testDetectInnerWorkflows() {
    assertNotNull(this.repo);
    List<String> events = null;

    try {
      events = this.repo.getRegisteredEvents();
    } catch (Exception e) {
      fail(e.getMessage());
    }

    boolean foundFour = false, foundThree = false;
    for (String event : events) {
      List<Workflow> workflows = null;

      try {
        workflows = this.repo.getWorkflowsForEvent(event);
      } catch (Exception e) {
        fail(e.getMessage());
      }

      assertNotNull(workflows);
      assertTrue(workflows.size() > 0);
      if (workflows.size() == 3) {
        foundThree = true;
      } else if (workflows.size() == 4) {
        foundFour = true;
      }

    }

    assertTrue(foundThree);
    assertTrue(foundFour);
  }

  /*
   * (non-Javadoc)
   * 
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    repo = new PackagedWorkflowRepository(Collections.singletonList(new File(
        "src/main/resources/examples/wengine/GranuleMaps.xml")));
  }

  /*
   * (non-Javadoc)
   * 
   * @see junit.framework.TestCase#tearDown()
   */
  @Override
  protected void tearDown() throws Exception {
    repo = null;
  }

}
