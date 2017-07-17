//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package org.apache.oodt.cas.workflow.policy;

//OODT imports
import java.util.Iterator;

import org.apache.oodt.cas.workflow.structs.WorkflowCondition;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A {@link WorkflowTask} that is aware of what policy directory
 * that it comes from.
 * </p>.
 */
public class PolicyAwareWorkflowTask extends WorkflowTask {

    private String policyDirPath;

    public PolicyAwareWorkflowTask() {
        super();
        this.policyDirPath = null;
    }

    public PolicyAwareWorkflowTask(WorkflowTask task) {
        super(task.getTaskId(), task.getTaskName(), task.getTaskConfig(), task
                .getConditions(), task.getTaskInstanceClassName(), task
                .getOrder());
        this.setRequiredMetFields(task.getRequiredMetFields());
    }

    /**
     * @return the policyDirPath
     */
    public String getPolicyDirPath() {
        return policyDirPath;
    }

    /**
     * @param policyDirPath
     *            the policyDirPath to set
     */
    public void setPolicyDirPath(String policyDirPath) {
        this.policyDirPath = policyDirPath;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("task: [name=");
        buf.append(this.taskName);
        buf.append(",policyDirPath=");
        buf.append(this.policyDirPath);
        buf.append(",id=");
        buf.append(this.taskId);
        buf.append(",class=");
        buf.append(this.taskInstanceClassName);
        buf.append(",conditions=[");

        if (this.getConditions() != null && this.getConditions().size() > 0) {
            for (Iterator i = this.getConditions().iterator(); i.hasNext();) {
                WorkflowCondition cond = (WorkflowCondition) i.next();
                buf.append(cond.getConditionId());
                buf.append(",");
            }

            buf.deleteCharAt(buf.length() - 1);
        }

        buf.append("]");
        buf.append(",reqMetFields=[");

        if (this.requiredMetFields != null && this.requiredMetFields.size() > 0) {
            for (Iterator i = this.requiredMetFields.iterator(); i.hasNext();) {
                String metField = (String) i.next();
                buf.append(metField);
                buf.append(",");
            }

            buf.deleteCharAt(buf.length() - 1);
        }

        buf.append("]");
        buf.append(",taskConfig=[");

        if (this.taskConfig != null
                && this.taskConfig.getProperties().keySet() != null
                && this.taskConfig.getProperties().keySet().size() > 0) {
            for (Iterator i = this.taskConfig.getProperties().keySet()
                    .iterator(); i.hasNext();) {
                String propName = (String) i.next();
                EnvSavingConfiguration config = (EnvSavingConfiguration) this.taskConfig;
                String propVal = this.taskConfig.getProperty(propName);
                buf.append("[propName=");
                buf.append(propName);
                buf.append(",propVal=");
                buf.append(propVal);
                buf.append(",envReplace=");
                buf.append(String.valueOf(config.isReplace(propName)));
                buf.append("],");
            }

            buf.deleteCharAt(buf.length() - 1);
        }

        buf.append("]");

        return buf.toString();
    }

}
