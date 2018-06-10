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
package org.apache.oodt.cas.pge.staging;

import org.apache.commons.lang.Validate;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.structs.exceptions.DataTransferException;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;
import org.apache.oodt.cas.filemgr.util.RpcCommunicationFactory;
import org.apache.oodt.cas.pge.config.FileStagingInfo;
import org.apache.oodt.cas.pge.exceptions.PGEException;
import org.apache.oodt.cas.pge.metadata.PgeMetadata;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import static org.apache.oodt.cas.pge.metadata.PgeTaskMetKeys.QUERY_FILE_MANAGER_URL;

/**
 * Responsible for transferring Product files to a directory accessible by
 * by CAS-PGE.
 *
 * @author bfoster (Brian Foster)
 */
public abstract class FileStager {

   public void stageFiles(FileStagingInfo fileStagingInfo, PgeMetadata pgeMetadata, Logger logger)
       throws PGEException, CatalogException, URISyntaxException, IOException, ConnectionException,
       InstantiationException, DataTransferException {
      logger.info("Creating staging directory [{}]", fileStagingInfo.getStagingDir());
      new File(fileStagingInfo.getStagingDir()).mkdirs();
      for (String file : fileStagingInfo.getFilePaths()) {
         File fileHandle = new File(file);
         if (fileStagingInfo.isForceStaging() || !fileHandle.exists()) {
            logger.info("Staging file [{}] to directory [{}]", file,  fileStagingInfo.getStagingDir());
            stageFile(asURI(file), new File(fileStagingInfo.getStagingDir()), pgeMetadata, logger);
         }
      }
      if (!fileStagingInfo.getProductIds().isEmpty()) {
         FileManagerClient fmClient = createFileManagerClient(pgeMetadata);
         for (String productId : fileStagingInfo.getProductIds()) {
            logger.info("Staging product [{}] to directory [{}]", productId, fileStagingInfo.getStagingDir());
            for (URI uri : getProductReferences(productId, fmClient)) {
               logger.info("Staging product [{}] reference [{}] to directory [{}]",
                       productId, uri, fileStagingInfo.getStagingDir());
               stageFile(uri, new File(fileStagingInfo.getStagingDir()), pgeMetadata, logger);
            }
         }
      }
   }

   @VisibleForTesting
   static FileManagerClient createFileManagerClient(PgeMetadata pgeMetadata)
       throws PGEException, MalformedURLException, ConnectionException {
      String filemgrUrl = pgeMetadata.getMetadata(QUERY_FILE_MANAGER_URL);
      if (filemgrUrl == null) {
         throw new PGEException("Must specify [" + QUERY_FILE_MANAGER_URL
               + "] if you want to stage product IDs");
      }
      return RpcCommunicationFactory.createClient(new URL(filemgrUrl));
   }

   @VisibleForTesting
   static List<URI> getProductReferences(
         String productId, FileManagerClient fmClient)
         throws URISyntaxException, CatalogException {
      List<URI> files = Lists.newArrayList();
      Product product = new Product();
      product.setProductId(productId);
      List<Reference> refs = fmClient.getProductReferences(product);
      for (Reference ref : refs) {
         files.add(new URI(ref.getDataStoreReference()));
      }
      return files;
   }

   @VisibleForTesting
   static URI asURI(String path) {
      Validate.notNull(path, "path must not be null");

      URI uri = URI.create(path);
      if (uri.getScheme() == null) {
         uri = URI.create("file://" + new File(path).getAbsolutePath());
      }
      return uri;
   }

   protected abstract void stageFile(URI stageFile, File destDir,
         PgeMetadata pgeMetadata, Logger logger)
       throws IOException, DataTransferException, InstantiationException;
}
