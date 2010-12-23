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
package org.apache.oodt.cas.filemgr.util;

//JDK imports
import java.util.HashSet;
import java.util.Map;

//Spring imports
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * 
 * @author bfoster
 *
 */
public class SpringUtils {

	public static HashSet<?> loadBeans(String beanRepo) throws Exception {
		return loadBeans(beanRepo, Object.class);
	}
	
	public static <T> HashSet<? extends T> loadBeans(String beanRepo, Class<T> classFilter) throws Exception {
        FileSystemXmlApplicationContext appContext = new FileSystemXmlApplicationContext(beanRepo);
        Map<String, T> catalogsMap = appContext.getBeansOfType(classFilter);
        HashSet<T> catalogs = new HashSet<T>();
        for (String key : catalogsMap.keySet()) {
        	T curCatalog = catalogsMap.get(key);
        	catalogs.add(curCatalog);
        }
        return catalogs;
	}
	
	
}
