/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.oodt.cas.workflow.instrepo;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.structs.Workflow;
import org.apache.oodt.cas.workflow.structs.WorkflowCondition;
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;
import org.apache.oodt.cas.workflow.structs.WorkflowStatus;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration;
import org.apache.oodt.cas.workflow.structs.exceptions.InstanceRepositoryException;

//JDK imports
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Vector;

//Apache Imports
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

    private LuceneWorkflowInstanceRepository repo = null;

    private WorkflowInstance testInst = null;

    private Workflow testWkflw;

    private WorkflowTask testTask;

    private WorkflowCondition testCond;
    
    private String tmpDirPath = null;

    public TestLuceneWorkflowInstanceRepository() {
        testInst = new WorkflowInstance();
        testWkflw = new Workflow();
        testTask = new WorkflowTask();
        testCond = new WorkflowCondition();
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
    
        // first load the example configuration
        try {
            System.getProperties().load(
                    new FileInputStream("./src/main/resources/workflow.properties"));
        } catch (Exception e) {
            fail(e.getMessage());
        }

        // get a temp directory

        File tempDir = null;
        File tempFile;

        try {
            tempFile = File.createTempFile("foo", "bar");
            tempFile.deleteOnExit();
            tempDir = tempFile.getParentFile();
        } catch (Exception e) {
            fail(e.getMessage());
        }

        tmpDirPath = tempDir.getAbsolutePath();
        if (!tmpDirPath.endsWith("/")) {
            tmpDirPath += "/";
        }

        tmpDirPath += "testInstRepo/";

        // now override the catalog ones
        System.setProperty(
                "org.apache.oodt.cas.workflow.instanceRep.lucene.idxPath",
                tmpDirPath);

        System.setProperty(
                "org.apache.oodt.cas.workflow.instanceRep.pageSize", "20");

    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        repo = (LuceneWorkflowInstanceRepository) new LuceneWorkflowInstanceRepositoryFactory().createInstanceRepository();
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        // now remove the temporary directory used
        if (tmpDirPath != null) {
            FileUtils.forceDelete(new File(tmpDirPath));
        }
        if (repo != null) {
            repo = null;
        }

    }
    
    /**
    * @since OODT-389
    **/
    public void testInstanceRepoInitialization() {
        // Getting the number of workflow instances should not fail even on an empty index
        try {
            int count = repo.getNumWorkflowInstances();
            // There should be no instances in the index at this point
            assertEquals(0, count);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
    
    public void testClearInstances(){
      try{
        repo.addWorkflowInstance(testInst);
        assertEquals(1, repo.getNumWorkflowInstances());
        repo.clearWorkflowInstances();
        assertEquals(0, repo.getNumWorkflowInstances());
        
        for(int i=0; i < 25; i++){
          repo.addWorkflowInstance(testInst);
        }
        assertEquals(25, repo.getNumWorkflowInstances());
        repo.clearWorkflowInstances();
        assertEquals(0, repo.getNumWorkflowInstances());
      }
      catch(InstanceRepositoryException e){
        fail(e.getLocalizedMessage());
      }      
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
        assertNotNull(foundInst.getSharedContext().getMap());
        assertEquals(2, foundInst.getSharedContext().getMap().keySet()
                .size());
        assertNotNull(foundInst.getSharedContext().getAllMetadata("TestKey1"));
        assertEquals(2, foundInst.getSharedContext().getAllMetadata("TestKey1")
                .size());

        boolean gotVal1 = false, gotVal2 = false;

        for (String val : foundInst.getSharedContext().getAllMetadata(
            "TestKey1")) {
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
