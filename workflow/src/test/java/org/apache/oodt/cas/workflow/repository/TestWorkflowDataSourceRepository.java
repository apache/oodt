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
import org.apache.oodt.cas.workflow.structs.exceptions.RepositoryException;
import org.apache.oodt.cas.workflow.util.GenericWorkflowObjectFactory;
import org.apache.oodt.commons.database.DatabaseConnectionBuilder;
import org.apache.oodt.commons.database.SqlScript;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.SQLException;
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
  @Ignore
  public void testGetWorkflowsNoConditionsOrTasks() throws RepositoryException {

    DataSourceWorkflowRepository repo = new DataSourceWorkflowRepository(ds);
    List<Workflow> flows = repo.getWorkflows(false, false);

    assertThat(flows, allOf(notNullValue(), hasSize(1)));

    assertThat(flows.get(0).getPreConditions(), hasSize(0));

    assertThat(flows.get(0).getPostConditions(), hasSize(0));

    assertThat(flows.get(0).getTasks(), hasSize(0));


  }

  @Ignore
  @Test
  public void testGetTaskByWorkflowName() throws RepositoryException {

    DataSourceWorkflowRepository repo = new DataSourceWorkflowRepository(ds);
    List<WorkflowTask> tasks = repo.getTasksByWorkflowName("Test Workflow");

    assertThat(tasks, allOf(notNullValue(), hasSize(2)));



  }

  @Ignore
  @Test
  public void testGetTasksByWorkflowNameNull() throws RepositoryException {

    DataSourceWorkflowRepository repo = new DataSourceWorkflowRepository(ds);
    List<WorkflowTask> tasks = repo.getTasksByWorkflowName(null);

    assertThat(tasks, allOf(notNullValue(), hasSize(2)));

  }

  @Ignore
  @Test
  public void testGetTasksByWorkflowNameNoDataSource() throws RepositoryException {
    DataSourceWorkflowRepository repo = new DataSourceWorkflowRepository(null);
    List<WorkflowTask> tasks = repo.getTasksByWorkflowName(null);

    assertThat(tasks, allOf(notNullValue(), hasSize(2)));

  }

  @Ignore
  @Test
  public void testGetWorkflowsForEvent() throws RepositoryException {
    DataSourceWorkflowRepository repo = new DataSourceWorkflowRepository(ds);
    List<WorkflowTask> tasks = repo.getWorkflowsForEvent("event");

    assertThat(tasks, allOf(notNullValue(), hasSize(2)));


  }

  @Ignore
  @Test
  public void testGetworkflowsForEventNoTasksOrConditions() throws RepositoryException {

    DataSourceWorkflowRepository repo = new DataSourceWorkflowRepository(ds);
    List<WorkflowTask> tasks = repo.getWorkflowsForEvent("event", false, false);

    assertThat(tasks, allOf(notNullValue(), hasSize(2)));


  }

  @Ignore
  @Test
  public void testGetWorkflowsForEventNoDataSource(){

  }

  @Ignore
  @Test
  public void testGetConditionsByTaskName(){

  }

  @Ignore
  @Test
  public void testGetConditionsByTaskNameNoDataSource(){

  }

  @Ignore
  @Test
  public void testGetConditionsByTaskId(){

  }

  @Ignore
  @Test
  public void testGetConditionsByTaskIdNoDataSource(){

  }

  @Ignore
  @Test
  public void testGetConfigurationByTaskId(){

  }

  @Ignore
  @Test
  public void testGetConfigurationByTaskIdNoDataSource(){

  }

  @Ignore
  @Test
  public void testGetWorkflowTaskById(){

  }

  @Ignore
  @Test
  public void testGetWorkflowTaskByIdNoDataSource(){

  }

  @Ignore
  @Test
  public void testGetRegisteredEvents(){

  }

  @Ignore
  @Test
  public void testAddTask(){

  }

  @Ignore
  @Test
  public void testAddWorkflow(){


  }

  @Ignore
  @Test
  public void testGetTaskById(){

  }

  @Ignore
  @Test
  public void testGetTaskByIdNoDataSource(){

  }

  @Ignore
  @Test
  public void testGetConditions(){

  }

    
}

