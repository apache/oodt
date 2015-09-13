/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.oodt.cas.resource.structs.exceptions;

/**
 * @author starchmd
 * @version $Revision$
 *
 * <p>
 * An exception thrown by the {@link BackendRepository} when an error occurs.
 * </p>
 */
public class RepositoryException extends Exception {

  /* serial version UID */
  private static final long serialVersionUID = 4568261126290589269L;

  /**
   *
   */
  public RepositoryException() {}

  /**
   * @param message
   */
  public RepositoryException(String message) {
    super(message);
  }

  /**
   * @param cause
   */
  public RepositoryException(Throwable cause) {
    super(cause);
  }

  /**
   * @param message
   * @param cause
   */
  public RepositoryException(String message, Throwable cause) {
    super(message, cause);
  }

}
