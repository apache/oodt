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

//JDK imports

import org.apache.oodt.product.ProductException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports

/**
 * 
 * Standard way of retrieving a file without performing 
 * any transformation on it.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class StdOFSNGetHandler implements OFSNGetHandler {
  private static Logger LOG = Logger.getLogger(StdOFSNGetHandler.class.getName());
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.product.handlers.ofsn.OFSNGetHandler#retrieveChunk(java
   * .lang.String, long, int)
   */
  public byte[] retrieveChunk(String filepath, long offset, int length)
      throws ProductException {
    InputStream in = null;
    byte[] buf = null;

    try {
      in = new FileInputStream(new File(filepath));

      buf = new byte[length];
      int numRead;
      long numSkipped;
      numSkipped = in.skip(offset);
      if (numSkipped != offset) {
        throw new ProductException("Was not able to skip: [" + offset
            + "] bytes into product: num actually skipped: [" + numSkipped
            + "]");
      }

      numRead = in.read(buf, 0, length);

      if (numRead != length) {
        throw new ProductException("Was not able to read: [" + length
            + "] bytes from product: num actually read: [" + numRead + "]");
      }
    } catch (IOException e) {
      LOG.log(Level.SEVERE, e.getMessage());
      throw new ProductException("IO exception retrieving chunk of product: ["
          + filepath + "]: Message: " + e.getMessage());
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (Exception ignore) {
        }

      }
    }

    return buf;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.product.handlers.ofsn.OFSNGetHandler#sizeOf(java.lang
   * .String)
   */
  public long sizeOf(String filepath) {
     return new File(filepath).length();
  }

  /* (non-Javadoc)
   * @see org.apache.oodt.product.handlers.ofsn.OFSNGetHandler#configure(java.util.Properties)
   */
  public void configure(Properties conf) {
    // no properties to configure    
  }

}
