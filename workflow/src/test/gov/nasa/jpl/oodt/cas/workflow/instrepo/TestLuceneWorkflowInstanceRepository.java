//Copyright (c) 2007, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.workflow.instrepo;

//OODT imports
import gov.nasa.jpl.oodt.cas.metadata.Metadata;
import gov.nasa.jpl.oodt.cas.workflow.structs.Workflow;
import gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowCondition;
import gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowInstance;
import gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowStatus;
import gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowTask;
import gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowTaskConfiguration;
import gov.nasa.jpl.oodt.cas.workflow.structs.exceptions.InstanceRepositoryException;

//JDK imports
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.FileUtils;

//Junit imports
import junit.framework.TestCase;

/**
 * @author mattmann
 * @version $Revision$
 * @since OODT-55
 * 
 * <p>
 * Test Case for the {@link LuceneWorkflowInstaceRepository}
 * </p>.
 */
public class TestLuceneWorkflowInstanceRepository extends TestCase implements
        WorkflowStatus {

    private static final String catalogPath = new File(
            "./target/instRepoTestCat").getAbsolutePath();

    private LuceneWorkflowInstanceRepository repo = null;

    private WorkflowInstance testInst = null;

    private Workflow testWkflw;

    private WorkflowTask testTask;

    private WorkflowCondition testCond;
    
    private static final int defaultPgSz = 20;

    public TestLuceneWorkflowInstanceRepository() {
        testInst = new WorkflowInstance();
        testWkflw = new Workflow();
        testTask = new WorkflowTask();
        testCond = new WorkflowCondition();

        // check to see if catalog path exists: (it may b/c
        // the user may run many unit tests)
        // if it exists, blow it away
        if (new File(catalogPath).exists()) {
            try {
                FileUtils.deleteDirectory(new File(catalogPath));
            } catch (IOException e) {
                fail(e.getMessage());
            }
        }
        repo = new LuceneWorkflowInstanceRepository(catalogPath, defaultPgSz);
        testWkflw.setName("test.workflow");
        testWkflw.setId("test.id");
        List tasks = new Vector();
        List conds = new Vector();

        testCond.setConditionId("test.cond.id");
        testCond.setConditionInstanceClassName("test.class");
        testCond.setConditionName("test.cond.name");
        testCond.setOrder(1);
        conds.add(testCond);

        testTask.setTaskConfig(new WorkflowTaskConfiguration());
        testTask.setTaskId("test.task.id");
        testTask.setConditions(conds);
        testTask.setOrder(1);
        testTask.setTaskInstanceClassName("test.class");
        testTask.setTaskName("test.task.name");
        tasks.add(testTask);
        testWkflw.setTasks(tasks);

        testInst = new WorkflowInstance();
        testInst.setCurrentTaskId("test.task");
        testInst.setStatus(STARTED);
        testInst.setWorkflow(testWkflw);

        Metadata sharedContext = new Metadata();
        sharedContext.addMetadata("TestKey1", "TestVal1");
        sharedContext.addMetadata("TestKey1", "TestVal2");
        sharedContext.addMetadata("TestKey2", "TestVal3");
        testInst.setSharedContext(sharedContext);
    }

    public void testUpdateDocumentAndPreserveId() {
        try {
            repo.addWorkflowInstance(testInst);
        } catch (InstanceRepositoryException e) {
            fail(e.getMessage());
        }

       
        // preserve its id
        String wInstId = testInst.getId();

        // modify it
        try {
            repo.updateWorkflowInstance(testInst);
        } catch (InstanceRepositoryException e) {
            fail(e.getMessage());
        }

        // make sure that the new id is the same
        assertEquals(wInstId, testInst.getId());

        // make sure that there is only 1 workflow instance
        List wInsts = null;
        try {
            wInsts = repo.getWorkflowInstances();
        } catch (InstanceRepositoryException e) {
            fail(e.getMessage());
        }

        assertNotNull(wInsts);
        assertEquals(1, wInsts.size());

        // make sure that we can look up that workflow inst by its id
        WorkflowInstance foundInst = null;
        try {
            foundInst = repo.getWorkflowInstanceById(wInstId);
        } catch (InstanceRepositoryException e) {
            fail(e.getMessage());
        }

        assertNotNull(foundInst);
        assertEquals(foundInst.getId(), wInstId);
        assertNotNull(foundInst.getSharedContext());
        assertNotNull(foundInst.getSharedContext().getHashtable());
        assertEquals(2, foundInst.getSharedContext().getHashtable().keySet()
                .size());
        assertNotNull(foundInst.getSharedContext().getAllMetadata("TestKey1"));
        assertEquals(2, foundInst.getSharedContext().getAllMetadata("TestKey1")
                .size());

        boolean gotVal1 = false, gotVal2 = false;

        for (Iterator i = foundInst.getSharedContext().getAllMetadata(
                "TestKey1").iterator(); i.hasNext();) {
            String val = (String) i.next();
            if (val.equals("TestVal1")) {
                gotVal1 = true;
            } else if (val.equals("TestVal2")) {
                gotVal2 = true;
            }
        }

        assert (gotVal1 && gotVal2);

        assertNotNull(foundInst.getSharedContext().getMetadata("TestKey2"));
        assertEquals("TestVal3", foundInst.getSharedContext().getMetadata(
                "TestKey2"));
    }

}
