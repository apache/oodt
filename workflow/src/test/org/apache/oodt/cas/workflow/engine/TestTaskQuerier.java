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
import org.apache.oodt.cas.workflow.engine.processor.TaskProcessor;
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

  public TestTaskQuerier() {
    this.dateGen = 0;
  }
  
  public void testGetNext(){
    FILOPrioritySorter prioritizer = new FILOPrioritySorter();
    MockProcessorQueue processorQueue = new MockProcessorQueue();
    assertNotNull(processorQueue.getProcessors());
    assertEquals(3, processorQueue.getProcessors().size());
    TaskQuerier querier = new TaskQuerier(processorQueue, prioritizer);
    Thread querierThread = new Thread(querier);
    querierThread.start();
    while (querier.getRunnableProcessors().size() != 2) {
      assertNotNull(querier.getRunnableProcessors());
    }

    querier.setRunning(false);
    assertNotNull(querier.getRunnableProcessors());
    assertEquals(2, querier.getRunnableProcessors().size());
    TaskProcessor next = querier.getNext();
    assertNotNull(next);
    assertEquals(1, querier.getRunnableProcessors().size());
  }

  public void testGetRunnableProcessors() {
    FILOPrioritySorter prioritizer = new FILOPrioritySorter();
    MockProcessorQueue processorQueue = new MockProcessorQueue();
    assertNotNull(processorQueue.getProcessors());
    assertEquals(3, processorQueue.getProcessors().size());
    TaskQuerier querier = new TaskQuerier(processorQueue, prioritizer);
    Thread querierThread = new Thread(querier);
    querierThread.start();
    while (querier.getRunnableProcessors().size() != 2) {
      assertNotNull(querier.getRunnableProcessors());
    }

    querier.setRunning(false);
    assertNotNull(querier.getRunnableProcessors());
    assertEquals(2, querier.getRunnableProcessors().size());
    assertNotNull(querier.getRunnableProcessors().get(0));
    assertNotNull(querier.getRunnableProcessors().get(0).getPriority());
    assertEquals(2.1, querier.getRunnableProcessors().get(0).getPriority()
        .getValue()); // extra .1 since it's a task
    assertEquals(7.1, querier.getRunnableProcessors().get(1).getPriority()
        .getValue()); // extra .1 since it's a task
    try{
      querierThread.join();
    }
    catch(InterruptedException ignore){}

  }

}
