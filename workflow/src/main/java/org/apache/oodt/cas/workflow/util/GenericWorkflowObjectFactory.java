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
import org.apache.oodt.cas.workflow.engine.WorkflowEngine;
import org.apache.oodt.cas.workflow.engine.WorkflowEngineFactory;
import org.apache.oodt.cas.workflow.engine.runner.EngineRunner;
import org.apache.oodt.cas.workflow.engine.runner.EngineRunnerFactory;
import org.apache.oodt.cas.workflow.instrepo.WorkflowInstanceRepository;
import org.apache.oodt.cas.workflow.instrepo.WorkflowInstanceRepositoryFactory;
import org.apache.oodt.cas.workflow.repository.WorkflowRepository;
import org.apache.oodt.cas.workflow.repository.WorkflowRepositoryFactory;
import org.apache.oodt.cas.workflow.structs.PrioritySorter;
import org.apache.oodt.cas.workflow.structs.WorkflowCondition;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskInstance;
import org.apache.oodt.cas.workflow.structs.WorkflowConditionInstance;
import org.apache.oodt.cas.workflow.structs.Workflow;

//JDK imports
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.Vector;

/**
 * Generic Workflow object construction utilities.
 *
 * @author mattmann (Chris Mattmann)
 * @author bfoster (Brian Foster)
 */
public final class GenericWorkflowObjectFactory {

	/* our log stream */
	public static Logger LOG = Logger.getLogger(GenericWorkflowObjectFactory.class
			.getName());

	private GenericWorkflowObjectFactory() throws InstantiationException{
		throw new InstantiationException(
		"Don't instantiate XML Struct Factories!");
	}

   public static WorkflowEngine getWorkflowEngineFromClassName(String engineFactory) {
      try {
         return ((WorkflowEngineFactory) Class.forName(engineFactory)
               .newInstance()).createWorkflowEngine();
      } catch (ClassNotFoundException e) {
         LOG.log(Level.WARNING, "ClassNotFoundException when "
               + "loading workflow engine factory class "
               + engineFactory + " Message: " + e.getMessage());
      } catch (InstantiationException e) {
         LOG.log(Level.WARNING, "InstantiationException when "
               + "loading workflow engine factory class "
               + engineFactory + " Message: " + e.getMessage());
      } catch (IllegalAccessException e) {
         LOG.log(Level.WARNING, "IllegalAccessException when loading "
               + "workflow engine factory class "
               + engineFactory + " Message: " + e.getMessage());
      }
      return null;
   }

	public static EngineRunner getEngineRunnerFromClassName(String engineFactory) {
	   try {
         return ((EngineRunnerFactory) Class.forName(engineFactory)
               .newInstance()).createEngineRunner();
      } catch (ClassNotFoundException e) {
         LOG.log(Level.WARNING, "ClassNotFoundException when "
               + "loading engine runner factory class "
               + engineFactory + " Message: " + e.getMessage());
      } catch (InstantiationException e) {
         LOG.log(Level.WARNING, "InstantiationException when "
               + "loading engine runner factory class "
               + engineFactory + " Message: " + e.getMessage());
      } catch (IllegalAccessException e) {
         LOG.log(Level.WARNING, "IllegalAccessException when loading "
               + "engine runner factory class "
               + engineFactory + " Message: " + e.getMessage());
      }
      return null;
	}

   public static WorkflowRepository getWorkflowRepositoryFromClassName(String repositoryFactory) {
      try {
         return ((WorkflowRepositoryFactory) Class.forName(repositoryFactory)
               .newInstance()).createRepository();
      } catch (ClassNotFoundException e) {
         LOG.log(Level.WARNING, "ClassNotFoundException when "
               + "loading engine runner factory class "
               + repositoryFactory + " Message: " + e.getMessage());
      } catch (InstantiationException e) {
         LOG.log(Level.WARNING, "InstantiationException when "
               + "loading engine runner factory class "
               + repositoryFactory + " Message: " + e.getMessage());
      } catch (IllegalAccessException e) {
         LOG.log(Level.WARNING, "IllegalAccessException when loading "
               + "engine runner factory class "
               + repositoryFactory + " Message: " + e.getMessage());
      }
      return null;
   }

