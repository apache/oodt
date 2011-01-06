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

//JDK imports
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

//OODT imports
import org.apache.oodt.cas.filemgr.catalog.Catalog;
import org.apache.oodt.cas.filemgr.catalog.DataSourceCatalogFactory;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.metadata.util.PathUtils;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Factory for constructing a special {@link DataSourceCatalog} called
 * {@link MappedDataSourceCatalog} which is able to override the default
 * policy of {@link ProductType#getName()}_metadata and {@link ProductType#getName()}_reference
 * as the underlying table names
 * </p>.
 */
public class MappedDataSourceCatalogFactory extends DataSourceCatalogFactory {

    private static final String TYPE_MAP_KEY = "org.apache.oodt.cas.filemgr."
            + "catalog.mappeddatasource.mapFile";

    protected String mapFilePath;
    
    public MappedDataSourceCatalogFactory() throws FileNotFoundException,
            IOException {
        super();
        mapFilePath = PathUtils.replaceEnvVariables(System
                .getProperty(TYPE_MAP_KEY));
    }

    public String getMapFilePath() {
		return mapFilePath;
	}

	public void setMapFilePath(String mapFilePath) {
		this.mapFilePath = mapFilePath;
	}

	/*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.catalog.DataSourceCatalogFactory#createCatalog()
     */
    @Override
    public Catalog createCatalog() {
    	try {
	        Properties props = new Properties();
	        props.load(new FileInputStream(mapFilePath));
	        return new MappedDataSourceCatalog(this.getJdbcUrl(), this.getUser(), this.getPass(), this.getDriver(), validationLayer,
                fieldIdStr, pageSize, cacheUpdateMinutes, props);
    	}catch (Exception e) {
    		e.printStackTrace();
    		return null;
    	}
    }

}
