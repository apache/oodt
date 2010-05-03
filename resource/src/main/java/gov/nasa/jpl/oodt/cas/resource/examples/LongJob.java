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
 * A Job that will take a variable amount of time to finish.
 * </p>
 */
public class LongJob implements JobInstance, JobMetadata {

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

        int waitTime = 0;
        waitTime = new Integer(input.getValue("wait")).intValue();

        System.out.println("LongJob running for " + waitTime + " sec");

        try {
            Thread.sleep(waitTime * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("LongJob finished.");

        return true;
    }

}
