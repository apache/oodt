/**
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

package org.apache.oodt.pcs.services;

//JDK imports
import org.apache.oodt.pcs.services.config.PCSServiceConfig;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

//JAX-RS imports
//OODT imports


/**
 *
 * Base class for PCS JAX-RS services. Loads up 
 * the {@link PCSServiceConfig} and makes it available
 * statically to downstream descendants.
 *
 * @author mattmann
 * @version $Revision$
 *
 */
public class PCSService extends HttpServlet {

  protected static final long serialVersionUID = 6256089700429938496L;
  
  protected static PCSServiceConfig conf;
  

  /* (non-Javadoc)
   * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
   */
  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    conf = new PCSServiceConfig(config);
  }
  

  protected class ResourceNotFoundException extends WebApplicationException {

    public ResourceNotFoundException() {
      super(Response.status(Status.NOT_FOUND).type(MediaType.TEXT_PLAIN)
          .build());
    }

    public ResourceNotFoundException(String msg) {
      super(Response.status(Status.NOT_FOUND).entity(msg).type(
          MediaType.TEXT_PLAIN).build());
    }
  }

}
