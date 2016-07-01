/*
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

//OODT imports
import org.apache.oodt.cas.workflow.engine.processor.TaskProcessor;
import org.apache.oodt.cas.workflow.instrepo.WorkflowInstanceRepository;

/**
 *
 * Obfuscates the underlying substrate on which a {@link org.apache.oodt.cas.workflow.structs.WorkflowTask} should
 * run. In short, executes a {@link org.apache.oodt.cas.workflow.structs.WorkflowTask} for the Workflow Engine.
 *
 * @author bfoster
 * @author mattmann
 * @version $Revision$
 *
 */
public abstract class EngineRunner {

  /**
   * Executes a {@link TaskProcessor} on an execution substrate. Ideally there
   * will only ever be two of these substrates, one for local execution, and
   * another for communication with the Resource Manager.
   *
   * @param taskProcessor
   *          The {@link TaskProcessor} to instantiate and execute.
   *
   * @throws Exception
   *           If any error occurs.
   */
  public abstract void execute(TaskProcessor taskProcessor)
  ;

  /**
   * Shuts this runner down and frees its resources.
   *
   * @throws Exception
   *           If any error occurs while freeing resources.
   *
   */
  public abstract void shutdown();
  
  /**
   * Decides whether or not there are available slots within this runner
   * to execute the provided {@link TaskProcessor}.
   * 
   * @param taskProcessor The {@link TaskProcessor} to execute.
   * @return True if there is an open slot, false otherwise.
   * @throws Exception If any error occurs.
   */
  public abstract boolean hasOpenSlots(TaskProcessor taskProcessor);
  
  
  public abstract void setInstanceRepository(WorkflowInstanceRepository instRep);

}
