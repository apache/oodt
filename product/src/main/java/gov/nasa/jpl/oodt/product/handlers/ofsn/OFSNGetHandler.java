//Copyright (c) 2009, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.product.handlers.ofsn;

//JDK imports
import java.util.Properties;

//OODT imports
import jpl.eda.product.ProductException;

/**
 * 
 * The default OFSN handler for getting remote data.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public interface OFSNGetHandler {

  /**
   * Retrieves a chunk of data from the remote file.
   * 
   * @param filepath
   *          The path to the remote file.
   * @param offset
   *          The offset in the remote data to retrieve.
   * @param length
   *          The length of data to read
   * @return The byte[] data, read, or null otherwise.
   * @throws ProductException
   *           If any error occurs.
   */
  public byte[] retrieveChunk(String filepath, long offset, int length)
      throws ProductException;

  /**
   * Returns the size of the remote data, which may be the entire file in
   * question, or some subset/transformation on it.
   * 
   * @param filepath
   *          The remote file in question.
   * @return The size of the remote file, potentially after a remote
   *         transformation has occured.
   */
  public long sizeOf(String filepath);

  /**
   * Configures this handler with the provided configuration stored in a
   * {@link Properties} object.
   * 
   * @param conf
   *          The configuration for this list handler.
   */
  public void configure(Properties conf);

}
