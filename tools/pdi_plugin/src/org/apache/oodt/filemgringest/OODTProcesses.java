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

package org.apache.oodt.filemgringest;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.filemgr.structs.exceptions.IngestException;
import org.apache.oodt.cas.metadata.Metadata;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by bugg on 07/03/14.
 */
public class OODTProcesses {

    private static final Logger LOG = Logger.getLogger(OODTProcesses.class.getName());

    private String filemgrUrl;

    public boolean isAlreadyInDatabase(OODTConfig config, String filename) throws CatalogException {
        return config.getIngester() != null && config.getIngester().hasProduct(
                config.getFmUrl(), filename);
    }

    Metadata getMetadata(String json) {
        Gson gson=new Gson();
        Hashtable table = new Hashtable<String, String>();
        table = (Hashtable<String, String>) gson.fromJson(json, table.getClass());

        ArrayList l = (ArrayList) table.get("data");
        LinkedTreeMap d = (LinkedTreeMap) l.get(0);
        Hashtable<String,Object> ht = new Hashtable<String,Object>();



        ht.putAll(d);
        Metadata m = new Metadata();

        m.addMetadata(ht);
        return m;

    }

    boolean ingest(OODTConfig config, File product, Metadata productMetdata) throws IngestException {
       // try {
            String productId = config.getIngester().ingest(config.getFmUrl(),
                    product, productMetdata);
/*            LOG.log(Level.INFO, "Successfully ingested product: [" + product
                    + "]: product id: " + productId);
        } catch (Exception e) {
            LOG.log(Level.WARNING,
                    "ProductCrawler: Exception ingesting product: [" + product
                            + "]: Message: " + e.getMessage()
                            + ": attempting to continue crawling", e);
            return false;
        }*/
        return true;
    }


}
