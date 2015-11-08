package org.apache.oodt.cas.pge.exceptions;

/**
 * Created by bugg on 27/10/15.
 */
public class PGEException extends Exception {

  public PGEException(String message){
    super(message);
  }

  public PGEException(String s, Exception e) {
    super(s, e);
  }

}
