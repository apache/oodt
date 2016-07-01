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

//OODT static imports
import static org.apache.oodt.cas.pge.metadata.PgeTaskMetKeys.QUERY_CLIENT_TRANSFER_SERVICE_FACTORY;
import static org.apache.oodt.cas.pge.metadata.PgeTaskMetKeys.QUERY_FILE_MANAGER_URL;

//OODT imports
import org.apache.oodt.cas.filemgr.datatransfer.DataTransfer;
import org.apache.oodt.cas.filemgr.datatransfer.RemoteDataTransferFactory;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.exceptions.DataTransferException;
import org.apache.oodt.cas.filemgr.util.GenericFileManagerObjectFactory;
import org.apache.oodt.cas.pge.metadata.PgeMetadata;

//Google imports
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;

//JDK imports
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A {@link FileStager} which uses a FileManager {@link DataTransferer}.
 *
 * @author bfoster (Brian Foster)
 */
public class FileManagerFileStager extends FileStager {

   @Override
   public void stageFile(URI stageFile, File destDir,
         PgeMetadata pgeMetadata, Logger logger) throws IOException, DataTransferException, InstantiationException {
      DataTransfer dataTransferer = createDataTransfer(pgeMetadata, logger);
      logger.log(Level.INFO, "Using DataTransfer ["
               + dataTransferer.getClass().getCanonicalName() + "]");
      setFileManagerUrl(dataTransferer, pgeMetadata, logger);
      dataTransferer.retrieveProduct(createProduct(stageFile), destDir);
   }

   @VisibleForTesting
   static DataTransfer createDataTransfer(PgeMetadata pgeMetadata,
         Logger logger) {
      if (pgeMetadata.getMetadata(QUERY_CLIENT_TRANSFER_SERVICE_FACTORY) != null) {
         return GenericFileManagerObjectFactory
               .getDataTransferServiceFromFactory(pgeMetadata
                     .getMetadata(QUERY_CLIENT_TRANSFER_SERVICE_FACTORY));
      } else {
         logger.log(Level.INFO, "Using default DataTransferer");
         return new RemoteDataTransferFactory().createDataTransfer();
      }
   }

   @VisibleForTesting
   static void setFileManagerUrl(DataTransfer dataTransferer,
         PgeMetadata pgeMetadata, Logger logger) throws MalformedURLException {
      String filemgrUrl = pgeMetadata.getMetadata(QUERY_FILE_MANAGER_URL);
      if (filemgrUrl != null) {
         dataTransferer.setFileManagerUrl(new URL(filemgrUrl));
      } else {
         logger.log(Level.WARNING, "Metadata field [" + QUERY_FILE_MANAGER_URL
               + "] was not set, if DataTranferer requires filemgr server,"
               + " your transfers will fail");
      }
   }

   @VisibleForTesting
   static Product createProduct(URI path) {
      Product product = new Product();
      Reference reference = new Reference();
      reference.setDataStoreReference(path.toString());
      product.setProductReferences(Lists.newArrayList(reference));
      return product;
   }
}
