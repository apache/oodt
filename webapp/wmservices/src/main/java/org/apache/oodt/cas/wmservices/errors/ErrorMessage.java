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

package org.apache.oodt.cas.wmservices.errors;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * This is the Corresponding JAVA object for HTTP exception response payload
 *
 * @author ngimhana (Nadeeshan Gimhana)
 */
@XmlRootElement
public class ErrorMessage {

  /** Status code = 400,404,500 etc. * */
  private int errorStatusCode;

  /** Basic Description for the Error = InternalServerException etc. * */
  private String errorType;

  /** More Information regarding the thrown exception = Reason for exception in Server side * */
  private String exception;

  public ErrorMessage() {}

  public ErrorMessage(int errorStatusCode, String errorType, String exception) {
    this.setErrorStatusCode(errorStatusCode);
    this.setErrorType(errorType);
    this.setException(exception);
  }

  public ErrorMessage(int errorStatusCode, String message) {
    this.setErrorStatusCode(errorStatusCode);
    this.setErrorType(message);
  }

  /** @return the errorStatusCode */
  public int getErrorStatusCode() {
    return errorStatusCode;
  }

  /** @param errorStatusCode the errorStatusCode to set */
  public void setErrorStatusCode(int errorStatusCode) {
    this.errorStatusCode = errorStatusCode;
  }

  /** @return the errorType */
  public String getErrorType() {
    return errorType;
  }

  /** @param errorType the errorType to set */
  public void setErrorType(String errorType) {
    this.errorType = errorType;
  }

  /** @return the exception */
  public String getException() {
    return exception;
  }

  /** @param exception the exception to set */
  public void setException(String exception) {
    this.exception = exception;
  }
}
