//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.workflow.examples;

//OODT imports
import gov.nasa.jpl.oodt.cas.metadata.Metadata;
import gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowTaskConfiguration;
import gov.nasa.jpl.oodt.cas.workflow.structs.exceptions.RepositoryException;

//JDK imports
import java.io.File;
import junit.framework.TestCase;
import java.io.IOException;

//APACHE imports
import org.apache.commons.io.FileUtils;

/**
 * @author davoodi
 * @version $Revision$
 * @since OODT-226
 * 
 * <p>
 * Unit test for running an external script as a task instance.
 * </p>.
 */
public class TestExternScriptTaskInstance extends TestCase {

    private static final String testScriptPath = new File(
            "./src/testdata/myScript.sh").getAbsolutePath();

    private ExternScriptTaskInstance myIns;

    private Metadata myMet;

    /**
     * <p>
     * Default Constructor
     * </p>
     */
    public TestExternScriptTaskInstance() {
        myIns = new ExternScriptTaskInstance();
    }

    public void testExternsalScript() throws RepositoryException {

        myMet = new Metadata();
        myMet.addMetadata("Args", "Faranak");
        myMet.addMetadata("Args", "Davoodi");
        assertNotNull(myMet);
        WorkflowTaskConfiguration myConfig = new WorkflowTaskConfiguration();
        myConfig.addConfigProperty("PathToScript", testScriptPath);
        myConfig.addConfigProperty("ShellType", "/bin/bash");
        assertNotNull(myConfig);
        myIns.run(myMet, myConfig);
        String outputFileStr = null;
        try {
            outputFileStr = FileUtils.readFileToString(new File(
                    "./src/testdata/myScript-Output.txt"), outputFileStr);
            String expectedStr = "Hi my first name is Faranak and my last name is Davoodi.";
            assertEquals(expectedStr.trim(), outputFileStr.trim());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
