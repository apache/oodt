/**
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

package org.apache.oodt.pcs.tools;

//OODT imports
import org.apache.oodt.pcs.listing.ListingConf;
import org.apache.oodt.pcs.metadata.PCSMetadata;
import org.apache.oodt.pcs.query.InputFilesQuery;
import org.apache.oodt.pcs.util.FileManagerUtils;
import org.apache.oodt.cas.filemgr.metadata.CoreMetKeys;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.metadata.Metadata;

//JDK imports
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

/**
 * 
 * The PCS Long Lister tool: mimics UNIX-ls and looks up descendant metadata for
 * fields like TestTag, SubTestTag, TestCounter. Also displays Filename,
 * StartDateTime, and EndDateTime for each listed file, and any of its met keys
 * configured in the <code>pcs-ll-conf.xml</code> file.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class PCSLongLister implements PCSMetadata, CoreMetKeys {

  private FileManagerUtils fm;

  private ListingConf conf;

  public PCSLongLister(String fmUrlStr, String confFile)
      throws InstantiationException {
    fm = new FileManagerUtils(fmUrlStr);
    try {
      this.conf = new ListingConf(new File(confFile));
    } catch (FileNotFoundException e) {
      throw new InstantiationException(e.getMessage());
    }
  }

  public void doList(List prodNames) {
    if (prodNames != null && prodNames.size() > 0) {
      System.out.println(getToolHeader());
      for (Object prodName1 : prodNames) {
        String prodName = (String) prodName1;
        // check to see if the product name has a "/" in it
        // (this is true in the case of someone using */* from
        // a shell): if it does, we'll consider the prodName a
        // path, and we'll clean it using new File(prodName).getName()
        if (prodName.contains("/")) {
          // clean the prodName
          prodName = new File(prodName).getName();
        }

        Product prod = fm.safeGetProductByName(prodName);
        if (prod == null) {
          // product not cataloged
          System.out.println(prodName + "\tNot Cataloged!");
          continue;
        }
        Metadata met = fm.safeGetMetadata(prod);
        outputListingLine(met, prodName);
      }
    }

  }

  public static void main(String[] args) throws InstantiationException {
    String usage = "PCSLongLister <conf file> <fmurl> [files]";
    List fileList;

    if (args.length < 2) {
      System.err.println(usage);
      System.exit(1);
    }

    if (args.length == 2) {
      // assume they gave us the current dir "."
      // get a list of files from there
      fileList = Arrays.asList(new File(".").list());
    } else if (args.length == 3 && new File(args[2]).isDirectory()) {
      fileList = Arrays.asList(new File(args[2]).list());
    } else {
      // it's going to be a list of files anyways
      // so just treat the whole argList as a list of file names
      List argList = Arrays.asList(args);
      fileList = argList.subList(2, argList.size());
    }

    PCSLongLister lister = new PCSLongLister(args[0], args[1]);
    lister.doList(fileList);

  }

  private String getToolHeader() {
    StringBuilder header = new StringBuilder();
    for (String colName : this.conf.getHeaderColKeys()) {
      header.append(this.conf.getHeaderColDisplayName(colName));
      header.append("\t");
    }
    return header.toString();
  }

  private void outputListingLine(Metadata met, String prodName) {
    StringBuilder output = new StringBuilder();
    for (String colNameKey : this.conf.getHeaderColKeys()) {
      if (!this.conf.isCollectionField(colNameKey)) {
        output.append(met.getMetadata(colNameKey));
        output.append("\t");
      } else {
        output.append(outputOrBlank(getAllProductsByTag(met, prodName,
            colNameKey)));
        output.append("\t");
      }
    }
    System.out.println(output.toString());
  }

  private static String outputOrBlank(List items) {
    if (items == null || (items.size() == 0)) {
      return "N/A";
    }

    StringBuilder buf = new StringBuilder();
    for (Object item1 : items) {
      String item = (String) item1;
      buf.append(item);
      buf.append(",");
    }

    buf.deleteCharAt(buf.length() - 1);
    return buf.toString();
  }

  private List getAllProductsByTag(Metadata met, String productName,
      String tagType) {
    if (met.containsKey(tagType)) {
      return met.getAllMetadata(tagType);
    }

    // assume now that there are no test tags
    // so what we want to do is to query the file catalog
    // for InputFiles:productName and then keep doing this until
    // we get an answer that has test tags: for each test tag (once
    // they are found), we'll collect them in a list
    List tags = new Vector();
    boolean foundTag = false;
    Stack descendants = new Stack();
    descendants.push(productName);

    while (!descendants.empty() && !foundTag) {
      String prodName = (String) descendants.pop();
      List products = fm.queryAllTypes(new InputFilesQuery(prodName, fm)
          .buildQuery(), this.conf.getExcludedTypes());

      // iterate over all the products
      // get each set of metadata
      // if you find one tag, then set foundTag = true, and we
      // break
      if (products != null && products.size() > 0) {
        for (Object product : products) {
          Product prod = (Product) product;
          Metadata prodMet = fm.safeGetMetadata(prod);

          if (prodMet.containsKey(tagType)) {
            // got one, done
            if (!foundTag) {
              foundTag = true;
            }
            if (!tags.contains(prodMet.getMetadata(tagType))) {
              tags.add(prodMet.getMetadata(tagType));
            }
          }
        }

        if (!foundTag) {
          // continue the search
          descendants.addAll(FileManagerUtils.toProductNameList(products));
        }
      }
    }

    return tags;
  }

}
