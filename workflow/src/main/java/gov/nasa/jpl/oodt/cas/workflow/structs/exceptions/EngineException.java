//Copyright (c) 2005, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.workflow.structs.exceptions;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>An exception throw by the Workflow Engine.</p>
 * 
 */
public class EngineException extends Exception {

    /* serial version UID */
    private static final long serialVersionUID = 3690762773826910000L;

    /**
     * 
     */
    public EngineException() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     */
    public EngineException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param cause
     */
    public EngineException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     * @param cause
     */
    public EngineException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

}
