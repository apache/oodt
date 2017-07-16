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
import java.io.FileFilter;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Stack;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports

/**
 * 
 * An abstract {@link OFSNListHandler} for generating file lists based on two
 * simple flags: recurse, and crawlForDirs, indicating to crawl for directories
 * rather than files.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public abstract class AbstractCrawlLister implements OFSNListHandler {

  protected final static Logger LOG = Logger
      .getLogger(AbstractCrawlLister.class.getName());

  protected static final FileFilter FILE_FILTER = new FileFilter() {
    public boolean accept(File pathname) {
      return pathname.isFile();
    }
  };

  protected static final FileFilter DIR_FILTER = new FileFilter() {
    public boolean accept(File pathname) {
      return pathname.isDirectory();
    }
  };

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.product.handlers.ofsn.OFSNListHandler#configure(java.
   * util.Properties)
   */
  public abstract void configure(Properties conf);

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.product.handlers.ofsn.OFSNListHandler#getListing(java
   * .lang.String)
   */
  public abstract File[] getListing(String ofsn) throws ProductException;

  protected File[] crawlFiles(File dirRoot, boolean recur,
      boolean crawlForDirs) {
    if (dirRoot == null || ((!dirRoot.exists()))) {
      throw new IllegalArgumentException("dir root: [" + dirRoot
                                         + "] is null or non existant!");
    }

    List<File> fileList = new Vector<File>();

    // start crawling
    Stack<File> stack = new Stack<File>();
    stack.push(dirRoot.isDirectory() ? dirRoot : dirRoot.getParentFile());
    while (!stack.isEmpty()) {
      File dir = (File) stack.pop();
      LOG.log(Level.INFO, "OFSN: Crawling " + dir);

      File[] productFiles;
      productFiles = crawlForDirs ? dir.listFiles(DIR_FILTER) : dir.listFiles(FILE_FILTER);

      if(productFiles!=null) {
        Collections.addAll(fileList, productFiles);
      }
      if (recur) {
        File[] subdirs = dir.listFiles(DIR_FILTER);
        if (subdirs != null) {
          for (File subdir : subdirs) {
            stack.push(subdir);
          }
        }
      }
    }

    return fileList.toArray(new File[fileList.size()]);
  }

}
