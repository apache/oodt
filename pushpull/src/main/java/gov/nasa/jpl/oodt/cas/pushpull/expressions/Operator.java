//Copyright (c) 2007, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.pushpull.expressions;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public class Operator implements ValidInput {

    private String value;

    public Operator(String value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public String toString() {
        return value;
    }
}
