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

//JDK imports
import org.apache.oodt.cas.metadata.Metadata;

import java.util.List;
import java.util.Vector;

//OODT imports

/**
 * 
 * The response from a call to {@link SSOMetKeys#IDENTITY_READ_ENDPOINT}.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class IdentityDetails {

  private String name;

  private String type;

  private String realm;

  private List<String> groups;

  private Metadata attributes;

  public IdentityDetails() {
    this.name = null;
    this.type = null;
    this.realm = null;
    this.groups = new Vector<String>();
    this.attributes = new Metadata();
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name
   *          the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the type
   */
  public String getType() {
    return type;
  }

  /**
   * @param type
   *          the type to set
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * @return the realm
   */
  public String getRealm() {
    return realm;
  }

  /**
   * @param realm
   *          the realm to set
   */
  public void setRealm(String realm) {
    this.realm = realm;
  }

  /**
   * @return the groups
   */
  public List<String> getGroups() {
    return groups;
  }

  /**
   * @param groups
   *          the groups to set
   */
  public void setGroups(List<String> groups) {
    this.groups = groups;
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
    return "[name=" + this.name + ",type=" + this.type + ",realm=" + this.realm + ",roles=" + this.groups
           + ",attributes=" + this.attributes.getMap() + "]";
  }

}
