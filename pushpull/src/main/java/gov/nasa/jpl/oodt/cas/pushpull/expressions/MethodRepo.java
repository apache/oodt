//Copyright (c) 2007, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.pushpull.expressions;

//JDK imports
import java.util.HashMap;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public class MethodRepo {

    public final static HashMap<String, Method> hashMap = new HashMap<String, Method>();

    private MethodRepo() throws InstantiationException {
        throw new InstantiationException("Don't construct method repos!");
    }

}
