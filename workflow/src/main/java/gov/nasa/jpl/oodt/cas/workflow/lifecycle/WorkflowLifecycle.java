//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.workflow.lifecycle;

//OODT imports
import gov.nasa.jpl.oodt.cas.workflow.structs.Workflow; //javadoc
import gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowInstance; //javadoc
import gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowStatus; //javadoc

//JDK imports
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Defines the lifecycle of a {@link Workflow}, identifying what
 * {@link WorkflowStatus}es belong to a particular phase.
 * </p>.
 */
public class WorkflowLifecycle {

    public static final String DEFAULT_LIFECYCLE = "__default__";
    
    public static final String NO_WORKFLOW_ID = "__no__workflow__id";

    private SortedSet stages;

    private String name;

    private String workflowId;
    

    /**
     * Default Constructor.
     * 
     */
    public WorkflowLifecycle() {
        this(null, null);
    }

    /**
     * Constructs a new WorkflowLifecycle with the given parameters.
     * 
     * @param name
     *            The name of the WorkflowLifecycle.
     * @param workflowId
     *            The associated identifier for the {@link Workflow}s that this
     *            WorkflowLifecycle is appropriate for.
     */
    public WorkflowLifecycle(String name, String workflowId) {
        this.name = name;
        this.workflowId = workflowId;
        this.stages = new TreeSet(new Comparator() {

            public int compare(Object o1, Object o2) {
                WorkflowLifecycleStage stage1 = (WorkflowLifecycleStage) o1;
                WorkflowLifecycleStage stage2 = (WorkflowLifecycleStage) o2;

                if (stage1.getOrder() < stage2.getOrder()) {
                    return -1;
                } else if (stage1.getOrder() == stage2.getOrder()) {
                    return 0;
                } else {
                    return 1;
                }
            }

        });

    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the stages
     */
    public SortedSet getStages() {
        return stages;
    }

    /**
     * Adds a {@link WorkflowStage} to this WorkflowLifecycle.
     * 
     * @param stage
     *            The {@link WorkflowStage} to add to this WorkflowLifecycle.
     */
    public void addStage(WorkflowLifecycleStage stage) {
        if (!stages.contains(stage)) {
            stages.add(stage);
        }
    }

    /**
     * Removes the given {@link WorkflowStage} from this WorkflowLifecycle.
     * 
     * @param stage
     *            The {@link WorkflowStage} to remove.
     * @return True on success, false on failure.
     */
    public boolean removeStage(WorkflowLifecycleStage stage) {
        return stages.remove(stage);
    }

    /**
     * Clears the {@link WorkflowStage}s in this WorkflowLifecycle.
     * 
     */
    public void clearStages() {
        stages.clear();
    }

    /**
     * @return the workflowId
     */
    public String getWorkflowId() {
        return workflowId;
    }

    /**
     * @param workflowId
     *            the workflowId to set
     */
    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    /**
     * Gets the associated {@link WorkflowLifecycleStage} for a
     * {@link WorkflowInstance} with a given status.
     * 
     * @param status
     *            The status of the {@link WorkflowInstance} to get the
     *            {@link WorkflowLifecycleStage} for.
     * @return The corresponding {@link WorkflowLifecycleStage} for the
     *         {@link WorkflowInstance} with the given status, or null if that
     *         status does not exist in any defined
     *         {@link WorkflowLifecycleStage}.
     */
    public WorkflowLifecycleStage getStageForWorkflow(String status) {
        if (this.stages != null && this.stages.size() > 0) {
            for (Iterator i = this.stages.iterator(); i.hasNext();) {
                WorkflowLifecycleStage stage = (WorkflowLifecycleStage) i
                        .next();
                if (stage.getStates().contains(status)) {
                    return stage;
                }
            }

            return null;
        } else
            return null;
    }

}
