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

package org.apache.oodt.security.sso.opensso;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;

//JDK imports
import java.util.List;
import java.util.Vector;

/**
 * 
 * The response from a query to {@link SSOMetKeys#IDENTITY_ATTRIBUTES_ENDPOINT}.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class UserDetails {

  private String token;

  private List<String> roles;

  private Metadata attributes;

  public UserDetails() {
    this.token = null;
    this.roles = new Vector<String>();
    this.attributes = new Metadata();
  }

  /**
   * @return the token
   */
  public String getToken() {
    return token;
  }

  /**
   * @param token
   *          the token to set
   */
  public void setToken(String token) {
    this.token = token;
  }

  /**
   * @return the roles
   */
  public List<String> getRoles() {
    return roles;
  }

  /**
   * @param roles
   *          the roles to set
   */
  public void setRoles(List<String> roles) {
    this.roles = roles;
  }

  /**
   * @return the attributes
   */
  public Metadata getAttributes() {
    return attributes;
  }

  /**
   * @param attributes
   *          the attributes to set
   */
  public void setAttributes(Metadata attributes) {
    this.attributes = attributes;
  }

  public String toString() {
    return "[token=" + this.token + ",roles=" + this.roles + ",attributes=" + this.attributes.getMap() + "]";
  }
}
