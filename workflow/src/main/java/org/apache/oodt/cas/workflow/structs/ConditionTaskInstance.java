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

package org.apache.oodt.cas.workflow.structs;

//JDK imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.structs.exceptions.WorkflowTaskInstanceException;
import org.apache.oodt.cas.workflow.util.GenericWorkflowObjectFactory;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports

/**
 * 
 * This is a {@link WorkflowTaskInstance} that is able to run a
 * {@link WorkflowConditionInstance}, identified by the provided task config
 * property name "ConditionClassName". If the run of the
 * {@link WorkflowConditionInstance} does not return true, then a
 * {@link WorkflowTaskInstanceException} is thrown. Note, this exception is also
 * thrown if there is an error with the provided ConditionClassName, or if it's
 * not provided.
 * 
 * If the underlying {@link WorkflowConditionInstance} returns true, then this
 * task completes successfully, and does not throw any Exceptions.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class ConditionTaskInstance implements WorkflowTaskInstance {

  private static final Logger LOG = Logger
      .getLogger(ConditionTaskInstance.class.getName());

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.workflow.structs.WorkflowTaskInstance#run(org.apache
   * .oodt.cas.metadata.Metadata,
   * org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration)
   */
  @Override
  public void run(Metadata metadata, WorkflowTaskConfiguration config)
      throws WorkflowTaskInstanceException {
    String conditionClassName = config.getProperty("ConditionClassName");
    if (conditionClassName == null || (conditionClassName.equals(""))) {
      throw new WorkflowTaskInstanceException(
          "Condition class name is null or " + "unreadable: ["
              + conditionClassName + "]: unable to run ConditionTaskInstance!");
    }

    LOG.log(Level.INFO, "ConditionTaskInstance: evaluating condition: ["
        + conditionClassName + "]");
    WorkflowConditionInstance cond = GenericWorkflowObjectFactory
        .getConditionObjectFromClassName(conditionClassName);
    WorkflowConditionConfiguration condConfig = fromWorkflowTaskConfig(config);
    if (!cond.evaluate(metadata, condConfig)) {
      throw new WorkflowTaskInstanceException("Condition: ["
          + conditionClassName + "] failed!");
    }

  }

  private WorkflowConditionConfiguration fromWorkflowTaskConfig(
      WorkflowTaskConfiguration config) {
    WorkflowConditionConfiguration cfg = new WorkflowConditionConfiguration();
    for (String propName : (Set<String>) (Set<?>) config.getProperties()
        .keySet()) {
      if (!propName.equals("ConditionClassName")) {
        cfg.addConfigProperty(propName, config.getProperty(propName));
      }
    }

    return cfg;
  }

}
