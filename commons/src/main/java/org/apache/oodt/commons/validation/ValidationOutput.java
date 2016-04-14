package org.apache.oodt.commons.validation;

/**
 * Validation Response for serialization back to UI.
 *
 * @author tbarber
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
