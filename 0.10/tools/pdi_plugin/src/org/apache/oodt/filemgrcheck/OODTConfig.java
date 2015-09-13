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

package org.apache.oodt.filemgrcheck;

import org.apache.oodt.cas.filemgr.ingest.Ingester;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by bugg on 07/03/14.
 */
public class OODTConfig {

    private static final Logger LOG = Logger.getLogger(OODTConfig.class.getName());

    private URL fmUrl;

    private Ingester ingester;

    public Ingester getIngester() {
        return this.ingester;
    }

    public URL getFmUrl(){
        return this.fmUrl;
    }


    void loadIngester(String fmUrlStr) throws InstantiationException {
        try {

            String ingesterClass = "org.apache.oodt.cas.filemgr.ingest.StdIngester";

            String dataTransferClass ="org.apache.oodt.cas.filemgr.datatransfer.LocalDataTransferFactory";

            String cacheFactoryClass = null;
             //"org.apache.oodt.cas.filemgr.ingest.cache.factory";

            LOG.log(Level.INFO, "Configuring and building ingester: ["
                        + ingesterClass + "]: data transfer: ["
                        + dataTransferClass + "]: to ingest to file manager: ["
                        + fmUrlStr + "]");

           if (cacheFactoryClass != null) {
                    LOG.log(Level.INFO, "Configuring Ingester cache: ["
                            + cacheFactoryClass + "]");
           }

           this.ingester = PushPullObjectFactory.createIngester(
                  ingesterClass, cacheFactoryClass);

           this.fmUrl = safeGetUrlFromString(fmUrlStr);

           } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        } catch (InvocationTargetException e1) {
            e1.printStackTrace();
        } catch (NoSuchMethodException e1) {
            e1.printStackTrace();
        } catch (IllegalAccessException e1) {
            e1.printStackTrace();
        }

    }


    private static URL safeGetUrlFromString(String urlStr) {
        URL url = null;

        try {
            url = new URL(urlStr);
        } catch (MalformedURLException e) {
            LOG.log(Level.WARNING, "Unable to generate url from url string: ["
                    + urlStr + "]: Message: " + e.getMessage());
        }

        return url;
    }
}
