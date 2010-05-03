//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.resource.util;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A Ulimit returned property, mapping a name to a particular
 * value. The value may be "unlimited", indicating there is no
 * limit on the properties value. In this case, a call to
 * {@link #isUnlimited()} can be used to detect this.
 * </p>.
 */
public class UlimitProperty {

    private String name;

    private String value;

    private static final String UNLIMITED_VAL = "unlimited";

    public UlimitProperty() {
    }

    /**
     * @param name
     * @param value
     */
    public UlimitProperty(String name, String value) {
        super();
        this.name = name;
        this.value = value;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value
     *            the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    public boolean isUnlimited() {
        if (this.value.equals(UNLIMITED_VAL)) {
            return true;
        } else {
            try {
                Integer.parseInt(this.value);
                return false;
            } catch (Exception ignore) {
                // not a number, so unlimited
                return true;
            }
        }

    }

    public int getIntValue() {
        if (isUnlimited()) {
            return -1;
        } else
            return Integer.parseInt(this.value);
    }

}
