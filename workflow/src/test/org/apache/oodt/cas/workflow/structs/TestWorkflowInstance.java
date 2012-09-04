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

package org.apache.oodt.cas.workflow.structs;

//Junit imports
import junit.framework.TestCase;

/**
 * 
 * Test harness for WorkflowInstance methods.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class TestWorkflowInstance extends TestCase {

  /**
   * @since OODT-486
   */
  public void testSetStartEndDateTimesNull() {
    WorkflowInstance inst = new WorkflowInstance();
    inst.setCurrentTaskEndDateTimeIsoStr(null);
    inst.setCurrentTaskStartDateTimeIsoStr(null);
    inst.setStartDateTimeIsoStr(null);
    inst.setEndDateTimeIsoStr(null);

    assertNull(inst.getEndDate());
    assertNotNull(inst.getStartDate()); // only one initially set to new Date()
    assertNull(inst.getCurrentTaskStartDateTimeIsoStr());
    assertNull(inst.getCurrentTaskEndDateTimeIsoStr());
  }

}
