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
import org.apache.oodt.cas.workflow.instance.TaskInstance;
import org.apache.oodt.cas.workflow.model.WorkflowGraph;
import org.apache.oodt.cas.workflow.model.WorkflowModel;
import org.apache.oodt.cas.workflow.processor.ProcessorSkeleton;
import org.apache.oodt.cas.workflow.processor.TaskProcessor;
import org.apache.oodt.cas.workflow.processor.WorkflowProcessor;
import org.apache.oodt.cas.workflow.state.WorkflowState;
import org.apache.oodt.cas.workflow.state.initial.LoadedState;
import org.apache.oodt.cas.workflow.state.running.ExecutingState;
import org.apache.oodt.cas.workflow.state.waiting.QueuedState;
import org.apache.oodt.cas.workflow.state.waiting.WaitingOnResourcesState;

//JDK imports
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;
import java.util.HashMap;
import java.util.Map.Entry;

//APACHE imports
import org.apache.commons.lang.StringUtils;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * Utilities for working with WorkflowProcessors and WorkflowModels
 * <p>
 */
public class WorkflowUtils {

	public static String toString(WorkflowGraph graph) {
		return toString(graph, "\n   ");
	}
	
	private static String toString(WorkflowGraph graph, String indent) {
		StringBuffer stringModel = new StringBuffer(toString(graph.getModel()));
		if (graph.getPreConditions() != null)
			stringModel.append(indent + "{PreCond:" + indent + "   " + toString(graph.getPreConditions(), indent + "      ") + "}");
		if (graph.getPostConditions() != null)
			stringModel.append(indent + "{PostCond:" + indent + "   " + toString(graph.getPostConditions(), indent + "      ") + "}");
		if (graph.getChildren() != null)
			for (WorkflowGraph childGraph : graph.getChildren())
				stringModel.append(indent + toString(childGraph, indent + "   "));
		return stringModel.toString();
	}
	
	public static String toString(WorkflowModel model) {
		return toString(model, "\n   ");
	}
	
	public static String toString(WorkflowModel model, String indent) {
		return "[" + (model.getId() == null ? "" : "id = '" + model.getId() + "', name = '" + model.getName() + "', ") + "execution = '" + model.getExecutionType() + "']";//, properties = " + model.getStaticMetadata().asHashtable() + "]";

	}
	
	public static String toString(WorkflowProcessor processor) {
		return toString(processor, "\n   ");
	}
	
	private static String toString(WorkflowProcessor processor, String indent) {
		StringBuffer stringModel = new StringBuffer("[" + (processor.getModelId() == null ? "" : "id = '" + processor.getModelId() + "', name = '" + processor.getModelName() + "', ") + "execution = '" + processor.getExecutionType() + "', state = '" + processor.getState().getName() + "']");//, properties = " + processor.getStaticMetadata().asHashtable() + "]");
		if (processor.getPreConditions() != null)
			stringModel.append(indent + "{PreCond:" + indent + "   " + toString(processor.getPreConditions(), indent + "      ") + "}");
		if (processor.getPostConditions() != null)
			stringModel.append(indent + "{PostCond:" + indent + "   " + toString(processor.getPostConditions(), indent + "      ") + "}");
		if (processor.getSubProcessors() != null)
			for (WorkflowProcessor subProcessor : processor.getSubProcessors())
				stringModel.append(indent + toString(subProcessor, indent + "   "));
		return stringModel.toString();
	}
	
	public static String toString(ProcessorSkeleton skeleton) {
		return toString(skeleton, "\n   ");
	}
	
	private static String toString(ProcessorSkeleton skeleton, String indent) {
		StringBuffer stringModel = new StringBuffer("[" + (skeleton.getModelId() == null ? "" : "id = '" + skeleton.getModelId() + "', name = '" + skeleton.getModelName() + "', ") + "execution = '" + skeleton.getExecutionType() + "', state = '" + skeleton.getState().getName() + "']");//, properties = " + processor.getStaticMetadata().asHashtable() + "]");
		if (skeleton.getPreConditions() != null)
			stringModel.append(indent + "{PreCond:" + indent + "   " + toString(skeleton.getPreConditions(), indent + "      ") + "}");
		if (skeleton.getPostConditions() != null)
			stringModel.append(indent + "{PostCond:" + indent + "   " + toString(skeleton.getPostConditions(), indent + "      ") + "}");
		if (skeleton.getSubProcessors() != null)
			for (ProcessorSkeleton subProcessor : skeleton.getSubProcessors())
				stringModel.append(indent + toString(subProcessor, indent + "   "));
		return stringModel.toString();
	}
	
