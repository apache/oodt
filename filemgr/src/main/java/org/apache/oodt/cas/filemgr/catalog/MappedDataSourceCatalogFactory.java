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
import org.apache.oodt.cas.metadata.util.PathUtils;

//JDK imports
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;



/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Factory for constructing a special {@link DataSourceCatalog} called
 * {@link MappedDataSourceCatalog} which is able to override the default
 * policy of {@link org.apache.oodt.cas.filemgr.structs.ProductType#getName()}_metadata
 * and {@link org.apache.oodt.cas.filemgr.structs.ProductType#getName()}_reference
 * as the underlying table names
 * </p>.
 */
public class MappedDataSourceCatalogFactory extends DataSourceCatalogFactory {

    protected Properties typeMap;

    private static final String TYPE_MAP_KEY = "org.apache.oodt.cas.filemgr."
            + "catalog.mappeddatasource.mapFile";

    public MappedDataSourceCatalogFactory() throws
        IOException {
        super();
        String mapFilePath = PathUtils.replaceEnvVariables(System
                .getProperty(TYPE_MAP_KEY));
        Properties props = new Properties();
        InputStream is = new FileInputStream(mapFilePath);
        try {
            props.load(is);
        }
        finally{
            is.close();
        }
        this.typeMap = props;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.catalog.DataSourceCatalogFactory#createCatalog()
     */
    @Override
    public Catalog createCatalog() {
        return new MappedDataSourceCatalog(dataSource, validationLayer,
                fieldIdStr, pageSize, cacheUpdateMinutes, typeMap);
    }

}
