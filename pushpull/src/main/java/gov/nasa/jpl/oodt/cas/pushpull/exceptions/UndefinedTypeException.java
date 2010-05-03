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
public class UndefinedTypeException extends PushPullFrameworkException {

    private static final long serialVersionUID = -5217518860967878598L;

    public UndefinedTypeException() {
        super();
    }

    public UndefinedTypeException(String msg) {
        super(msg);
    }
}
