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

package org.apache.oodt.cas.product.jaxrs.resources;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "FMStatus")
@XmlType(propOrder = {"url", "serverUp", "message"})
@XmlAccessorType(XmlAccessType.NONE)
public class FMStatusResource {

  private String url;
  private boolean serverUp;
  private String message;

  public FMStatusResource() {}

  public FMStatusResource(String url, boolean serverUp, String message) {
    this.url = url;
    this.serverUp = serverUp;
    this.message = message;
  }

  @XmlElement(name = "url")
  public String getUrl() {
    return url;
  }

  @XmlElement(name = "serverUp")
  public boolean isServerUp() {
    return serverUp;
  }

  @XmlElement(name = "message")
  public String getMessage() {
    return message;
  }
}
