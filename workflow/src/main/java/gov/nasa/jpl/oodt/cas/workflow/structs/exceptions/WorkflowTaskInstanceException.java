//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.workflow.structs.exceptions;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>An exception thrown by {@link WorkflowTaskInstance}</p>
 */
public class WorkflowTaskInstanceException extends Exception {

    private static final long serialVersionUID = 2666346581066070156L;

    public WorkflowTaskInstanceException() {}

    public WorkflowTaskInstanceException(String msg) {
        super(msg);
    }

    public WorkflowTaskInstanceException(Throwable throwable) {
        super(throwable);
    }

    public WorkflowTaskInstanceException(String msg, Throwable throwable) {
        super(msg, throwable);
    }

    
}
