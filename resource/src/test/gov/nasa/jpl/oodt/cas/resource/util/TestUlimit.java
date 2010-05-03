//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.resource.util;

//JDK imports
import junit.framework.TestCase;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Test case for the Ulimit monitoring API.
 * </p>.
 */
public class TestUlimit extends TestCase {

    public void testGetMaxOpenFiles() {
        String maxOpenFiles = Ulimit.getMaxOpenFiles();
        assertNotNull(maxOpenFiles);
        assertTrue(new UlimitProperty("foo", maxOpenFiles).isUnlimited()
                || !new UlimitProperty("foo", maxOpenFiles).isUnlimited());

    }

    public void testUlimitProperty() {
        String maxStackSize = Ulimit.getMaxStackSize();
        if (new UlimitProperty("foo", maxStackSize).isUnlimited()) {
            assertTrue(new UlimitProperty("foo", maxStackSize).getIntValue() == -1);
        } else {
            assertTrue(new UlimitProperty("foo", maxStackSize).getIntValue() != -1);
        }
    }

}
