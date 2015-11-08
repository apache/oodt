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


package org.apache.oodt.cas.workflow.repository;


//JDK imports

import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.structs.Workflow;
import org.apache.oodt.cas.workflow.structs.WorkflowCondition;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration;
import org.apache.oodt.cas.workflow.structs.exceptions.RepositoryException;
import org.apache.oodt.cas.workflow.util.GenericWorkflowObjectFactory;

import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.*;


/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Unit tests for the Workflow Repository.
 * </p>
 * 
 */
public class TestWorkflowRepository  {

    private static Logger LOG = Logger.getLogger(TestWorkflowRepository.class.getName());
    private XMLWorkflowRepository workflowRepository = null;

    private static List workflowDirUris = new Vector();

    /**
     * <p>
     * Default Constructor
     * </p>
     */
    public TestWorkflowRepository() {
        workflowDirUris.add(new File("./src/main/resources/examples").toURI()
                .toString());
        workflowRepository = new XMLWorkflowRepository(workflowDirUris);
    }
    
    /**
     * @since OODT-205
     */
    @Test
    public void testWorkflowConditions(){
      Workflow w = null;
      try{
        w = this.workflowRepository.getWorkflowById("urn:oodt:conditionsWorkflow");
      }
      catch(Exception e){
        fail(e.getMessage());
      }
      
      assertNotNull(w);
      assertNotNull(w.getConditions());
      assertTrue(w.getConditions().size() > 0);
      assertEquals(w.getConditions().size(), 1);
    }    

    public void testGetWorkflowByName() {
        Workflow w = null;

        try {
            w = workflowRepository.getWorkflowByName("backwardsTestWorkflow");
        } catch (RepositoryException e) {
            LOG.log(Level.SEVERE, e.getMessage());
            fail(e.getMessage());
        }

        validateBackwardsWorkflow(w);
    }

    public void testGetWorkflowById() {
        Workflow w = null;

        try {
            w = workflowRepository
                    .getWorkflowById("urn:oodt:backwardsTestWorkflow");
        } catch (RepositoryException e) {
            LOG.log(Level.SEVERE, e.getMessage());
            fail(e.getMessage());
        }

        validateBackwardsWorkflow(w);
    }

    public void testGetWorkflows() {
        List workflows = null;

        try {
            workflows = workflowRepository.getWorkflows();
        } catch (RepositoryException e) {
            LOG.log(Level.SEVERE, e.getMessage());
            fail(e.getMessage());
        }

        assertNotNull(workflows);
        assertEquals(11, workflows.size());
    }

    public void testGetWorkflowsForEvent() {
        List workflows = null;

        try {
            workflows = workflowRepository.getWorkflowsForEvent("test");
        } catch (RepositoryException e) {
            LOG.log(Level.SEVERE, e.getMessage());
            fail(e.getMessage());
        }

        assertNotNull(workflows);
        assertEquals(1, workflows.size());

        try {
            workflows = workflowRepository.getWorkflowsForEvent("backwards");
        } catch (RepositoryException e) {
            LOG.log(Level.SEVERE, e.getMessage());
            fail(e.getMessage());
        }

        assertNotNull(workflows);
        assertEquals(1, workflows.size());
        

        try {
            workflows = workflowRepository.getWorkflowsForEvent("externalScript");
        } catch (RepositoryException e) {
            LOG.log(Level.SEVERE, e.getMessage());
            fail(e.getMessage());
        }

        assertNotNull(workflows);
        assertEquals(1, workflows.size());
    }

    public void testGetTasksByWorkflowId() {
        List tasks = null;

        try {
            tasks = workflowRepository
                    .getTasksByWorkflowId("urn:oodt:backwardsTestWorkflow");
        } catch (RepositoryException e) {
            LOG.log(Level.SEVERE, e.getMessage());
            fail(e.getMessage());
        }

        validateBackwardsWorkflowTasks(tasks);
    }

    public void testGetTasksByWorkflowName() {
        List tasks = null;

        try {
            tasks = workflowRepository
                    .getTasksByWorkflowName("backwardsTestWorkflow");
        } catch (RepositoryException e) {
            LOG.log(Level.SEVERE, e.getMessage());
            fail(e.getMessage());
        }

        validateBackwardsWorkflowTasks(tasks);
    }

    public void testGetConditionsByTaskId() {
        List conditions = null;

        try {
            conditions = workflowRepository
                    .getConditionsByTaskId("urn:oodt:GoodbyeWorld");
        } catch (RepositoryException e) {
            LOG.log(Level.SEVERE, e.getMessage());
            fail(e.getMessage());
        }

        validateTaskCondition(conditions);
    }

    public void testGetConditionsByTaskName() {
        List conditions = null;

        try {
            conditions = workflowRepository
                    .getConditionsByTaskName("Goodbye World");
        } catch (RepositoryException e) {
            LOG.log(Level.SEVERE, e.getMessage());
            fail(e.getMessage());
        }

        validateTaskCondition(conditions);
    }
    
    /**
     * @since OODT-207
     */
    public void testGetConditionTimeout(){
      WorkflowCondition cond = null;
      try{
        cond = workflowRepository.getWorkflowConditionById("urn:oodt:TimeoutCondition");
      }
      catch(Exception e){
        LOG.log(Level.SEVERE, e.getMessage());
        fail(e.getMessage());
      }
      
      assertEquals(30L, cond.getTimeoutSeconds());
    }
    
