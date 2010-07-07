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

package org.apache.oodt.cas.catalog.util;

//OODT imports
import org.apache.oodt.cas.catalog.system.Catalog;

//JDK imports
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

//SPRING imports
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * 
 * 
 * 
 * @author bfoster
 *
 */
public class SpringUtils {

	private static Logger LOG = Logger.getLogger(SpringUtils.class.getName());
	
	public static HashSet<Catalog> loadCatalogs(String catalogBeanRepo) throws Exception {
        FileSystemXmlApplicationContext appContext = new FileSystemXmlApplicationContext(catalogBeanRepo);
        Map<String, Catalog> catalogsMap = appContext.getBeansOfType(Catalog.class);
        HashSet<Catalog> catalogs = new HashSet<Catalog>();
        for (String key : catalogsMap.keySet()) {
        	Catalog curCatalog = catalogsMap.get(key);
        	LOG.log(Level.INFO, "Loading catalog configuration for Catalog: '" + curCatalog + "'");
        	if (catalogs.contains(curCatalog))
        		throw new Exception("Catalog URN : '" + curCatalog + "' conflicts with another Catalog's URN.  **NOTE: URNs are created based on the following rule: urn:<namespace>:<id or name (if set)>");
        	catalogs.add(curCatalog);
        }
        return catalogs;
	}
	
}
