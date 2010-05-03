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
 * Handles a listing request for the raw size of a file. This listing
 * returns a single file or directory, which will then have its size
 * computed.
 *
 * @author mattmann
 * @version $Revision$
 *
 */
public class RawSizeListHandler implements OFSNListHandler {

  /* (non-Javadoc)
   * @see gov.nasa.jpl.oodt.product.handlers.ofsn.OFSNListHandler#configure(java.util.Properties)
   */
  public void configure(Properties conf) {
    // TODO Auto-generated method stub
    // nothing yet

  }

  /* (non-Javadoc)
   * @see gov.nasa.jpl.oodt.product.handlers.ofsn.OFSNListHandler#getListing(java.lang.String)
   */
  public File[] getListing(String ofsn) throws ProductException {
    if (!new File(ofsn).exists()) {
          throw new ProductException("file: [" + ofsn
                  + "] does not exist!");
      }
      return new File[] { new File(ofsn) };
  }

}
