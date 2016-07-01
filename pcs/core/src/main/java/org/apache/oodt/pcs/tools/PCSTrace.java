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

//JDK imports
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;
import org.apache.oodt.pcs.metadata.PCSConfigMetadata;
import org.apache.oodt.pcs.metadata.PCSMetadata;
import org.apache.oodt.pcs.pedigree.Pedigree;
import org.apache.oodt.pcs.pedigree.PedigreeTree;
import org.apache.oodt.pcs.util.FileManagerUtils;
import org.apache.oodt.pcs.util.WorkflowManagerUtils;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

//OODT imports

/**
 * 
 * A program to trace the history of a particular PCS product.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public final class PCSTrace implements PCSMetadata, PCSConfigMetadata {

  /* our interface to the file manager */
  private FileManagerUtils fm;

  /* our interface to the workflow manager */
  private WorkflowManagerUtils wm;

  /* whether or not we should list non-cataloged products in pedigree */
  private boolean listNotCataloged = false;

  /* exclude list */
  private List excludeTypeList;

  private static final String REPORT_LINE_SEPARATOR = "-------------------------------------------------- ";

  public PCSTrace(URL wmgrUrl, URL fmUrl) {
    fm = new FileManagerUtils(fmUrl);
    wm = new org.apache.oodt.pcs.util.WorkflowManagerUtils(wmgrUrl);
  }

  public PCSTrace(String wmgrUrlStr, String fmUrlStr) {
    this(FileManagerUtils.safeGetUrlFromString(wmgrUrlStr), FileManagerUtils
        .safeGetUrlFromString(fmUrlStr));
  }

  /**
   * @param excludeTypeList
   *          the excludeTypeList to set
   */
  public void setExcludeTypeList(List excludeTypeList) {
    this.excludeTypeList = excludeTypeList;
  }

  /**
   * @return the excludeTypeList
   */
  public List getExcludeTypeList() {
    return excludeTypeList;
  }

  /**
   * Outputs a trace report in the following format:
   * 
   * Product Name: <i>oco_L1aRad09088_0939838838.hdf</i>
   * -------------------------------------------------- Location:
   * /preflt/jpl/Rad/9088/l1a/oco_L1aRad09088_0939838838.hdf
   * 
   * Metadata:
   * 
   * &lt;Met Field Name&gt; =&gt; &lt;Met Field Value&gt; &lt;Met Field
   * Name2&gt; =&gt; &lt;Met Field Value2&gt;
   * 
   * --------------------------------------------------- Generated from
   * workflows:
   * 
   * CPT Pipeline: ID: [99299-add99-92983-9d9d9-0099d] State: FINISHED
   * Associated products:
   * 
   * oco_RawL017_93983993938.pkt oco_RawL032_93939388383.pkt
   * oco_RawL033_93838383838.pkt oco_RawL034_93938378585.pkt
   * 
   * --------------------------------------------------- Full lineage:
   * 
   * Upstream: CPTOverview_98383388d.txt=&gt;oco_RawL017_93983993938.pkt=&gt;
   * oco_RawL032_93939388383
   * .pkt=&gt;oco_RawL033_93838383838.pkt=&gt;oco_RawL034_93938378585.pkt
   * 
   * Downstream: oco_L1BRad_19889_983838383.hdf
   * ---------------------------------------------------
   * 
   * 
   * @param productName
   *          The name of the product to trace lineage for.
   */
  public void doTrace(String productName) {

    Product prod = fm.safeGetProductByName(productName);

    Pedigree pedigree = new Pedigree(fm, this.listNotCataloged,
        this.excludeTypeList);

    System.out.println("");
    System.out.println(REPORT_LINE_SEPARATOR);
    System.out.println("Product: " + prod.getProductName());
    System.out.println(REPORT_LINE_SEPARATOR);

    prod.setProductReferences(fm.safeGetProductReferences(prod));
    System.out.println("Location: " + fm.getFilePath(prod));
    System.out.println(REPORT_LINE_SEPARATOR);
    System.out.println("Metadata: ");
    Metadata met = fm.safeGetMetadata(prod);
    if (met != null && met.getMap() != null
        && met.getMap().keySet().size() > 0) {
      for (String key : met.getMap().keySet()) {
        List vals = met.getAllMetadata(key);
        System.out.println(key + "=>" + vals);
      }
    }

    System.out.println(REPORT_LINE_SEPARATOR);
    System.out.println("Generated from workflow:");
    System.out.println("");
    WorkflowInstance inst;

    try {
      inst = getWorkflowInstanceById(wm.safeGetWorkflowInstances(), met
          .getMetadata(JOB_ID));

      System.out.println(inst.getWorkflow().getName() + ": ID: ["
          + inst.getId() + "]");
      System.out.println("Status: " + inst.getStatus());
      List wInstProds = pedigree.getWorkflowInstProds(inst.getId());
      if (wInstProds != null && wInstProds.size() > 0) {
        System.out.println("Associated products:");
        System.out.println("");
        for (Object wInstProd1 : wInstProds) {
          Product wInstProd = (Product) wInstProd1;
          System.out.println(wInstProd.getProductName());
        }
      }
    } catch (Exception e) {
      System.out.println("Unable to obtain workflow instance for product!");
    }

    System.out.println(REPORT_LINE_SEPARATOR);
    System.out.println("Full lineage:");
    System.out.println("");
    System.out.println("Downstream:");

    PedigreeTree downstreamLineageTree = pedigree.doPedigree(prod, false);
    downstreamLineageTree.printPedigreeTree();

    System.out.println("");
    System.out.println("Upstream:");
    PedigreeTree upstreamLineageTree = pedigree.doPedigree(prod, true);
    upstreamLineageTree.printPedigreeTree();

  }

  public void enableNonCatalogProductsInPed() {
    this.listNotCataloged = true;
  }

  /**
   * 
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) {
    String productName = null;
    String workflowUrlStr = null, filemgrUrlStr = null;
    boolean enableNotCat = false;
    List exList = null;

    String usage = "PCSTrace --fm <url> --wm <url> --product <name> "
        + " [--enableNonCat] [--exclude <type name 1>,<type name 2>...,<type name n>]\n";

    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("--fm")) {
        filemgrUrlStr = args[++i];
      } else if (args[i].equals("--wm")) {
        workflowUrlStr = args[++i];
      } else if (args[i].equals("--product")) {
        productName = args[++i];
      } else if (args[i].equals("--enableNonCat")) {
        enableNotCat = true;
      } else if (args[i].equals("--exclude")) {
        String[] excludeTypes = args[++i].split(",");
        exList = Arrays.asList(excludeTypes);
      }

    }

    if (productName == null || workflowUrlStr == null || filemgrUrlStr == null) {
      System.err.println(usage);
      System.exit(1);
    }

    PCSTrace tracer = new PCSTrace(workflowUrlStr, filemgrUrlStr);
    if (enableNotCat) {
      tracer.enableNonCatalogProductsInPed();
    }
    if (exList != null) {
      tracer.setExcludeTypeList(exList);
    }
    tracer.doTrace(productName);

  }

  private WorkflowInstance getWorkflowInstanceById(List insts, String id) {
    if (insts == null) {
      return null;
    }

    for (Object inst1 : insts) {
      WorkflowInstance inst = (WorkflowInstance) inst1;
      if (inst.getId().equals(id)) {
        return inst;
      }
    }

    return null;
  }

}
