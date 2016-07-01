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
import org.apache.oodt.commons.database.DatabaseConnectionBuilder;
import org.apache.oodt.cas.filemgr.util.GenericFileManagerObjectFactory;
import org.apache.oodt.cas.filemgr.validation.ValidationLayer;
import org.apache.oodt.cas.metadata.util.PathUtils;

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
  public static final int VAL = 20;
  public static final long VAL1 = 5L;
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
    
    /* Whether or not to enforce strict definition of metadata fields:
  	 * 'lenient=false' means that all metadata fields need to be explicitly defined in the XML configuration file */
    protected boolean lenientFields = false;
  	
    /* Flag to indicate whether the "product_id" key type should be treated as a string */
    protected boolean productIdString = false;

    protected boolean orderedValues = false;

    /**
     * <p>
     * Default Constructor
     * </p>.
     */
    public DataSourceCatalogFactory() {
    	
    	  // instantiate data source
        String jdbcUrl, user, pass, driver;

        jdbcUrl = PathUtils
                .replaceEnvVariables(System
                        .getProperty("org.apache.oodt.cas.filemgr.catalog.datasource.jdbc.url"));
        user = PathUtils
                .replaceEnvVariables(System
                        .getProperty("org.apache.oodt.cas.filemgr.catalog.datasource.jdbc.user"));
        pass = PathUtils
                .replaceEnvVariables(System
                        .getProperty("org.apache.oodt.cas.filemgr.catalog.datasource.jdbc.pass"));
        driver = PathUtils
                .replaceEnvVariables(System
                        .getProperty("org.apache.oodt.cas.filemgr.catalog.datasource.jdbc.driver"));

        dataSource = DatabaseConnectionBuilder.buildDataSource(user, pass,
                driver, jdbcUrl);

        this.configure();

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
        
     		this.configure();
    		
    }
    
    /** Method to configure the factory (and validation layer) from the environment properties,
     *  before any Catalog instance is created 
     **/
    private void configure() {
    
  		lenientFields = Boolean.parseBoolean( System.getProperty("org.apache.oodt.cas.filemgr.catalog.datasource.lenientFields", "false") );
  		if (!lenientFields) {
	        String validationLayerFactoryClass = System
                .getProperty("filemgr.validationLayer.factory",
                        "org.apache.oodt.cas.filemgr.validation.DataSourceValidationLayerFactory");
	        validationLayer = GenericFileManagerObjectFactory
              .getValidationLayerFromFactory(validationLayerFactoryClass);
  		} 
  		
      fieldIdStr = Boolean
      	.getBoolean("org.apache.oodt.cas.filemgr.catalog.datasource.quoteFields");

			pageSize = Integer
                .getInteger(
                    "org.apache.oodt.cas.filemgr.catalog.datasource.pageSize",
                    VAL);
			cacheUpdateMinutes = Long
                .getLong(
                    "org.apache.oodt.cas.filemgr.catalog.datasource.cacheUpdateMinutes",
                    VAL1);
			
			productIdString = Boolean.parseBoolean( 
				System.getProperty("org.apache.oodt.cas.filemgr.catalog.datasource.productId.string", "false") );
  	
			orderedValues = Boolean.parseBoolean(System.getProperty("org.apache.oodt.cas.filemgr.catalog.datasource.orderedValues"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.catalog.CatalogFactory#createCatalog()
     */
    public Catalog createCatalog() {
    	if (validationLayer==null) {
    			return new LenientDataSourceCatalog(dataSource, null, fieldIdStr,
							    pageSize, cacheUpdateMinutes, productIdString, orderedValues);
    	} else {
        return new DataSourceCatalog(dataSource, validationLayer, fieldIdStr,
				     pageSize, cacheUpdateMinutes, productIdString, orderedValues);
    	}
    }

}
