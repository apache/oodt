package org.apache.oodt.cas.wmservices.enums;

/**
 * This is the Enumeration file for storing HTTP Exception types. Use these constants instead of
 * hardcoding errors in REST API implementations
 *
 * @author ngimhana (Nadeeshan Gimhana)
 */
public enum ErrorType {
  CAS_PRODUCT_EXCEPTION_WORKFLOWMGR_CLIENT_UNAVILABLE(
      "Unable to get the workflow manager " + "client from the servlet context."),
  BAD_REQUEST_EXCEPTION("Malformed message"),
  NOT_FOUND_EXCEPTION("Couldn’t find resource");

  private String errorType;

  ErrorType(String errorType) {
    this.errorType = errorType;
  }

  /** @return the errorType */
  public String getErrorType() {
    return errorType;
  }

  /** @param errorType the errorType to set */
  public void setErrorType(String errorType) {
    this.errorType = errorType;
  }
}
