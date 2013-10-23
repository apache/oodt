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


package org.apache.oodt.product.handlers.ofsn;

//OODT imports
import org.apache.oodt.product.handlers.ofsn.util.OFSNUtils;
import org.apache.oodt.product.ProductException;

//JDK imports
import java.io.File;
import java.util.Properties;


/**
 * 
 * Generates a listing of the size of a remote file identified by its OFSN as a
 * zip would be.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class SingleZipFileListHandler implements OFSNListHandler {

  private String cacheRoot;

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.product.handlers.ofsn.OFSNListHandler#configure(java.
   * util.Properties)
   */
  public void configure(Properties conf) {
    // TODO Auto-generated method stub

    this.cacheRoot = conf.getProperty("cacheDirRoot");

    if (this.cacheRoot == null) {
      this.cacheRoot = "/tmp";
    }

    if (!this.cacheRoot.endsWith("/")) {
      this.cacheRoot += "/";
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.product.handlers.ofsn.OFSNListHandler#getListing(java
   * .lang.String)
   */
  public File[] getListing(String ofsn) throws ProductException {
    if (!new File(ofsn).exists()) {
      throw new ProductException("file: [" + ofsn + "] does not exist!");
    }

    String zipFilePath = this.cacheRoot + new File(ofsn).getName() + ".zip";
    File zipFile = OFSNUtils.buildZipFile(zipFilePath, new File[] { new File(
        ofsn) });
    return new File[] { zipFile };
  }

}
