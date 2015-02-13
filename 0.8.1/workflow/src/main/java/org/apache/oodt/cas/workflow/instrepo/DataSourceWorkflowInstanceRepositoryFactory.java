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
import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.oodt.cas.workflow.instrepo.WorkflowInstanceRepository;
import org.apache.oodt.cas.workflow.instrepo.WorkflowInstanceRepositoryFactory;

//APACHE imports
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;

//JDK imports
import javax.sql.DataSource;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A factory for creating {@link DataSource} based
 * {@link WorkflowInstanceRepository}s.
 * </p>
 * 
 */
public class DataSourceWorkflowInstanceRepositoryFactory implements
        WorkflowInstanceRepositoryFactory {

    /* our data source */
    private DataSource dataSource = null;

    /* whether or not we are quoting the task_id and workflow_id fields */
    private boolean quoteFields = false;

    /* the number of workflow instances returned per page when paginating */
    private int pageSize = -1;

    /**
     * <p>
     * Default constructor
     * </p>
     */
    public DataSourceWorkflowInstanceRepositoryFactory() throws Exception {
        String jdbcUrl = null, user = null, pass = null, driver = null;

        jdbcUrl = PathUtils
                .replaceEnvVariables(System
                        .getProperty("org.apache.oodt.cas.workflow.instanceRep.datasource.jdbc.url"));
        user = PathUtils
                .replaceEnvVariables(System
                        .getProperty("org.apache.oodt.cas.workflow.instanceRep.datasource.jdbc.user"));
        pass = PathUtils
                .replaceEnvVariables(System
                        .getProperty("org.apache.oodt.cas.workflow.instanceRep.datasource.jdbc.pass"));
        driver = PathUtils
                .replaceEnvVariables(System
                        .getProperty("org.apache.oodt.cas.workflow.instanceRep.datasource.jdbc.driver"));

        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new Exception("Cannot load driver: " + driver);
        }

        GenericObjectPool connectionPool = new GenericObjectPool(null);
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(
                jdbcUrl, user, pass);
        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(
                connectionFactory, connectionPool, null, null, false, true);

        dataSource = new PoolingDataSource(connectionPool);
        quoteFields = Boolean
                .getBoolean("org.apache.oodt.cas.workflow.instanceRep.datasource.quoteFields");
        pageSize = Integer.getInteger(
                "org.apache.oodt.cas.workflow.instanceRep.pageSize", 20)
                .intValue();

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.instrepo.WorkflowInstanceRepositoryFactory#createInstanceRepository()
     */
    public WorkflowInstanceRepository createInstanceRepository() {
        return new DataSourceWorkflowInstanceRepository(dataSource,
                quoteFields, pageSize);
    }

}
