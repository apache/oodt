//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package org.apache.oodt.cas.workflow.policy;

/**
 * @author mattmann
 * @version $Revision$
 *
 * <p>Met keys for writing out the tasks.xml policy file</p>.
 */
public interface TaskPolicyMetKeys {
    
    public static final String CAS_TASKS_OUTER_ELEM = "cas:tasks";
    
    public static final String CAS_XML_NS_DESC = "xmlns:cas";
    
    public static final String CAS_NS = "http://oodt.jpl.nasa.gov/1.0/cas";
    
    public static final String TASK_ELEM = "task";
    
    public static final String TASK_ID_ATTR= "id";
    
    public static final String TASK_NAME_ATTR = "name";
    
    public static final String TASK_INST_CLASS_ATTR = "class";
    
    public static final String TASK_CONDITIONS_ELEM = "conditions";
    
    public static final String TASK_COND_ELEM = "condition";
    
    public static final String TASK_COND_ID_ATTR  = "id";
    
    public static final String TASK_REQ_MET_FIELDS_ELEM = "requiredMetFields";
    
    public static final String TASK_REQ_MET_FIELD_ELEM = "metfield";
    
    public static final String TASK_REQ_MET_FIELD_NAME_ATTR = "name";
    
    public static final String TASK_CONFIG_ELEM = "configuration";
    
    public static final String PROPERTY_ELEM = "property";
    
    public static final String PROPERTY_ELEM_NAME_ATTR = "name";
    
    public static final String PROPERTY_ELEM_VALUE_ATTR = "value";
    
    public static final String PROPERTY_ELEM_ENVREPLACE_ATTR = "envReplace";

}
