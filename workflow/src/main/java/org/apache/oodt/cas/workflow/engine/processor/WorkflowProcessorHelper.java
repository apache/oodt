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
package org.apache.oodt.cas.workflow.engine.processor;

import org.apache.commons.lang.StringUtils;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.lifecycle.WorkflowLifecycle;
import org.apache.oodt.cas.workflow.lifecycle.WorkflowLifecycleManager;
import org.apache.oodt.cas.workflow.structs.Graph;
import org.apache.oodt.cas.workflow.structs.ParentChildWorkflow;
import org.apache.oodt.cas.workflow.structs.Priority;
import org.apache.oodt.cas.workflow.structs.WorkflowCondition;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskInstance;
import org.apache.oodt.commons.exec.ExecHelper;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * 
 * Utilities for working with WorkflowProcessors and WorkflowModels.
 * 
 * @author bfoster
 * @author mattmann
 * @version $Revision$
 * 
 */
//TODO: go through me and find all the methods that aren't used and remove them
public class WorkflowProcessorHelper {

  private WorkflowLifecycleManager lifecycle;
  
  public WorkflowProcessorHelper(){
    this(null);
  }

  public WorkflowProcessorHelper(WorkflowLifecycleManager lifecycle) {
    this.lifecycle = lifecycle;
  }

  public String toString(WorkflowProcessor skeleton) {
    return toString(skeleton, "\n   ");
  }

  private String toString(WorkflowProcessor skeleton, String indent) {
    StringBuilder stringModel = new StringBuilder(
        "["
            + (skeleton.getWorkflowInstance().getParentChildWorkflow().getId() == null ? ""
                : "id = '"
                    + skeleton.getWorkflowInstance().getParentChildWorkflow()
                        .getId()
                    + "', name = '"
                    + skeleton.getWorkflowInstance().getParentChildWorkflow()
                        .getName() + "', ")
            + "execution = '"
            + skeleton.getWorkflowInstance().getParentChildWorkflow()
                .getGraph().getExecutionType() + "', state = '"
            + skeleton.getWorkflowInstance().getState().getName() + "']");
     if (skeleton.getPreConditions() != null) {
      stringModel.append(indent).append("{PreCond:").append(indent).append("   ")
                 .append(toString(skeleton.getPreConditions(), indent + "      ")).append("}");
    }
    if (skeleton.getPostConditions() != null) {
      stringModel.append(indent).append("{PostCond:").append(indent).append("   ")
                 .append(toString(skeleton.getPostConditions(), indent + "      ")).append("}");
    }
    if (skeleton.getSubProcessors() != null) {
      for (WorkflowProcessor subProcessor : skeleton.getSubProcessors()) {
        stringModel.append(indent).append(toString(subProcessor, indent + "   "));
      }
    }
    return stringModel.toString();
  }

  public String describe(WorkflowProcessor skeleton) {
    StringBuilder stringModel = new StringBuilder("");
    stringModel.append("Processor [id = '").append(skeleton.getWorkflowInstance().getParentChildWorkflow().getId())
               .append("', name = '").append(skeleton.getWorkflowInstance().getParentChildWorkflow().getName())
               .append("']\n");
    stringModel.append("   - instance = '").append(skeleton.getWorkflowInstance().getId()).append("'\n");
    stringModel.append("   - execution = '").append(skeleton.getWorkflowInstance().getParentChildWorkflow().getGraph()
                                                            .getExecutionType()).append("'\n");
    stringModel.append("   - timesBlocked = '").append(skeleton.getWorkflowInstance().getTimesBlocked()).append("'\n");
    stringModel.append("   - dates: \n");
    stringModel.append("        CreationDate = '").append(skeleton.getWorkflowInstance().getStartDate()).append("'\n");
    stringModel.append("        CompletionDate = '").append(skeleton.getWorkflowInstance().getEndDate()).append("'\n");
    stringModel.append("   - state: \n");
    stringModel.append("        name = '").append(skeleton.getWorkflowInstance().getState().getName()).append("'\n");
    stringModel.append("        startTime = '").append(skeleton.getWorkflowInstance().getState().getStartTime())
               .append("'\n");
    stringModel.append("        message = '").append(skeleton.getWorkflowInstance().getState().getMessage())
               .append("'\n");
    stringModel.append("   - priority = '").append(skeleton.getWorkflowInstance().getPriority()).append("'\n");
    stringModel.append("   - execusedSubProcessors = '")
               .append(StringUtils.join(skeleton.getExcusedSubProcessorIds().iterator(),
                   ",")).append("'\n");
    stringModel.append("   - static metadata = \n");
    for (String key : skeleton.getWorkflowInstance().getSharedContext()
        .getAllKeys()) {
      stringModel.append("      + ").append(key).append(" -> '")
                 .append(StringUtils.join(skeleton.getWorkflowInstance().getSharedContext()
                                                  .getAllMetadata(key), ",")).append("'\n");
    }
    stringModel.append("   - dynamic metadata = \n");
    for (String key : skeleton.getWorkflowInstance().getSharedContext()
        .getAllKeys()) {
      stringModel.append("      + ").append(key).append(" -> '")
                 .append(StringUtils.join(skeleton.getWorkflowInstance().getSharedContext()
                                                  .getAllMetadata(key), ",")).append("'\n");
    }
    return stringModel.toString();
  }