	public static WorkflowInstanceRepository getWorkflowInstanceRepositoryFromClassName(
			String serviceFactory) {
		WorkflowInstanceRepositoryFactory factory;
		Class clazz;

		try {
			clazz = Class.forName(serviceFactory);
			factory = (WorkflowInstanceRepositoryFactory) clazz.newInstance();
			return factory.createInstanceRepository();
		} catch (ClassNotFoundException e) {
			LOG.log(Level.SEVERE, e.getMessage());
			LOG.log(Level.WARNING, "ClassNotFoundException when "
					+ "loading workflow instance repository factory class "
					+ serviceFactory + " Message: " + e.getMessage());
		} catch (InstantiationException e) {
			LOG.log(Level.SEVERE, e.getMessage());
			LOG.log(Level.WARNING, "InstantiationException when "
					+ "loading workflow instance repository factory class "
					+ serviceFactory + " Message: " + e.getMessage());
		} catch (IllegalAccessException e) {
			LOG.log(Level.SEVERE, e.getMessage());
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
			WorkflowTaskInstance taskInstance;

			try {
				Class workflowTaskClass = Class.forName(className);
				taskInstance = (WorkflowTaskInstance) workflowTaskClass.newInstance();

				return taskInstance;

			} catch (ClassNotFoundException e) {
				LOG.log(Level.SEVERE, e.getMessage());
				LOG.log(Level.WARNING,
						"ClassNotFound, Unable to locate task class: "
								+ className + ": cannot instantiate!");
				return null;
			} catch (InstantiationException e) {
				LOG.log(Level.SEVERE, e.getMessage());
				LOG.log(Level.WARNING, "Unable to instantiate task class: "
						+ className + ": Reason: " + e.getMessage() + " !");
				return null;
			} catch (IllegalAccessException e) {
				LOG.log(Level.SEVERE, e.getMessage());
				LOG.log(Level.WARNING,
						"IllegalAccessException when instantiating task class: "
								+ className + ": cannot instantiate!");
				return null;
			}
		} else {
		  return null;
		}
	}

  /**
   * <p>
   * Constructs a {@link WorkflowTaskInstance} from the given implementation
   * class name.
   * </p>
   *
   * @param className
   *            The String name of the inner class (including package qualifiers)
   *            that implements the WorkflowTaskInstance interface to
   *            construct.
   * @return A new {@link WorkflowTaskInstance} implementation specified by
   *         its class name.
   */
  public static WorkflowTaskInstance getTaskObjectFromInnerClassName(Class<?> enclosingInstance, String className) {

    if (className != null) {
      WorkflowTaskInstance taskInstance;

      try {
        Class workflowTaskClass = Class.forName(className);
        Constructor construct = workflowTaskClass.getConstructor(enclosingInstance);
        taskInstance = (WorkflowTaskInstance) construct.newInstance();

        return taskInstance;

      } catch (ClassNotFoundException e) {
        LOG.log(Level.SEVERE, e.getMessage());
        LOG.log(Level.WARNING,
            "ClassNotFound, Unable to locate task class: "
                + className + ": cannot instantiate!");
        return null;
      } catch (InstantiationException e) {
        LOG.log(Level.SEVERE, e.getMessage());
        LOG.log(Level.WARNING, "Unable to instantiate task class: "
            + className + ": Reason: " + e.getMessage() + " !");
        return null;
      } catch (IllegalAccessException e) {
        LOG.log(Level.SEVERE, e.getMessage());
        LOG.log(Level.WARNING,
            "IllegalAccessException when instantiating task class: "
                + className + ": cannot instantiate!");
        return null;
      }
      catch (NoSuchMethodException e) {
        LOG.log(Level.SEVERE, e.getMessage());
        LOG.log(Level.WARNING,
            "NoSuchMethodException when instantiating task class: "
                + className + ": cannot instantiate!");
        return null;
      }
      catch (InvocationTargetException e) {
        LOG.log(Level.SEVERE, e.getMessage());
        LOG.log(Level.WARNING,
            "InvocationTargetException when instantiating task class: "
                + className + ": cannot instantiate!");
        return null;
      }
    } else {
	  return null;
	}
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
			WorkflowConditionInstance conditionInstance;

			try {
				Class workflowConditionClass = Class.forName(className);
				conditionInstance = (WorkflowConditionInstance) workflowConditionClass
						.newInstance();
				return conditionInstance;
			} catch (ClassNotFoundException e) {
				LOG.log(Level.SEVERE, e.getMessage());
				LOG.log(Level.WARNING, "Unable to locate condition class: "
						+ className + ": cannot instantiate!");
				return null;
			} catch (InstantiationException e) {
				LOG.log(Level.SEVERE, e.getMessage());
				LOG.log(Level.WARNING,
						"Unable to instantiate condition class: " + className
								+ ": Reason: " + e.getMessage() + " !");
				return null;
			} catch (IllegalAccessException e) {
				LOG.log(Level.SEVERE, e.getMessage());
				LOG.log(Level.WARNING,
						"IllegalAccessException when instantiating condition class: "
								+ className + ": cannot instantiate!");
				return null;
			}
		} else {
		  return null;
		}
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
			Workflow workflow;

