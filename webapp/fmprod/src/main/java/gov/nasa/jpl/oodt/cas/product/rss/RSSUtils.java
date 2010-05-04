//Copyright (c) 2010, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.product.rss;

//OODT imports
import static gov.nasa.jpl.oodt.cas.product.rss.RSSConfigMetKeys.RSS_CONTEXT_CONF_KEY;
import gov.nasa.jpl.oodt.cas.commons.xml.XMLUtils;
import gov.nasa.jpl.oodt.cas.metadata.Metadata;
import gov.nasa.jpl.oodt.cas.metadata.util.PathUtils;

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
      String cLink = channelMet.getMetadata("BaseUrl") + "/rdf/dataset?type="
      + channelMet.getMetadata("ProductType") + "&typeID="
      + channelMet.getMetadata("ProductTypeId");
      return cLink;
    }
  }

  public static Element emitRSSTag(RSSTag tag, Metadata prodMet, Document doc,
      Element item) {
    Element rssMetElem = XMLUtils.addNode(doc, item, tag.getName());

    // first check if there is a source defined, if so, use that as the value
    if (tag.getSource() != null) {
      rssMetElem.appendChild(doc.createTextNode(PathUtils.replaceEnvVariables(
          tag.getSource(), prodMet)));
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
