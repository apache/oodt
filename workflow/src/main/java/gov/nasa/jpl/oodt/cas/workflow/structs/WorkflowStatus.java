//Copyright (c) 2005, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.workflow.structs;

/**
 * 
 * @author ahart
 * @version $Revision$
 *
 * <p>Default Workflow Status Strings</p>.
 */
public interface WorkflowStatus {

    /* A set of final Strings representing the status of a workflow, or Process */
    public static final String STARTED = "STARTED";

    public static final String FINISHED = "FINISHED";

    public static final String PAUSED = "PAUSED";

    public static final String ERROR = "ERROR";
    
    public static final String QUEUED = "QUEUED";
    
    public static final String CREATED = "CREATED";
    
    public static final String RESMGR_SUBMIT = "RSUBMIT";
    
    public static final String METADATA_MISSING = "METMISS";
    
}