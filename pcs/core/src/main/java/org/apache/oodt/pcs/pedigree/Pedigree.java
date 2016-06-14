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

package org.apache.oodt.pcs.pedigree;

//OODT imports
import org.apache.oodt.pcs.metadata.PCSMetadata;
import org.apache.oodt.pcs.metadata.PCSConfigMetadata;
import org.apache.oodt.pcs.query.FilenameQuery;
import org.apache.oodt.pcs.query.InputFilesQuery;
import org.apache.oodt.pcs.query.JobIdQuery;
import org.apache.oodt.pcs.util.FileManagerUtils;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.metadata.Metadata;

//JDK imports
import java.util.List;
import java.util.Stack;
import java.util.Vector;

/**
 * 
 * A class to provide pedigre tracking for PCS {@link Product}s.
 * 
 * @author mattmann
 * @version $Revision$
 */
public class Pedigree implements PCSMetadata, PCSConfigMetadata {

  /* our file manager interface */
  private FileManagerUtils fm;

  /* should we include not cataloged products in the pedigree? */
  private boolean listNotCataloged = false;

  /* are there any product types that we should exclude from the pedigree? */
  private List prodTypeExcludeList;

  /**
   * 
   * Constructs a new Pedigree object that will connect to the file manager at
   * the given {@link URL} specified by the <code>fmUrlStr</code> string
   * parameter.
   * 
   * @param fmUrlStr
   *          The String {@link URL} of the file manager to connect to.
   * @param listNotCat
   *          Whether or not we should include non-cataloged products in the
   *          pedigree.
   * @param excludeList
   *          A {@link List} of String {@link ProductType} names that should be
   *          excluded from the Pedigree.
   */
  public Pedigree(String fmUrlStr, boolean listNotCat, List excludeList) {
    this(new FileManagerUtils(FileManagerUtils.safeGetUrlFromString(fmUrlStr)),
        listNotCat, excludeList);

  }

  /**
   * 
   * Constructs a new Pedigree object that will connect to the file manager
   * specified by the {@link FileManagerUtils} object passed in.
   * 
   * @param fm
   *          The PCS interface to the File Manager.
   * @param listNotCat
   *          Whether or not we should include non-cataloged products in the
   *          pedigree.
   * @param excludeList
   *          A {@link List} of String {@link ProductType} names that should be
   *          excluded from the Pedigree.
   */
  public Pedigree(FileManagerUtils fm, boolean listNotCat, List excludeList) {
    this.fm = fm;
    this.listNotCataloged = listNotCat;
    this.prodTypeExcludeList = excludeList;
  }

  /**
   * Performs a full pedigree of the specified {@link Product} <code>orig</code>
   * . If <code>upstream</code> is set to true, an upstream pedigree is
   * performed, otherwise, a downstream pedigree is performed.
   * 
   * @param orig
   *          The {@link Product} to perform a pedigree of.
   * @param upstream
   *          Whether or not we should do an upstream (true) or downstream
   *          (false) pedigree.
   * @return A {@link PedigreeTree} containing the Pedigree of a given product.
   */
  public PedigreeTree doPedigree(Product orig, boolean upstream) {
    List pedProds;
    PedigreeTreeNode origRoot = PedigreeTreeNode
        .getPedigreeTreeNodeFromProduct(orig, null);

    Stack roots = new Stack();
    roots.add(origRoot);

    do {

      PedigreeTreeNode currRoot = (PedigreeTreeNode) roots.pop();

      if (upstream) {
        pedProds = getUpstreamPedigreedProducts(currRoot.getNodeProduct());
      } else {
        pedProds = getDownstreamPedigreedProducts(currRoot.getNodeProduct());
      }

      if (pedProds != null && pedProds.size() > 0) {
        for (Object pedProd : pedProds) {
          Product p = (Product) pedProd;
          if (p.getProductName().equals(
              currRoot.getNodeProduct().getProductName())) {
            // don't allow for the same pedigreed product to be
            // added to the list
            continue;
          }
          PedigreeTreeNode prodNode = PedigreeTreeNode
              .getPedigreeTreeNodeFromProduct(p, currRoot);
          roots.add(prodNode);
        }
      }

    } while (!roots.empty());

    return new PedigreeTree(origRoot);
  }

  /**
   * Returns the most direct ancestors (a {@link List} of {@link Product}s)
   * upstream from the given {@link Product} named <code>orig</code>.
   * 
   * @param orig
   *          The {@link Product} to get direct upstream relatives of.
   * @return A {@link List} of {@link Product}s directly upstream from the given
   *         {@link Product}.
   */
  public List getUpstreamPedigreedProducts(Product orig) {
    if (orig == null || (orig.getProductType() == null) ||
        (orig.getProductType().getName() == null) || (orig.getProductType().getName().equals(UNKNOWN))) {
      return new Vector();
    }
    Metadata pMet = fm.safeGetMetadata(orig);
    return getProducts(pMet.getAllMetadata(INPUT_FILES));

  }

  /**
   * Returns the most direct ancestors (a {@link List} of {@link Product}s)
   * downstream from the given {@link Product} named <code>orig</code>.
   * 
   * @param orig
   *          The {@link Product} to get direct downstream relatives of.
   * @return A {@link List} of {@link Product}s directly downstream from the
   *         given {@link Product}.
   */
  public List getDownstreamPedigreedProducts(Product orig) {
    return fm.queryAllTypes(new InputFilesQuery(orig.getProductName(), fm)
        .buildQuery(), this.prodTypeExcludeList);
  }

  /**
   * Gets all associated {@link Product}s with the provided
   * {@link WorkflowInstance} identified by its ID (the <code>wInstId</code>
   * string parameter).
   * 
   * @param wInstId
   *          The ID of the {@link WorkflowInstance} to look up the
   *          {@link Product}s for.
   * @return A {@link List} of {@link Product}s associated with the provided
   *         Workflow Instance ID.
   */
  public List getWorkflowInstProds(String wInstId) {
    return fm.queryAllTypes(new JobIdQuery(wInstId, fm).buildQuery(),
        this.prodTypeExcludeList);
  }

  private List getProducts(List prodNames) {
    if (prodNames == null || (prodNames.size() == 0)) {
      return new Vector();
    }

    List prods = new Vector(prodNames.size());

    for (Object prodName1 : prodNames) {
      String prodName = (String) prodName1;
      List prodList = fm.queryAllTypes(new FilenameQuery(prodName, fm)
          .buildQuery(), this.prodTypeExcludeList);
      if (prodList != null && prodList.size() > 0) {
        prods.add((Product) prodList.get(0));
      } else {
        if (this.listNotCataloged) {
          // create a new product and add it
          prods.add(Product.getDefaultFlatProduct(prodName, UNKNOWN));
        }
      }
    }

    return prods;
  }

}
