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

//APACHE imports

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.oodt.cas.workflow.exceptions.WorkflowException;

import javax.sql.DataSource;

//JDK imports

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A {@link WorkflowRepositoryFactory} that creates
 * {@DataSourceWorkflowRepository} instances.
 * </p>
 * 
 */
public class DataSourceWorkflowRepositoryFactory implements
        WorkflowRepositoryFactory {

    /* our data source */
    private DataSource dataSource = null;

    /**
     * <p>
     * Default Constructor
     * </p>.
     */
    public DataSourceWorkflowRepositoryFactory() throws WorkflowException {
        String jdbcUrl, user, pass, driver;

        jdbcUrl = System
                .getProperty("org.apache.oodt.cas.workflow.repo.datasource.jdbc.url");
        user = System
                .getProperty("org.apache.oodt.cas.workflow.repo.datasource.jdbc.user");
        pass = System
                .getProperty("org.apache.oodt.cas.workflow.repo.datasource.jdbc.pass");
        driver = System
                .getProperty("org.apache.oodt.cas.workflow.repo.datasource.jdbc.driver");

        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new WorkflowException("Cannot load driver: " + driver);
        }

        GenericObjectPool connectionPool = new GenericObjectPool(null);
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(
                jdbcUrl, user, pass);
        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(
                connectionFactory, connectionPool, null, null, false, true);

        dataSource = new PoolingDataSource(connectionPool);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.repository.WorkflowRepositoryFactory#createRepository()
     */
    public WorkflowRepository createRepository() {
        return new DataSourceWorkflowRepository(dataSource);
    }

}
