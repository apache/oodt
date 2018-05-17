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

package org.apache.oodt.cas.filemgr.tools;

//OODT imports

import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.filemgr.structs.exceptions.DataTransferException;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;
import org.apache.oodt.cas.filemgr.util.RpcCommunicationFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

//JDK imports

/**
 * @author woollard
 * @version $Revision$
 * 
 *          <p>
 *          A utility class that deletes products in the File Manager Catalog
 *          based on productID.
 *          </p>
 * 
 */
public class DeleteProduct {

  /* our log stream */
  private static final Logger LOG = Logger
      .getLogger(DeleteProduct.class.getName());

  private FileManagerClient client;
  /* whether or not we should commit our deletions */
  private boolean commit = true;

  public DeleteProduct(String fileManagerUrl, boolean commit) {
    this.commit = commit;

    try {
      client = RpcCommunicationFactory.createClient(new URL(fileManagerUrl));
    } catch (Exception e) {
      LOG.severe("Unable to create client: " + e.getMessage());
    }

    if (!this.commit) {
      LOG.log(Level.INFO, "Commit disabled.");
    } else {
      LOG.log(Level.INFO, "Commit enabled.");
    }
  }

  public void remove(String productId) {
    Product target = null;

    try {
      target = client.getProductById(productId);
    } catch (CatalogException e) {
      LOG.log(Level.WARNING,
          "Unable to obtain product : [" + productId + "] from file manager: ["
              + client.getFileManagerUrl() + "]: Message: " + e.getMessage());
    }

    if (target == null) {
      // could not file product
      return;
    }

    // delete references first
    Vector refs = new Vector();

    try {
      refs = (Vector) client.getProductReferences(target);
    } catch (CatalogException e) {
      LOG.log(Level.WARNING,
          "Unable to obtain references for product : [" + productId
              + "] from file manager: [" + client.getFileManagerUrl()
              + "]: Message: " + e.getMessage());
    }

    for (Object ref1 : refs) {
      Reference ref = (Reference) ref1;

      if (commit) {
        try {
          client.removeFile(
              new File(new URI(ref.getDataStoreReference())).getAbsolutePath());
        } catch (DataTransferException e) {
          LOG.log(Level.WARNING,
              "Unable to delete reference : [" + ref.getDataStoreReference()
                  + "] for product : [" + productId + "] from file manager : ["
                  + client.getFileManagerUrl() + "]: Message: "
                  + e.getMessage());
        } catch (URISyntaxException e) {
          LOG.log(Level.WARNING,
              "uri syntax exception getting file absolute path from URI: ["
                  + ref.getDataStoreReference() + "]: Message: "
                  + e.getMessage());
        }
      } else {
        LOG.log(Level.INFO,
            "Delete file: [" + ref.getDataStoreReference() + "]");
      }

    }

    if (commit) {

      // now delete product
      try {
        client.removeProduct(target);
      } catch (CatalogException e) {
        LOG.log(Level.WARNING,
            "Unable to remove product : [" + productId
                + "] from file manager: [" + client.getFileManagerUrl()
                + "]: Message: " + e.getMessage());
      }
    } else {
      LOG.log(Level.INFO, "Remote catalog entry for product: ["
          + target.getProductName() + "]");
    }

  }

  /**
   * @return the commit
   */
  public boolean isCommit() {
    return commit;
  }

  /**
   * @param commit
   *          the commit to set
   */
  public void setCommit(boolean commit) {
    this.commit = commit;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    String productId = null;
    String fileManagerUrl = null;
    boolean commitChanges = true;
    boolean readFromStdIn = false;

    String usage = "DeleteProduct --productID <product id> "
        + "--fileManagerUrl <url to file manager> [--read] [--nocommit]\n";

    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("--productID")) {
        productId = args[++i];
      } else if (args[i].equals("--fileManagerUrl")) {
        fileManagerUrl = args[++i];
      } else if (args[i].equals("--read")) {
        readFromStdIn = true;
      } else if (args[i].equals("--nocommit")) {
        commitChanges = false;
      }
    }

    if ((productId == null && !readFromStdIn) || fileManagerUrl == null) {
      System.err.println(usage);
      System.exit(1);
    }

    DeleteProduct remover = new DeleteProduct(fileManagerUrl, commitChanges);
    if (readFromStdIn) {
      List prodIds = readProdIdsFromStdin();
      for (Object prodId1 : prodIds) {
        String prodId = (String) prodId1;
        remover.remove(prodId);
      }
    } else {
      remover.remove(productId);
    }

  }

  private static List readProdIdsFromStdin() {
    List prodIds = new Vector();
    BufferedReader br;

    br = new BufferedReader(new InputStreamReader(System.in));

    String line = null;

    try {
      while ((line = br.readLine()) != null) {
        prodIds.add(line);
      }
    } catch (IOException e) {
      LOG.log(Level.WARNING, "Error reading prod id: line: [" + line
          + "]: Message: " + e.getMessage(), e);
    } finally {
      try {
        br.close();
      } catch (Exception ignore) {
      }

    }

    return prodIds;
  }

  /**
   * Not the best place to do this. But, no other option at the moment. Mandatory to close the client once done.
   * @throws IOException
   */
  @Override
  public void finalize() throws IOException {
    if (client != null) {
      client.close();
    }
  }
}
