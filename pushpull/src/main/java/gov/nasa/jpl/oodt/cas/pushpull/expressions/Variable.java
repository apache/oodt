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
public class Variable implements ValidInput {

    private String name;

    private Object value;

    private int fillSide;

    private String fillString;

    private int precision;

    private int type;

    public final int FILL_FRONT = 0;

    public final int FILL_BACK = 1;

    public final int TYPE_STRING = 2;

    public final int TYPE_INTEGER = 3;

    public Variable(String name) {
        this(name, null);
    }

    public Variable(String name, Object value) {
        this.name = name;
        this.value = value;
        fillSide = FILL_FRONT;
        fillString = " ";
        precision = -1;
    }

    public boolean isString() {
        if (value instanceof String)
            return true;
        return false;
    }

    public boolean isInteger() {
        if (value instanceof Integer)
            return true;
        return false;
    }

    public void setValue(Object value) {
        this.value = value;
        if (value instanceof String) {
            type = TYPE_STRING;
        } else if (value instanceof Integer) {
            type = TYPE_INTEGER;
        }
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }

    public void setFillString(String fillString) {
        this.fillString = fillString;
    }

    public void setFillSide(int fillSide) {
        this.fillSide = fillSide;
    }

    public Object getValue() {
        return value;
    }

    public int getPrecision() {
        return precision;
    }

    public String getFillString() {
        return fillString;
    }

    public int getFillSide() {
        return fillSide;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        String strValue = value.toString();
        if (strValue.length() < this.precision) {
            while (strValue.length() < this.precision) {
                if (fillSide == FILL_FRONT) {
                    strValue = fillString + strValue;
                } else {
                    strValue = strValue + fillString;
                }
            }
        }
        return strValue;
    }
}
