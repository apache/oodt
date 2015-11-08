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
import org.apache.oodt.cas.workflow.engine.processor.TaskProcessor;
import org.apache.oodt.cas.workflow.engine.runner.EngineRunner;
import org.apache.oodt.cas.workflow.structs.ParentChildWorkflow;
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;

import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports

/**
 * 
 * Implements the TaskRunner framework. Acts as a thread that works with the
 * TaskQuerier to take the next sorted (aka ones that have been sorted with the
 * Workflow PrioritySorter) task and then leverage the Engine's Runner to
 * execute the task.
 * 
 * The TaskRunner thread first pops a task off the list using
 * {@link TaskQuerier#getNext()} and then so long as the thread's
 * {@link #runner} has open slots as returned by
 * , and  is
 * false and {@link #isRunning()} is true, then the task is handed off to the
 * runner for execution.
 * 
 * The TaskRunner thread can be paused during which time it waits
 *  seconds, wakes up to see if it's unpaused, and then goes
 * back to sleep if not, otherwise, resumes executing if it was unpaused.
 * 
 * @since Apache OODT 0.5
 * 
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 */
// TODO(bfoster): Rename... Runner is missleading.
public class TaskRunner implements Runnable {

  private boolean running;

  private final TaskQuerier taskQuerier;

  private final EngineRunner runner;

  private static final Logger LOG = Logger
      .getLogger(TaskRunner.class.getName());

  public TaskRunner(TaskQuerier taskQuerier, EngineRunner runner) {
    this.running = true;
    this.taskQuerier = taskQuerier;
    this.runner = runner;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run() {
    TaskProcessor nextTaskProcessor;

    while (running) {
      nextTaskProcessor = taskQuerier.getNext();

      try {
        if (nextTaskProcessor != null && runner.hasOpenSlots(nextTaskProcessor)) {
          runner.execute(nextTaskProcessor);
        }
      } catch (Exception e) {
        LOG.log(Level.SEVERE, e.getMessage());
        LOG.log(
            Level.SEVERE,
            "Engine failed while submitting jobs to its runner : "
                + e.getMessage(), e);
        this.flagProcessorAsFailed(nextTaskProcessor, e.getMessage());
      }
    }

  }

  /**
   * @return the running
   */
  public boolean isRunning() {
    return running;
  }

  /**
   * @param running
   *          the running to set
   */
  public void setRunning(boolean running) {
    this.running = running;
  }

  protected WorkflowTask extractTaskFromProcessor(TaskProcessor taskProcessor) {
    WorkflowInstance inst = taskProcessor.getWorkflowInstance();
    ParentChildWorkflow workflow = inst.getParentChildWorkflow();
    String taskId = inst.getCurrentTaskId();
    for (WorkflowTask task : workflow.getTasks()) {
      if (task.getTaskId().equals(taskId)) {
        return task;
      }
    }

    return null;
  }

  private void flagProcessorAsFailed(TaskProcessor nextTaskProcessor, String msg) {
    nextTaskProcessor.getWorkflowInstance().setState(nextTaskProcessor
        .getLifecycleManager()
        .getDefaultLifecycle()
        .createState("Failure", "done",
            "Failed while submitting job to Runner : " + msg));
    //TODO: persist me?

  }

}
