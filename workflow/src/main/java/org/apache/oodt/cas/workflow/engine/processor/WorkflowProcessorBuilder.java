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

package org.apache.oodt.cas.workflow.engine.processor;

//JDK imorts
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

//OODT imports
import org.apache.oodt.cas.workflow.lifecycle.WorkflowLifecycleManager;
import org.apache.oodt.cas.workflow.structs.Priority;
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;

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
  private WorkflowInstance workflowInstance;

  private WorkflowProcessorBuilder() {
    subProcessors = Lists.newArrayList();
    this.id = null;
    this.priority = -1;
    this.lifecycleManager = null;
    this.workflowInstance = null;
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

  public WorkflowProcessorBuilder withInstance(WorkflowInstance workflowInstance) {
    this.workflowInstance = workflowInstance;
    return this;
  }

  public WorkflowProcessorBuilder with(WorkflowProcessorBuilder wpb,
      Class<? extends WorkflowProcessor> clazz) throws InstantiationException,
      IllegalAccessException, IllegalArgumentException,
      InvocationTargetException, SecurityException, NoSuchMethodException {
    subProcessors.add(wpb.build(clazz));
    return this;
  }

  public WorkflowProcessor build(Class<? extends WorkflowProcessor> clazz)
      throws InstantiationException, IllegalAccessException,
      IllegalArgumentException, InvocationTargetException, SecurityException,
      NoSuchMethodException {
    Constructor<? extends WorkflowProcessor> clazzConstructor = clazz
        .getConstructor(new Class[] { WorkflowLifecycleManager.class,
            WorkflowInstance.class });
    WorkflowProcessor wp = clazzConstructor.newInstance(this.lifecycleManager,
        this.workflowInstance);
    if (this.id != null) {
      wp.getWorkflowInstance().setId(id);
    }
    if (this.priority != -1) {
      wp.getWorkflowInstance().setPriority(Priority.getPriority(priority));
    }
    if (this.subProcessors != null) {
      wp.setSubProcessors(subProcessors);
    }
    if (this.workflowInstance != null){
      wp.setWorkflowInstance(workflowInstance);
    }
    return wp;
  }
}