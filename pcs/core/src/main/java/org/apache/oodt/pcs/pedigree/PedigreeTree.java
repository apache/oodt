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

/**
 * 
 * A data structure representing an upstream and downstream 
 * pedigree for a particular {@link Product}. That is: what files 
 * went into producing this {@link Product}, and in what files 
 * was this {@link Product} included?
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class PedigreeTree {

    private PedigreeTreeNode root;

    public PedigreeTree(PedigreeTreeNode root) {
        this.root = root;
    }

    public int getNumLevels() {
        if (root != null) {
            return traverse(root, 0) + 1;
        } else {
            return 0;
        }
    }

    public PedigreeTreeNode getRoot() {
        return root;
    }

    public void setRoot(PedigreeTreeNode root) {
        this.root = root;
    }

    /**
     * Tree:
     *            -------->[N3]
     *          ---->[N2]
     *            -------->[N5]
     *   [N1]---
     *          ---->[N4]
     *    
     * 
     * 
     * @param node
     * @param level
     * @return
     */
    private int traverse(PedigreeTreeNode node, int level) {
        int maxLevel = level;
        if (node.getNumChildren() > 0) {
            for (int i = 0; i < node.getNumChildren(); i++) {
                int lvl = traverse(node.getChildAt(i), level + 1);
                if (lvl > maxLevel) {
                    maxLevel = lvl;
                }
            }
        }

        return maxLevel;
    }

    public void traverseAndPrint(PedigreeTreeNode node, int level) {
        System.out.println(getTabStr(level)
                + node.getNodeProduct().getProductName());
        if (node.getNumChildren() > 0) {
            for (int i = 0; i < node.getNumChildren(); i++) {
                traverseAndPrint(node.getChildAt(i), level + 1);

            }
        }
    }

    private String getTabStr(int num) {
        StringBuilder tabStrBuf = new StringBuilder();
        for (int i = 0; i < num; i++) {
            tabStrBuf.append("\t");
        }

        return tabStrBuf.toString();
    }
    
    public void printPedigreeTree() {
        PedigreeTreeNode root = getRoot();
        traverseAndPrint(root, 0);
    }

}
