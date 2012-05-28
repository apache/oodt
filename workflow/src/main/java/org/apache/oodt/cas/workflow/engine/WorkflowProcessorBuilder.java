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

//JDK imorts
import java.util.List;

//OODT imports
import org.apache.oodt.cas.workflow.lifecycle.WorkflowLifecycleManager;
import org.apache.oodt.cas.workflow.structs.Priority;

//Google imports
import com.google.common.collect.Lists;

/**
 * 
 * Builds {@link WorkflowProcessor}s.
 * 
 * @author bfoster
 * @author mattmann
 * @version $Revision$
 * 
 */
public class WorkflowProcessorBuilder {

  private String id;
  private double priority;
  private List<WorkflowProcessor> subProcessors;
  private WorkflowLifecycleManager lifecycleManager;

  private WorkflowProcessorBuilder() {
    subProcessors = Lists.newArrayList();
  }

  public static WorkflowProcessorBuilder aWorkflowProcessor() {
    return new WorkflowProcessorBuilder();
  }

  public WorkflowProcessorBuilder withId(String id) {
    this.id = id;
    return this;
  }

  public WorkflowProcessorBuilder withLifecycleManager(
      WorkflowLifecycleManager lifecycleManager) {
    this.lifecycleManager = lifecycleManager;
    return this;
  }

  public WorkflowProcessorBuilder withPriority(double priority) {
    this.priority = priority;
    return this;
  }

  public WorkflowProcessorBuilder with(WorkflowProcessorBuilder wpb,
      Class<? extends WorkflowProcessor> clazz) throws InstantiationException,
      IllegalAccessException {
    subProcessors.add(wpb.build(clazz));
    return this;
  }

  public WorkflowProcessor build(Class<? extends WorkflowProcessor> clazz)
      throws InstantiationException, IllegalAccessException {
    WorkflowProcessor wp = clazz.newInstance();
    wp.getWorkflowInstance().setId(id);
    wp.setLifecycleManager(lifecycleManager);
    wp.setPriority(Priority.getPriority(priority));
    wp.setSubProcessors(subProcessors);
    return wp;
  }
}