  public WorkflowProcessor buildProcessor(String instanceId,
      ParentChildWorkflow workflow,
      Map<String, Class<? extends WorkflowProcessor>> modelToProcessorMap,
      boolean preCond) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
    List<WorkflowProcessor> subProcessors = new Vector<WorkflowProcessor>();
    List<WorkflowCondition> conditions = preCond ? workflow.getPreConditions()
        : workflow.getPostConditions();
    for (WorkflowCondition cond : conditions) {
      ParentChildWorkflow condWorkflow = new ParentChildWorkflow(new Graph());
      condWorkflow.getGraph().setExecutionType("condition");
      condWorkflow.getGraph().setCond(cond);
      subProcessors.add(buildProcessor(instanceId, condWorkflow,
          modelToProcessorMap, preCond));
    }
    WorkflowProcessor wp = buildProcessor(instanceId, workflow, workflow
        .getGraph().isCondition(), modelToProcessorMap);
    wp.setSubProcessors(subProcessors);
    return wp;
  }

  public WorkflowProcessor buildProcessor(String instanceId,
      ParentChildWorkflow model, boolean isCondition,
      Map<String, Class<? extends WorkflowProcessor>> modelToProcessorMap)
      throws IllegalAccessException, InstantiationException, ClassNotFoundException {
    WorkflowProcessor wp = modelToProcessorMap.get(
        model.getGraph().getExecutionType()).newInstance();
    WorkflowLifecycle wLifecycle = getLifecycle(model);
    // FIXME: I'm not sure what these excused processor Ids are. I didn't seem
    // need them in the PackagedWorkflowRepository, so not sure what they do.
    wp.getWorkflowInstance().setId(instanceId);
    if (model.getPreConditions() != null) {
      wp.setPreConditions(buildProcessor(instanceId, model,
          modelToProcessorMap, true));
    }
    if (model.getPostConditions() != null) {
      wp.setPostConditions(buildProcessor(instanceId, model,
          modelToProcessorMap, false));
    }
    wp.getWorkflowInstance().setPriority(Priority.getDefault());
    wp.setMinReqSuccessfulSubProcessors(Integer.parseInt(model.getGraph()
        .getMinReqSuccessfulSubProcessors()));
    wp.getWorkflowInstance().setSharedContext(new Metadata());
    wp.getWorkflowInstance().setState(
        wLifecycle.createState("Loaded",
            wLifecycle.getStageForWorkflow("Loaded").getName(), ""));
    if (wp instanceof TaskProcessor) {
      ((TaskProcessor) wp)
          .setInstanceClass((Class<? extends WorkflowTaskInstance>) Class
              .forName(model.getGraph().getTask().getTaskInstanceClassName()));
    }
    return wp;
  }

  public WorkflowProcessor findSkeleton(WorkflowProcessor skeleton,
      String modelId) {
    if (skeleton.getWorkflowInstance().getParentChildWorkflow().getGraph()
        .getModelId().equals(modelId)) {
      return skeleton;
    }
    WorkflowProcessor found = findSkeleton(skeleton.getSubProcessors(), modelId);
    if (found == null) {
      if (skeleton.getPreConditions() != null) {
        found = findSkeleton(skeleton.getPreConditions(), modelId);
      }
      if (found == null && skeleton.getPostConditions() != null) {
        found = findSkeleton(skeleton.getPostConditions(), modelId);
      }
    }
    return found;
  }

  protected WorkflowProcessor findSkeleton(List<WorkflowProcessor> skeletons,
      String modelId) {
    for (WorkflowProcessor skeleton : skeletons) {
      if (skeleton.getWorkflowInstance().getParentChildWorkflow().getId()
          .equals(modelId)) {
        return skeleton;
      } else {
        skeleton = findSkeleton(skeleton, modelId);
        if (skeleton != null) {
          return skeleton;
        }
      }
    }
    return null;
  }

  public WorkflowProcessor findProcessor(WorkflowProcessor wp, String modelId) {
    if (wp.getWorkflowInstance().getParentChildWorkflow().getId()
        .equals(modelId)) {
      return wp;
    }
    WorkflowProcessor found = findProcessor(wp.getSubProcessors(), modelId);
    if (found == null) {
      if (wp.getPreConditions() != null) {
        found = findProcessor(wp.getPreConditions(), modelId);
      }
      if (found == null && wp.getPostConditions() != null) {
        found = findProcessor(wp.getPostConditions(), modelId);
      }
    }
    return found;
  }

  protected WorkflowProcessor findProcessor(List<WorkflowProcessor> processors,
      String modelId) {
    for (WorkflowProcessor processor : processors) {
      if (processor.getWorkflowInstance().getId().equals(modelId)) {
        return processor;
      } else {
        processor = findProcessor(processor, modelId);
        if (processor != null) {
          return processor;
        }
      }
    }
    return null;
  }

  /**
   * Verifies that all provided WorkflowProcessors are in a state belonging to
   * the given categoryName.
   * 
   * @param workflowProcessors
   *          The {@link List} of WorkflowProcessors to inspect.
   * @param categoryName
   *          The name of the WorkflowState's category to check against.
   * @return True if they are all in the same category, false otherwise.
   */
  public boolean allProcessorsSameCategory(
      List<WorkflowProcessor> workflowProcessors, String categoryName) {
    for (WorkflowProcessor workflowProcessor : workflowProcessors) {
      if (!workflowProcessor.getWorkflowInstance().getState().getCategory()
                            .getName().equals(categoryName)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Sub-selects all WorkflowProcessors provided by the provided state
   * identified by stateName.
   * 
   * @param workflowProcessors
   *          The {@link List} of WorkflowProcessors to subset.
   * 
   * @param stateName
   *          The name of the state to subset by.
   * @return A subset version of the provided {@link List} of
   *         WorkflowProcessors.
   */
  public List<WorkflowProcessor> getWorkflowProcessorsByState(
      List<WorkflowProcessor> workflowProcessors, String stateName) {
    List<WorkflowProcessor> returnProcessors = new Vector<WorkflowProcessor>();
    for (WorkflowProcessor workflowProcessor : workflowProcessors) {
      if (workflowProcessor.getWorkflowInstance().getState().equals(stateName)) {
        returnProcessors.add(workflowProcessor);
      }
    }
    return returnProcessors;
  }

  /**
   * Sub-selects all WorkflowProcessors provided by the provided category
   * identified by categoryName.
   * 
   * @param workflowProcessors
   *          The {@link List} of WorkflowProcessors to subset.
   * 
   * @param categoryName
   *          The name of the category to subset by.
   * @return A subset version of the provided {@link List} of
   *         WorkflowProcessors.
   */
  public List<WorkflowProcessor> getWorkflowProcessorsByCategory(
      List<WorkflowProcessor> workflowProcessors, String categoryName) {
    List<WorkflowProcessor> returnProcessors = new Vector<WorkflowProcessor>();
    for (WorkflowProcessor workflowProcessor : workflowProcessors) {
      if (workflowProcessor.getWorkflowInstance().getState().getCategory()
          .getName().equals(categoryName)) {
        returnProcessors.add(workflowProcessor);
      }
    }
    return returnProcessors;
  }

  public List<WorkflowProcessor> toTasks(WorkflowProcessor processor) {
    List<WorkflowProcessor> options = new Vector<WorkflowProcessor>();
    options.add(processor);
    List<WorkflowProcessor> tasks = new Vector<WorkflowProcessor>();
    while (!options.isEmpty()) {
      WorkflowProcessor currentOption = options.remove(0);
      if (currentOption.getSubProcessors().isEmpty()) {
        tasks.add(currentOption);
      } else {
        if (currentOption.getPreConditions() != null) {
          options.add(currentOption.getPreConditions());
        }
        if (currentOption.getPostConditions() != null) {
          options.add(currentOption.getPostConditions());
        }
        for (WorkflowProcessor ps : currentOption.getSubProcessors()) {
          options.add(ps);
        }
      }
    }
    return tasks;
  }

  public boolean containsCategory(List<WorkflowProcessor> workflowProcessors,
      String categoryName) {
    for (WorkflowProcessor workflowProcessor : workflowProcessors) {
      if (workflowProcessor.getWorkflowInstance().getState().getCategory()
                           .getName().equals(categoryName)) {
        return true;
      }
    }
    return false;
  }

  public String getHostName() {
    String host = null;
    try {
      host = InetAddress.getLocalHost().getHostName();
    } catch (Exception e) {
      try {
        host = ExecHelper.exec(new String[] { "hostname" }).getOutput().trim();
      } catch (Exception ignored) {
      }
    }
    if (host == null) {
      return "Unknown";
    }
    return host;
  }
  
  public WorkflowLifecycle getLifecycleForProcessor(WorkflowProcessor processor) {
    if (processor.getWorkflowInstance() != null
        && processor.getWorkflowInstance().getParentChildWorkflow() != null) {
      return processor.getLifecycleManager().getLifecycleForWorkflow(
          processor.getWorkflowInstance().getParentChildWorkflow());
    } else {
      return processor.getLifecycleManager().getDefaultLifecycle();
    }
  }

  private WorkflowLifecycle getLifecycle(ParentChildWorkflow model) {
    return lifecycle.getLifecycleForWorkflow(model) != null ? lifecycle
        .getLifecycleForWorkflow(model) : lifecycle.getDefaultLifecycle();
  }
}
