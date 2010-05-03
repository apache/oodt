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

package gov.nasa.jpl.oodt.cas.filemgr.validation;

//OODT imports
import gov.nasa.jpl.oodt.cas.commons.database.DatabaseConnectionBuilder;

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
    public DataSourceValidationLayerFactory() throws Exception {
        String jdbcUrl = null, user = null, pass = null, driver = null;

        jdbcUrl = System
                .getProperty("gov.nasa.jpl.oodt.cas.filemgr.validation.datasource.jdbc.url");
        user = System
                .getProperty("gov.nasa.jpl.oodt.cas.filemgr.validation.datasource.jdbc.user");
        pass = System
                .getProperty("gov.nasa.jpl.oodt.cas.filemgr.validation.datasource.jdbc.pass");
        driver = System
                .getProperty("gov.nasa.jpl.oodt.cas.filemgr.validation.datasource.jdbc.driver");

        dataSource = DatabaseConnectionBuilder.buildDataSource(user, pass,
                driver, jdbcUrl);
        quoteFields = Boolean
                .getBoolean("gov.nasa.jpl.oodt.cas.filemgr.validation.datasource.quoteFields");
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.filemgr.validation.ValidationLayerFactory#createValidationLayer()
     */
    public ValidationLayer createValidationLayer() {
        return new DataSourceValidationLayer(dataSource, quoteFields);
    }

}
