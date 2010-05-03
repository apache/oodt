//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.pushpull.exceptions;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public class ProtocolDeletionError extends ProtocolException {

    private static final long serialVersionUID = 2209601187397273414L;

    public ProtocolDeletionError() {
        super();
    }

    public ProtocolDeletionError(String msg) {
        super(msg);
    }
}
