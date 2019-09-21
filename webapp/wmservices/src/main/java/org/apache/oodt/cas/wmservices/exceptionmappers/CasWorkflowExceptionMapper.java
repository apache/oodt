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

package org.apache.oodt.cas.wmservices.exceptionmappers;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.apache.oodt.cas.wmservices.errors.ErrorMessage;
import org.apache.oodt.cas.workflow.exceptions.WorkflowException;

/**
 * This is an exception mapper which maps "BadRequestException" to "ErrorMessage JSON payload"
 *
 * @author ngimhana (Nadeeshan Gimhana)
 */
@Provider
public class CasWorkflowExceptionMapper implements ExceptionMapper<WorkflowException> {

  /** Maps CasProductException to HTTP Response * */
  @Override
  public Response toResponse(WorkflowException exception) {

    // Initialising ErrorMessage Entity for Mapping to Response
    ErrorMessage errorMessageEntity =
        new ErrorMessage(
            Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), exception.getMessage());

    // Maps Error Status 500 to Response
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
        .entity(errorMessageEntity)
        .type(MediaType.APPLICATION_JSON)
        .build();
  }
}
