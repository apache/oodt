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

package org.apache.oodt.cas.filemgr.datatransfer;

//APACHE Imports
import org.apache.commons.io.FileUtils;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.structs.exceptions.DataTransferException;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;
import org.apache.oodt.cas.filemgr.util.RpcCommunicationFactory;
import org.apache.oodt.cas.filemgr.versioning.VersioningUtils;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.apache.tika.mime.MimeTypesFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Vector;

//OODT imports
//JDK imports

/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 *          <p>
 *          An implementation of the {@link DataTransfer} interface that moves
 *          products that are available via URIs on the same machine, through an
 *          NFS mounted disk, or via the locally mounted file repository.
 *          </p>
 * 
 */
public class LocalDataTransferer implements DataTransfer {

   /* our log stream */
   private static final Logger LOG = LoggerFactory.getLogger(LocalDataTransferer.class);

   /* file manager client */
   private FileManagerClient client = null;

   /**
    * <p>
    * Default Constructor
    * </p>
    */
   public LocalDataTransferer() {
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.apache.oodt.cas.filemgr.datatransfer.DataTransfer#setFileManagerUrl
    * (java.net.URL)
    */
   public void setFileManagerUrl(URL url) {
      try {
         client = RpcCommunicationFactory.createClient(url);
         LOG.info("Local Data Transfer to: [{}] enabled", client.getFileManagerUrl());
      } catch (ConnectionException e) {
          LOG.warn("ConnectionException for filemgr: [{}]: {}", url, e.getMessage(), e);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.apache.oodt.cas.datatransfer.DataTransfer#transferProduct(org.apache
    * .oodt.cas.data.structs.Product)
    */
   public void transferProduct(Product product) throws DataTransferException,
         IOException {
      // check whether or not it's a set of files, or it's actually a dir
      // structure
      if (product.getProductStructure().equals(Product.STRUCTURE_HIERARCHICAL)) {
         try {
            moveDirToProductRepo(product);
         } catch (URISyntaxException e) {
            LOG.warn("URI Syntax Exception when moving dir [{}]: {}", product.getProductReferences().get(0).getOrigReference(), e.getMessage(), e);
            throw new DataTransferException(e);
         }
      } else if (product.getProductStructure().equals(Product.STRUCTURE_FLAT)) {
         try {
            moveFilesToProductRepo(product);
         } catch (URISyntaxException e) {
            LOG.warn("URI Syntax Exception when moving files: {}", e.getMessage(), e);
            throw new DataTransferException(e);
         }
      } else if (product.getProductStructure().equals(Product.STRUCTURE_STREAM)) {
            LOG.info("Streaming products are not moved.");
      } else {
         throw new DataTransferException(
               "Cannot transfer product on unknown ProductStructure: "
                     + product.getProductStructure());
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.apache.oodt.cas.filemgr.datatransfer.DataTransfer#retrieveProduct(org.
    * apache.oodt.cas.filemgr.structs.Product, java.io.File)
    */
   public void retrieveProduct(Product product, File directory)
         throws DataTransferException, IOException {
      // check whether or not it's a set of files, or it's actually a dir
      // structure
      if (product.getProductStructure().equals(Product.STRUCTURE_HIERARCHICAL)) {
         try {
            copyDirToDir(product, directory);
         } catch (URISyntaxException e) {
            LOG.warn("URI Syntax Exception when moving dir: [{}]: {}", product.getProductReferences().get(0).getDataStoreReference(), e.getMessage(), e);
            throw new DataTransferException(e);
         }
      } else if (product.getProductStructure().equals(Product.STRUCTURE_FLAT)) {
         try {
            copyFilesToDir(product, directory);
         } catch (URISyntaxException e) {
             LOG.warn("URI Syntax Exception when moving files: {}", e.getMessage(), e);
            throw new DataTransferException(e);
         }
      } else if (product.getProductStructure().equals(Product.STRUCTURE_STREAM)) {
         LOG.info("Streaming products are not moved.");
      } else {
         throw new DataTransferException(
               "Cannot transfer product on unknown ProductStructure: "
                     + product.getProductStructure());
      }
   }

   @Override
   public void deleteProduct(Product product) throws DataTransferException, IOException {
     for (Reference ref : product.getProductReferences()) {
       String u;
       try {
          u = URI.create(ref.getDataStoreReference()).toURL().getPath();
       }
       catch (IllegalArgumentException e) {
         u = URI.create("file://"+ref.getDataStoreReference()).toURL().getPath();
       }
       File dataFile = new File(u);
       if (!dataFile.delete()) {
        throw new IOException(String.format("Failed to delete file %s - delete returned false",
            dataFile));
       }
     }
   }
   
   /**
    * @param args
    */
   public static void main(String[] args) throws DataTransferException,
         IOException, URISyntaxException {
      String usage = "LocalFileTransfer --productName <name> --productRepo <repo> [--dir <dirRef>] [--files <origRef 1>...<origRef N>]\n";

      MimeTypes mimeTypeRepo;
      try {
         mimeTypeRepo = MimeTypesFactory
               .create(System
                     .getProperty("org.apache.oodt.cas.filemgr.mime.type.repository"));
      } catch (MimeTypeException e) {
         LOG.error(e.getMessage(), e);
         throw new IOException(e.getMessage());
      }

      String productName = null;
      String productRepo = null;
      String transferType = null;
      Reference dirReference = null;

      List<Reference> fileReferences = null;

      for (int i = 0; i < args.length; i++) {
         if (args[i].equals("--dir")) {
            transferType = "dir";
            dirReference = new Reference();
            dirReference.setOrigReference(new File(new URI(args[++i])).toURI()
                  .toString());
            LOG.info("LocalFileTransfer.main: Generated orig reference: {}", dirReference.getOrigReference());
         } else if (args[i].equals("--files")) {
            transferType = "files";
            fileReferences = new Vector<Reference>();
            for (int j = i + 1; j < args.length; j++) {
               LOG.info("LocalFileTransfer.main: Adding file ref: {}", args[j]);
               fileReferences.add(new Reference(args[j], null,
                     new File(args[j]).length(), mimeTypeRepo
                           .getMimeType(args[j])));
            }
         } else if (args[i].equals("--productName")) {
            productName = args[++i];
         } else if (args[i].equals("--productRepo")) {
            productRepo = args[++i];
         }
      }

      if (transferType == null || (((transferType.equals("dir") && dirReference == null) || (
          transferType.equals("files") && fileReferences == null) || (!(
          transferType.equals("dir") || transferType
              .equals("files"))) || productName == null || productRepo == null))) {
         System.err.println(usage);
         System.exit(1);
      }

      // construct a new Product
      Product p = new Product();
      p.setProductName(productName);

      if (transferType.equals("dir")) {
         p.setProductStructure(Product.STRUCTURE_HIERARCHICAL);
         dirReference.setDataStoreReference(new File(new URI(productRepo))
               .toURI().toURL().toExternalForm()
               + URLEncoder.encode(p.getProductName(), "UTF-8") + "/");
         p.getProductReferences().add(dirReference);
         /* we'll do a simple versioning scheme ourselves: no versioning! */
         p.getProductReferences().addAll(
               VersioningUtils.getReferencesFromDir(new File(new URI(
                     dirReference.getOrigReference()))));
         VersioningUtils.createBasicDataStoreRefsHierarchical(p
               .getProductReferences());
      } else if (transferType.equals("files")) {
         p.setProductStructure("Flat");
         p.getProductReferences().addAll(fileReferences);
         VersioningUtils.createBasicDataStoreRefsFlat(productName, productRepo,
               p.getProductReferences());
      }

      DataTransfer transfer = new LocalDataTransferer();
      transfer.transferProduct(p);

   }

   private void copyDirToDir(Product product, File directory)
         throws IOException, URISyntaxException {
      Reference dirRef = product.getProductReferences().get(0);
      LOG.info("LocalDataTransfer: Staging Directory: {} into directory: {}", dirRef.getDataStoreReference(), directory.getAbsolutePath());

     for (Reference r : product.getProductReferences()) {
       File fileRef = new File(new URI(r.getDataStoreReference()));

       if (fileRef.isFile()) {
         copyFile(r, directory);
       } else if (fileRef.isDirectory()
                  && (fileRef.list() != null && fileRef.list().length == 0)) {
         // if it's a directory and it doesn't exist yet, we should
         // create it
         // just in case there's no files in it
         File dest = new File(directory, fileRef.getName());
         if (!new File(new URI(dest.getAbsolutePath())).exists()) {
           LOG.info("Directory: [{}] doesn't exist: creating it", dest.getAbsolutePath());
           try {
             FileUtils.forceMkdir(new File(new URI(dest.getAbsolutePath())));
           } catch (IOException e) {
             LOG.warn("Unable to create directory: [{}] in local data transfer: {}", dest.getAbsolutePath(), e.getMessage(), e);

           }
         }
       }
     }
   }

   private void moveDirToProductRepo(Product product) throws IOException,
         URISyntaxException {
      Reference dirRef = product.getProductReferences().get(0);
      LOG.info("LocalDataTransfer: Moving Directory: {} to: {}", dirRef.getOrigReference(), dirRef.getDataStoreReference());

      // notify the file manager that we started
      quietNotifyTransferProduct(product);

     for (Reference r : product.getProductReferences()) {
       File fileRef = new File(new URI(r.getOrigReference()));

       if (fileRef.isFile()) {
         moveFile(r, false);
       } else if (fileRef.isDirectory()
                  && (fileRef.list() != null && fileRef.list().length == 0)) {
         // if it's a directory and it doesn't exist yet, we should
         // create it
         // just in case there's no files in it
         if (!new File(new URI(r.getDataStoreReference())).exists()) {
           LOG.info("Directory: [{}] doesn't exist: creating it", r.getDataStoreReference());
           try {
             FileUtils.forceMkdir(new File(new URI(r.getDataStoreReference())));
           } catch (IOException e) {
             LOG.warn("Unable to create directory: [{}] in local data transfer: {}", r.getDataStoreReference(), e.getMessage(), e);
           }
         }
       }
     }

      // notify the file manager that we're done
      quietNotifyProductTransferComplete(product);

   }

   private void moveFilesToProductRepo(Product product) throws IOException,
         URISyntaxException {
      List<Reference> refs = product.getProductReferences();

      // notify the file manager that we started
      quietNotifyTransferProduct(product);

     for (Reference r : refs) {
       moveFile(r, true);
     }

      // notify the file manager that we're done
      quietNotifyProductTransferComplete(product);
   }

   private void copyFilesToDir(Product product, File directory)
         throws IOException, URISyntaxException {
      List<Reference> refs = product.getProductReferences();
     for (Reference r : refs) {
       copyFile(r, directory);
     }
   }

   private void moveFile(Reference r, boolean log) throws IOException,
         URISyntaxException {
      if (log) {
         LOG.info("LocalDataTransfer: Moving File: {} to: {}", r.getOrigReference(), r.getDataStoreReference());
      }
      File srcFileRef = new File(new URI(r.getOrigReference()));
      File destFileRef = new File(new URI(r.getDataStoreReference()));

      FileUtils.copyFile(srcFileRef, destFileRef);
   }

   private void copyFile(Reference r, File directory) throws IOException,
         URISyntaxException {
      File srcFileRef = new File(new URI(r.getDataStoreReference()));
      LOG.info("LocalDataTransfer: Copying File: {} to file: {}/{}", r.getDataStoreReference(), directory.getAbsolutePath(), srcFileRef.getName());
      FileUtils.copyFile(srcFileRef, new File(directory, srcFileRef.getName()));
   }

   private void quietNotifyTransferProduct(Product p) {
      if (client == null) {
         LOG.warn("File Manager service not defined: this transfer will not be tracked");
         return;
      }

      try {
         client.transferringProduct(p);
      } catch (DataTransferException e) {
         LOG.warn("Error notifying file manager of product transfer initiation for product: [{}]: {}", p.getProductId(), e.getMessage(), e);
      }
   }

   private void quietNotifyProductTransferComplete(Product p) {
      if (client == null) {
         LOG.warn("File Manager service not defined: this transfer will not be tracked");
         return;
      }

      try {
         client.removeProductTransferStatus(p);
      } catch (DataTransferException e) {
         LOG.warn("Error notifying file manager of product transfer completion for product: [{}]: {}", p.getProductId(), e.getMessage(), e);
      }
   }

    @Override
    public void finalize() throws IOException {
        if (client != null) {
            client.close();
        }
    }
}
