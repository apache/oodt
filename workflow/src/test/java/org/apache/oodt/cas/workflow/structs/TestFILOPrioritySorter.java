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

//JDK imports
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Vector;

//OODT imports
import org.apache.oodt.cas.workflow.engine.processor.SequentialProcessor;
import org.apache.oodt.cas.workflow.engine.processor.WorkflowProcessor;
import org.apache.oodt.cas.workflow.lifecycle.WorkflowLifecycleManager;

//Junit imports
import junit.framework.TestCase;

/**
 * 
 * Tests the {@link FILOPrioritySorter}.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class TestFILOPrioritySorter extends TestCase {
  
  private int dateGen;
  
  public TestFILOPrioritySorter(){
    this.dateGen = 0;
  }

  public void testSort() throws InstantiationException {
    FILOPrioritySorter sorter = new FILOPrioritySorter();
    WorkflowProcessor proc = getProcessor(2.0);
    WorkflowProcessor proc2 = getProcessor(7.0);
    WorkflowProcessor proc3 = getProcessor(9.0);
    List<WorkflowProcessor> candidates = new Vector<WorkflowProcessor>();
    candidates.add(proc3);
    candidates.add(proc2);
    candidates.add(proc);    
    sorter.sort(candidates);
    
    assertNotNull(candidates);
    assertEquals(3, candidates.size());
    assertEquals(2.0, candidates.get(0).getWorkflowInstance().getPriority()
        .getValue());
    assertEquals(7.0, candidates.get(1).getWorkflowInstance().getPriority()
        .getValue());
    assertEquals(9.0, candidates.get(2).getWorkflowInstance().getPriority()
        .getValue());
  }

  private WorkflowProcessor getProcessor(double priority) throws InstantiationException {
    WorkflowLifecycleManager lifecycleManager = new WorkflowLifecycleManager("./src/main/resources/examples/wengine/wengine-lifecycle.xml");
    WorkflowInstance inst = new WorkflowInstance();
    Date sd = new Date();
    sd.setTime(sd.getTime()+(this.dateGen*5000));
    this.dateGen++;
    inst.setStartDate(sd);
    inst.setId("winst-"+priority);
    Workflow workflow = new Workflow();
    workflow.setTasks(Collections.EMPTY_LIST);
    inst.setWorkflow(workflow);
    inst.setPriority(Priority.getPriority(priority));
    return new SequentialProcessor(lifecycleManager, inst);
  }

}
