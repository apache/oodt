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
import org.apache.oodt.cas.workflow.instrepo.WorkflowInstanceRepository;
import org.apache.oodt.cas.workflow.instrepo.WorkflowInstanceRepositoryFactory;
import org.apache.oodt.cas.workflow.structs.WorkflowCondition;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskInstance;
import org.apache.oodt.cas.workflow.structs.WorkflowConditionInstance;
import org.apache.oodt.cas.workflow.structs.Workflow;

//JDK imports
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.Vector;
import java.util.Iterator;

/**
 * @author mattmann
 * @version $Revision$
 *
 * <p>Generic Workflow object construction utilities.</p>
 *
 */
public final class GenericWorkflowObjectFactory {

	/* our log stream */
	public static Logger LOG = Logger.getLogger(GenericWorkflowObjectFactory.class
			.getName());

	private GenericWorkflowObjectFactory() throws InstantiationException{
		throw new InstantiationException(
		"Don't instantiate XML Struct Factories!");
	}
	
	
	public static WorkflowInstanceRepository getWorkflowInstanceRepositoryFromClassName(
			String serviceFactory) {
		WorkflowInstanceRepositoryFactory factory = null;
		Class clazz = null;

		try {
			clazz = Class.forName(serviceFactory);
			factory = (WorkflowInstanceRepositoryFactory) clazz.newInstance();
			return factory.createInstanceRepository();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			LOG.log(Level.WARNING, "ClassNotFoundException when "
					+ "loading workflow instance repository factory class "
					+ serviceFactory + " Message: " + e.getMessage());
		} catch (InstantiationException e) {
			e.printStackTrace();
			LOG.log(Level.WARNING, "InstantiationException when "
					+ "loading workflow instance repository factory class "
					+ serviceFactory + " Message: " + e.getMessage());
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			LOG.log(Level.WARNING, "IllegalAccessException when loading "
					+ "workflow instance repository factory class "
					+ serviceFactory + " Message: " + e.getMessage());
		}

		return null;
	}
	
	/**
	 * <p>
	 * Constructs a {@link WorkflowTaskInstance} from the given implementation
	 * class name.
	 * </p>
	 * 
	 * @param className
	 *            The String name of the class (including package qualifiers)
	 *            that implements the WorkflowTaskInstance interface to
	 *            construct.
	 * @return A new {@link WorkflowTaskInstance} implementation specified by
	 *         its class name.
	 */
	public static WorkflowTaskInstance getTaskObjectFromClassName(String className) {

		if (className != null) {
			WorkflowTaskInstance taskInstance = null;

			try {
				Class workflowTaskClass = Class.forName(className);
				taskInstance = (WorkflowTaskInstance) workflowTaskClass.newInstance();

				return taskInstance;

			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				LOG.log(Level.WARNING,
						"ClassNotFound, Unable to locate task class: "
								+ className + ": cannot instantiate!");
				return null;
			} catch (InstantiationException e) {
				e.printStackTrace();
				LOG.log(Level.WARNING, "Unable to instantiate task class: "
						+ className + ": Reason: " + e.getMessage() + " !");
				return null;
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				LOG.log(Level.WARNING,
						"IllegalAccessException when instantiating task class: "
								+ className + ": cannot instantiate!");
				return null;
			}
		} else
			return null;
	}

	/**
	 * <p>
	 * Constructs a {@link WorkflowConditionInstance} from the given implementation
	 * class name.
	 * </p>
	 * 
	 * @param className
	 *            The String name of the class (including package qualifiers)
	 *            that implements the WorkflowConditionInstance interface to construct.
	 * @return A new {@link WorkflowConditionInstance} implementation specified by its class
	 *         name.
	 */
	public static WorkflowConditionInstance getConditionObjectFromClassName(
			String className) {
		if (className != null) {
			WorkflowConditionInstance conditionInstance = null;

			try {
				Class workflowConditionClass = Class.forName(className);
				conditionInstance = (WorkflowConditionInstance) workflowConditionClass
						.newInstance();
				return conditionInstance;
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				LOG.log(Level.WARNING, "Unable to locate condition class: "
						+ className + ": cannot instantiate!");
				return null;
			} catch (InstantiationException e) {
				e.printStackTrace();
				LOG.log(Level.WARNING,
						"Unable to instantiate condition class: " + className
								+ ": Reason: " + e.getMessage() + " !");
				return null;
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				LOG.log(Level.WARNING,
						"IllegalAccessException when instantiating condition class: "
								+ className + ": cannot instantiate!");
				return null;
			}
		} else
			return null;
	}
	
