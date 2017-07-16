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


package org.apache.oodt.product.handlers.ofsn;

//JDK imports
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * The OFSN product handler's configuration object.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class OFSNFileHandlerConfiguration {

  protected Map<String, OFSNHandlerConfig> handlerTable;

  private String productRoot;

  private String id;

  private String name;

  public OFSNFileHandlerConfiguration(String productRoot, String id, String name) {
    this.handlerTable = new ConcurrentHashMap<String, OFSNHandlerConfig>();
    this.productRoot = productRoot;
    this.id = id;
    this.name = name;
    cleanse(this.productRoot);
  }

  public OFSNFileHandlerConfiguration() {
    this(null, null, null);
  }

  public String getHandlerType(String handlerName) {
    if (this.handlerTable.containsKey(handlerName)) {
      return this.handlerTable.get(handlerName).getType();
    } else {
      return null;
    }
  }

  public String getHandlerClass(String handlerName) {
    if (this.handlerTable.containsKey(handlerName)) {
      return this.handlerTable.get(handlerName).getClassName();
    } else {
      return null;
    }
  }

  public List<OFSNHandlerConfig> getHandlerConfigs() {
    return Arrays.asList(this.handlerTable.values().toArray(
        new OFSNHandlerConfig[this.handlerTable.size()]));
  }

  public OFSNHandlerConfig getHandlerConfig(String handlerName) {
    return this.handlerTable.get(handlerName);
  }

  /**
   * @return the productRoot
   */
  public String getProductRoot() {
    return productRoot;
  }

  /**
   * @param productRoot
   *          the productRoot to set
   */
  public void setProductRoot(String productRoot) {
    this.productRoot = productRoot;
    cleanse(this.productRoot);
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @param id
   *          the id to set
   */
  public void setId(String id) {
    this.id = id;
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

  private void cleanse(String path) {

  }
}
