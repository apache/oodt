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

package org.apache.oodt.opendapps.config;

//JDK imports
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

//APACHE imports
import org.apache.oodt.commons.xml.XMLUtils;
import static org.apache.oodt.opendapps.config.OpendapConfigMetKeys.*;

/**
 * 
 * Reads the {@link OpendapConfig} from a provided file and returns it.
 * 
 */
public class OpendapConfigReader {

  public static OpendapConfig read(String confFilePath)
      throws FileNotFoundException, MalformedURLException {
    OpendapConfig conf = new OpendapConfig();
    Document doc = XMLUtils.getDocumentRoot(new FileInputStream(new File(
        confFilePath)));
    Element rootElem = doc.getDocumentElement();

    NodeList dapRootNodeList = rootElem.getElementsByTagName(DAP_ROOT_TAG);
    for (int i = 0; i < dapRootNodeList.getLength(); i++) {
      Element dapRootElem = (Element) dapRootNodeList.item(i);
      DapRoot root = new DapRoot();
      root.setCatalogUrl(new URL(dapRootElem.getAttribute(CATALOG_URL_ATTR)));
      root.setDatasetUrl(new URL(dapRootElem.getAttribute(DATASET_URL_ATTR)));
      root.setFilter(dapRootElem.getAttribute(FILTER_ATTR));
      conf.getRoots().add(root);
    }

    Element rewriteRootElem = XMLUtils.getFirstElement(REWRITE_ROOT_TAG,
        rootElem);
    NodeList rewriteNodeList = rewriteRootElem
        .getElementsByTagName(REWRITE_VAR_TAG);
    for (int i = 0; i < rewriteNodeList.getLength(); i++) {
      Element rewriteElem = (Element) rewriteNodeList.item(i);
      RewriteSpec spec = new RewriteSpec();
      spec.setOrigName(rewriteElem.getAttribute(REWRITE_VAR_NAME_ATTR));
      spec.setRename(rewriteElem.getAttribute(REWRITE_VAR_RENAME_ATTR));
      spec.setElementType(rewriteElem.getAttribute(REWRITE_VAR_TYPE_ATTR));
      conf.getRewriteSpecs().add(spec);
    }

    Element datasetMetRootElem = XMLUtils.getFirstElement(DATASET_MET_ROOT_TAG,
        rootElem);
    NodeList datasetMetElemNodeList = datasetMetRootElem
        .getElementsByTagName(DATASET_MET_ELEM_TAG);
    for (int i = 0; i < datasetMetElemNodeList.getLength(); i++) {
      Element datasetMetElem = (Element) datasetMetElemNodeList.item(i);
      DatasetMetElem datasetMetSpec = new DatasetMetElem();
      datasetMetSpec.setProfileElementName(datasetMetElem
          .getAttribute(DATASET_MET_NAME_ATTR));
      datasetMetSpec.setValue(datasetMetElem
          .getAttribute(DATASET_MET_VALUE_ATTR));
      conf.getDatasetMetSpecs().add(datasetMetSpec);
    }

    Element constRootElem = XMLUtils.getFirstElement(CONSTANT_ROOT_TAG,
        rootElem);
    NodeList constNodeList = constRootElem.getElementsByTagName(CONSTANT_TAG);
    for (int i = 0; i < constNodeList.getLength(); i++) {
      Element constElem = (Element) constNodeList.item(i);
      ConstantSpec constSpec = new ConstantSpec();
      constSpec.setName(constElem.getAttribute(CONSTANT_NAME_ATTR));
      constSpec.setType(constElem.getAttribute(CONSTANT_TYPE_ATTR));
      constSpec.setValue(constElem
          .getAttribute(CONSTANT_VALUE_ATTR));
      conf.getConstSpecs().add(constSpec);
    }

    return conf;
  }

}
