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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import javax.sql.DataSource;

//OODT imports
import org.apache.oodt.commons.database.DatabaseConnectionBuilder;
import org.apache.oodt.commons.database.SqlScript;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.structs.WorkflowCondition;
import org.apache.oodt.cas.workflow.structs.WorkflowConditionInstance;
import org.apache.oodt.cas.workflow.structs.exceptions.RepositoryException;
import org.apache.oodt.cas.workflow.util.GenericWorkflowObjectFactory;

//Junit imports
import junit.framework.TestCase;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Test suite for the {@link DataSourceCatalog} and
 * {@link DataSourceCatalogFactory}.
 * </p>.
 */
public class TestWorkflowDataSourceRepository extends TestCase {

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
        File tempFile = null;

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
    protected void setUp() throws Exception {
        ds = DatabaseConnectionBuilder.buildDataSource("sa", "", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:file:" + tmpDirPath + "/testCat;shutdown=true");
        SqlScript coreSchemaScript = new SqlScript("src/testdata/workflow.sql", ds);
        coreSchemaScript.loadScript();
        coreSchemaScript.execute();
        ds.getConnection().commit();
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        ds.getConnection().close();
    }
    
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
    
}

