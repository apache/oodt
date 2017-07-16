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
import java.io.File;
import java.util.Properties;

//OODT imports
import org.apache.oodt.product.ProductException;

/**
 * <p>
 * Classes that implement this interface define how to return file listings on a
 * remote server from an <code>ofsn</code>.
 * </p>
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public interface OFSNListHandler {

  /**
   * Handlers that implement this method take an <code>O</code>nline
   * <code>F</code>ile <code>S</code>pecification <code>N</code>ame and return
   * back a listing of files on the remote server.
   * 
   * @param ofsn
   *          The OFSN path to list files from.
   * @return An array of {@link File} objects.
   * @throws ProductException
   *           If any error occurs performing the listing on the server side.
   */
  File[] getListing(String ofsn) throws ProductException;

  /**
   * Configures this handler with the provided configuration stored in a
   * {@link Properties} object.
   * 
   * @param conf
   *          The configuration for this list handler.
   */
  void configure(Properties conf);
}
