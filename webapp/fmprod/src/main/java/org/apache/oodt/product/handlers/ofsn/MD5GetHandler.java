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

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.apache.oodt.product.ProductException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

//APACHE imports
//OODT imports

/**
 * 
 * A {@link OFSNGetHandler} to perform an MD5 for a file on the server side.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class MD5GetHandler implements OFSNGetHandler {

  private MessageDigest md = null;
  private static Logger LOG = Logger.getLogger(MD5GetHandler.class.getName());
  public MD5GetHandler() throws InstantiationException {
    try {
      this.md = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      LOG.log(Level.SEVERE, e.getMessage());
      throw new InstantiationException(e.getMessage());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.product.handlers.ofsn.OFSNListHandler#configure(java.
   * util.Properties)
   */
  public void configure(Properties conf) {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.product.handlers.ofsn.OFSNGetHandler#retrieveChunk(java
   * .lang.String, long, int)
   */
  public byte[] retrieveChunk(String filepath, long offset, int length)
      throws ProductException {
    try {
      String hash = this.hashData(FileUtils.readFileToByteArray(new File(
          filepath)));
      byte[] retBytes = new byte[length];
      byte[] hashBytes = hash.getBytes();      
      ByteArrayInputStream is = new ByteArrayInputStream(hashBytes);
      is.skip(offset);
      is.read(retBytes, 0, length);
      return retBytes;
    } catch (IOException e) {
      LOG.log(Level.SEVERE, e.getMessage());
      throw new ProductException("Error reading bytes from file: [" + filepath
          + "] MD5: Message: " + e.getMessage());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.product.handlers.ofsn.OFSNGetHandler#sizeOf(java.lang
   * .String)
   */
  public long sizeOf(String filepath) {
    try {
      String hash = this.hashData(FileUtils.readFileToByteArray(new File(
          filepath)));
      return hash.getBytes().length;
    } catch (IOException e) {
      LOG.log(Level.SEVERE, e.getMessage());
      return -1;
    }
  }

  /**
   * Use this method ot generate a test MD5 of a provide {@link File} specified
   * in args[0].
   * 
   * @param args
   *          Only need to specify 1 arg, the full path to the {@link File} to
   *          MD5.
   * @throws Exception
   *           If any error occurs.
   */
  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.err.println("MD5GetHandler <file>");
      System.exit(1);
    }

    String filepath = args[0];
    String hashString = new MD5GetHandler().hashData(FileUtils
        .readFileToByteArray(new File(filepath)));
    System.out.println(hashString);
  }

  private String hashData(byte[] dataToHash) {
    this.md.update(dataToHash, 0, dataToHash.length);
    byte[] hash = this.md.digest();
    return new String(Hex.encodeHex(hash));
  }

}
