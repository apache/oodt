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
import org.apache.oodt.cas.filemgr.structs.Product;

//JDK imports
import java.util.List;
import java.util.Vector;

/**
 * 
 * A node in the {@link PedigreeTree}.
 * 
 * @author mattmann
 * @version $Revision$
 */
public class PedigreeTreeNode {

  private Product prod;

  private List children;

  private PedigreeTreeNode parent;

  public PedigreeTreeNode(PedigreeTreeNode parent) {
    this(parent, null);
  }

  public PedigreeTreeNode(PedigreeTreeNode parent, Product prod) {
    this(parent, new Vector(), prod);
  }

  public PedigreeTreeNode(PedigreeTreeNode parent, List children, Product prod) {
    this.parent = parent;
    if (this.parent != null) {
      this.parent.addChild(this);
    }
    this.children = children;
    this.prod = prod;
  }

  public void setParent(PedigreeTreeNode parent) {
    this.parent = parent;
  }

  public void addChild(PedigreeTreeNode child) {
    this.children.add(child);
  }

  public int getNumChildren() {
    if (this.children == null) {
      return 0;
    }
    return this.children.size();
  }

  public PedigreeTreeNode getChildAt(int idx) {
    return (PedigreeTreeNode) this.children.get(idx);
  }

  public Product getNodeProduct() {
    return this.prod;
  }

  public void setNodeProduct(Product prod) {
    this.prod = prod;
  }

  public static PedigreeTreeNode getPedigreeTreeNodeFromProduct(Product p,
      PedigreeTreeNode parent) {
    PedigreeTreeNode node = new PedigreeTreeNode(parent);
    node.setNodeProduct(p);
    return node;
  }

}
