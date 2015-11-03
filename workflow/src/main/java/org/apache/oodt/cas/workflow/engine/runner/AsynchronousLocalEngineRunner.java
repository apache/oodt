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

package org.apache.oodt.cas.workflow.engine.runner;

//JDK imports
import org.apache.oodt.cas.workflow.engine.processor.TaskProcessor;
import org.apache.oodt.cas.workflow.instrepo.WorkflowInstanceRepository;
import org.apache.oodt.cas.workflow.lifecycle.WorkflowLifecycle;
import org.apache.oodt.cas.workflow.lifecycle.WorkflowState;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskInstance;
import org.apache.oodt.cas.workflow.util.GenericWorkflowObjectFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports

/**
 * Runs a local version of a {@link TaskProcessor} asynchronously.
 * 
 * @author mattmann (Chris Mattmann)
 * @author bfoster (Brian Foster)
 */
public class AsynchronousLocalEngineRunner extends AbstractEngineRunnerBase {

  private static final Logger LOG = Logger
      .getLogger(AsynchronousLocalEngineRunner.class.getName());

  public static final int DEFAULT_NUM_THREADS = 25;

  private final ExecutorService executor;
  private final Map<String, Thread> workerMap;

  public AsynchronousLocalEngineRunner() {
    this(DEFAULT_NUM_THREADS);
  }

  public AsynchronousLocalEngineRunner(int numThreads) {
    super();
    this.executor = Executors.newFixedThreadPool(numThreads);
    this.workerMap = new ConcurrentHashMap<String, Thread>();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.workflow.engine.runner.EngineRunner#execute(org.apache
   * .oodt.cas.workflow.engine.processor.TaskProcessor)
   */
  @Override
  public void execute(final TaskProcessor taskProcessor) {
    Thread worker = new Thread() {

      @Override
      public void run() {
        WorkflowLifecycle lifecycle = getLifecycle(taskProcessor);
        WorkflowTask workflowTask = getTaskFromProcessor(taskProcessor);
        WorkflowTaskInstance inst = GenericWorkflowObjectFactory
            .getTaskObjectFromClassName(workflowTask.getTaskInstanceClassName());
        try {
          inst.run(taskProcessor.getWorkflowInstance().getSharedContext(),
              workflowTask.getTaskConfig());
          String msg = "Task: [" + workflowTask.getTaskName()
              + "] for instance id: ["
              + taskProcessor.getWorkflowInstance().getId()
              + "] completed successfully";
          LOG.log(Level.INFO, msg);
          WorkflowState state = lifecycle.createState("ExecutionComplete", "transition", msg);
          taskProcessor.getWorkflowInstance().setState(state);
          persist(taskProcessor.getWorkflowInstance());
        } catch (Exception e) {
          LOG.log(Level.SEVERE, e.getMessage());
          String msg = "Exception executing task: ["
              + workflowTask.getTaskName() + "]: Message: " + e.getMessage();
          LOG.log(Level.WARNING, msg);
          WorkflowState state = lifecycle.createState("Failure", "done", msg);
          taskProcessor.getWorkflowInstance().setState(state);
          persist(taskProcessor.getWorkflowInstance());
        }

      }

      /*
       * (non-Javadoc)
       * 
       * @see java.lang.Thread#interrupt()
       */
      @SuppressWarnings("deprecation")
      @Override
      public void interrupt() {
        super.interrupt();
        this.destroy();
      }

    };

    String id = "";
    synchronized (id) {
      id = UUID.randomUUID().toString();
      this.workerMap.put(id, worker);
      this.executor.execute(worker);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.oodt.cas.workflow.engine.EngineRunner#shutdown()
   */
  @Override
  public void shutdown() {
    for (Thread worker : this.workerMap.values()) {
      if (worker != null) {
        worker.interrupt();
      }
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.workflow.engine.runner.EngineRunner#hasOpenSlots(org
   * .apache.oodt.cas.workflow.engine.processor.TaskProcessor)
   */
  @Override
  public boolean hasOpenSlots(TaskProcessor taskProcessor) {
    // TODO Auto-generated method stub
    return true;
  }

  /* (non-Javadoc)
   * @see org.apache.oodt.cas.workflow.engine.runner.EngineRunner#setInstanceRepository(org.apache.oodt.cas.workflow.instrepo.WorkflowInstanceRepository)
   */
  @Override
  public void setInstanceRepository(WorkflowInstanceRepository instRep) {
    this.instRep = instRep;    
  }

}