    /**
     * @since OODT-208
     */
    public void testGetConditionOptional(){
      WorkflowCondition cond = null;
      try{
        cond = workflowRepository.getWorkflowConditionById("urn:oodt:OptionalCondition");
      }
      catch(Exception e){
        LOG.log(Level.SEVERE, e.getMessage());
        fail(e.getMessage());
      }
      
      assertEquals(true, cond.isOptional());
    }    

    public void testGetConfigurationByTaskId() {
        WorkflowTaskConfiguration config = null;

        try {
            config = workflowRepository
                    .getConfigurationByTaskId("urn:oodt:GoodbyeWorld");
        } catch (RepositoryException e) {
            LOG.log(Level.SEVERE, e.getMessage());
            fail(e.getMessage());
        }

        validateTaskConfiguration(config);
    }

    public void testEnvVarReplaceConfigProperties() {
        WorkflowTaskConfiguration config = null;

        try {
            config = workflowRepository
                    .getConfigurationByTaskId("urn:oodt:PropReplaceTask");
        } catch (RepositoryException e) {
            LOG.log(Level.SEVERE, e.getMessage());
            fail(e.getMessage());
        }

        assertNotNull(config);
        String replacedPath = config.getProperty("PathToReplace");
        String expectedValue = System.getenv("HOME") + "/my/path";
        assertEquals("The path: [" + replacedPath
                + "] does not equal the expected" + "path: [" + expectedValue
                + "]", replacedPath, expectedValue);

        String notReplacedPath = config.getProperty("DontReplaceMe");
        String notReplacedPathNoSpec = config
                .getProperty("DontReplaceMeNoSpec");

        assertNotNull(notReplacedPath);
        assertNotNull(notReplacedPathNoSpec);
        assertEquals(notReplacedPath, notReplacedPathNoSpec);

        String expected = "[HOME]/my/path";
        assertEquals("The path: [" + notReplacedPath + "] is not equal to the "
                + "expected value: [" + expected + "]", expected,
                notReplacedPath);

    }

    public void testMultipleConditions() {
        WorkflowTask multiTask = null;

        try {
            multiTask = workflowRepository
                    .getWorkflowTaskById("urn:oodt:TestMultiConditionTask");
        } catch (Exception e) {
            fail(e.getMessage());
        }

        assertNotNull(multiTask);
        assertNotNull(multiTask.getConditions());
        assertEquals(2, multiTask.getConditions().size());

        boolean gotTrueCond = false, gotFalseCond = false;

        for (int i = 0; i < multiTask.getConditions().size(); i++) {
            WorkflowCondition c = (WorkflowCondition) multiTask.getConditions()
                    .get(i);
            assertNotNull(c);
            if (c.getConditionName().equals("True Condition")) {
                gotTrueCond = true;
            } else if (c.getConditionName().equals("False Condition")) {
                gotFalseCond = true;
            }
        }

        assertTrue(gotTrueCond && gotFalseCond);
    }
    
    public void testConditionsConfiguration() {
        WorkflowCondition condition = null;

        try {
            condition = workflowRepository
                    .getWorkflowConditionById("urn:oodt:CheckForMetadataKeys");
        } catch (Exception e) {
            fail(e.getMessage());
        }

        assertNotNull(condition);
        assertNotNull(condition.getTaskConfig());

        Metadata m = new Metadata();
        m.addMetadata("Met1", "Val1");
        m.addMetadata("Met2", "Val2");
        m.addMetadata("Met3", "Val3");
        GenericWorkflowObjectFactory.getConditionObjectFromClassName(
                condition.getConditionInstanceClassName()).evaluate(m,
                condition.getTaskConfig());
    }

    private void validateBackwardsWorkflow(Workflow w) {
        assertNotNull(w);
        assertNotNull(w.getId());
        assertEquals("urn:oodt:backwardsTestWorkflow", w.getId());
        assertNotNull(w.getName());
        assertEquals("backwardsTestWorkflow", w.getName());
        validateBackwardsWorkflowTasks(w.getTasks());
    }

    private void validateBackwardsWorkflowTasks(List tasks) {
        assertNotNull(tasks);
        assertEquals(2, tasks.size());

        WorkflowTask t1 = (WorkflowTask) tasks.get(0);
        assertEquals("Goodbye World", t1.getTaskName());
        assertEquals("urn:oodt:GoodbyeWorld", t1.getTaskId());
        assertEquals(1, t1.getOrder());
        validateTaskConfiguration(t1.getTaskConfig());
        validateTaskCondition(t1.getConditions());

        WorkflowTask t2 = (WorkflowTask) tasks.get(1);
        assertEquals("Hello World", t2.getTaskName());
        assertEquals("urn:oodt:HelloWorld", t2.getTaskId());
        assertEquals(2, t2.getOrder());
        validateTaskConfiguration(t2.getTaskConfig());
        validateTaskCondition(t2.getConditions());

    }

    private void validateTaskCondition(List conditions) {
        assertNotNull(conditions);
        assertEquals(1, conditions.size());
        WorkflowCondition c = (WorkflowCondition) conditions.get(0);
        assertEquals("urn:oodt:TrueCondition", c.getConditionId());
        assertEquals("True Condition", c.getConditionName());
        assertEquals(1, c.getOrder());
        assertEquals(-1L, c.getTimeoutSeconds());
        assertEquals(false, c.isOptional());
    }

    private void validateTaskConfiguration(WorkflowTaskConfiguration config) {
        assertNotNull(config);
        assertNotNull(config.getProperties());
        assertNotNull(config.getProperties().get("Person"));
        assertEquals("Chris", (String) config.getProperties().get("Person"));
    }

}
