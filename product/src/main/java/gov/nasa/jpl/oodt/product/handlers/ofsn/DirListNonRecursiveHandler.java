//Copyright (c) 2009, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.product.handlers.ofsn;

//JDK imports
import java.io.File;
import java.util.Properties;

//OODT imports
import jpl.eda.product.ProductException;

/**
 * 
 * Generates a directory listing, without recursing into the OFSN path.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class DirListNonRecursiveHandler extends AbstractCrawlLister {

  /*
   * (non-Javadoc)
   * 
   * @see
   * gov.nasa.jpl.oodt.product.handlers.ofsn.OFSNListHandler#getListing(java
   * .lang.String)
   */
  public File[] getListing(String ofsn) throws ProductException {
     return crawlFiles(new File(ofsn), false, true);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * gov.nasa.jpl.oodt.product.handlers.ofsn.OFSNListHandler#configure(java.
   * util.Properties)
   */
  public void configure(Properties conf) {
     // no properties defined yet
  }

}