			try {
				Class workflowClass = Class.forName(className);
				workflow = (Workflow) workflowClass
						.newInstance();
				return workflow;
			} catch (ClassNotFoundException e) {
				LOG.log(Level.SEVERE, e.getMessage());
				LOG.log(Level.WARNING, "Unable to locate workflow class: "
						+ className + ": cannot instantiate!");
				return null;
			} catch (InstantiationException e) {
				LOG.log(Level.SEVERE, e.getMessage());
				LOG.log(Level.WARNING,
						"Unable to instantiate workflow class: " + className
								+ ": Reason: " + e.getMessage() + " !");
				return null;
			} catch (IllegalAccessException e) {
				LOG.log(Level.SEVERE, e.getMessage());
				LOG.log(Level.WARNING,
						"IllegalAccessException when instantiating workflow class: "
								+ className + ": cannot instantiate!");
				return null;
			}
		} else {
		  return null;
		}
	}
	
	public static PrioritySorter getPrioritySorterFromClassName(String className){
	  if(className != null){
	    try{
	      Class<PrioritySorter> sorterClass = (Class<PrioritySorter>)Class.forName(className);	      
	      return sorterClass.newInstance();
	    }
	    catch (ClassNotFoundException e) {
        LOG.log(Level.SEVERE, e.getMessage());
        LOG.log(Level.WARNING, "Unable to locate workflow prioritizer class: "
            + className + ": cannot instantiate!");
        return null;
      } catch (InstantiationException e) {
        LOG.log(Level.SEVERE, e.getMessage());
        LOG.log(Level.WARNING,
            "Unable to instantiate workflow prioritizer class: " + className
                + ": Reason: " + e.getMessage() + " !");
        return null;
      } catch (IllegalAccessException e) {
        LOG.log(Level.SEVERE, e.getMessage());
        LOG.log(Level.WARNING,
            "IllegalAccessException when instantiating workflow prioritizer class: "
                + className + ": cannot instantiate!");
        return null;
      }
	  }
	  else {
		return null;
	  }
	}

	public static List copyWorkflows(List workflows){
		if(workflows != null){
			List newWorkflows = new Vector(workflows.size());
		  for (Object workflow : workflows) {
			Workflow w = (Workflow) workflow;
			Workflow newWorkflow = copyWorkflow(w);
			newWorkflows.add(newWorkflow);
		  }

			return newWorkflows;
		}
		else {
		  return null;
		}
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
		Workflow newWorkflow;


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

		  for (Object aTaskList : taskList) {
			WorkflowTask t = (WorkflowTask) aTaskList;
			WorkflowTask newTask = copyTask(t);
			newTaskList.add(newTask);
		  }

			return newTaskList;
		}
		else {
		  return null;
		}
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

		  for (Object aConditionList : conditionList) {
			WorkflowCondition c = (WorkflowCondition) aConditionList;
			WorkflowCondition newCondition = copyCondition(c);
			newConditionList.add(newCondition);
		  }

			return newConditionList;
		}
		else {
		  return null;
		}
	}

	public static WorkflowCondition copyCondition(WorkflowCondition c){
		WorkflowCondition newCondition = new WorkflowCondition();
		newCondition.setConditionName(c.getConditionName());
		newCondition.setOrder(c.getOrder());
		newCondition.setConditionInstanceClassName(c.getConditionInstanceClassName());
		return newCondition;
	}

}
