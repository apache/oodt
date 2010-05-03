//Copyright (c) 2007, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.workflow.examples;

//OODT imports
import gov.nasa.jpl.oodt.cas.metadata.Metadata;
import gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowTaskConfiguration;
import gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowTaskInstance;

/**
 * @author mattmann
 * @version $Revision$
 * @since OODT-53
 * 
 * <p>
 * This task illustrates OODT-53 by taking a <code>num</code> {@link Metadata}
 * parameter and then incrementing it. Subsequent executions of this same
 * {@link WorkflowTaskInstance} within a {@link Workflow} should yield
 * incremented versions of the initially provided <code>num</code> parameter.
 * </p>.
 */
public class NumIncrementTask implements WorkflowTaskInstance {

  /*
   * (non-Javadoc)
   * 
   * @see gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowTaskInstance#run(gov.nasa.jpl.oodt.cas.metadata.Metadata,
   *      gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowTaskConfiguration)
   */
  public void run(Metadata metadata, WorkflowTaskConfiguration config) {
    // read the num from the metadata, and then increment it
    // and update the metadata
    if (metadata.getMetadata("num") == null
        || (metadata.getMetadata("num") != null && metadata.getMetadata("num")
            .equals(""))) {
      return;
    }

    int num = Integer.parseInt(metadata.getMetadata("num"));
    System.out.println("Num pre increment: ["+num+"]");
    num++;
    System.out.println("Num post increment: ["+num+"]");
    metadata.replaceMetadata("num", String.valueOf(num));
  }

}
