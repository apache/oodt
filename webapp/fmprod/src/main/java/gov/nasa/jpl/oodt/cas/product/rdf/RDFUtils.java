//Copyright (c) 2009, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.product.rdf;

//OODT imports
import static gov.nasa.jpl.oodt.cas.product.rdf.RDFConfigMetKeys.RDF_CONTEXT_CONF_KEY;
import gov.nasa.jpl.oodt.cas.metadata.util.PathUtils;
import gov.nasa.jpl.oodt.cas.commons.xml.XMLUtils;

//JDK imports
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;
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
    Element elem = null;
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
    for (Iterator<String> i = rdfConf.getNsMap().keySet().iterator(); i
        .hasNext();) {
      String nsName = i.next();
      String nsUrl = rdfConf.getNsMap().get(nsName);

      XMLUtils.addAttribute(doc, rdf, "xmlns:" + nsName, nsUrl);
    }
  }
  

  public static RDFConfig initRDF(ServletConfig conf) throws FileNotFoundException {
    return RDFConfigReader.readConfig(new File(PathUtils.replaceEnvVariables(conf
        .getServletContext().getInitParameter((RDF_CONTEXT_CONF_KEY)))));
  }

}
