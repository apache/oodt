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

import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.oodt.cas.workflow.engine.runner.EngineRunner;
import org.apache.oodt.cas.workflow.instrepo.WorkflowInstanceRepository;
import org.apache.oodt.cas.workflow.lifecycle.WorkflowLifecycleManager;
import org.apache.oodt.cas.workflow.repository.WorkflowRepository;
import org.apache.oodt.cas.workflow.structs.PrioritySorter;
import org.apache.oodt.cas.workflow.util.GenericWorkflowObjectFactory;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * Constructs an instance of the {@link PrioritizedQueueBasedWorkflowEngine},
 * based on its constituent instance repository, workflow task prioritizer,
 * workflow lifecycle, and engine runner.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class PrioritizedQueueBasedWorkflowEngineFactory implements
    WorkflowEngineFactory {
  private static Logger LOG = Logger.getLogger(PrioritizedQueueBasedWorkflowEngine.class.getName());
  private static final String MODEL_REPO_FACTORY_PROPERTY = "workflow.repo.factory";

  private static final String INSTANCE_REPO_FACTORY_PROPERTY = "workflow.engine.instanceRep.factory";

  private static final String PRIORITIZER_CLASS_PROPERTY = "org.apache.oodt.cas.workflow.wengine.prioritizer";

  private static final String LIFECYCLES_FILE_PATH_PROPERTY = "org.apache.oodt.cas.workflow.lifecycle.filePath";

  private static final String ENGINE_RUNNER_CLASS = "workflow.wengine.runner.factory";

  private static final String WAIT_SECS_PROPERTY = "org.apache.oodt.cas.workflow.wengine.taskquerier.waitSeconds";

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.workflow.engine.WorkflowEngineFactory#createWorkflowEngine
   * ()
   */
  @Override
  public WorkflowEngine createWorkflowEngine() {
    try {
      return new PrioritizedQueueBasedWorkflowEngine(
          getWorkflowInstanceRepository(), getPrioritizer(),
          getWorkflowLifecycle(), getEngineRunner(), getModelRepository(),
          getWaitSeconds());
    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      return null;
    }
  }

  protected WorkflowRepository getModelRepository() {
    return GenericWorkflowObjectFactory
        .getWorkflowRepositoryFromClassName(System
            .getProperty(MODEL_REPO_FACTORY_PROPERTY));
  }

  protected long getWaitSeconds() {
    return Long.getLong(WAIT_SECS_PROPERTY, 2);
  }

  protected EngineRunner getEngineRunner() {
    return GenericWorkflowObjectFactory.getEngineRunnerFromClassName(System
        .getProperty(ENGINE_RUNNER_CLASS));
  }

  protected WorkflowLifecycleManager getWorkflowLifecycle()
      throws InstantiationException {
    return new WorkflowLifecycleManager(PathUtils.replaceEnvVariables(System
        .getProperty(LIFECYCLES_FILE_PATH_PROPERTY)));
  }

  protected PrioritySorter getPrioritizer() {
    return GenericWorkflowObjectFactory.getPrioritySorterFromClassName(System
        .getProperty(PRIORITIZER_CLASS_PROPERTY));

  }

  protected WorkflowInstanceRepository getWorkflowInstanceRepository() {
    return GenericWorkflowObjectFactory
        .getWorkflowInstanceRepositoryFromClassName(System
            .getProperty(INSTANCE_REPO_FACTORY_PROPERTY));
  }

}
