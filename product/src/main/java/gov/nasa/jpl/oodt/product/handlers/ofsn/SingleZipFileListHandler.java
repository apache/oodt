//Copyright (c) 2009, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.product.handlers.ofsn;

//OODT imports
import gov.nasa.jpl.oodt.product.handlers.ofsn.util.OFSNUtils;
import jpl.eda.product.ProductException;

//JDK imports
import java.io.File;
import java.util.Properties;


/**
 * 
 * Generates a listing of the size of a remote file identified by its OFSN as a
 * zip would be.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class SingleZipFileListHandler implements OFSNListHandler {

  private String cacheRoot;

  /*
   * (non-Javadoc)
   * 
   * @see
   * gov.nasa.jpl.oodt.product.handlers.ofsn.OFSNListHandler#configure(java.
   * util.Properties)
   */
  public void configure(Properties conf) {
    // TODO Auto-generated method stub

    this.cacheRoot = conf.getProperty("cacheDirRoot");

    if (this.cacheRoot == null) {
      this.cacheRoot = "/tmp";
    }

    if (!this.cacheRoot.endsWith("/")) {
      this.cacheRoot += "/";
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * gov.nasa.jpl.oodt.product.handlers.ofsn.OFSNListHandler#getListing(java
   * .lang.String)
   */
  public File[] getListing(String ofsn) throws ProductException {
    if (!new File(ofsn).exists()) {
      throw new ProductException("file: [" + ofsn + "] does not exist!");
    }

    String zipFilePath = this.cacheRoot + new File(ofsn).getName() + ".zip";
    File zipFile = OFSNUtils.buildZipFile(zipFilePath, new File[] { new File(
        ofsn) });
    return new File[] { zipFile };
  }

}
