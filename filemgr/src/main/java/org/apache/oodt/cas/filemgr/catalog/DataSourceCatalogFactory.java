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
public class DataSourceCatalogFactory implements CatalogFactory {
    /* our data source */
    private DataSource dataSource = null;

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
    public DataSourceCatalogFactory() {
        jdbcUrl = PathUtils
                .replaceEnvVariables(System
                        .getProperty("org.apache.oodt.cas.filemgr.catalog.datasource.jdbc.url", "some_datasource_url"));
        user = PathUtils
                .replaceEnvVariables(System
                        .getProperty("org.apache.oodt.cas.filemgr.catalog.datasource.jdbc.user", "user"));
        pass = PathUtils
                .replaceEnvVariables(System
                        .getProperty("org.apache.oodt.cas.filemgr.catalog.datasource.jdbc.pass", "pass"));
        driver = PathUtils
                .replaceEnvVariables(System
                        .getProperty("org.apache.oodt.cas.filemgr.catalog.datasource.jdbc.driver", "some_driver_class"));

        validationLayerFactoryClass = System
                .getProperty("filemgr.validationLayer.factory",
                        "org.apache.oodt.cas.filemgr.validation.DataSourceValidationLayerFactory");
        fieldIdStr = Boolean
                .getBoolean("org.apache.oodt.cas.filemgr.catalog.datasource.quoteFields");

        pageSize = Integer
                .getInteger(
                        "org.apache.oodt.cas.filemgr.catalog.datasource.pageSize",
                        20).intValue();
        cacheUpdateMinutes = Long
                .getLong(
                        "org.apache.oodt.cas.filemgr.catalog.datasource.cacheUpdateMinutes",
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
        this.validationLayerFactoryClass = System
                .getProperty("filemgr.validationLayer.factory",
                        "org.apache.oodt.cas.validation.DataSourceValidationLayerFactory");
    }

    
    
    public boolean isFieldIdStr() {
		return fieldIdStr;
	}

	public void setFieldIdStr(boolean fieldIdStr) {
		this.fieldIdStr = fieldIdStr;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public long getCacheUpdateMinutes() {
		return cacheUpdateMinutes;
	}

	public void setCacheUpdateMinutes(long cacheUpdateMinutes) {
		this.cacheUpdateMinutes = cacheUpdateMinutes;
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
		if (validationLayer == null)
	        validationLayer = GenericFileManagerObjectFactory.getValidationLayerFromFactory(validationLayerFactoryClass);
        return new DataSourceCatalog(this.jdbcUrl, this.user, this.pass, this.driver, validationLayer, fieldIdStr,
                pageSize, cacheUpdateMinutes);
    }

}
