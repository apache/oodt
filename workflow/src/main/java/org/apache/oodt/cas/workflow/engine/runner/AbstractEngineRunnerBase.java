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
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.oodt.cas.workflow.structs.exceptions.InstanceRepositoryException;

import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports

/**
 * 
 * An abstract base class providing helper functionality to persist
 * {@link WorkflowInstance}s, to get {@link WorkflowLifecycle}s from underlying
 * {@link TaskProcessor}s, and to get {@link WorkflowTask}s from the underlying
 * {@link TaskProcessor}.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public abstract class AbstractEngineRunnerBase extends EngineRunner {

  protected WorkflowInstanceRepository instRep;

  private static final Logger LOG = Logger
      .getLogger(AbstractEngineRunnerBase.class.getName());

  /**
   * Creates a new AbsractEngineRunnerBase with the provided
   * {@link WorkflowInstanceRepository}.
   *
   */
  public AbstractEngineRunnerBase() {
    this.instRep = null;
  }

  protected WorkflowTask getTaskFromProcessor(TaskProcessor taskProcessor) {
    if (taskProcessor.getWorkflowInstance() != null
        && taskProcessor.getWorkflowInstance().getParentChildWorkflow() != null
        && taskProcessor.getWorkflowInstance().getParentChildWorkflow()
            .getGraph() != null && 
           taskProcessor.getWorkflowInstance().getParentChildWorkflow().getGraph().getTask() != null) {
      return taskProcessor.getWorkflowInstance().getParentChildWorkflow()
            .getGraph().getTask();
    } else {
      return taskProcessor.getWorkflowInstance().getParentChildWorkflow()
                          .getTasks().get(0);
    }
  }

  protected WorkflowLifecycle getLifecycle(TaskProcessor taskProcessor) {
    return taskProcessor.getLifecycleManager().getDefaultLifecycle();
  }

  protected synchronized void persist(WorkflowInstance instance) {
    if(instRep == null) {
      return;
    }
    try {
      if (instance.getId() == null || (instance.getId().equals(""))) {
        // we have to persist it by adding it
        // rather than updating it
        instRep.addWorkflowInstance(instance);
      } else {
        // persist by update
        instRep.updateWorkflowInstance(instance);
      }
    } catch (InstanceRepositoryException e) {
      LOG.log(Level.SEVERE, e.getMessage());
      LOG.log(Level.WARNING, "Unabled to persist workflow instance: ["
          + instance.getId() + "]: Message: " + e.getMessage());
    }    
  }

}
