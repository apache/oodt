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
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;

/**
 * 
 * Obfuscates the underlying substrate on which a {@link WorkflowTask} should
 * run. In short, executes a {@link WorkflowTask} for the Workflow Engine.
 * 
 * @author bfoster
 * @author mattmann
 * @version $Revision$
 * 
 */
public abstract class EngineRunner {

  /**
   * Executes a {@link WorkflowTask} on an execution substrate. Ideally there
   * will only ever be two of these substrates, one for local execution, and
   * another for communication with the Resource Manager.
   * 
   * @param workflowTask
   *          The model of the {@link WorkflowTask} to instantiate and execute.
   * @param dynMetadata
   *          The dynamic {@link Metadata} passed to this {@link WorkflowTask}.
   * 
   * @throws Exception
   *           If any error occurs.
   */
  public abstract void execute(WorkflowTask workflowTask, Metadata dynMetadata)
      throws Exception;

  /**
   * Shuts this runner down and frees its resources.
   * 
   * @throws Exception
   *           If any error occurs while freeing resources.
   * 
   */
  public abstract void shutdown() throws Exception;

}
