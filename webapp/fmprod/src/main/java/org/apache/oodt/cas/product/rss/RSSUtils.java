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


package org.apache.oodt.cas.product.rss;

//OODT imports
import static org.apache.oodt.cas.product.rss.RSSConfigMetKeys.RSS_CONTEXT_CONF_KEY;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.oodt.commons.xml.XMLUtils;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.util.PathUtils;

//JDK imports
import java.io.File;
import java.io.FileNotFoundException;
import javax.servlet.ServletConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * Utility functions to help out in configuring the {@link RSSProductServlet}.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class RSSUtils {

  public static String getChannelLink(String channelLink, Metadata channelMet) {
    if (channelLink != null && !channelLink.equals("")) {
      return PathUtils.replaceEnvVariables(channelLink, channelMet);
    } else {
      return channelMet.getMetadata("BaseUrl") + "/rdf/dataset?type="
      + channelMet.getMetadata("ProductType") + "&typeID="
      + channelMet.getMetadata("ProductTypeId");
    }
  }

  public static Element emitRSSTag(RSSTag tag, Metadata prodMet, Document doc,
      Element item) {
    String outputTag = tag.getName();
    if (outputTag.contains(" ")) {
      outputTag = StringUtils.join(WordUtils.capitalizeFully(outputTag).split(
          " "));
    }
    Element rssMetElem = XMLUtils.addNode(doc, item, outputTag);

    // first check if there is a source defined, if so, use that as the value
    if (tag.getSource() != null) {
      rssMetElem.appendChild(doc.createTextNode(StringEscapeUtils.escapeXml(PathUtils.replaceEnvVariables(
          tag.getSource(), prodMet))));
    }

    // check if there are attributes defined, and if so, add to the attributes
    for (RSSTagAttribute attr : tag.getAttrs()) {
      rssMetElem.setAttribute(attr.getName(), PathUtils.replaceEnvVariables(
          attr.getValue(), prodMet));
    }

    return rssMetElem;
  }

  public static RSSConfig initRSS(ServletConfig conf)
      throws FileNotFoundException {
    return RSSConfigReader.readConfig(new File(PathUtils
        .replaceEnvVariables(conf.getServletContext().getInitParameter(
            (RSS_CONTEXT_CONF_KEY)))));
  }

}
