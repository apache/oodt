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

package org.apache.oodt.filemgrget;

import com.google.gson.Gson;
import org.apache.oodt.cas.filemgr.datatransfer.DataTransfer;
import org.apache.oodt.cas.filemgr.datatransfer.LocalDataTransferFactory;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductPage;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.filemgr.structs.exceptions.DataTransferException;
import org.apache.oodt.cas.filemgr.system.XmlRpcFileManagerClient;
import org.apache.oodt.cas.metadata.Metadata;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Copyright 2014 OSBI Ltd
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class OODTProcesses {


    public Map<String, Map<String, String>> getAllProducts(OODTConfig config, String productTypeName) throws Exception {
        XmlRpcFileManagerClient client = config.getXMLRpcClient();
        ProductType type = client.getProductTypeByName(productTypeName);
        if (type == null) {
            throw new Exception("FileManager returned null ProductType");
        }
        ProductPage firstPage = client.getFirstPage(type);
        if (firstPage == null) {
            throw new Exception("FileManager returned null product page");
        }

        Map<String, Map<String, String>> o = new HashMap<String, Map<String, String>>();

        for (int pid = 0; pid < firstPage.getTotalPages(); pid++) {
            if (pid > 0) {
                firstPage = client.getNextPage(type, firstPage);
            }
            for (Product p : firstPage.getPageProducts()) {
                Metadata met = client.getMetadata(p);

                Map<String, String> h = new HashMap<String, String>();
                h.put("name", p.getProductName());
                h.put("type", p.getProductType().getName());
                h.put("structure", p.getProductStructure());
                h.put("transferstatus", p.getTransferStatus());
                Gson g = new Gson();
                String json = g.toJson(met.getHashtable());
                h.put("metadata", json);
                o.put(p.getProductId(), h);

            }

        }
        return o;

    }


    public String getProductByID(OODTConfig config, String id) throws CatalogException, IOException, DataTransferException {
        Product product = config.getXMLRpcClient().getProductById(id);
        product.setProductReferences(config.getXMLRpcClient().getProductReferences(product));
        LocalDataTransferFactory ldtf = new LocalDataTransferFactory();
        DataTransfer dt = ldtf.createDataTransfer();
        String rand = UUID.randomUUID().toString();
        File theDir = new File("/tmp/oodt/" + rand);
        boolean mkdir = false;
        if (!theDir.exists()) {
            mkdir = theDir.mkdir();
        }
        if (mkdir) {
            dt.retrieveProduct(product, new File("/tmp/oodt/" + rand));
            return "/tmp/oodt/" + rand + "/" + product.getProductName();
        } else {
            return null;
        }
    }

    public String getProductByName(OODTConfig config, String name) throws CatalogException, IOException, DataTransferException {
        Product product = config.getXMLRpcClient().getProductByName(name);
        product.setProductReferences(config.getXMLRpcClient().getProductReferences(product));
        LocalDataTransferFactory ldtf = new LocalDataTransferFactory();
        DataTransfer dt = ldtf.createDataTransfer();
        String rand = UUID.randomUUID().toString();
        File theDir = new File("/tmp/oodt/" + rand);
        boolean mkdir = false;
        if (!theDir.exists()) {
            mkdir = theDir.mkdir();
        }
        if (mkdir) {
            dt.retrieveProduct(product, new File("/tmp/oodt/" + rand));
            return "/tmp/oodt/" + rand + "/" + product.getProductName();
        } else {
            return null;
        }

    }

}
