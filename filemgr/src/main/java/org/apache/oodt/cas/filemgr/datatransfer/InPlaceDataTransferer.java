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
import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.structs.exceptions.DataTransferException;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;
import org.apache.oodt.cas.filemgr.util.RpcCommunicationFactory;

//JDK imports
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.net.URL;

/** 
 * An implementation of the {@link DataTransfer} interface that leaves
 * products in the same place (i.e., "in place") and doesn't transfer
 * them at all.
 *
 * @author mattmann@apache.org (Chris Mattmann)
 */
public class InPlaceDataTransferer implements DataTransfer {

  private static final Logger LOG = Logger.getLogger(InPlaceDataTransferer.class.getName());

  private FileManagerClient client = null;

  public InPlaceDataTransferer() {}

  public void setFileManagerUrl(URL url) {
    try {
      client = RpcCommunicationFactory.createClient(url);
      LOG.log(Level.INFO, "In Place Data Transfer to: [" + client.getFileManagerUrl().toString()
          + "] enabled");
    } catch (ConnectionException e) {
      LOG.log(Level.WARNING, "Connection exception for filemgr: [" + url + "]");
    }
  }

  @Override
  public void transferProduct(Product product) throws DataTransferException, IOException {
    // do nothing
  }

  @Override
  public void retrieveProduct(Product product, File directory) throws DataTransferException,
      IOException {
    // do nothing
  }

  @Override
  public void deleteProduct(Product product) throws DataTransferException, IOException {
    // do nothing
  }
}
