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
package org.apache.oodt.cas.workflow.util;

//OODT imports
import org.apache.oodt.commons.exec.ExecHelper;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.structs.Graph;
import org.apache.oodt.cas.workflow.structs.Priority;
import org.apache.oodt.cas.workflow.structs.WorkflowCondition;
import org.apache.oodt.cas.workflow.structs.ParentChildWorkflow;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskInstance;
import org.apache.oodt.cas.workflow.engine.processor.TaskProcessor;
import org.apache.oodt.cas.workflow.engine.processor.WorkflowProcessor;
import org.apache.oodt.cas.workflow.lifecycle.WorkflowLifecycle;
import org.apache.oodt.cas.workflow.lifecycle.WorkflowLifecycleManager;
import org.apache.oodt.cas.workflow.lifecycle.WorkflowState;

//JDK imports
import java.net.InetAddress;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.HashMap;
import java.util.Map.Entry;

//APACHE imports
import org.apache.commons.lang.StringUtils;

/**
 * 
 * Utilities for working with WorkflowProcessors and WorkflowModels.
 * 
 * @author bfoster
 * @author mattmann
 * @version $Revision$
 * 
 */
public class WorkflowUtils {

  private WorkflowLifecycleManager lifecycle;

  public WorkflowUtils(WorkflowLifecycleManager lifecycle) {
    this.lifecycle = lifecycle;
  }

  public String toString(WorkflowProcessor skeleton) {
    return toString(skeleton, "\n   ");
  }

  private String toString(WorkflowProcessor skeleton, String indent) {
    StringBuffer stringModel = new StringBuffer(
        "["
            + (skeleton.getWorkflowInstance().getParentChildWorkflow().getId() == null ? ""
                : "id = '"
                    + skeleton.getWorkflowInstance().getParentChildWorkflow()
                        .getId()
                    + "', name = '"
                    + skeleton.getWorkflowInstance().getParentChildWorkflow()
                        .getName() + "', ") + "execution = '"
            + skeleton.getExecutionType() + "', state = '"
            + skeleton.getState().getName() + "']");// , properties
                                                    // =
                                                    // " + processor.getStaticMetadata().asHashtable() + "]");
    if (skeleton.getPreConditions() != null)
      stringModel.append(indent + "{PreCond:" + indent + "   "
          + toString(skeleton.getPreConditions(), indent + "      ") + "}");
    if (skeleton.getPostConditions() != null)
      stringModel.append(indent + "{PostCond:" + indent + "   "
          + toString(skeleton.getPostConditions(), indent + "      ") + "}");
    if (skeleton.getSubProcessors() != null)
      for (WorkflowProcessor subProcessor : skeleton.getSubProcessors())
        stringModel.append(indent + toString(subProcessor, indent + "   "));
    return stringModel.toString();
  }