	public static String describe(ProcessorSkeleton skeleton) {
		StringBuffer stringModel = new StringBuffer("");
		stringModel.append("Processor [id = '" + skeleton.getModelId() + "', name = '" + skeleton.getModelName() + "']\n");
		stringModel.append("   - instance = '" + skeleton.getInstanceId() + "'\n");
		stringModel.append("   - execution = '" + skeleton.getExecutionType() + "'\n");
		stringModel.append("   - timesBlocked = '" + skeleton.getTimesBlocked() + "'\n");
		stringModel.append("   - dates: \n");
		stringModel.append("        CreationDate = '" + skeleton.getProcessorInfo().getCreationDate() + "'\n");
		stringModel.append("        ReadyDate = '" + skeleton.getProcessorInfo().getReadyDate() + "'\n");
		stringModel.append("        ExecutionDate = '" + skeleton.getProcessorInfo().getExecutionDate() + "'\n");
		stringModel.append("        CompletionDate = '" + skeleton.getProcessorInfo().getCompletionDate() + "'\n");
		stringModel.append("   - state: \n");
		stringModel.append("        name = '" + skeleton.getState().getName() + "'\n");
		stringModel.append("        startTime = '" + skeleton.getState().getStartTime() + "'\n");
		stringModel.append("        message = '" + skeleton.getState().getMessage() + "'\n");
		stringModel.append("   - priority = '" + skeleton.getPriority() + "'\n");
		stringModel.append("   - execusedSubProcessors = '" + StringUtils.join(skeleton.getExcusedSubProcessorIds().iterator(), ",") + "'\n");
		stringModel.append("   - static metadata = \n");
		for (String key : skeleton.getStaticMetadata().getAllKeys())
			stringModel.append("      + " + key + " -> '" + StringUtils.join(skeleton.getStaticMetadata().getAllMetadata(key), ",") + "'\n");
		stringModel.append("   - dynamic metadata = \n");
		for (String key : skeleton.getDynamicMetadata().getAllKeys())
			stringModel.append("      + " + key + " -> '" + StringUtils.join(skeleton.getDynamicMetadata().getAllMetadata(key), ",") + "'\n");
		return stringModel.toString();
	}
	
	public static WorkflowProcessor buildProcessor(String instanceId, WorkflowGraph graph, Map<String, Class<? extends WorkflowProcessor>> modelToProcessorMap) throws Exception {
		List<WorkflowProcessor> subProcessors = new Vector<WorkflowProcessor>();
		for (WorkflowGraph childGraph : graph.getChildren())
			subProcessors.add(buildProcessor(instanceId, childGraph, modelToProcessorMap));
		WorkflowProcessor wp = buildProcessor(instanceId, graph.getModel(), graph.isCondition(), graph.getPreConditions(), graph.getPostConditions(), modelToProcessorMap);
		wp.setSubProcessors(subProcessors);
		return wp;
	}
	
	public static WorkflowProcessor buildProcessor(String instanceId, WorkflowModel model, boolean isCondition, WorkflowGraph preConditions, WorkflowGraph postConditions, Map<String, Class<? extends WorkflowProcessor>> modelToProcessorMap) throws Exception {
		WorkflowProcessor wp = modelToProcessorMap.get(model.getExecutionType()).newInstance();
		wp.setExcusedSubProcessorIds(model.getExcusedSubProcessorIds());
		wp.setInstanceId(instanceId);
		wp.setModelId(model.getId());
		wp.setModelName(model.getName());
		wp.setIsConditionProcessor(isCondition);
		wp.setExecutionType(model.getExecutionType());
		if (preConditions != null)
			wp.setPreConditions(buildProcessor(instanceId, preConditions, modelToProcessorMap));
		if (postConditions != null)
			wp.setPostConditions(buildProcessor(instanceId, postConditions, modelToProcessorMap));
		wp.setPriority(model.getPriority());
		wp.setMinReqSuccessfulSubProcessors(model.getMinReqSuccessfulSubProcessors());
		wp.setStaticMetadata(model.getStaticMetadata());
		wp.setState(new LoadedState(""));
		if (wp instanceof TaskProcessor)
			((TaskProcessor) wp).setInstanceClass((Class<? extends TaskInstance>) Class.forName(model.getInstanceClass()));
		return wp;
	}
	
