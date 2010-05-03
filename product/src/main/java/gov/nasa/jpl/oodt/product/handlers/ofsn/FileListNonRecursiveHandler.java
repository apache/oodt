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
 * A non recursive file listing from a given OFSN.
 *
 * @author mattmann
 * @version $Revision$
 *
 */
public class FileListNonRecursiveHandler extends AbstractCrawlLister {

  /* (non-Javadoc)
   * @see gov.nasa.jpl.oodt.product.handlers.ofsn.OFSNListHandler#configure(java.util.Properties)
   */
  @Override
  public void configure(Properties conf) {
    // TODO Auto-generated method stub
    // nothing yet

  }

  /* (non-Javadoc)
   * @see gov.nasa.jpl.oodt.product.handlers.ofsn.OFSNListHandler#getListing(java.lang.String)
   */
  @Override
  public File[] getListing(String ofsn) throws ProductException {
    return crawlFiles(new File(ofsn), false, false);
  }

}
