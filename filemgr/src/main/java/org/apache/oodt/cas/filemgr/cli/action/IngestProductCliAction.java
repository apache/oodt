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
package org.apache.oodt.cas.filemgr.cli.action;

//OODT static imports

import org.apache.commons.lang.Validate;
import org.apache.oodt.cas.cli.exception.CmdLineActionException;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;
import org.apache.oodt.cas.metadata.SerializableMetadata;

import com.google.common.collect.Lists;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.oodt.cas.filemgr.structs.Product.STRUCTURE_HIERARCHICAL;
import static org.apache.oodt.cas.filemgr.structs.Product.STRUCTURE_STREAM;
import static org.apache.oodt.cas.filemgr.util.GenericFileManagerObjectFactory.getDataTransferServiceFromFactory;
import static org.apache.oodt.cas.filemgr.versioning.VersioningUtils.addRefsFromUris;
import static org.apache.oodt.cas.filemgr.versioning.VersioningUtils.getURIsFromDir;

//JDK imports
//Apache imports
//OODT imports
//Google imports

/**
 * A {@link CmdLineAction} which ingests a {@link Product}.
 * 
 * @author bfoster (Brian Foster)
 */
public class IngestProductCliAction extends FileManagerCliAction {
   private static Logger LOG = Logger.getLogger(IngestProductCliAction.class.getName());
   private String productName;
   private String productStructure;
   private String productTypeName;
   private String metadataFile;
   private String dataTransferer;
   private List<String> references;

   @Override
   public void execute(ActionMessagePrinter printer)
         throws CmdLineActionException {
      try (FileManagerClient client = getClient()) {
         Validate.notNull(productName, "Must specify productName");
         Validate.notNull(productStructure, "Must specify productStructure");
         Validate.notNull(productTypeName, "Must specify productTypeName");
         Validate.notNull(metadataFile, "Must specify metadataFile");
         Validate.notNull(references, "Must specify references");

         ProductType pt = client.getProductTypeByName(productTypeName);
         if (pt == null) {
            throw new Exception("FileManager returned null ProductType");
         }

         Product product = new Product();
         product.setProductName(productName);
         product.setProductStructure(productStructure);
         product.setProductType(pt);

         if (dataTransferer != null) {
            client.setDataTransfer(getDataTransferServiceFromFactory(dataTransferer));
         }

         // need to build up the ref uri list in case the Product structure
         // is heirarchical
         if (product.getProductStructure().equals(STRUCTURE_HIERARCHICAL)) {
            List<String> uriRefs = Lists.newArrayList();
            URI hierRefUri = getUri(references.get(0));
            uriRefs.add(hierRefUri.toString());
            uriRefs.addAll(getURIsFromDir(new File(hierRefUri.getPath())));
            references = uriRefs;
         } else if (product.getProductStructure().equals(STRUCTURE_STREAM)) {
            List<String> uriRefs = Lists.newArrayList();
            for (String ref : references) {
               URI uri = URI.create(ref);
               if (!uri.getScheme().equals("stream")) {
                  throw new IllegalArgumentException("Streaming data must use 'stream' scheme not " + uri.getScheme());
               }
               uriRefs.add(uri.toString());
            }
            references = uriRefs;
         } else {
            List<String> uriRefs = Lists.newArrayList();
            for (String reference : references) {
               uriRefs.add(getUri(reference).toString());
            }
            references = uriRefs;
         }

         // add Product References from the URI list
         addRefsFromUris(product, references);

         printer.println("ingestProduct: Result: "
               + client.ingestProduct(product,
                     new SerializableMetadata(getUri(metadataFile).toURL()
                           .openStream()), dataTransferer != null));
      } catch (Exception e) {
         LOG.log(Level.SEVERE, e.getMessage());
         throw new CmdLineActionException("Failed to ingest product '"
               + productName + "' : " + e.getMessage(), e);
      }
   }

   public void setProductName(String productName) {
      this.productName = productName;
   }

   public void setProductStructure(String productStructure) {
      this.productStructure = productStructure;
   }

   public void setProductTypeName(String productTypeName) {
      this.productTypeName = productTypeName;
   }

   public void setMetadataFile(String metadataFile) {
      this.metadataFile = metadataFile;
   }

   public void setDataTransferer(String dataTransferer) {
      this.dataTransferer = dataTransferer;
   }

   public void setReferences(List<String> references) {
      this.references = references;
   }

   private URI getUri(String filePath) {
      if (new File(filePath).exists()) {
         return new File(filePath).getAbsoluteFile().toURI();
      } else {
         return URI.create(filePath);
      }
   }
}
