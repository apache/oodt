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

import java.util.List;
import java.util.Vector;

/**
 *
 * Describe your class here.
 *
 * @author mattmann
 * @version $Revision$
 *
 */
public class TaskQuerier implements Runnable {
  
  private boolean running;
  
  
  public void run() {
    /*while(running) {
      try {
        Vector<CachedWorkflowProcessor> processors = null; 
        synchronized(PrioritizedQueueBasedWorkflowEngine.this.processorQueue) {
          processors = new Vector<CachedWorkflowProcessor>(PrioritizedQueueBasedWorkflowEngine.this.processorQueue.values());
        }
        List<WorkflowProcessor> runnableProcessors = new Vector<WorkflowProcessor>();
        for (CachedWorkflowProcessor cachedWP : processors) {
          if (!allowQueuerToWork)
            break;
          if (!(cachedWP.getStub().getState().getCategory().equals(WorkflowState.Category.DONE) || cachedWP.getStub().getState().getCategory().equals(WorkflowState.Category.HOLDING))) {
            cachedWP.uncache();
            if (!PrioritizedQueueBasedWorkflowEngine.this.debugMode) {
              processorLock.lock(cachedWP.getInstanceId());
              WorkflowProcessor wp = cachedWP.getWorkflowProcessor();
              for (TaskProcessor tp : wp.getRunnableWorkflowProcessors()) {
                tp.setState(new WaitingOnResourcesState("Added to Runnable queue", new ExecutingState("")));
                runnableProcessors.add(tp.getStub());
              }
              processorLock.unlock(cachedWP.getInstanceId());
            }
            cachedWP.cache();
          }else {
            continue;
          }

          if (runnableProcessors.size() > 0) {
            synchronized (PrioritizedQueueBasedWorkflowEngine.this.runnableTasks) {
              PrioritizedQueueBasedWorkflowEngine.this.runnableTasks.addAll(runnableProcessors);
              PrioritizedQueueBasedWorkflowEngine.this.priorityManager.sort(PrioritizedQueueBasedWorkflowEngine.this.runnableTasks);
            }
          }
          runnableProcessors.clear();
          
          //take a breather
          try {
            synchronized(this) {
              this.wait(1);
            }
          }catch (Exception e){}
        }
      }catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    try {
      synchronized(this) {
        this.wait(2000);
      }
    }catch (Exception e){}
  }*/
}
  
}
