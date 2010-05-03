//Copyright (c) 2009, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.product.handlers.ofsn;

//JDK imports
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

//APACHE imports
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;

//OODT imports
import jpl.eda.product.ProductException;

/**
 * 
 * A {@link OFSNGetHandler} to perform an MD5 for a file on the server side.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class MD5GetHandler implements OFSNGetHandler {

  private MessageDigest md = null;

  public MD5GetHandler() throws InstantiationException {
    try {
      this.md = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      throw new InstantiationException(e.getMessage());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * gov.nasa.jpl.oodt.product.handlers.ofsn.OFSNListHandler#configure(java.
   * util.Properties)
   */
  public void configure(Properties conf) {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * gov.nasa.jpl.oodt.product.handlers.ofsn.OFSNGetHandler#retrieveChunk(java
   * .lang.String, long, int)
   */
  public byte[] retrieveChunk(String filepath, long offset, int length)
      throws ProductException {
    try {
      String hash = this.hashData(FileUtils.readFileToByteArray(new File(
          filepath)));
      byte[] retBytes = new byte[length];
      byte[] hashBytes = hash.getBytes();      
      ByteArrayInputStream is = new ByteArrayInputStream(hashBytes);
      is.skip(offset);
      is.read(retBytes, 0, length);
      return retBytes;
    } catch (IOException e) {
      e.printStackTrace();
      throw new ProductException("Error reading bytes from file: [" + filepath
          + "] MD5: Message: " + e.getMessage());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * gov.nasa.jpl.oodt.product.handlers.ofsn.OFSNGetHandler#sizeOf(java.lang
   * .String)
   */
  public long sizeOf(String filepath) {
    try {
      String hash = this.hashData(FileUtils.readFileToByteArray(new File(
          filepath)));
      return hash.getBytes().length;
    } catch (IOException e) {
      e.printStackTrace();
      return -1;
    }
  }

  /**
   * Use this method ot generate a test MD5 of a provide {@link File} specified
   * in args[0].
   * 
   * @param args
   *          Only need to specify 1 arg, the full path to the {@link File} to
   *          MD5.
   * @throws Exception
   *           If any error occurs.
   */
  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.err.println("MD5GetHandler <file>");
      System.exit(1);
    }

    String filepath = args[0];
    String hashString = new MD5GetHandler().hashData(FileUtils
        .readFileToByteArray(new File(filepath)));
    System.out.println(hashString);
  }

  private String hashData(byte[] dataToHash) {
    this.md.update(dataToHash, 0, dataToHash.length);
    byte[] hash = this.md.digest();
    return new String(Hex.encodeHex(hash));
  }

}
