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

//OODT imports
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.structs.exceptions.DataTransferException;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;
import org.apache.oodt.cas.filemgr.util.RpcCommunicationFactory;


//JDK imports
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 *          <p>
 *          An implementation of the {@link DataTransfer} interface that
 *          transfers files to a remote file manager over XML-RPC, using the
 *          File Manager Client.
 *          </p>
 * 
 */
public class RemoteDataTransferer implements DataTransfer {

  public static final int NUM_BYTES = 1024;
  /*
        * the url pointer to the file manager that we'll remotely transfer the file
        * to
        */
   private URL fileManagerUrl = null;

   /*
    * the size of the chunks that files should be transferred over XML-RPC using
    */
   private int chunkSize = 1024;

   /* our file manager client */
   private FileManagerClient client = null;

   /* our log stream */
   private static final Logger LOG = Logger
         .getLogger(RemoteDataTransferer.class.getName());

   /**
     * 
     */
   public RemoteDataTransferer(int chunkSz) {
      this.chunkSize = chunkSz;
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
         this.fileManagerUrl = url;
         LOG.log(Level.INFO, "Remote Data Transfer to: ["
               + client.getFileManagerUrl().toString() + "] enabled");
      } catch (ConnectionException e) {
         LOG.log(Level.WARNING, "Connection exception for filemgr: [" + url
               + "]");
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.apache.oodt.cas.filemgr.datatransfer.DataTransfer#transferProduct(
    * org.apache.oodt.cas.filemgr.structs.Product)
    */
   public void transferProduct(Product product) throws DataTransferException,
         IOException {

      if (fileManagerUrl == null) {
         throw new DataTransferException(
               "No file manager url specified for remote data transfer: cannot transfer product: ["
                     + product.getProductName() + "]!");
      }

      quietNotifyTransferProduct(product);

      // for each file reference, transfer the file to the remote file manager
     for (Reference r : product.getProductReferences()) {
       // test whether or not the reference is a directory or a file
       File refFile;
       try {
         refFile = new File(new URI(r.getOrigReference()));
       } catch (URISyntaxException e) {
         LOG.log(Level.WARNING,
             "Unable to test if reference: [" + r.getOrigReference()
             + "] is a directory: skipping it");
         continue;
       }

       if (!refFile.isDirectory()) {
         LOG.log(Level.FINE, "Reference: [" + r.getOrigReference()
                             + "] is file: transferring it");

         try {
           remoteTransfer(r, product);
         } catch (URISyntaxException e) {
           LOG.log(Level.WARNING,
               "Error transferring file: [" + r.getOrigReference()
               + "]: URISyntaxException: " + e.getMessage());
         }
       } else {
         LOG.log(
             Level.FINE,
             "RemoteTransfer: skipping reference: ["
             + refFile.getAbsolutePath() + "] of product: ["
             + product.getProductName() + "]: ref is a directory");
       }
     }

      quietNotifyProductTransferComplete(product);

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
      for (Reference reference : product.getProductReferences()) {
         FileOutputStream fOut = null;
         try {
            File dataStoreFile = new File(new URI(
                  reference.getDataStoreReference()));
            File dest = new File(directory, dataStoreFile.getName());
            fOut = new FileOutputStream(dest, false);
            LOG.log(
                  Level.INFO,
                  "RemoteDataTransfer: Copying File: " + "fmp:"
                        + dataStoreFile.getAbsolutePath() + " to " + "file:"
                        + dest.getAbsolutePath());
            byte[] fileData;
            int offset = 0;
            while (true) {
               fileData = (byte[]) client.retrieveFile(
                     dataStoreFile.getAbsolutePath(), offset, NUM_BYTES);
               if (fileData.length <= 0) {
                 break;
               }
               fOut.write(fileData);
               if (fileData.length < NUM_BYTES) {
                 break;
               }
               offset += NUM_BYTES;
            }
         } catch (Exception e) {
            throw new DataTransferException("", e);
         } finally {
            try {
               fOut.close();
            } catch (Exception ignored) {
            }
         }
      }
   }

   @Override
   public void deleteProduct(Product product) throws DataTransferException, IOException {
     for (Reference ref : product.getProductReferences()) {
       File dataFile = new File(URI.create(ref.getDataStoreReference()).toURL().getPath());
       if (!dataFile.delete()) {
        throw new IOException(String.format("Failed to delete file %s - delete returned false",
            dataFile));
       }
     }
   }
   
   private void remoteTransfer(Reference reference, Product product)
         throws URISyntaxException {
      // get the file path
      File origFile = new File(new URI(reference.getOrigReference()));
      File destFile = new File(new URI(reference.getDataStoreReference()));
      String origFilePath = origFile.getAbsolutePath();
      String destFilePath = destFile.getAbsolutePath();

      // read the file in chunk by chunk

      byte[] buf = new byte[chunkSize];

      FileInputStream is = null;

      try {
         is = new FileInputStream(origFile);
         int offset = 0;
         int numBytes;

         // remove the file if it already exists: this operation
         // is an overwrite
         if (!client.removeFile(destFilePath)) {
            LOG.log(Level.WARNING,
                  "RemoteDataTransfer: attempt to perform overwrite of dest file: ["
                        + destFilePath + "] failed");
         }

         while ((numBytes = is.read(buf, offset, chunkSize)) != -1) {
            client.transferFile(destFilePath, buf, offset, numBytes);
         }
      } catch (IOException e) {
         LOG.log(Level.WARNING,
               "Error opening input stream to read file to transfer: Message: "
                     + e.getMessage());
      } catch (DataTransferException e) {
         LOG.log(
               Level.WARNING,
               "DataTransferException when transfering file: [" + origFilePath
                     + "] to [" + destFilePath + "]: Message: "
                     + e.getMessage());
      } finally {
         if (is != null) {
            try {
               is.close();
            } catch (Exception ignore) {
            }

         }
      }
   }

   private void quietNotifyTransferProduct(Product p) {
      try {
         client.transferringProduct(p);
      } catch (DataTransferException e) {
         LOG.log(Level.SEVERE, e.getMessage());
         LOG.log(Level.WARNING,
               "Error notifying file manager of product transfer initiation for product: ["
                     + p.getProductId() + "]: Message: " + e.getMessage());
      }
   }

   private void quietNotifyProductTransferComplete(Product p) {
      try {
         client.removeProductTransferStatus(p);
      } catch (DataTransferException e) {
         LOG.log(Level.SEVERE, e.getMessage());
         LOG.log(Level.WARNING,
               "Error notifying file manager of product transfer completion for product: ["
                     + p.getProductId() + "]: Message: " + e.getMessage());
      }
   }

}
