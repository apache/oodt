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
public class ToManyFailedDownloadsException extends PushPullFrameworkException {

    private static final long serialVersionUID = -5603700192543496505L;

    public ToManyFailedDownloadsException() {
        super();
    }

    public ToManyFailedDownloadsException(String msg) {
        super(msg);
    }
}
