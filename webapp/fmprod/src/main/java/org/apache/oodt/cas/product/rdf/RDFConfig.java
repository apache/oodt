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


package org.apache.oodt.cas.product.rdf;


import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * 
 * The RDF REST-ful web service configuration for the CAS product service layer.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class RDFConfig {

  private Map<String, String> nsMap;

  private Map<String, String> rewriteMap;

  private Map<String, String> resLinkMap;

  private Map<String, String> keyNsMap;

  private Map<String, String> typesNsMap;

  private String defaultKeyNs;

  private String defaultTypeNs;

  /**
   * Default constructor.
   */
  public RDFConfig() {
    this.nsMap = new ConcurrentHashMap<String, String>();
    this.rewriteMap = new ConcurrentHashMap<String, String>();
    this.resLinkMap = new ConcurrentHashMap<String, String>();
    this.keyNsMap = new ConcurrentHashMap<String, String>();
    this.typesNsMap = new ConcurrentHashMap<String, String>();
    this.defaultKeyNs = null;
    this.defaultTypeNs = null;
  }

  /**
   * @return the nsMap
   */
  public Map<String, String> getNsMap() {
    return nsMap;
  }

  /**
   * @param nsMap
   *          the nsMap to set
   */
  public void setNsMap(Map<String, String> nsMap) {
    this.nsMap = nsMap;
  }

  /**
   * @return the rewriteMap
   */
  public Map<String, String> getRewriteMap() {
    return rewriteMap;
  }

  /**
   * @param rewriteMap
   *          the rewriteMap to set
   */
  public void setRewriteMap(Map<String, String> rewriteMap) {
    this.rewriteMap = rewriteMap;
  }

  /**
   * @return the resLinkMap
   */
  public Map<String, String> getResLinkMap() {
    return resLinkMap;
  }

  /**
   * @param resLinkMap
   *          the resLinkMap to set
   */
  public void setResLinkMap(Map<String, String> resLinkMap) {
    this.resLinkMap = resLinkMap;
  }

  /**
   * @return the keyNsMap
   */
  public Map<String, String> getKeyNsMap() {
    return keyNsMap;
  }

  /**
   * @param keyNsMap
   *          the keyNsMap to set
   */
  public void setKeyNsMap(Map<String, String> keyNsMap) {
    this.keyNsMap = keyNsMap;
  }

  /**
   * @return the defaultKeyNs
   */
  public String getDefaultKeyNs() {
    return defaultKeyNs;
  }

  /**
   * @param defaultKeyNs
   *          the defaultKeyNs to set
   */
  public void setDefaultKeyNs(String defaultKeyNs) {
    this.defaultKeyNs = defaultKeyNs;
  }

  /**
   * @return the defaultTypeNs
   */
  public String getDefaultTypeNs() {
    return defaultTypeNs;
  }

  /**
   * @param defaultTypeNs
   *          the defaultTypeNs to set
   */
  public void setDefaultTypeNs(String defaultTypeNs) {
    this.defaultTypeNs = defaultTypeNs;
  }

  /**
   * @return the typesNsMap
   */
  public Map<String, String> getTypesNsMap() {
    return typesNsMap;
  }

  /**
   * @param typesNsMap
   *          the typesNsMap to set
   */
  public void setTypesNsMap(Map<String, String> typesNsMap) {
    this.typesNsMap = typesNsMap;
  }

  /**
   * Convenience method. First checks to see if there is a declared key
   * namespace for this key, otherwise returns {@link #getDefaultKeyNs()}
   * 
   * @param key
   *          The key to find the namespace for.
   * @return Either the key's declared namespace, or {@link #getDefaultKeyNs()}.
   */
  public String getKeyNs(String key) {
    if (this.keyNsMap != null && this.keyNsMap.containsKey(key)) {
      return this.keyNsMap.get(key);
    } else {
      return this.getDefaultKeyNs();
    }
  }

  /**
   * Convenience method. First checks to see if there is a declared type
   * namespace for this {@link org.apache.oodt.cas.filemgr.structs.ProductType}, otherwise, returns
   * {@link #getDefaultTypeNs()}.
   * 
   * @param type
   *          The {@link org.apache.oodt.cas.filemgr.structs.ProductType#getName()} to find the namespace for.
   * @return Either the type's declared namespace, or
   *         {@link #getDefaultTypeNs()}.
   */
  public String getTypeNs(String type) {
    if (this.typesNsMap != null && this.typesNsMap.containsKey(type)) {
      return this.typesNsMap.get(type);
    } else {
      return this.getDefaultTypeNs();
    }
  }

}
