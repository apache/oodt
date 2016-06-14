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

package org.apache.oodt.cas.filemgr.catalog;

//JDK imports
import javax.sql.DataSource;

//OODT imports
import org.apache.oodt.commons.database.DatabaseConnectionBuilder;
import org.apache.oodt.cas.filemgr.catalog.Catalog;
import org.apache.oodt.cas.filemgr.catalog.CatalogFactory;
import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.oodt.cas.filemgr.validation.ScienceDataValidationLayerFactory;

/**
 * Implements the CatalogFactory interface to provide a factory for
 * {@link ScienceDataCatalog}. The properties referenced in this class can be
 * edited in the filemgr.properties file in
 * [filemgr_home]/etc/filemgr.properties
 * 
 * @author ahart
 * @author cgoodale
 * @author mattmann
 * 
 */
public class ScienceDataCatalogFactory implements CatalogFactory {

  // The data source
  protected DataSource dataSource = null;

  // The page size
  protected int pageSize;

  public ScienceDataCatalogFactory() {

    String jdbcUrl, user, pass, driver;

    jdbcUrl = PathUtils.replaceEnvVariables(System
        .getProperty("org.apache.cas.filemgr.catalog.science.jdbc.url"));
    user = PathUtils.replaceEnvVariables(System
        .getProperty("org.apache.cas.filemgr.catalog.science.jdbc.user"));
    pass = PathUtils.replaceEnvVariables(System
        .getProperty("org.apache.cas.filemgr.catalog.science.jdbc.pass"));
    driver = PathUtils.replaceEnvVariables(System
        .getProperty("org.apache.cas.filemgr.catalog.science.jdbc.driver"));

    dataSource = DatabaseConnectionBuilder.buildDataSource(user, pass, driver,
        jdbcUrl);
    pageSize = Integer.valueOf(PathUtils.replaceEnvVariables(System
        .getProperty("org.apache.cas.filemgr.catalog.science.pageSize")));

  }

  public Catalog createCatalog() {
    return new ScienceDataCatalog(dataSource,
        new ScienceDataValidationLayerFactory().createValidationLayer(),
        pageSize);
  }

}
