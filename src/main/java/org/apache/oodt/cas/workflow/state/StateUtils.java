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
package org.apache.oodt.cas.workflow.state;

//JDK imports
import java.util.List;
import java.util.Stack;
import java.util.Vector;

//OODT imports
import org.apache.oodt.cas.workflow.processor.TaskProcessor;
import org.apache.oodt.cas.workflow.processor.WorkflowProcessor;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * Utilities for working with WorkflowState
 * <p>
 */
public class StateUtils {
	
	private StateUtils() {}
	
	public static boolean allOfGivenState(List<WorkflowProcessor> workflowProcessors, Class<? extends WorkflowState> stateClass) {
		for (WorkflowProcessor workflowProcessor : workflowProcessors) 
			if (stateClass.isAssignableFrom(workflowProcessor.getState().getClass()))
				return false;
		return true;
	}
	
	public static boolean containsGivenState(List<WorkflowProcessor> workflowProcessors, Class<? extends WorkflowState> stateClass) {
		for (WorkflowProcessor workflowProcessor : workflowProcessors) 
			if (stateClass.isAssignableFrom(workflowProcessor.getState().getClass()))
				return true;
		return false;
	}
	
	public static boolean allOfGivenCategory(List<WorkflowProcessor> workflowProcessors, WorkflowState.Category category) {
		for (WorkflowProcessor workflowProcessor : workflowProcessors) 
			if (!workflowProcessor.getState().getCategory().equals(category))
				return false;
		return true;
	}
	
	public static boolean containsTaskInGivenCategory(WorkflowProcessor workflowProcessor, WorkflowState.Category category) {
		Stack<WorkflowProcessor> stack = new Stack<WorkflowProcessor>();
		stack.add(workflowProcessor);
		while (!stack.empty()) {
			WorkflowProcessor wp = stack.pop();
			if (wp instanceof TaskProcessor && wp.getState().getCategory().equals(category))
				return true;
			stack.addAll(wp.getSubProcessors());
		}
		return false;
	}
	
	public static boolean constainsGivenCategory(List<WorkflowProcessor> workflowProcessors, WorkflowState.Category category) {
		for (WorkflowProcessor workflowProcessor : workflowProcessors) 
			if (workflowProcessor.getState().getCategory().equals(category))
				return true;
		return false;
	}
	
	public static List<WorkflowProcessor> getWorkflowProcessorsOfGivenState(List<WorkflowProcessor> workflowProcessors, Class<? extends WorkflowState> stateClass) {
		List<WorkflowProcessor> returnProcessors = new Vector<WorkflowProcessor>();
		for (WorkflowProcessor workflowProcessor : workflowProcessors)
			if (stateClass.isAssignableFrom(workflowProcessor.getState().getClass()))
				returnProcessors.add(workflowProcessor);
		return returnProcessors;
	}
	
	public static List<WorkflowProcessor> getWorkflowProcessorsOfGivenCategory(List<WorkflowProcessor> workflowProcessors, WorkflowState.Category category) {
		List<WorkflowProcessor> returnProcessors = new Vector<WorkflowProcessor>();
		for (WorkflowProcessor workflowProcessor : workflowProcessors)
			if (workflowProcessor.getState().getCategory().equals(category))
				returnProcessors.add(workflowProcessor);
		return returnProcessors;
	}
	
	public static List<WorkflowProcessor> getWorkflowProcessorsNotOfGivenState(List<WorkflowProcessor> workflowProcessors, Class<? extends WorkflowState> stateClass) {
		List<WorkflowProcessor> returnProcessors = new Vector<WorkflowProcessor>();
		for (WorkflowProcessor workflowProcessor : workflowProcessors)
			if (!stateClass.isAssignableFrom(workflowProcessor.getState().getClass()))
				returnProcessors.add(workflowProcessor);
		return returnProcessors;
	}
	
	public static List<WorkflowProcessor> getWorkflowProcessorsNotOfGivenCategory(List<WorkflowProcessor> workflowProcessors, WorkflowState.Category category) {
		List<WorkflowProcessor> returnProcessors = new Vector<WorkflowProcessor>();
		for (WorkflowProcessor workflowProcessor : workflowProcessors)
			if (!workflowProcessor.getState().getCategory().equals(category))
				returnProcessors.add(workflowProcessor);
		return returnProcessors;
	}
	
	public static WorkflowState.Category getCategoryByName(List<WorkflowState> states, String name) {
		for (WorkflowState state : states) {
			if (state.getCategory().toString().toLowerCase().equals(name.toLowerCase()))
				return state.getCategory();
		}
		return null;
	}
	
	public static WorkflowState getStateByName(List<WorkflowState> states, String name) {
		String[] splitName = name.split("[\\(\\)]{1}");
		String revertState = null;
		if (splitName.length == 2) {
			name = splitName[0];
			revertState = splitName[1];
		}
		WorkflowState state = _getStateByName(states, name);
		if (state != null && state instanceof RevertableWorkflowState && revertState != null) 
			((RevertableWorkflowState) state).setPrevState(_getStateByName(states, revertState));
		return state;
	}
	
	private static WorkflowState _getStateByName(List<WorkflowState> states, String name) {
		for (WorkflowState state : states) {
			if (state instanceof RevertableWorkflowState) {
				if (state.getName().toLowerCase().startsWith(name.toLowerCase()))
					return state;
			}else if (state.getName().toLowerCase().equals(name.toLowerCase())) 
				return state;
		}
		return null;
	}
	
}
