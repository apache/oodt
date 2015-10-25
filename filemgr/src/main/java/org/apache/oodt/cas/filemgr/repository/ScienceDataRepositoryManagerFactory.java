/**
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

//JDK imports
import javax.sql.DataSource;

//OODT imports
import org.apache.oodt.commons.database.DatabaseConnectionBuilder;
import org.apache.oodt.cas.filemgr.repository.RepositoryManager;
import org.apache.oodt.cas.filemgr.repository.RepositoryManagerFactory;
import org.apache.oodt.cas.metadata.util.PathUtils;

/**
 * 
 * Constructs new {@link ScienceDataRepositoryManager}s.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class ScienceDataRepositoryManagerFactory implements
    RepositoryManagerFactory {

  private DataSource dataSource;

  public ScienceDataRepositoryManagerFactory() {
    String jdbcUrl, user, pass, driver;

    jdbcUrl = PathUtils
        .replaceEnvVariables(System
            .getProperty("org.apache.oodt.cas.filemgr.repositorymgr.science.jdbc.url"));
    user = PathUtils
        .replaceEnvVariables(System
            .getProperty("org.apache.oodt.cas.filemgr.repositorymgr.science.jdbc.user"));
    pass = PathUtils
        .replaceEnvVariables(System
            .getProperty("org.apache.oodt.cas.filemgr.repositorymgr.science.jdbc.pass"));
    driver = PathUtils
        .replaceEnvVariables(System
            .getProperty("org.apache.oodt.cas.filemgr.repositorymgr.science.jdbc.driver"));

    this.dataSource = DatabaseConnectionBuilder.buildDataSource(user, pass,
        driver, jdbcUrl);
  }

  /*
   * (non-Javadoc)
   * 
   * @seeorg.apache.oodt.oodt.cas.filemgr.repository.RepositoryManagerFactory#
   * createRepositoryManager()
   */
  public RepositoryManager createRepositoryManager() {
    return new ScienceDataRepositoryManager(this.dataSource);
  }

}
