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
 * Generates a directory listing, recursing into the OFSN path.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class DirListRecursiveHandler extends AbstractCrawlLister {

  /*
   * (non-Javadoc)
   * 
   * @see
   * gov.nasa.jpl.oodt.product.handlers.ofsn.AbstractCrawlLister#configure(java
   * .util.Properties)
   */
  @Override
  public void configure(Properties conf) {
    // no properties yet

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * gov.nasa.jpl.oodt.product.handlers.ofsn.AbstractCrawlLister#getListing(
   * java.lang.String)
   */
  @Override
  public File[] getListing(String ofsn) throws ProductException {
    return crawlFiles(new File(ofsn), true, true);
  }

}