  public String describe(WorkflowProcessor skeleton) {
    StringBuffer stringModel = new StringBuffer("");
    stringModel.append("Processor [id = '"
        + skeleton.getWorkflowInstance().getParentChildWorkflow().getId()
        + "', name = '"
        + skeleton.getWorkflowInstance().getParentChildWorkflow().getName()
        + "']\n");
    stringModel.append("   - instance = '"
        + skeleton.getWorkflowInstance().getId() + "'\n");
    stringModel.append("   - execution = '" + skeleton.getExecutionType()
        + "'\n");
    stringModel.append("   - timesBlocked = '" + skeleton.getTimesBlocked()
        + "'\n");
    stringModel.append("   - dates: \n");
    stringModel.append("        CreationDate = '"
        + skeleton.getProcessorDateTimeInfo().getCreationDate() + "'\n");
    stringModel.append("        ReadyDate = '"
        + skeleton.getProcessorDateTimeInfo().getReadyDate() + "'\n");
    stringModel.append("        ExecutionDate = '"
        + skeleton.getProcessorDateTimeInfo().getExecutionDate() + "'\n");
    stringModel.append("        CompletionDate = '"
        + skeleton.getProcessorDateTimeInfo().getCompletionDate() + "'\n");
    stringModel.append("   - state: \n");
    stringModel.append("        name = '" + skeleton.getState().getName()
        + "'\n");
    stringModel.append("        startTime = '"
        + skeleton.getState().getStartTime() + "'\n");
    stringModel.append("        message = '" + skeleton.getState().getMessage()
        + "'\n");
    stringModel.append("   - priority = '" + skeleton.getPriority() + "'\n");
    stringModel
        .append("   - execusedSubProcessors = '"
            + StringUtils.join(skeleton.getExcusedSubProcessorIds().iterator(),
                ",") + "'\n");
    stringModel.append("   - static metadata = \n");
    for (String key : skeleton.getStaticMetadata().getAllKeys())
      stringModel.append("      + "
          + key
          + " -> '"
          + StringUtils.join(skeleton.getStaticMetadata().getAllMetadata(key),
              ",") + "'\n");
    stringModel.append("   - dynamic metadata = \n");
    for (String key : skeleton.getDynamicMetadata().getAllKeys())
      stringModel.append("      + "
          + key
          + " -> '"
          + StringUtils.join(skeleton.getDynamicMetadata().getAllMetadata(key),
              ",") + "'\n");
    return stringModel.toString();
  }

  public WorkflowProcessor buildProcessor(String instanceId,
      ParentChildWorkflow workflow,
      Map<String, Class<? extends WorkflowProcessor>> modelToProcessorMap,
      boolean preCond) throws Exception {
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
      throws Exception {
    WorkflowProcessor wp = modelToProcessorMap.get(
        model.getGraph().getExecutionType()).newInstance();
    WorkflowLifecycle wLifecycle = lifecycle.getLifecycleForWorkflow(model) != null ? lifecycle
        .getLifecycleForWorkflow(model) : lifecycle.getDefaultLifecycle();
    // FIXME: I'm not sure what these excused processor Ids are. I didn't seem
    // need them in the PackagedWorkflowRepository, so not sure what they do.
    // wp.setExcusedSubProcessorIds(model.getGraph().getExcusedSubProcessorIds());
    wp.getWorkflowInstance().setId(instanceId);
    wp.setConditionProcessor(isCondition);
    wp.setExecutionType(model.getGraph().getExecutionType());
    if (model.getPreConditions() != null)
      wp.setPreConditions(buildProcessor(instanceId, model,
          modelToProcessorMap, true));
    if (model.getPostConditions() != null)
      wp.setPostConditions(buildProcessor(instanceId, model,
          modelToProcessorMap, false));
    wp.setPriority(Priority.getDefault());
    wp.setMinReqSuccessfulSubProcessors(Integer.parseInt(model.getGraph()
        .getMinReqSuccessfulSubProcessors()));
    wp.setStaticMetadata(new Metadata());
    wp.setState(wLifecycle.createState("Loaded", wLifecycle
        .getStageForWorkflow("Loaded").getName(), ""));
    if (wp instanceof TaskProcessor)
      ((TaskProcessor) wp)
          .setInstanceClass((Class<? extends WorkflowTaskInstance>) Class
              .forName(model.getGraph().getTask().getTaskInstanceClassName()));
    return wp;
  }

  public WorkflowProcessor findSkeleton(WorkflowProcessor skeleton,
      String modelId) {
    if (skeleton.getWorkflowInstance().getParentChildWorkflow().getGraph()
        .getModelId().equals(modelId))
      return skeleton;
    WorkflowProcessor found = findSkeleton(skeleton.getSubProcessors(), modelId);
    if (found == null) {
      if (skeleton.getPreConditions() != null)
        found = findSkeleton(skeleton.getPreConditions(), modelId);
      if (found == null && skeleton.getPostConditions() != null)
        found = findSkeleton(skeleton.getPostConditions(), modelId);
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
        if (skeleton != null)
          return skeleton;
      }
    }
    return null;
  }

