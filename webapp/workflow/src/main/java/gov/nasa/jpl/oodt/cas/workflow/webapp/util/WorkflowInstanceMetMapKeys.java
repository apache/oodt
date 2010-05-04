//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.workflow.webapp.util;

/**
 * @author mattmann
 * @version $Revision$
 *
 * <p>Describe your class here</p>.
 */
public interface WorkflowInstanceMetMapKeys {
    
    public static final String DEFAULT_WORKFLOW_MAP = "default";

    /* this is a RESERVED keyword: DON'T give your workflows this ID! */
    public static final String DEFAULT_WORKFLOW_ID = "__default__";
    
    public static final String FIELD_TAG = "field";
    
    public static final String FIELD_TAG_NAME_ATTR = "name";
    
    public static final String WORKFLOW_TAG_NAME = "workflow";
    
    public static final String WORKFLOW_TAG_ID_ATTR = "id";

}
