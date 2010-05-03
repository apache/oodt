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
public class ProtocolException extends PushPullFrameworkException {

    private static final long serialVersionUID = -5585263924925230711L;

    public ProtocolException() {
        super();
    }

    public ProtocolException(String msg) {
        super(msg);
    }

}
