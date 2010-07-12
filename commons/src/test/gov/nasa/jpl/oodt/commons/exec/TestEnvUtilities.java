//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.commons.exec;

//JDK imports
import java.io.ByteArrayInputStream;
import java.io.InputStream;

//Junit imports
import junit.framework.TestCase;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Test case for {@link EnvUtilities}
 * </p>.
 */
public class TestEnvUtilities extends TestCase {

    private static final String envVarStr = "TOMCAT_HOME=/usr/local/tomcat\nPROMPT=\\u \\p\n";

    private static final String expectedVarStr = "TOMCAT_HOME=/usr/local/tomcat\nPROMPT=\\\\u \\\\p\n";

    /**
     * @since OODT-178
     * 
     */
    public void testPreProcessInputStream() {
        ByteArrayInputStream is = new ByteArrayInputStream(envVarStr.getBytes());
        InputStream translatedIs = null;
        try {
            translatedIs = EnvUtilities.preProcessInputStream(is);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        assertNotNull(translatedIs);
        String translatedEnvStr = null;
        try {
            translatedEnvStr = EnvUtilities.slurp(translatedIs);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertNotNull(translatedEnvStr);
        assertEquals(translatedEnvStr, expectedVarStr);

    }

}
