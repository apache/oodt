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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskInstance;
import org.apache.oodt.cas.workflow.util.GenericWorkflowObjectFactory;

/**
 * Runs a local version of a {@link WorkflowTask} asynchronously.
 *
 * @author mattmann (Chris Mattmann)
 * @author bfoster (Brian Foster)
 */
public class AsynchronousLocalEngineRunner extends EngineRunner {

  private static final Logger LOG = Logger
      .getLogger(AsynchronousLocalEngineRunner.class.getName());

  public static final int DEFAULT_NUM_THREADS = 25;

  private final ExecutorService executor;
  private final Map<String, Thread> workerMap;

  public AsynchronousLocalEngineRunner() {
     this(DEFAULT_NUM_THREADS);
  }

  public AsynchronousLocalEngineRunner(int numThreads) {
    this.executor = Executors.newFixedThreadPool(DEFAULT_NUM_THREADS);
    this.workerMap = new HashMap<String, Thread>();
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * org.apache.oodt.cas.workflow.engine.EngineRunner#execute(org.apache.oodt
   * .cas.workflow.structs.WorkflowTask, org.apache.oodt.cas.metadata.Metadata)
   */
  @Override
  public void execute(final WorkflowTask workflowTask,
      final Metadata dynMetadata) throws Exception {
    Thread worker = new Thread() {

      @Override
      public void run() {
        WorkflowTaskInstance inst = GenericWorkflowObjectFactory
            .getTaskObjectFromClassName(workflowTask.getTaskInstanceClassName());
        try {
          inst.run(dynMetadata, workflowTask.getTaskConfig());
        } catch (Exception e) {
          e.printStackTrace();
          LOG.log(Level.WARNING,
              "Exception executing task: [" + workflowTask.getTaskName()
                  + "]: Message: " + e.getMessage());
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

    String id = UUID.randomUUID().toString();
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
  public void shutdown() throws Exception {
    for (Thread worker : this.workerMap.values()) {
      if (worker != null) {
        worker.interrupt();
        worker = null;
      }
    }

  }

  /* (non-Javadoc)
   * @see org.apache.oodt.cas.workflow.engine.EngineRunner#hasOpenSlots(org.apache.oodt.cas.workflow.structs.WorkflowTask)
   */
  @Override
  public boolean hasOpenSlots(WorkflowTask workflowTask) throws Exception {
    // TODO Auto-generated method stub
    return true;
  }

}
