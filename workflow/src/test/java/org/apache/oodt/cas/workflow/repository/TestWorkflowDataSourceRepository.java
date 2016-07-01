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


import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.structs.Workflow;
import org.apache.oodt.cas.workflow.structs.WorkflowCondition;
import org.apache.oodt.cas.workflow.structs.WorkflowConditionInstance;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration;
import org.apache.oodt.cas.workflow.structs.exceptions.RepositoryException;
import org.apache.oodt.cas.workflow.util.GenericWorkflowObjectFactory;
import org.apache.oodt.commons.database.DatabaseConnectionBuilder;
import org.apache.oodt.commons.database.SqlScript;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.sql.DataSource;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;


/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Test suite for the {@link DataSourceCatalog} and
 * {@link DataSourceCatalogFactory}.
 * </p>.
 */

public class TestWorkflowDataSourceRepository {

    private String tmpDirPath = null;

    private DataSource ds;
    
    public TestWorkflowDataSourceRepository() throws SQLException, FileNotFoundException {
        // set the log levels
        System.setProperty("java.util.logging.config.file", new File(
                "./src/main/resources/logging.properties").getAbsolutePath());

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
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    public void setUp() throws Exception {
        ds = DatabaseConnectionBuilder.buildDataSource("sa", "", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:file:" + tmpDirPath + "/testCat;shutdown=true");
        SqlScript coreSchemaScript = new SqlScript("src/test/resources/workflow.sql", ds);
        coreSchemaScript.loadScript();
        coreSchemaScript.execute();
        ds.getConnection().commit();
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#tearDown()
     */
    @After
    public void tearDown() throws Exception {
        ds.getConnection().close();
    }
    
    /**
     * @since OODT-205
     */
    @Test
    public void testWorkflowConditions(){
      DataSourceWorkflowRepository repo = new DataSourceWorkflowRepository(ds);
            
      Workflow w = null;
      try{
        w = repo.getWorkflowById("1");
      }
      catch(Exception e){
        fail(e.getMessage());
      }
      
      assertNotNull(w);
      assertNotNull(w.getConditions());
      assertTrue(w.getConditions().size() > 0);
      assertEquals(w.getConditions().size(), 1);
    }


    @Test
    public void testDataSourceRepo() throws SQLException, RepositoryException {
        DataSourceWorkflowRepository repo = new DataSourceWorkflowRepository(ds);
        
        //test id 1
        WorkflowCondition wc = repo.getWorkflowConditionById("1");
        assertEquals(wc.getConditionName(), "CheckCond");
        WorkflowConditionInstance condInst = GenericWorkflowObjectFactory.getConditionObjectFromClassName(wc.getConditionInstanceClassName());
        Metadata m = new Metadata();
        m.addMetadata("Met1", "Val1");
        m.addMetadata("Met2", "Val2");
        m.addMetadata("Met3", "Val3");
        assertTrue(condInst.evaluate(m, wc.getTaskConfig()));
        
        //test id 2
        wc = repo.getWorkflowConditionById("2");
        assertEquals(wc.getConditionName(), "FalseCond");
        condInst = GenericWorkflowObjectFactory.getConditionObjectFromClassName(wc.getConditionInstanceClassName());
        assertFalse(condInst.evaluate(m, wc.getTaskConfig()));
        
        //test id 3
        wc = repo.getWorkflowConditionById("3");
        assertEquals(wc.getConditionName(), "TrueCond");
        condInst = GenericWorkflowObjectFactory.getConditionObjectFromClassName(wc.getConditionInstanceClassName());
        assertTrue(condInst.evaluate(m, wc.getTaskConfig()));
    }

  @Test
  public void testGetworkflowByIncorredId() throws RepositoryException {
    DataSourceWorkflowRepository repo = new DataSourceWorkflowRepository(ds);

    Workflow w = repo.getWorkflowById("100");

    assertNull(w);


  }

  @Test(expected=RepositoryException.class)
  public void testGetworkflowByIdNoDataSource() throws RepositoryException {
    DataSourceWorkflowRepository repo = new DataSourceWorkflowRepository(null);

    Workflow w = repo.getWorkflowById("1");

    assertNull(w);


  }

  @Test
  public void testGetworkflowByName() throws RepositoryException {
    DataSourceWorkflowRepository repo = new DataSourceWorkflowRepository(ds);

    Workflow w = repo.getWorkflowByName("Test Workflow");

    assertNotNull(w);

    assertThat("Test Workflow", equalTo(w.getName()));


  }

  @Test
  public void testGetWorkflowByNameIncorrect() throws RepositoryException{
    DataSourceWorkflowRepository repo = new DataSourceWorkflowRepository(ds);

    Workflow w = repo.getWorkflowByName("Broken Workflow");

    assertNull(w);
  }

  @Test(expected=RepositoryException.class)
  public void testGetWorkflowByNameNoDataSource() throws RepositoryException {
    DataSourceWorkflowRepository repo = new DataSourceWorkflowRepository(null);

    repo.getWorkflowByName("Broken Workflow");

  }

  @Test(expected=RepositoryException.class)
  public void testGetWorkflowsNullRepository() throws RepositoryException {
    DataSourceWorkflowRepository repo = new DataSourceWorkflowRepository(null);
    List flows = repo.getWorkflows();

    assertNotNull(flows);

  }

  @Test
  public void testGetWorkflows() throws RepositoryException {
    DataSourceWorkflowRepository repo = new DataSourceWorkflowRepository(ds);
    List<Workflow> flows = repo.getWorkflows();

    assertThat(flows, allOf(notNullValue(), hasSize(1)));

  }

  @Test
  public void testGetWorkflowsNoConditionsOrTasks() throws RepositoryException {

    DataSourceWorkflowRepository repo = new DataSourceWorkflowRepository(ds);
    List<Workflow> flows = repo.getWorkflows(false, false);

    assertThat(flows, allOf(notNullValue(), hasSize(1)));

    assertThat(flows.get(0).getPreConditions(), hasSize(0));

    assertThat(flows.get(0).getPostConditions(), hasSize(0));

    assertThat(flows.get(0).getTasks(), hasSize(0));


  }

  @Test
  public void testGetTaskByWorkflowName() throws RepositoryException {

    DataSourceWorkflowRepository repo = new DataSourceWorkflowRepository(ds);
    List<WorkflowTask> tasks = repo.getTasksByWorkflowName("Test Workflow");

    assertThat(tasks, allOf(notNullValue(), hasSize(2)));

    assertThat(tasks.get(0).getTaskName(), equalTo("Test Task"));

    assertThat(tasks.get(1).getTaskName(), equalTo("Test Task2"));

  }

  @Test
  public void testGetTasksByWorkflowNameNull() throws RepositoryException {

    DataSourceWorkflowRepository repo = new DataSourceWorkflowRepository(ds);
    List<WorkflowTask> tasks = repo.getTasksByWorkflowName(null);

    assertThat(tasks, nullValue());

  }

  @Test(expected=RepositoryException.class)
  public void testGetTasksByWorkflowNameNoDataSource() throws RepositoryException {
    DataSourceWorkflowRepository repo = new DataSourceWorkflowRepository(null);
    List<WorkflowTask> tasks = repo.getTasksByWorkflowName(null);

    assertThat(tasks, allOf(notNullValue(), hasSize(2)));

  }

  @Test
  public void testGetWorkflowsForEvent() throws RepositoryException {
    DataSourceWorkflowRepository repo = new DataSourceWorkflowRepository(ds);
    List<Workflow> workflow = repo.getWorkflowsForEvent("event");

    assertThat(workflow, allOf(notNullValue(), hasSize(1)));

    assertThat(workflow.get(0).getName(), equalTo("Test Workflow"));

  }

  @Test
  public void testGetworkflowsForEventNoTasksOrConditions() throws RepositoryException {

    DataSourceWorkflowRepository repo = new DataSourceWorkflowRepository(ds);
    List<Workflow> workflow = repo.getWorkflowsForEvent("event", false, false);

    assertThat(workflow, allOf(notNullValue(), hasSize(1)));

    assertThat(workflow.get(0).getPreConditions(), allOf(notNullValue(), hasSize(0)));
    assertThat(workflow.get(0).getPostConditions(), allOf(notNullValue(), hasSize(0)));

  }

  @Test(expected=RepositoryException.class)
  public void testGetWorkflowsForEventNoDataSource() throws RepositoryException {

    DataSourceWorkflowRepository repo = new DataSourceWorkflowRepository(null);
    List<Workflow> workflow = repo.getWorkflowsForEvent("event", false, false);

    assertThat(workflow, allOf(notNullValue(), hasSize(2)));

    assertThat(workflow.get(0).getPreConditions(), allOf(notNullValue(), hasSize(0)));
    assertThat(workflow.get(0).getPostConditions(), allOf(notNullValue(), hasSize(0)));

  }

  @Test
  public void testGetConditionsByTaskName() throws RepositoryException {

    DataSourceWorkflowRepository repo = new DataSourceWorkflowRepository(ds);
    List<WorkflowCondition> workflow = repo.getConditionsByTaskName("Test Task");

    assertThat(workflow, allOf(notNullValue(), hasSize(1)));
    assertThat(workflow.get(0).getConditionName(), equalTo("TrueCond"));



  }

  @Test(expected=RepositoryException.class)
  public void testGetConditionsByTaskNameNoDataSource() throws RepositoryException {
    DataSourceWorkflowRepository repo = new DataSourceWorkflowRepository(null);
    List<WorkflowCondition> workflow = repo.getConditionsByTaskName("Test Task");

    assertThat(workflow, allOf(notNullValue(), hasSize(1)));
    assertThat(workflow.get(0).getConditionName(), equalTo("Test Condition"));
  }

  @Test
  public void testGetConditionsByTaskId() throws RepositoryException {

    DataSourceWorkflowRepository repo = new DataSourceWorkflowRepository(ds);
    List<WorkflowCondition> workflow = repo.getConditionsByTaskId("1");

    assertThat(workflow, allOf(notNullValue(), hasSize(1)));
    assertThat(workflow.get(0).getConditionName(), equalTo("TrueCond"));

  }

  @Test(expected=RepositoryException.class)
  public void testGetConditionsByTaskIdNoDataSource() throws RepositoryException {
    DataSourceWorkflowRepository repo = new DataSourceWorkflowRepository(null);
    List<WorkflowCondition> workflow = repo.getConditionsByTaskName("1");

    assertThat(workflow, allOf(notNullValue(), hasSize(1)));
    assertThat(workflow.get(0).getConditionName(), equalTo("Test Condition"));
  }

  @Test
  public void testGetConfigurationByTaskId() throws RepositoryException {
    DataSourceWorkflowRepository repo = new DataSourceWorkflowRepository(ds);
    WorkflowTaskConfiguration workflow = repo.getConfigurationByTaskId("1");

    assertThat(workflow, notNullValue());
    assertThat(workflow.getProperties(), notNullValue());
    assertThat(workflow.getProperties().getProperty("TestProp"), notNullValue());
  }

  @Test(expected=RepositoryException.class)
  public void testGetConfigurationByTaskIdNoDataSource() throws RepositoryException {
    DataSourceWorkflowRepository repo = new DataSourceWorkflowRepository(null);
    WorkflowTaskConfiguration workflow = repo.getConfigurationByTaskId("1");

    assertThat(workflow, notNullValue());
    assertThat(workflow.getProperties(), notNullValue());
    assertThat(workflow.getProperties().getProperty("test"), notNullValue());
  }

  @Test
  public void testGetWorkflowTaskById() throws RepositoryException {
    DataSourceWorkflowRepository repo = new DataSourceWorkflowRepository(ds);
    WorkflowTask workflow = repo.getWorkflowTaskById("1");

    assertThat(workflow, notNullValue());
    assertThat(workflow.getTaskName(), equalTo("Test Task"));
  }

  @Test(expected=RepositoryException.class)
  public void testGetWorkflowTaskByIdNoDataSource() throws RepositoryException {
    DataSourceWorkflowRepository repo = new DataSourceWorkflowRepository(null);
    WorkflowTask workflow = repo.getWorkflowTaskById("1");

    assertThat(workflow, notNullValue());
    assertThat(workflow.getTaskName(), equalTo("Test"));
  }

  @Test
  public void testGetRegisteredEvents() throws RepositoryException {
    DataSourceWorkflowRepository repo = new DataSourceWorkflowRepository(ds);
    List<String> workflow = repo.getRegisteredEvents();

    assertThat(workflow, allOf(notNullValue(), hasSize(1)));
    assertThat(workflow.get(0), equalTo("event"));
  }

  @Test
  public void testAddTask() throws RepositoryException {
    DataSourceWorkflowRepository repo = new DataSourceWorkflowRepository(ds);
    WorkflowTask t = new WorkflowTask();
    t.setTaskName("Manual");
    t.setTaskId("50");
    t.setPreConditions(Collections.EMPTY_LIST);
    t.setPostConditions(Collections.EMPTY_LIST);
    String workflow = repo.addTask(t);

    assertThat(workflow, notNullValue());
    assertThat(workflow, equalTo("3"));

    //TODO GET TASK BACK

  }

  @Test
  public void testAddWorkflow() throws RepositoryException {
    DataSourceWorkflowRepository repo = new DataSourceWorkflowRepository(ds);
    Workflow w = new Workflow();
    w.setId("50");
    w.setName("Manual");
    WorkflowTask t = new WorkflowTask();
    t.setTaskId("1");
    List<WorkflowTask> l = new ArrayList<WorkflowTask>();
    l.add(t);
    w.setTasks(l);
    String workflow = repo.addWorkflow(w);

    assertThat(workflow, notNullValue());
    assertThat(workflow, equalTo("50"));

    //TODO GET WORKFLOW

  }

  @Test
  public void testGetTaskById() throws RepositoryException {
    DataSourceWorkflowRepository repo = new DataSourceWorkflowRepository(ds);
    WorkflowTask task = repo.getTaskById("1");

    assertThat(task, notNullValue());
    assertThat(task.getTaskName(), allOf(notNullValue(), equalTo("Test Task")));


  }

  @Test(expected=RepositoryException.class)
  public void testGetTaskByIdNoDataSource() throws RepositoryException {
    DataSourceWorkflowRepository repo = new DataSourceWorkflowRepository(null);
    WorkflowTask task = repo.getTaskById("1");

    assertThat(task, notNullValue());
    assertThat(task.getTaskName(), allOf(notNullValue(), equalTo("Test")));
  }

  @Test
  public void testGetConditions() throws RepositoryException {

    DataSourceWorkflowRepository repo = new DataSourceWorkflowRepository(ds);
    List<WorkflowCondition> task = repo.getConditions();

    assertThat(task, allOf(notNullValue(), hasSize(4)));
    assertThat(task.get(0).getConditionName(), allOf(notNullValue(), equalTo("CheckCond")));

  }

    
}

