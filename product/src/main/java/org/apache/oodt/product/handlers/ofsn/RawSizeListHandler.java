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
 *
 * Handles a listing request for the raw size of a file. This listing
 * returns a single file or directory, which will then have its size
 * computed.
 *
 * @author mattmann
 * @version $Revision$
 *
 */
public class RawSizeListHandler implements OFSNListHandler {

  /* (non-Javadoc)
   * @see org.apache.oodt.product.handlers.ofsn.OFSNListHandler#configure(java.util.Properties)
   */
  public void configure(Properties conf) {
    // TODO Auto-generated method stub
    // nothing yet

  }

  /* (non-Javadoc)
   * @see org.apache.oodt.product.handlers.ofsn.OFSNListHandler#getListing(java.lang.String)
   */
  public File[] getListing(String ofsn) throws ProductException {
    if (!new File(ofsn).exists()) {
          throw new ProductException("file: [" + ofsn
                  + "] does not exist!");
      }
      return new File[] { new File(ofsn) };
  }

}
