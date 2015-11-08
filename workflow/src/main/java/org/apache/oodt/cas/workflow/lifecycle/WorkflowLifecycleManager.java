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


package org.apache.oodt.cas.workflow.lifecycle;

//JDK imports
import java.text.NumberFormat;
import java.util.List;

//OODT imports
import org.apache.oodt.cas.workflow.structs.Workflow;
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;
import org.apache.oodt.cas.workflow.structs.WorkflowStatus;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A Manager interface for the {@link WorkflowLifecycles} to
 * determine status for a {@link WorkflowInstance}.
 * </p>.
 */
public class WorkflowLifecycleManager {

    private List lifecycles;

    /**
     * Constructs a new WorkflowLifecycleManager with the
     * {@link WorkflowLifecycle}s identified in the provided file path.
     * 
     * @param lifecyclesFilePath
     */
    public WorkflowLifecycleManager(String lifecyclesFilePath)
            throws InstantiationException {
        try {
            this.lifecycles = WorkflowLifecyclesReader
                    .parseLifecyclesFile(lifecyclesFilePath);
        } catch (Exception e) {
            throw new InstantiationException(e.getMessage());
        }
    }

    /**
     * Gets the number of the current {@link WorkflowLifecycleStage} for the
     * provided {@link WorkflowInstance}.
     * 
     * @param inst
     *            The {@link WorkflowInstance} to get the current stage num for.
     * @return The int number representing the current
     *         {@link WorkflowLifecycleStage} for the provided
     *         {@link WorkflowInstance}.
     */
    public int getStageNum(WorkflowInstance inst) {
        WorkflowLifecycleStage stage = getStage(inst);
        if (stage != null) {
            return stage.getOrder();
        } else {
            return -1;
        }
    }

    /**
     * Gets the total number of {@link WorkflowLifecycleStage}s for the
     * provided {@link Workflow} model.
     * 
     * @param workflow
     *            The {@link Workflow} model to get the number of
     *            {@link WorkflowLifecycleStage}s for. Only the identifier
     *            parameter from
     * @link {@link Workflow#getId()} is used.
     * @return The total number of {@link WorkflowLifecycleStage}s for the
     *         provided {@link Workflow} model.
     */
    public int getNumStages(Workflow workflow) {
        WorkflowLifecycle lifecycle = getLifecycleForWorkflow(workflow);
        if (lifecycle != null) {
            return lifecycle.getStages().size();
        } else {
            return -1;
        }
    }

    /**
     * Gets the current {@link WorkflowLifecycleStage} for the provided
     * {@link WorkflowInstance} based on its
     * {@link WorkflowInstance#getStatus()} value.
     * 
     * @param inst
     *            The {@link WorkflowInstance} to get the current stage for.
     * @return The current {@link WorkflowLifecycleStage} for the provided
     *         {@link WorkflowInstance} based on its
     *         {@link WorkflowInstance#getStatus()} value.
     */
    public WorkflowLifecycleStage getStage(WorkflowInstance inst) {
        WorkflowLifecycle lifecycle = getLifecycleForWorkflow(inst
                .getWorkflow());
        if (lifecycle != null) {
            return lifecycle.getStageForWorkflow(inst
                    .getStatus());
        } else {
            return null;
        }
    }

    /**
     * Gets the percentage complete that this {@link WorkflowInstance} is based
     * on its {@link WorkflowLifecycle}.
     * 
     * @param inst
     *            The {@link WorkflowInstance} to get the completion percentage
     *            for.
     * @return The double value representing the completion percentage for this
     *         {@link WorkflowInstance}.
     */
    public double getPercentageComplete(WorkflowInstance inst) {
        int numStages = getNumStages(inst.getWorkflow());
        int lastCompletedStageNum = getLastCompletedStageNum(inst);

        return (double) ((lastCompletedStageNum * 1.0) / (numStages * 1.0));
    }

    /**
     * Formats a double percent number to a 2 decimal place String.
     * 
     * @param pct
     *            The double percent number to format.
     * @return A String formatted 2 decimal place String.
     */
    public static String formatPct(double pct) {
        NumberFormat formatter = NumberFormat.getInstance();
        formatter.setMaximumFractionDigits(2);
        return formatter.format(pct);
    }

    /**
     * 
     * @return The default {@link WorkflowLifecycle} managed by this
     *         WorkflowLifecycleManager.
     */
    public WorkflowLifecycle getDefaultLifecycle() {
        WorkflowLifecycle defaultLifecycle = null;

        if (this.lifecycles != null && this.lifecycles.size() > 0) {
            for (Object lifecycle1 : this.lifecycles) {
                WorkflowLifecycle lifecycle = (WorkflowLifecycle) lifecycle1;

                if (lifecycle.getName().equals(
                    WorkflowLifecycle.DEFAULT_LIFECYCLE)) {
                    defaultLifecycle = lifecycle;
                }
            }

        }

        return defaultLifecycle;
    }

    /**
     * Gets the {@link WorkflowLifecycle} associated with the provided
     * {@link Workflow} model.
     * 
     * @param workflow
     *            The {@link Workflow} to obtain the {@link WorkflowLifecycle}
     *            for.
     * @return The {@link WorkflowLifecycle} associated with the provided
     *         {@link Workflow} model.
     */
    public WorkflowLifecycle getLifecycleForWorkflow(Workflow workflow) {
        WorkflowLifecycle defaultLifecycle = null;

        if (this.lifecycles != null && this.lifecycles.size() > 0) {
            for (Object lifecycle1 : this.lifecycles) {
                WorkflowLifecycle lifecycle = (WorkflowLifecycle) lifecycle1;
                if (lifecycle.getWorkflowId().equals(workflow.getId())) {
                    return lifecycle;
                }

                if (lifecycle.getName().equals(
                    WorkflowLifecycle.DEFAULT_LIFECYCLE)) {
                    defaultLifecycle = lifecycle;
                }
            }

            return defaultLifecycle;
        } else {
            return null;
        }
    }

    /**
     * Gets the last completed stage for the given {@link WorkflowInstance}.
     * 
     * @param inst
     *            The {@link WorkflowInstance} to obtain the last completed
     *            stage number for.
     * @return The last completed stage for the given {@link WorkflowInstance}.
     */
    public int getLastCompletedStageNum(WorkflowInstance inst) {  
        int currStageNum = getStageNum(inst);
        if(inst.getState().getCategory() == null){
          WorkflowLifecycleStage category;
          if((category = getStage(inst)) != null){
            inst.getState().setCategory(category);
          }          
        }
        
        if ((inst.getStatus().equals(WorkflowStatus.FINISHED) || 
             (inst.getState().getCategory() != null && 
             inst.getState().getCategory().getName().equals("done")))
                && currStageNum == getNumStages(inst.getWorkflow())) {
            return currStageNum;
        } else {
            return currStageNum - 1;
        }
    }

}
