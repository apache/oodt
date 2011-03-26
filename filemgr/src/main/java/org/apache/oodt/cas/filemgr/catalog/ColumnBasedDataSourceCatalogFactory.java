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
package org.apache.oodt.cas.filemgr.catalog;

//OODT imports
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.oodt.cas.filemgr.util.GenericFileManagerObjectFactory;
import org.apache.oodt.cas.filemgr.validation.ValidationLayer;
import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.oodt.commons.database.DatabaseConnectionBuilder;

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
public class ColumnBasedDataSourceCatalogFactory implements CatalogFactory {

	protected DataSource ds;
	
    /* our validation layer */
    protected ValidationLayer validationLayer = null;

    /*
     * page size: size of the pages used by the catalog to paginate products
     * back to the user
     */
    protected int pageSize = -1;
    
    protected String jdbcUrl;
    protected String user;
    protected String pass;
    protected String driver;
    
    protected String validationLayerFactoryClass;
    
    /**
     * 
     * <p>
     * Default Constructor
     * </p>.
     */
    public ColumnBasedDataSourceCatalogFactory() {
        jdbcUrl = PathUtils
                .replaceEnvVariables(System
                        .getProperty("org.apache.oodt.cas.filemgr.catalog.column.based.datasource.jdbc.url", "some_datasource_url"));
        user = PathUtils
                .replaceEnvVariables(System
                        .getProperty("org.apache.oodt.cas.filemgr.catalog.column.based.datasource.jdbc.user", "user"));
        pass = PathUtils
                .replaceEnvVariables(System
                        .getProperty("org.apache.oodt.cas.filemgr.catalog.column.based.datasource.jdbc.pass", "pass"));
        driver = PathUtils
                .replaceEnvVariables(System
                        .getProperty("org.apache.oodt.cas.filemgr.catalog.column.based.datasource.jdbc.driver", "some_driver_class"));
        
		String dbIntTypes = PathUtils
				.replaceEnvVariables(System
						.getProperty("org.apache.oodt.cas.filemgr.catalog.column.based.datasource.db.int.types"));
        
        validationLayerFactoryClass = System
                .getProperty("filemgr.validationLayer.factory",
                        "org.apache.oodt.cas.filemgr.validation.DataSourceValidationLayerFactory");

        pageSize = Integer
                .getInteger(
                        "org.apache.oodt.cas.filemgr.catalog.column.based.datasource.pageSize",
                        20).intValue();
    }

    /**
     * <p>
     * Constructs a new Factory from the specified {@link DataSource}.
     * </p>
     * 
     * @param ds
     *            The DataSource to construct this factory from.
     */
    public ColumnBasedDataSourceCatalogFactory(DataSource ds) {
        this.ds = ds;
        this.validationLayerFactoryClass = System
                .getProperty("filemgr.validationLayer.factory",
                        "org.apache.oodt.cas.validation.DataSourceValidationLayerFactory");
    }

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public String getJdbcUrl() {
		return jdbcUrl;
	}

	public void setJdbcUrl(String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public void setValidationLayer(ValidationLayer validationLayer) {
		this.validationLayer = validationLayer;
	}

	/*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.catalog.CatalogFactory#createCatalog()
     */
    public Catalog createCatalog() {
		if (this.validationLayer == null)
	        this.validationLayer = GenericFileManagerObjectFactory.getValidationLayerFromFactory(validationLayerFactoryClass);
		if (this.ds == null)
			this.ds = DatabaseConnectionBuilder.buildDataSource(user, pass,driver, jdbcUrl);
        return new ColumnBasedDataSourceCatalog(this.ds, validationLayer, pageSize);
    }

}
