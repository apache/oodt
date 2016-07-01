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


package org.apache.oodt.cas.curation.structs;

//JDK imports
import java.util.Date;
import java.util.List;
import java.util.Vector;

/**
 * 
 * A specification for ingestion using the {@link org.apache.oodt.cas.filemgr.ingest.Ingester} interface in the
 * CAS.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class IngestionTask implements IngestionTaskStatus {

  private String id;

  private Date createDate;

  private List<String> fileList;

  private String policy;

  private String productType;

  private String status;

  private ExtractorConfig extConf;

  public IngestionTask() {
    this.id = null;
    this.createDate = null;
    this.fileList = new Vector<String>();
    this.policy = null;
    this.productType = null;
    this.extConf = new ExtractorConfig(null, null, null);
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
   * @return the createDate
   */
  public Date getCreateDate() {
    return createDate;
  }

  /**
   * @param createDate
   *          the createDate to set
   */
  public void setCreateDate(Date createDate) {
    this.createDate = createDate;
  }

  /**
   * @return the fileList
   */
  public List<String> getFileList() {
    return fileList;
  }

  /**
   * @param fileList
   *          the fileList to set
   */
  public void setFileList(List<String> fileList) {
    this.fileList = fileList;
  }

  /**
   * @return the policy
   */
  public String getPolicy() {
    return policy;
  }

  /**
   * @param policy
   *          the policy to set
   */
  public void setPolicy(String policy) {
    this.policy = policy;
  }

  /**
   * @return the productType
   */
  public String getProductType() {
    return productType;
  }

  /**
   * @param productType
   *          the productType to set
   */
  public void setProductType(String productType) {
    this.productType = productType;
  }

  /**
   * @return the status
   */
  public String getStatus() {
    return status;
  }

  /**
   * @param status
   *          the status to set
   */
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   * @return the extConf
   */
  public ExtractorConfig getExtConf() {
    return extConf;
  }

  /**
   * @param extConf
   *          the extConf to set
   */
  public void setExtConf(ExtractorConfig extConf) {
    this.extConf = extConf;
  }

}
