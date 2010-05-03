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
public class PushPullFrameworkException extends Exception {

    private static final long serialVersionUID = 1500358666243383567L;

    public PushPullFrameworkException() {
        super();
    }

    public PushPullFrameworkException(String msg) {
        super(PushPullFrameworkException.addCallingClassToMsg(msg));
    }

    private static String addCallingClassToMsg(String msg) {
        try {
            Throwable stack = new Throwable();
            stack.fillInStackTrace();
            String[] splitName = stack.getStackTrace()[3].getClassName().split(
                    "\\.");
            return "[" + splitName[splitName.length - 1] + "] " + msg;
        } catch (Exception e) {
            return "[Unknown] " + msg;
        }
    }
}
