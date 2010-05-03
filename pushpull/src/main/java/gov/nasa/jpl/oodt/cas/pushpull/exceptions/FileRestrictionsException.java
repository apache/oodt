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
public class FileRestrictionsException extends PushPullFrameworkException {

    private static final long serialVersionUID = 2429807743624154441L;

    public FileRestrictionsException() {
        super();
    }

    public FileRestrictionsException(String msg) {
        super(msg);
    }
}
