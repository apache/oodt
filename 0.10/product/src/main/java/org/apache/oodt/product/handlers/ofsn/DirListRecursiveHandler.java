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
 * Generates a directory listing, recursing into the OFSN path.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class DirListRecursiveHandler extends AbstractCrawlLister {

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.product.handlers.ofsn.AbstractCrawlLister#configure(java
   * .util.Properties)
   */
  @Override
  public void configure(Properties conf) {
    // no properties yet

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.product.handlers.ofsn.AbstractCrawlLister#getListing(
   * java.lang.String)
   */
  @Override
  public File[] getListing(String ofsn) throws ProductException {
    return crawlFiles(new File(ofsn), true, true);
  }

}
