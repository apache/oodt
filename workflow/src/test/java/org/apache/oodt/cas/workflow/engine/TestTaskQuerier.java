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

package org.apache.oodt.cas.workflow.engine;

//OODT imports
import java.util.List;

import org.apache.oodt.cas.workflow.engine.processor.TaskProcessor;
import org.apache.oodt.cas.workflow.engine.processor.WorkflowProcessor;
import org.apache.oodt.cas.workflow.structs.FILOPrioritySorter;

//Junit imports
import junit.framework.TestCase;

/**
 * 
 * Test harness for the {@link TestTaskQuerier}.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class TestTaskQuerier extends TestCase {
  
  private int dateGen;
  
  private static final long WAIT_SECS = 1;

  public TestTaskQuerier() {
    this.dateGen = 0;
  }
  
  public void testGetNext(){
    FILOPrioritySorter prioritizer = new FILOPrioritySorter();
    MockProcessorQueue processorQueue = new MockProcessorQueue();
    List<WorkflowProcessor> queued;
    assertNotNull(queued = processorQueue.getProcessors());
    assertEquals(3, queued.size());
    processorQueue = new MockProcessorQueue();
    TaskQuerier querier = new TaskQuerier(processorQueue, prioritizer, null, WAIT_SECS);
    Thread querierThread = new Thread(querier);
    querierThread.start();
    List<WorkflowProcessor> runnables;
    while ((runnables = querier.getRunnableProcessors()) != null && 
        runnables.size() < 2) {
      assertNotNull(runnables);
    }

    querier.setRunning(false);
    assertNotNull(runnables);
    assertEquals(2, runnables.size());
    TaskProcessor next = querier.getNext();
    assertNotNull(next);
    assertEquals(1, querier.getRunnableProcessors().size());
  }

  public void testGetRunnableProcessors() {
    FILOPrioritySorter prioritizer = new FILOPrioritySorter();
    MockProcessorQueue processorQueue = new MockProcessorQueue();    
    List<WorkflowProcessor> queued;
    assertNotNull(queued = processorQueue.getProcessors());
    assertEquals(3, queued.size());
    processorQueue = new MockProcessorQueue();
    TaskQuerier querier = new TaskQuerier(processorQueue, prioritizer, null, WAIT_SECS);
    Thread querierThread = new Thread(querier);
    querierThread.start();
    List<WorkflowProcessor> runnables;
    while ((runnables = querier.getRunnableProcessors()) != null && 
        runnables.size() < 2) {
      assertNotNull(runnables);
    }

    querier.setRunning(false);
    assertNotNull(runnables);
    assertEquals(2, runnables.size());
    assertNotNull(runnables.get(0));
    assertNotNull(runnables.get(0).getWorkflowInstance().getPriority());
    assertEquals(2.1, runnables.get(0).getWorkflowInstance().getPriority()
        .getValue()); // extra .1 since it's a task
    assertEquals(7.1, runnables.get(1).getWorkflowInstance().getPriority()
        .getValue()); // extra .1 since it's a task
    try{
      querierThread.join();
    }
    catch(InterruptedException ignore){}

  }

}
