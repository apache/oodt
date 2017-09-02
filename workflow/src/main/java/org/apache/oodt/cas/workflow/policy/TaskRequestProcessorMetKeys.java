//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package org.apache.oodt.cas.workflow.policy;


/**
 * @author mattmann
 * @version $Revision$
 *
 * <p>Met keys for unpackaging a {@link PolicyAwareWorkflowTask} out of a 
 * {@link HttpServletRequestWrapper}</p>.
 */
public interface TaskRequestProcessorMetKeys {
    
    public static final String TASK_POLICY_DIR_PATH = "taskPolicyDirPath";
    
    public static final String UPDATE_TASK_ID = "updateTaskId";
    
    public static final String TASK_NAME = "task_name";
    
    public static final String TASK_CLASS_NAME = "task_class_name";
    
    public static final String TASK_CONDITION_IDS = "selectedConditionIds";
    
    public static final String REQ_MET_FIELDS_COUNT = "reqMetFieldCnt";
    
    public static final String REQ_MET_FIELD_TAGBASE = "reqMetField";
    
    public static final String TASK_CONFIG_FIELD_COUNT = "taskConfigCnt";
    
    public static final String TASK_CONFIG_PROPNAME_BASE = "taskConfigPropName";
    
    public static final String TASK_CONFIG_PROPVAL_BASE = "taskConfigPropValue";

}
