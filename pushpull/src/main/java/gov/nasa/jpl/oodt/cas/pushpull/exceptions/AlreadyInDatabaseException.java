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
public class AlreadyInDatabaseException extends PushPullFrameworkException {

    private static final long serialVersionUID = 5268322663053758675L;

    public AlreadyInDatabaseException() {
        super();
    }

    public AlreadyInDatabaseException(String msg) {
        super(msg);
    }
}
