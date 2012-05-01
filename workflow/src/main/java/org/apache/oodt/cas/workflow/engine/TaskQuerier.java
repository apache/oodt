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
import java.util.List;
import java.util.Vector;

//OODT imports
import org.apache.oodt.cas.workflow.lifecycle.WorkflowLifecycle;
import org.apache.oodt.cas.workflow.structs.PrioritySorter;

/**
 * 
 * The purpose of this class is to constantly pop off tasks that are run to run
 * and made available by the {@link WorkflowProcessorQueue}, and then to set
 * their state to Executing (running Category), so they will be picked up on the
 * next WorkflowState change, and end up executing.
 * 
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 */
public class TaskQuerier implements Runnable {

  private boolean running;

  private WorkflowProcessorQueue processorQueue;

  private List<WorkflowProcessor> runnableProcessors;

  private PrioritySorter prioritizer;

  /**
   * Constructs a new TaskQuerier with the given {@link WorkflowProcessorQueue},
   * and with the associated {@link PrioritySorter} which acts as a sorter of
   * the runnable {@link WorkflowProcessor}s.
   * 
   * @param processorQueue
   *          The associated set of queued Workflow Tasks.
   * @param prioritizer
   *          The prioritizer to use to sort the ready-to-run Workflow Tasks.
   */
  public TaskQuerier(WorkflowProcessorQueue processorQueue,
      PrioritySorter prioritizer) {
    this.running = true;
    this.processorQueue = processorQueue;
    this.runnableProcessors = new Vector<WorkflowProcessor>();
    this.prioritizer = prioritizer;
  }

  /**
   * Marches through the set of processors that are currently in the Processor
   * queue, checks to see if they are NOT in the done state, or if they are
   * currently in the holding state. If either of those are true, the processor
   * is popped off the queue, and then added to the runnableProcessors list (in
   * a synchronized fashion), and then their state is set to Executing
   * (category, running).
   * 
   * Finally the runnableProcessors list is sorted according to the given
   * {@link #prioritizer}.
   */
  public void run() {
    while (running) {
      List<WorkflowProcessor> processors = processorQueue.getProcessors();
      synchronized (runnableProcessors) {
        runnableProcessors.clear();
      }
      for (WorkflowProcessor processor : processors) {
        // OK now get its lifecycle
        WorkflowLifecycle lifecycle = getLifecycleForProcessor(processor);
        if (!(processor.getState().getCategory().getName().equals("done") || processor
            .getState().getCategory().getName().equals("holding"))) {
          for (TaskProcessor tp : processor.getRunnableWorkflowProcessors()) {
            synchronized (runnableProcessors) {
              tp.setState(lifecycle.createState("Executing", "running",
                  "Added to Runnable queue"));
              runnableProcessors.add(processor);
            }
          }

          // now prioritize the runnable processors
          synchronized (runnableProcessors) {
            prioritizer.sort(runnableProcessors);
          }

        } else {
          continue;
        }
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

  /**
   * @return the runnableProcessors
   */
  public synchronized List<WorkflowProcessor> getRunnableProcessors() {
    return runnableProcessors;
  }

  private WorkflowLifecycle getLifecycleForProcessor(WorkflowProcessor processor) {
    if (processor.getWorkflowInstance() != null
        && processor.getWorkflowInstance().getParentChildWorkflow() != null) {
      return processor.getLifecycleManager().getLifecycleForWorkflow(
          processor.getWorkflowInstance().getParentChildWorkflow());
    } else
      return processor.getLifecycleManager().getDefaultLifecycle();
  }

}
