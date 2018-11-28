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

//APACHE imports
import org.apache.oodt.profile.EnumeratedProfileElement;

/**
 * 
 * Specification for the {@link OpendapConfig} that specifies what THREDDS
 * dataset met to use to create {@link EnumeratedProfileElement}s from.
 * 
 */
public class DatasetMetElem {

  private String profileElementName;

  private String value;

  public DatasetMetElem() {
    this.profileElementName = null;
    this.value = null;
  }

  /**
   * @return the profileElementName
   */
  public String getProfileElementName() {
    return profileElementName;
  }

  /**
   * @param profileElementName
   *          the profileElementName to set
   */
  public void setProfileElementName(String profileElementName) {
    this.profileElementName = profileElementName;
  }

  /**
   * @return the value
   */
  public String getValue() {
    return value;
  }

  /**
   * @param value
   *          the value to set
   */
  public void setValue(String value) {
    this.value = value;
  }

}
