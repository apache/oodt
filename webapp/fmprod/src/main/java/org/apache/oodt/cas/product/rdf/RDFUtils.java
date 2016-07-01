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


package org.apache.oodt.cas.product.rdf;

//OODT imports
import static org.apache.oodt.cas.product.rdf.RDFConfigMetKeys.RDF_CONTEXT_CONF_KEY;
import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.oodt.commons.xml.XMLUtils;

//JDK imports
import java.io.File;
import java.io.FileNotFoundException;

import javax.servlet.ServletConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * Utility class to leverage the {@link RDFConfig} to determine how to display
 * an RDF tag.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public final class RDFUtils {

  private static final String RDF_RES_ATTR = "rdf:resource";

  public static Element getRDFElement(String key, String val, RDFConfig conf,
      Document doc) {
    // first apply the rewrite rules
    String tagName = conf.getRewriteMap().containsKey(key) ? conf
        .getRewriteMap().get(key) : key;

    // does this tag have a namespace? if not, use the default
    String ns = conf.getKeyNs(key);
    Element elem;
    // is this a resource link?
    if (conf.getResLinkMap().containsKey(key)) {
      elem = doc.createElement(ns + ":" + tagName);
      String linkBase = conf.getResLinkMap().get(key).endsWith("/") ? conf
          .getResLinkMap().get(key) : conf.getResLinkMap().get(key) + "/";
      elem.setAttribute(RDF_RES_ATTR, linkBase + val);
    } else {
      elem = doc.createElement(ns + ":" + tagName);
      elem.appendChild(doc.createTextNode(val));
    }

    return elem;
  }

  public static void addNamespaces(Document doc, Element rdf, RDFConfig rdfConf) {
    for (String nsName : rdfConf.getNsMap().keySet()) {
      String nsUrl = rdfConf.getNsMap().get(nsName);

      XMLUtils.addAttribute(doc, rdf, "xmlns:" + nsName, nsUrl);
    }
  }
  

  public static RDFConfig initRDF(ServletConfig conf) throws FileNotFoundException {
    return RDFConfigReader.readConfig(new File(PathUtils.replaceEnvVariables(conf
        .getServletContext().getInitParameter((RDF_CONTEXT_CONF_KEY)))));
  }

}
