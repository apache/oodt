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
import org.apache.oodt.cas.workflow.engine.processor.WorkflowProcessor;
import org.apache.oodt.cas.workflow.engine.processor.WorkflowProcessorHelper;
import org.apache.oodt.cas.workflow.engine.processor.WorkflowProcessorQueue;
import org.apache.oodt.cas.workflow.instrepo.WorkflowInstanceRepository;
import org.apache.oodt.cas.workflow.lifecycle.WorkflowLifecycle;
import org.apache.oodt.cas.workflow.lifecycle.WorkflowState;
import org.apache.oodt.cas.workflow.structs.PrioritySorter;
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;
import org.apache.oodt.cas.workflow.structs.exceptions.InstanceRepositoryException;

import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports

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

  private WorkflowInstanceRepository repo;
  
  private long waitSeconds;

  private static final Logger LOG = Logger.getLogger(TaskQuerier.class
      .getName());

  /**
   * Constructs a new TaskQuerier with the given {@link WorkflowProcessorQueue},
   * and with the associated {@link PrioritySorter} which acts as a sorter of
   * the runnable {@link WorkflowProcessor}s.
   * 
   * @param processorQueue
   *          The associated set of queued Workflow Tasks.
   * @param prioritizer
   *          The prioritizer to use to sort the ready-to-run Workflow Tasks.
   * 
   * @param repo
   *          The {@link WorkflowInstanceRepository} to save the state of
   *          WorkflowInstances.
   *          
   * @param waitSeconds The default amount of seconds to wait while dispositioning
   *        processors
   */
  public TaskQuerier(WorkflowProcessorQueue processorQueue,
      PrioritySorter prioritizer, WorkflowInstanceRepository repo, long waitSeconds) {
    this.running = true;
    this.processorQueue = processorQueue;
    this.runnableProcessors = new Vector<WorkflowProcessor>();
    this.prioritizer = prioritizer;
    this.repo = repo;
    this.waitSeconds = waitSeconds;
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
    LOG.log(Level.FINE, "TaskQuerier configured with wait seconds: ["+this.waitSeconds+"]");
    while (running) {
      List<WorkflowProcessor> processors = processorQueue.getProcessors();
      List<WorkflowProcessor> processorsToRun = new Vector<WorkflowProcessor>();

      for (WorkflowProcessor processor : processors) {
        // OK now get its lifecycle
        WorkflowProcessorHelper helper = new WorkflowProcessorHelper(
            processor.getLifecycleManager());
        WorkflowLifecycle lifecycle = helper
            .getLifecycleForProcessor(processor);

        LOG.log(Level.FINE, "TaskQuerier: dispositioning processor with id: ["
            + processor.getWorkflowInstance().getId() + "]: state: "
            + processor.getWorkflowInstance().getState());

        if (!processor.isAnyCategory("done", "holding")
            && !processor.isAnyState("Executing")
            && processor.getRunnableWorkflowProcessors().size() > 0) {
          for (TaskProcessor tp : processor.getRunnableWorkflowProcessors()) {
            WorkflowState state = lifecycle.createState("WaitingOnResources",
                "waiting", "Added to Runnable queue");
            tp.getWorkflowInstance().setState(state);
            persist(tp.getWorkflowInstance());
            LOG.log(Level.INFO, "Added processor with priority: ["
                + tp.getWorkflowInstance().getPriority() + "]");
            processorsToRun.add(tp);
          }

          if (processorsToRun.size() > 1) {
            prioritizer.sort(processorsToRun);
          }

          synchronized (runnableProcessors) {
            if (running) {
              runnableProcessors = processorsToRun;
            }
          }

        } else {
          // simply call nextState and persist it
          LOG.log(Level.FINE, "Processor for workflow instance: ["
              + processor.getWorkflowInstance().getId()
              + "] not ready to Execute or already Executing: "
              + "advancing it to next state.");
          processor.nextState();
          persist(processor.getWorkflowInstance());
        }
      }
      
      try{
        Thread.currentThread().sleep(waitSeconds*1000);
      }
      catch(InterruptedException ignore){}
      
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
  public List<WorkflowProcessor> getRunnableProcessors() {
    return runnableProcessors;
  }

  /**
   * Gets the next available {@link TaskProcessor} from the {@link List} of
   * {@link #runnableProcessors}. Removes that {@link TaskProcessor} from the
   * actual {@link #runnableProcessors} {@link List}.
   * 
   * @return The next available {@link TaskProcessor} from the {@link List} of
   *         {@link #runnableProcessors}.
   */
  public TaskProcessor getNext() {
    if (getRunnableProcessors().size() == 0) {
      return null;
    }
    return (TaskProcessor) getRunnableProcessors().remove(0);
  }

  private synchronized void persist(WorkflowInstance instance) {
    if (this.repo != null) {
      try {
        if (instance.getId() == null || (instance.getId().equals(""))) {
          // we have to persist it by adding it
          // rather than updating it
          repo.addWorkflowInstance(instance);
        } else {
          // persist by update
          repo.updateWorkflowInstance(instance);
        }
      } catch (InstanceRepositoryException e) {
        LOG.log(Level.SEVERE, e.getMessage());
        LOG.log(Level.WARNING, "Unable to update workflow instance: ["
            + instance.getId() + "] status to [" + instance.getState().getName()
            + "]. Message: " + e.getMessage());
      }
    }
  }

}
