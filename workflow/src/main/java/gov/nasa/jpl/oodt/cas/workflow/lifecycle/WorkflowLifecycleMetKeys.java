//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.workflow.lifecycle;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Metadata keys for reading the {@link WorkflowLicycle}s file
 * </p>.
 */
public interface WorkflowLifecycleMetKeys {

    public static final String DEFAULT_LIFECYCLE = "default";

    public static final String LIFECYCLE_TAG_NAME_ATTR = "name";

    public static final String STAGE_TAG_NAME_ATTR = "name";

    public static final String STATUS_TAG_NAME = "status";

    public static final String STAGE_ELEM_NAME = "stage";

    public static final String LIFECYCLE_TAG_NAME = "lifecycle";
}
