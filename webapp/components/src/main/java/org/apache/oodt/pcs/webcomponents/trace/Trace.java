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

//OODT imports
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.pcs.pedigree.Pedigree;
import org.apache.oodt.pcs.pedigree.PedigreeTree;
import org.apache.oodt.pcs.util.FileManagerUtils;

//Wicket imports
import org.apache.wicket.markup.html.panel.Panel;

/**
 * 
 * A Trace web widget to expose the underlying PCS {@link Pedigree}
 * functionality.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class Trace extends Panel {

  private static final long serialVersionUID = 5965790268202443144L;

  /**
   * @param id
   */
  public Trace(String id, String fmUrlStr, boolean listNotCat,
      List<String> excludeList, Product product) {
    super(id);
    Pedigree pedigree = new Pedigree(new FileManagerUtils(fmUrlStr),
        listNotCat, excludeList);
    PedigreeTree upstream = pedigree.doPedigree(product, true);
    PedigreeTree downstream = pedigree.doPedigree(product, false);

    add(new TraceNode("upstream_pedigree_tree", "up", upstream.getRoot(), 0, 0));
    add(new TraceNode("downstream_pedigree_tree", "down", downstream.getRoot(), 0, 0));
  }

}
