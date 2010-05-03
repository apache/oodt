//Copyright (c) 2006, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.resource.monitor;

//OODT imports
import gov.nasa.jpl.oodt.cas.resource.structs.ResourceNode;
import gov.nasa.jpl.oodt.cas.resource.structs.exceptions.MonitorException;

//JDK imports
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

//JUnit imports
import junit.framework.TestCase;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Test Suite for the {@link AssignmentMonitor} service
 * </p>.
 */
public class TestAssignmentMonitor extends TestCase {

    private AssignmentMonitor assgnMon = null;

    protected void setUp() {
        List uris = new Vector();
        uris.add(new File("./src/main/resources/examples").toURI().toString());
        assgnMon = new AssignmentMonitor(uris);
    }

    public void testGetNodes() {
        List resNodes = null;

        try {
            resNodes = assgnMon.getNodes();
        } catch (MonitorException e) {
            fail(e.getMessage());
        }

        assertNotNull(resNodes);
        assertEquals(1, resNodes.size());
    }

    public void testGetNodeById() {
        ResourceNode node = null;

        try {
            node = assgnMon.getNodeById("localhost");
        } catch (MonitorException e) {
            fail(e.getMessage());
        }

        assertNotNull(node);
        assertEquals("localhost", node.getNodeId());
    }

    public void testGetNodeInfo() {
        List resNodes = null;

        try {
            resNodes = assgnMon.getNodes();
        } catch (MonitorException e) {
            fail(e.getMessage());
        }

        assertNotNull(resNodes);

        boolean hasNode1 = false;

        for (Iterator i = resNodes.iterator(); i.hasNext();) {
            ResourceNode node = (ResourceNode) i.next();
            assertNotNull(node);
            if (node.getNodeId().equals("localhost")) {
                hasNode1 = true;
                assertEquals(node.getIpAddr().toExternalForm(),
                        "http://localhost:2001");
            }
            assertEquals(node.getCapacity(), 8);
        }

        assertTrue(hasNode1);
    }

}
