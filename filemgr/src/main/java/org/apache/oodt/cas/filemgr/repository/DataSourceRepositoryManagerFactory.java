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

package org.apache.oodt.cas.filemgr.repository;

//OODT imports
import org.apache.oodt.commons.database.DatabaseConnectionBuilder;

//JDK imports
import javax.sql.DataSource;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A Factory for creating {@link DataSourceRepositoryManager}s.
 * </p>
 * 
 */
public class DataSourceRepositoryManagerFactory implements
        RepositoryManagerFactory {

    /* our data source */
    private DataSource dataSource = null;

    /**
     * <p>
     * Default Constructor
     * </p>.
     */
    public DataSourceRepositoryManagerFactory() {
        String jdbcUrl, user, pass, driver;

        jdbcUrl = System
                .getProperty("org.apache.oodt.cas.filemgr.repositorymgr.datasource.jdbc.url");
        user = System
                .getProperty("org.apache.oodt.cas.filemgr.repositorymgr.datasource.jdbc.user");
        pass = System
                .getProperty("org.apache.oodt.cas.filemgr.repositorymgr.datasource.jdbc.pass");
        driver = System
                .getProperty("org.apache.oodt.cas.filemgr.repositorymgr.datasource.jdbc.driver");

        dataSource = DatabaseConnectionBuilder.buildDataSource(user, pass,
                driver, jdbcUrl);
    }

    /**
     * <p>
     * Constructs a RepositoryManager from the given {@link DataSource}.
     * </p>
     * 
     * @param ds
     *            The DataSource to construct the RepositoryManager from.
     */
    public DataSourceRepositoryManagerFactory(DataSource ds) {
        dataSource = ds;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.repository.RepositoryManagerFactory#createRepositoryManager()
     */
    public RepositoryManager createRepositoryManager() {
        return new DataSourceRepositoryManager(dataSource);
    }

}
