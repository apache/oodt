/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership.  The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.oodt.cas.product.jaxrs.enums;

/**
 * This is the Enumeration file for storing HTTP Exception types. Use these constants instead of
 * hardcoding errors in REST API implementations
 *
 * @author ngimhana (Nadeeshan Gimhana)
 */
public enum ErrorType {
  BAD_REQUEST_EXCEPTION("Malformed message"),
  BAD_REQUEST_EXCEPTION_REFERENCE_RESOURCE(
      "This URL requires a productId query parameter with a product ID value,"
          + " e.g. /reference?productId=1787a257-df87-11e2-8a2d-e3f6264e86c5"),

  BAD_REQUEST_EXCEPTION_PRODUCT_RESOURCE(
      "Failed to load resource: the server responded with a status of 400"),
  BAD_REQUEST_EXCEPTION_DATASET_RESOURCE(
      "This URL requires a productTypeId query parameter and either a "
          + "product type ID value or 'ALL' for all product types"),

  BAD_REQUEST_EXCEPTION_TRANSFER_RESOURCE(
      "This URL requires a dataStoreRef query parameter "
          + "and a data store reference value, e.g. /transfer?dataStoreRef=file:/repository/test.txt/test.txt"),

  INTERNAL_SERVER_ERROR("General Server Error"),

  NOT_FOUND_EXCEPTION("Couldn’t find resource"),
  NOT_FOUND_EXCEPTION_TRANSFER_RESOURCE(
      "Unable to find a current file transfer status for data store reference: "),

  CAS_PRODUCT_EXCEPTION_FILEMGR_CLIENT_UNAVILABLE(
      "Unable to get the file manager client from the servlet context."),
  CAS_PRODUCT_EXCEPTION_FILEMGR_WORKING_DIR_UNAVILABLE(
      "Unable to get the file manager's" + " working directory from the servlet context.");

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
