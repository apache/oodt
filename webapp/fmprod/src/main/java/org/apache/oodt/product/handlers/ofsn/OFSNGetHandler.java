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
import java.util.Properties;

//OODT imports
import org.apache.oodt.product.ProductException;

/**
 * 
 * The default OFSN handler for getting remote data.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public interface OFSNGetHandler {

  /**
   * Retrieves a chunk of data from the remote file.
   * 
   * @param filepath
   *          The path to the remote file.
   * @param offset
   *          The offset in the remote data to retrieve.
   * @param length
   *          The length of data to read
   * @return The byte[] data, read, or null otherwise.
   * @throws ProductException
   *           If any error occurs.
   */
  byte[] retrieveChunk(String filepath, long offset, int length)
      throws ProductException;

  /**
   * Returns the size of the remote data, which may be the entire file in
   * question, or some subset/transformation on it.
   * 
   * @param filepath
   *          The remote file in question.
   * @return The size of the remote file, potentially after a remote
   *         transformation has occured.
   */
  long sizeOf(String filepath);

  /**
   * Configures this handler with the provided configuration stored in a
   * {@link Properties} object.
   * 
   * @param conf
   *          The configuration for this list handler.
   */
  void configure(Properties conf);

}
