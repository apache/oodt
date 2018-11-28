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

package org.apache.oodt.pcs.webcomponents.trace;

//JDK imports
import java.util.List;
import java.util.Vector;

//OODT imports
import org.apache.oodt.pcs.pedigree.PedigreeTreeNode;

//Wicket imports
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * 
 * A node wrapper around a {@link PedigreeTreeNode} that exposes the node in a
 * recursive way via Wicket and as a component of a {@link Trace} panel.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class TraceNode extends Panel {

  private static final long serialVersionUID = -6672032112136408625L;

  /**
   * @param id
   */
  public TraceNode(String id, String folderKey, PedigreeTreeNode parentNode, int branch, int level) {
    super(id);
    nodeAdd(this, folderKey, parentNode, branch, level);
  }

  public List<PedigreeTreeNode> getChildren(PedigreeTreeNode node) {
    List<PedigreeTreeNode> children = new Vector<PedigreeTreeNode>(
        node.getNumChildren());

    for (int i = 0; i < node.getNumChildren(); i++) {
      children.add(node.getChildAt(i));
    }

    return children;
  }

  public void nodeAdd(WebMarkupContainer container, final String folderKey, 
      final PedigreeTreeNode node, final int branch, final int level) {

    if (node.getNumChildren() > 0) {
      final String folderId = folderKey+"_folder" + (level + 1) + "." + (branch + 1);
      final String branchId = folderKey+"_branch" + (level + 1) + "." + (branch + 1);


      // deactivate no_children
      WebMarkupContainer noChildren = new WebMarkupContainer("no_children");
      noChildren.setVisible(false);
      container.add(noChildren);

      // activate has_children
      WebMarkupContainer hasChildren = new WebMarkupContainer("has_children");
      WebMarkupContainer parentTrigger = new WebMarkupContainer(
          "parent_trigger");
      parentTrigger.add(new SimpleAttributeModifier("onclick",
          "javascript:showBranch('" + branchId + "');swapFolder('" + folderId
              + "');"));

      Image folderImage = new Image("parent_folder");
      folderImage.add(new SimpleAttributeModifier("id", folderId));
      parentTrigger.add(folderImage);
      parentTrigger.add(new Label("parent_product_name", node.getNodeProduct()
          .getProductName()));
      hasChildren.add(parentTrigger);

      WebMarkupContainer childrenContainer = new WebMarkupContainer("children_container");
      childrenContainer.add(new SimpleAttributeModifier("id", branchId));
      childrenContainer.add(new SimpleAttributeModifier("class", "branch"));
      
      ListView<PedigreeTreeNode> children = new ListView<PedigreeTreeNode>(
          "children", getChildren(node)) {
        /*
         * (non-Javadoc)
         * 
         * @see
         * org.apache.wicket.markup.html.list.ListView#populateItem(org.apache
         * .wicket.markup.html.list.ListItem)
         */
        @Override
        protected void populateItem(ListItem<PedigreeTreeNode> item) {
          TraceNode childContainer = new TraceNode("child", folderKey, 
              item.getModelObject(), item.getIndex(), level + 1);
          item.add(childContainer);
        }
      };

      childrenContainer.add(children);
      
      hasChildren.add(childrenContainer);
      container.add(hasChildren);

    } else {
      // active no_children
      WebMarkupContainer noChildren = new WebMarkupContainer("no_children");
      noChildren.add(new Label("product_name", node.getNodeProduct()
          .getProductName()));
      container.add(noChildren);

      // de-activate has_children
      WebMarkupContainer hasChildren = new WebMarkupContainer("has_children");
      hasChildren.setVisible(false);
      container.add(hasChildren);
    }
  }

}
