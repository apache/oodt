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

package org.apache.oodt.opendapps.config;

/**
 * 
 * A specification for rewriting OPeNDAP element names and tags from their
 * original OPeNDAP/THREDDS names into OODT profile elements, and their names
 * and types. Part of the {@link OpendapConfig}.
 * 
 */
public class RewriteSpec {

  private String origName;

  private String rename;

  private String elementType;

  public RewriteSpec() {
    this.origName = null;
    this.rename = null;
    this.elementType = null;
  }

  /**
   * @return the origName
   */
  public String getOrigName() {
    return origName;
  }

  /**
   * @param origName
   *          the origName to set
   */
  public void setOrigName(String origName) {
    this.origName = origName;
  }

  /**
   * @return the rename
   */
  public String getRename() {
    return rename;
  }

  /**
   * @param rename
   *          the rename to set
   */
  public void setRename(String rename) {
    this.rename = rename;
  }

  /**
   * @return the elementType
   */
  public String getElementType() {
    return elementType;
  }

  /**
   * @param elementType
   *          the elementType to set
   */
  public void setElementType(String elementType) {
    this.elementType = elementType;
  }

}
