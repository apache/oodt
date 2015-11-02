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

import java.io.File;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public class TestMappedDataSourceCatalog extends TestDataSourceCatalog {

    private static Logger LOG = Logger.getLogger(TestDataSourceCatalog.class.getName());
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Get additional resources and set additional properties.
        Properties properties = new Properties(System.getProperties());
        URL url = this.getClass().getResource(
          "/testcatalog.typemap.properties");
        properties.setProperty(
            "org.apache.oodt.cas.filemgr.catalog.mappeddatasource.mapFile",
            new File(url.getFile()).getAbsolutePath());
        System.setProperties(properties);

        setCatalog(getCatalog());

    }

    protected Catalog getCatalog() {
        try {
            return new MappedDataSourceCatalogFactory().createCatalog();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.catalog.TestDataSourceCatalog#getSchemaPath()
     */
    @Override
    protected String getSchemaPath() {
        URL url = this.getClass().getResource(
            "/testcat.mapped.sql");

        return new File(url.getFile()).getAbsolutePath();
    }
}
