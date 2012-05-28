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

//JDK imports
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Vector;

//OODT imports
import org.apache.oodt.cas.workflow.lifecycle.WorkflowLifecycleManager;
import org.apache.oodt.cas.workflow.structs.FILOPrioritySorter;
import org.apache.oodt.cas.workflow.structs.Priority;
import org.apache.oodt.cas.workflow.structs.Workflow;
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;

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
    assertEquals(2.0, querier.getRunnableProcessors().get(0).getPriority()
        .getValue());
    assertEquals(7.0, querier.getRunnableProcessors().get(1).getPriority()
        .getValue());
    try{
      querierThread.join();
    }
    catch(InterruptedException ignore){}

  }

  private WorkflowProcessor getProcessor(double priority, String stateName,
      String categoryName) throws InstantiationException, IllegalAccessException {
    WorkflowLifecycleManager lifecycleManager = new WorkflowLifecycleManager(
        "./src/main/resources/examples/wengine/wengine-lifecycle.xml");
    WorkflowInstance inst = new WorkflowInstance();
    Date sd = new Date();
    sd.setTime(sd.getTime() + (this.dateGen * 5000));
    this.dateGen++;
    inst.setStartDate(sd);
    inst.setId("winst-" + priority);
    Workflow workflow = new Workflow();
    workflow.setTasks(Collections.EMPTY_LIST);
    inst.setWorkflow(workflow);
    inst.setPriority(Priority.getPriority(priority));
    WorkflowProcessorBuilder builder = WorkflowProcessorBuilder.aWorkflowProcessor()
    .withLifecycleManager(lifecycleManager)
    .withPriority(priority);
    SequentialProcessor processor = (SequentialProcessor)builder.build(SequentialProcessor.class);
    processor.setWorkflowInstance(inst);
    processor.setState(lifecycleManager.getDefaultLifecycle().createState(
        stateName, categoryName, ""));
    assertNotNull(processor.getState());
    assertNotNull(processor.getState().getCategory());
    assertNotNull(processor.getState().getCategory().getName());
    List<WorkflowProcessor> runnables = new Vector<WorkflowProcessor>();
    TaskProcessor taskProcessor = (TaskProcessor)builder.build(TaskProcessor.class);
    taskProcessor.setState(lifecycleManager.getDefaultLifecycle().createState(
        "Queued", "waiting", ""));
    runnables.add(taskProcessor);
    processor.setSubProcessors(runnables);    
    return processor;
  }

  class MockProcessorQueue extends WorkflowProcessorQueue {

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.oodt.cas.workflow.engine.WorkflowProcessorQueue#getProcessors
     * ()
     */
    @Override
    public synchronized List<WorkflowProcessor> getProcessors() {
      List<WorkflowProcessor> processors = new Vector<WorkflowProcessor>();
      try {
        processors.add(getProcessor(10.0, "Success", "done"));
        processors.add(getProcessor(2.0, "Loaded", "initial"));
        processors.add(getProcessor(7.0, "Loaded", "initial"));
      } catch (Exception e) {
        e.printStackTrace();
        fail(e.getMessage());
      }

      return processors;
    }

  }

}
