//Copyright (c) 2007, California Institute of Technology.
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
public class RemoteConnectionException extends PushPullFrameworkException {

    private static final long serialVersionUID = -8694907817201161502L;

    public RemoteConnectionException() {
        super();
    }

    public RemoteConnectionException(String msg) {
        super(msg);
    }
}
