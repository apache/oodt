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

package org.apache.oodt.cas.wmservices.exceptions;

import javax.ws.rs.WebApplicationException;

/**
 * This type of exception returns an HTTP 'not found' response (status code 404) with an additional
 * message.
 *
 * @author rlaidlaw
 * @version $Revision$
 */
public class NotFoundException extends WebApplicationException {

  /** Auto-generated ID for serialization. */
  private static final long serialVersionUID = -3835077083758525919L;

  /**
   * Constructor that adds a message to the 'not found' (status code 404) HTTP response.
   *
   * @param message the message to add to the response
   */
  public NotFoundException(String message) {
    super(message);
  }
}