	public static ProcessorSkeleton findSkeleton(ProcessorSkeleton skeleton, String modelId) {
		if (skeleton.getModelId().equals(modelId))
			return skeleton;
		ProcessorSkeleton found = findSkeleton(skeleton.getSubProcessors(), modelId);
		if (found == null) {
			if (skeleton.getPreConditions() != null)
				found = findSkeleton(skeleton.getPreConditions(), modelId);
			if (found == null && skeleton.getPostConditions() != null)
				found = findSkeleton(skeleton.getPostConditions(), modelId);
		}
		return found;
	}
	
	protected static ProcessorSkeleton findSkeleton(List<ProcessorSkeleton> skeletons, String modelId) {
		for (ProcessorSkeleton skeleton : skeletons) {
			if (skeleton.getModelId().equals(modelId)) {
				return skeleton;
			}else {
				skeleton = findSkeleton(skeleton, modelId);
				if (skeleton != null)
					return skeleton;
			}
		}
		return null;
	}
	
	public static WorkflowProcessor findProcessor(WorkflowProcessor wp, String modelId) {
		if (wp.getModelId().equals(modelId))
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
	
	protected static WorkflowProcessor findProcessor(List<WorkflowProcessor> processors, String modelId) {
		for (WorkflowProcessor processor : processors) {
			if (processor.getModelId().equals(modelId)) {
				return processor;
			}else {
				processor = findProcessor(processor, modelId);
				if (processor != null)
					return processor;
			}
		}
		return null;
	}

	public static void validateWorkflowProcessor(WorkflowProcessor wp) {
		if (wp instanceof TaskProcessor) {
			WorkflowState state = wp.getState();
			if ((state instanceof WaitingOnResourcesState && ((WaitingOnResourcesState) state).getPrevState() instanceof ExecutingState) || state instanceof ExecutingState) 
				wp.setState(new QueuedState("Marked back to queued state because of system failure"));
			else
				wp.setState(state);
		}else {
			if (wp.getPreConditions() != null)
				validateWorkflowProcessor(wp.getPreConditions());
			for (WorkflowProcessor child : wp.getSubProcessors())
				validateWorkflowProcessor(child);
			if (wp.getPostConditions() != null)
				validateWorkflowProcessor(wp.getPostConditions());
		}
	}
	
	public static List<Metadata> getDynamicMetadata(List<WorkflowProcessor> workflowProcessors) {
		List<Metadata> metadatas = new Vector<Metadata>();
		for (WorkflowProcessor workflowProcessor: workflowProcessors)
			metadatas.add(workflowProcessor.getDynamicMetadata());
		return metadatas;
	}
		
	public static Metadata mergeMetadata(Metadata m1, Metadata m2) {
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
	
	public static void markAsCondition(WorkflowGraph graph) {
		Stack<WorkflowGraph> stack = new Stack<WorkflowGraph>();
		stack.push(graph);
		while (!stack.empty()) {
			WorkflowGraph curGraph = stack.pop();
			curGraph.setIsCondition(true);
			stack.addAll(curGraph.getChildren());
		}
	}
	
	public static String getHostName() {
		String host = null;
		try {
			host = InetAddress.getLocalHost().getHostName();
		}catch (Exception e) {
			try {
				host = ExecHelper.exec(new String[] { "hostname" }).getOutput().trim();
			}catch (Exception e1){}
		}
		if (host == null) 
			return "Unknown";
		return host;
	}
}
