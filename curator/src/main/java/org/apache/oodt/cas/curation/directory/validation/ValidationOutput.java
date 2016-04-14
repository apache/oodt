package org.apache.oodt.cas.curation.directory.validation;

/**
 * Created by bugg on 14/04/16.
 */
public class ValidationOutput {

  boolean valid;
  String message;
  String path;

  public ValidationOutput() {
  }

  public boolean isValid() {
    return valid;
  }

  public void setValid(boolean valid) {
    this.valid = valid;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }
}
