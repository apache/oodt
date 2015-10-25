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

package org.apache.oodt.cas.filemgr.validation;

//OODT imports
import org.apache.oodt.commons.database.DatabaseConnectionBuilder;

//JDK imports
import javax.sql.DataSource;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A Factory class for creating {@link DataSourceValidationLayer}s.
 * </p>
 * 
 */
public class DataSourceValidationLayerFactory implements ValidationLayerFactory {
    /* our data source */
    private DataSource dataSource = null;

    /* should we quote fields or not? */
    private boolean quoteFields = false;

    /**
     * <p>
     * Default Constructor
     * </p>
     */
    public DataSourceValidationLayerFactory() {
        String jdbcUrl, user, pass, driver;

        jdbcUrl = System
                .getProperty("org.apache.oodt.cas.filemgr.validation.datasource.jdbc.url");
        user = System
                .getProperty("org.apache.oodt.cas.filemgr.validation.datasource.jdbc.user");
        pass = System
                .getProperty("org.apache.oodt.cas.filemgr.validation.datasource.jdbc.pass");
        driver = System
                .getProperty("org.apache.oodt.cas.filemgr.validation.datasource.jdbc.driver");

        dataSource = DatabaseConnectionBuilder.buildDataSource(user, pass,
                driver, jdbcUrl);
        quoteFields = Boolean
                .getBoolean("org.apache.oodt.cas.filemgr.validation.datasource.quoteFields");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.validation.ValidationLayerFactory#createValidationLayer()
     */
    public ValidationLayer createValidationLayer() {
        return new DataSourceValidationLayer(dataSource, quoteFields);
    }

}
