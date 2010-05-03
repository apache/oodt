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

package gov.nasa.jpl.oodt.cas.filemgr.catalog;

//OODT imports
import gov.nasa.jpl.oodt.cas.commons.database.DatabaseConnectionBuilder;
import gov.nasa.jpl.oodt.cas.filemgr.util.GenericFileManagerObjectFactory;
import gov.nasa.jpl.oodt.cas.filemgr.validation.ValidationLayer;
import gov.nasa.jpl.oodt.cas.metadata.util.PathUtils;

//JDK imports
import javax.sql.DataSource;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A Factory class for creating {@link DataSourceCatalog}s.
 * </p>
 * 
 */
public class DataSourceCatalogFactory implements CatalogFactory {
    /* our data source */
    protected DataSource dataSource = null;

    /* our validation layer */
    protected ValidationLayer validationLayer = null;

    /* field Id Str: should product_type_id and element_id be quoted? */
    protected boolean fieldIdStr = false;

    /*
     * page size: size of the pages used by the catalog to paginate products
     * back to the user
     */
    protected int pageSize = -1;

    /* the amount of minutes to allow between updating the product cache */
    protected long cacheUpdateMinutes = -1L;

    /**
     * <p>
     * Default Constructor
     * </p>.
     */
    public DataSourceCatalogFactory() {
        String jdbcUrl = null, user = null, pass = null, driver = null;

        jdbcUrl = PathUtils
                .replaceEnvVariables(System
                        .getProperty("gov.nasa.jpl.oodt.cas.filemgr.catalog.datasource.jdbc.url"));
        user = PathUtils
                .replaceEnvVariables(System
                        .getProperty("gov.nasa.jpl.oodt.cas.filemgr.catalog.datasource.jdbc.user"));
        pass = PathUtils
                .replaceEnvVariables(System
                        .getProperty("gov.nasa.jpl.oodt.cas.filemgr.catalog.datasource.jdbc.pass"));
        driver = PathUtils
                .replaceEnvVariables(System
                        .getProperty("gov.nasa.jpl.oodt.cas.filemgr.catalog.datasource.jdbc.driver"));

        dataSource = DatabaseConnectionBuilder.buildDataSource(user, pass,
                driver, jdbcUrl);

        String validationLayerFactoryClass = System
                .getProperty("filemgr.validationLayer.factory",
                        "gov.nasa.jpl.oodt.cas.filemgr.validation.DataSourceValidationLayerFactory");
        validationLayer = GenericFileManagerObjectFactory
                .getValidationLayerFromFactory(validationLayerFactoryClass);
        fieldIdStr = Boolean
                .getBoolean("gov.nasa.jpl.oodt.cas.filemgr.catalog.datasource.quoteFields");

        pageSize = Integer
                .getInteger(
                        "gov.nasa.jpl.oodt.cas.filemgr.catalog.datasource.pageSize",
                        20).intValue();
        cacheUpdateMinutes = Long
                .getLong(
                        "gov.nasa.jpl.oodt.cas.filemgr.catalog.datasource.cacheUpdateMinutes",
                        5L).longValue();
    }

    /**
     * <p>
     * Constructs a new Factory from the specified {@link DataSource}.
     * </p>
     * 
     * @param ds
     *            The DataSource to construct this factory from.
     */
    public DataSourceCatalogFactory(DataSource ds) {
        this.dataSource = ds;
        String validationLayerFactoryClass = System
                .getProperty("filemgr.validationLayer.factory",
                        "gov.nasa.jpl.oodt.cas.validation.DataSourceValidationLayerFactory");
        validationLayer = GenericFileManagerObjectFactory
                .getValidationLayerFromFactory(validationLayerFactoryClass);
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.filemgr.catalog.CatalogFactory#createCatalog()
     */
    public Catalog createCatalog() {
        return new DataSourceCatalog(dataSource, validationLayer, fieldIdStr,
                pageSize, cacheUpdateMinutes);
    }

}
