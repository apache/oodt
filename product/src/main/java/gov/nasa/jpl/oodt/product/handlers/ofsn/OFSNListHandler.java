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
 * <p>
 * Classes that implement this interface define how to return file listings on a
 * remote server from an <code>ofsn</code>.
 * </p>
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public interface OFSNListHandler {

  /**
   * Handlers that implement this method take an <code>O</code>nline
   * <code>F</code>ile <code>S</code>pecification <code>N</code>ame and return
   * back a listing of files on the remote server.
   * 
   * @param ofsn
   *          The OFSN path to list files from.
   * @return An array of {@link File} objects.
   * @throws ProductException
   *           If any error occurs performing the listing on the server side.
   */
  public File[] getListing(String ofsn) throws ProductException;

  /**
   * Configures this handler with the provided configuration stored in a
   * {@link Properties} object.
   * 
   * @param conf
   *          The configuration for this list handler.
   */
  public void configure(Properties conf);
}
