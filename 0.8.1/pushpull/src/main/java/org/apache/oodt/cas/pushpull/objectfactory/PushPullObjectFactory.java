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


package org.apache.oodt.cas.pushpull.objectfactory;

//OODT imports
import org.apache.oodt.cas.filemgr.ingest.Cache;
import org.apache.oodt.cas.filemgr.ingest.CacheFactory;
import org.apache.oodt.cas.filemgr.ingest.Ingester;

//JDK imports
import java.lang.reflect.InvocationTargetException;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public class PushPullObjectFactory {

    private PushPullObjectFactory() throws InstantiationException {
        throw new InstantiationException("Don't construct factory classes!");
    }

    public static <T> T createNewInstance(Class<T> clazz) throws InstantiationException {
    	try {
			return clazz.newInstance();
		} catch (Exception e) {
            throw new InstantiationException(
                    "Failed to create new object : "
                            + e.getMessage());
		}
    }
    
    public static Ingester createIngester(String ingesterClass,
            String cacheFactoryClass) throws InstantiationException,
            IllegalAccessException, ClassNotFoundException,
            IllegalArgumentException, SecurityException,
            InvocationTargetException, NoSuchMethodException {
        String dataTransferFactory = System
                .getProperty("org.apache.oodt.cas.filemgr.datatransfer.factory");
        System.out.println("TRANSFER: " + dataTransferFactory);
        if (cacheFactoryClass == null || cacheFactoryClass.equals("")) {
            return (Ingester) Class.forName(ingesterClass).getConstructor(
                    dataTransferFactory.getClass()).newInstance(
                    dataTransferFactory);
        } else {
            Class<CacheFactory> cacheFactory = (Class<CacheFactory>) Class
                    .forName(cacheFactoryClass);
            Cache cache = cacheFactory.newInstance().createCache();
            return (Ingester) Class.forName(ingesterClass).getConstructor(
                    dataTransferFactory.getClass(), cache.getClass())
                    .newInstance(dataTransferFactory, cache);
        }
    }

}