	/**
	 * <p>
	 * Constructs a {@link Workflow} instance from the given implementation
	 * class name.
	 * </p>
	 * 
	 * @param className
	 *            The String name of the class (including package qualifiers)
	 *            that implements the Workflow interface to construct.
	 * @return A new {@link Workflow} implementation specified by its class
	 *         name.
	 */
	public static Workflow getWorkflowObjectFromClassName(String className){
		if (className != null) {
			Workflow workflow = null;

			try {
				Class workflowClass = Class.forName(className);
				workflow = (Workflow) workflowClass
						.newInstance();
				return workflow;
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				LOG.log(Level.WARNING, "Unable to locate workflow class: "
						+ className + ": cannot instantiate!");
				return null;
			} catch (InstantiationException e) {
				e.printStackTrace();
				LOG.log(Level.WARNING,
						"Unable to instantiate workflow class: " + className
								+ ": Reason: " + e.getMessage() + " !");
				return null;
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				LOG.log(Level.WARNING,
						"IllegalAccessException when instantiating workflow class: "
								+ className + ": cannot instantiate!");
				return null;
			}
		} else
			return null;		
	}
	
	public static List copyWorkflows(List workflows){
		if(workflows != null){
			List newWorkflows = new Vector(workflows.size());
			for(Iterator i = workflows.iterator(); i.hasNext(); ){
				Workflow w = (Workflow)i.next();
				Workflow newWorkflow = copyWorkflow(w);
				newWorkflows.add(newWorkflow);
			}
			
			return newWorkflows;
		}
		else return null;
	}
	
	/**
	 * <p>Creates an exact copy of the specified {@link Workflow} <code>w</code>, 
	 * allocating new memory for the new object, and then returning it. The Workflow's
	 * {@link WorkflowTask}s and {@link WorkflowCondition}s on those tasks are also constructed
	 * anew, and copied from their original instances.</p>
	 * 
	 * @param w The Workflow object to create a copy of.
	 * @return A copy of the specified Workflow.
	 */
	public static Workflow copyWorkflow(Workflow w){
		Workflow newWorkflow = null;
		
		
		newWorkflow = getWorkflowObjectFromClassName(w.getClass().getName());
		
		
		//copy through
		newWorkflow.setName(w.getName());
		newWorkflow.setId(w.getId());
		newWorkflow.setTasks(copyTasks(w.getTasks()));
		
		return newWorkflow;
	}
	
	/**
	 * <p>Creates copies of each {@link WorkflowTask} within the specified
	 * {@link List} of WorkflowTasks specified by <code>taskList</code>. The new
	 * List of WorkflowTasks is returned.</p>
	 * 
	 * @param taskList The original List of WorkflowTasks to copy.
	 * @return A new List of WorkflowTasks, copied from the original one specified.
	 */
	public static List copyTasks(List taskList){
		if(taskList != null){
			
			List newTaskList = new Vector(taskList.size());
			
			for(Iterator i = taskList.iterator(); i.hasNext(); ){
				WorkflowTask t = (WorkflowTask)i.next();
				WorkflowTask newTask = copyTask(t);
				newTaskList.add(newTask);
			}
			
			return newTaskList;
		}
		else return null;
	}
	
	public static WorkflowTask copyTask(WorkflowTask t){
		WorkflowTask newTask = new WorkflowTask();
		newTask.setTaskConfig(t.getTaskConfig());
		newTask.setTaskId(t.getTaskId());
		newTask.setTaskName(t.getTaskName());
		newTask.setTaskInstanceClassName(t.getTaskInstanceClassName());
		newTask.setOrder(t.getOrder());
		newTask.setConditions(copyConditions(t.getConditions()));
		return newTask;
	}

	/**
	 * <p>Creates copies of each {@link WorkflowCondition} within the specified
	 * {@link List} of WorkflowConditions specified by <code>conditionList</code>. The new
	 * List of WorkflowConditions is returned.</p>
	 * 
	 * @param conditionList The original List of WorkflowConditions to copy.
	 * @return A new List of WorkflowConditions, copied from the original one specified.
	 */
	public static List copyConditions(List conditionList){
		if(conditionList != null){
			List newConditionList = new Vector(conditionList.size());
			
			for(Iterator i = conditionList.iterator(); i.hasNext(); ){
				WorkflowCondition c = (WorkflowCondition)i.next();
				WorkflowCondition newCondition = copyCondition(c);
				newConditionList.add(newCondition);
			}
			
			return newConditionList;
		}
		else return null;
	}
	
	public static WorkflowCondition copyCondition(WorkflowCondition c){
		WorkflowCondition newCondition = new WorkflowCondition();
		newCondition.setConditionName(c.getConditionName());
		newCondition.setOrder(c.getOrder());
		newCondition.setConditionInstanceClassName(c.getConditionInstanceClassName());
		return newCondition;
	}

}