  public WorkflowProcessor findProcessor(WorkflowProcessor wp, String modelId) {
    if (wp.getWorkflowInstance().getParentChildWorkflow().getId()
        .equals(modelId))
      return wp;
    WorkflowProcessor found = findProcessor(wp.getSubProcessors(), modelId);
    if (found == null) {
      if (wp.getPreConditions() != null)
        found = findProcessor(wp.getPreConditions(), modelId);
      if (found == null && wp.getPostConditions() != null)
        found = findProcessor(wp.getPostConditions(), modelId);
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
        if (processor != null)
          return processor;
      }
    }
    return null;
  }

  public List<WorkflowProcessor> getTasks(WorkflowProcessor skeleton) {
    List<WorkflowProcessor> options = new Vector<WorkflowProcessor>();
    options.add(skeleton);
    List<WorkflowProcessor> tasks = new Vector<WorkflowProcessor>();
    while (!options.isEmpty()) {
      WorkflowProcessor currentOption = options.remove(0);
      if (currentOption.getSubProcessors().isEmpty()) {
        tasks.add(currentOption);
      } else {
        if (currentOption.getPreConditions() != null)
          options.add(currentOption.getPreConditions());
        if (currentOption.getPostConditions() != null)
          options.add(currentOption.getPostConditions());
        for (WorkflowProcessor ps : currentOption.getSubProcessors())
          options.add(ps);
      }
    }
    return tasks;
  }

  public void validateWorkflowProcessor(WorkflowProcessor wp) {
    if (wp instanceof TaskProcessor) {
      WorkflowState state = wp.getState();
      WorkflowLifecycle lc = this.lifecycle.getLifecycleForWorkflow(wp
          .getWorkflowInstance().getWorkflow());
      if (lc == null)
        lc = this.lifecycle.getDefaultLifecycle();
      if ((state.getName().equals("WaitingOnResources") && state.getPrevState()
          .getName().equals("Executing"))
          || state.getName().equals("Executing"))
        wp.setState(lc.createState("Queued", "waiting",
            "Marked back to queued state because of system failure"));
      else
        wp.setState(state);
    } else {
      if (wp.getPreConditions() != null)
        validateWorkflowProcessor(wp.getPreConditions());
      for (WorkflowProcessor child : wp.getSubProcessors())
        validateWorkflowProcessor(child);
      if (wp.getPostConditions() != null)
        validateWorkflowProcessor(wp.getPostConditions());
    }
  }

  public List<Metadata> getDynamicMetadata(
      List<WorkflowProcessor> workflowProcessors) {
    List<Metadata> metadatas = new Vector<Metadata>();
    for (WorkflowProcessor workflowProcessor : workflowProcessors)
      metadatas.add(workflowProcessor.getDynamicMetadata());
    return metadatas;
  }

  public Metadata mergeMetadata(Metadata m1, Metadata m2) {
    HashMap<String, LinkedHashSet<String>> merge = new HashMap<String, LinkedHashSet<String>>();
    List<Metadata> metadatas = Arrays.asList(m1, m2);
    for (Metadata m : metadatas) {
      for (String key : m.getAllKeys()) {
        LinkedHashSet<String> values = merge.get(key);
        if (values == null)
          values = new LinkedHashSet<String>();
        values.addAll(m.getAllMetadata(key));
        merge.put(key, values);
      }
    }
    Metadata m = new Metadata();
    for (Entry<String, LinkedHashSet<String>> entry : merge.entrySet())
      m.addMetadata(entry.getKey(), new Vector<String>(entry.getValue()));
    return m;
  }

  public String getHostName() {
    String host = null;
    try {
      host = InetAddress.getLocalHost().getHostName();
    } catch (Exception e) {
      try {
        host = ExecHelper.exec(new String[] { "hostname" }).getOutput().trim();
      } catch (Exception e1) {
      }
    }
    if (host == null)
      return "Unknown";
    return host;
  }
}
