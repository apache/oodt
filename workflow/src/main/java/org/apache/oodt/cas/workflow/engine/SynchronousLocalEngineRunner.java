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
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT Imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskInstance;
import org.apache.oodt.cas.workflow.util.GenericWorkflowObjectFactory;

/**
 * 
 * Executes a {@link WorkflowTask} locally on the WM's machine, using
 * synchronous blocking before running the next task.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class SynchronousLocalEngineRunner extends EngineRunner {

  private static final Logger LOG = Logger
      .getLogger(SynchronousLocalEngineRunner.class.getName());

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.workflow.engine.EngineRunner#execute(org.apache.oodt
   * .cas.workflow.structs.WorkflowTask, org.apache.oodt.cas.metadata.Metadata)
   */
  @Override
  public void execute(WorkflowTask workflowTask, Metadata dynMetadata)
      throws Exception {
    WorkflowTaskInstance inst = GenericWorkflowObjectFactory
        .getTaskObjectFromClassName(workflowTask.getTaskInstanceClassName());
    try {
      LOG.log(Level.INFO, "Executing task: [" + workflowTask.getTaskName()
          + "] locally");
      inst.run(dynMetadata, workflowTask.getTaskConfig());
    } catch (Exception e) {
      LOG.log(Level.WARNING,
          "Exception executing task: [" + workflowTask.getTaskName()
              + "]: Message: " + e.getMessage());
      e.printStackTrace();
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.oodt.cas.workflow.engine.EngineRunner#shutdown()
   */
  @Override
  public void shutdown() throws Exception {
    // TODO Auto-generated method stub

  }

}
