//Copyright (c) 2006, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.resource.examples;

//OODT imports
import gov.nasa.jpl.oodt.cas.resource.metadata.JobMetadata;
import gov.nasa.jpl.oodt.cas.resource.structs.JobInput;
import gov.nasa.jpl.oodt.cas.resource.structs.JobInstance;
import gov.nasa.jpl.oodt.cas.resource.structs.NameValueJobInput;
import gov.nasa.jpl.oodt.cas.resource.structs.exceptions.JobInputException;

/**
 * 
 * @author woollard
 * @version $Revision$
 * 
 * <p>
 * The classic programming example: the hello world job.
 * </p>
 */
public class HelloWorldJob implements JobInstance, JobMetadata {

  /*
   * (non-Javadoc)
   * 
   * @see gov.nasa.jpl.oodt.cas.resource.structs.JobInstance#execute(gov.nasa.jpl.oodt.cas.resource.structs.JobInput)
   */
  public boolean execute(JobInput in) throws JobInputException {
    if (!(in instanceof NameValueJobInput)) {
      throw new JobInputException(
          "Only know how to handle NameValueInput: unknown input type: ["
              + in.getClass().getName() + "]");
    }

    NameValueJobInput input = (NameValueJobInput) in;

    System.out.println("Hello world! How are you "
        + input.getValue("user.name") + "!");
    return true;
  }

}