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

package org.apache.oodt.pcs.health;

//OODT imports
import org.apache.oodt.commons.date.DateUtils;

//JDK imports
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 
 * The output generated from running the {@link org.apache.oodt.pcs.tools.PCSHealthMonitor#getReport()}
 * method.
 * 
 * @author mattmann
 * @version $Revision$
 */
public class PCSHealthMonitorReport implements Serializable {

  private Date generationDate;

  private PCSDaemonStatus fmStatus;

  private PCSDaemonStatus wmStatus;

  private PCSDaemonStatus rmStatus;

  private List batchStubStatus;

  private List crawlerStatus;

  private List latestProductsIngested;

  private List jobHealthStatus;

  private List crawlerHealthStatus;

  /**
   * Default Constructor.
   * 
   */
  public PCSHealthMonitorReport() {

  }

  /**
   * Constructs a new PCSHealthMonitorReport with the given parameters.
   * 
   * @param generationDate
   *          The {@link Date} that this report was generated.
   * @param fmStatus
   *          The {@link PCSDaemonStatus} for the File Manager.
   * @param wmStatus
   *          The {@link PCSDaemonStatus} for the Workflow Manager.
   * @param rmStatus
   *          THe {@link PCSDaemonStatus} for the Resource Manager.
   * @param batchStubStatus
   *          A {@lik List} of {@link PCSDaemonStatus}es for each
   *          {@link org.apache.oodt.cas.resource.system.extern.XmlRpcBatchStub}.
   * @param crawlerStatus
   *          A {@link List} of {@link CrawlerStatus}es.
   * @param latestProductsIngested
   *          A {@link List} of the top N {@link org.apache.oodt.cas.filemgr.structs.Product}s that have been
   *          ingested.
   * @param jobHealthStatus
   *          A {@link List} of {@link JobHealthStatus}es.
   * @param crawlerHealthStatus
   *          A {@link List} of {@link CrawlerHealth}s.
   */
  public PCSHealthMonitorReport(Date generationDate, PCSDaemonStatus fmStatus,
      PCSDaemonStatus wmStatus, PCSDaemonStatus rmStatus, List batchStubStatus,
      List crawlerStatus, List latestProductsIngested, List jobHealthStatus,
      List crawlerHealthStatus) {
    super();
    this.generationDate = generationDate;
    this.fmStatus = fmStatus;
    this.wmStatus = wmStatus;
    this.rmStatus = rmStatus;
    this.batchStubStatus = batchStubStatus;
    this.crawlerStatus = crawlerStatus;
    this.latestProductsIngested = latestProductsIngested;
    this.jobHealthStatus = jobHealthStatus;
    this.crawlerHealthStatus = crawlerHealthStatus;
  }

  /**
   * @return the batchStubStatus
   */
  public List getBatchStubStatus() {
    return batchStubStatus;
  }

  /**
   * @param batchStubStatus
   *          the batchStubStatus to set
   */
  public void setBatchStubStatus(List batchStubStatus) {
    this.batchStubStatus = batchStubStatus;
  }

  /**
   * @return the crawlerHealthStatus
   */
  public List getCrawlerHealthStatus() {
    return crawlerHealthStatus;
  }

  /**
   * @param crawlerHealthStatus
   *          the crawlerHealthStatus to set
   */
  public void setCrawlerHealthStatus(List crawlerHealthStatus) {
    this.crawlerHealthStatus = crawlerHealthStatus;
  }

  /**
   * @return the crawlerStatus
   */
  public List getCrawlerStatus() {
    return crawlerStatus;
  }

  /**
   * @param crawlerStatus
   *          the crawlerStatus to set
   */
  public void setCrawlerStatus(List crawlerStatus) {
    this.crawlerStatus = crawlerStatus;
  }

  /**
   * @return the fmStatus
   */
  public PCSDaemonStatus getFmStatus() {
    return fmStatus;
  }

  /**
   * @param fmStatus
   *          the fmStatus to set
   */
  public void setFmStatus(PCSDaemonStatus fmStatus) {
    this.fmStatus = fmStatus;
  }

  /**
   * @return the generationDate
   */
  public Date getGenerationDate() {
    return generationDate;
  }

  /**
   * @param generationDate
   *          the generationDate to set
   */
  public void setGenerationDate(Date generationDate) {
    this.generationDate = generationDate;
  }

  /**
   * @return the jobHealthStatus
   */
  public List getJobHealthStatus() {
    return jobHealthStatus;
  }

  /**
   * @param jobHealthStatus
   *          the jobHealthStatus to set
   */
  public void setJobHealthStatus(List jobHealthStatus) {
    this.jobHealthStatus = jobHealthStatus;
  }

  /**
   * @return the latestProductsIngested
   */
  public List getLatestProductsIngested() {
    return latestProductsIngested;
  }

  /**
   * @param latestProductsIngested
   *          the latestProductsIngested to set
   */
  public void setLatestProductsIngested(List latestProductsIngested) {
    this.latestProductsIngested = latestProductsIngested;
  }

  /**
   * @return the rmStatus
   */
  public PCSDaemonStatus getRmStatus() {
    return rmStatus;
  }

  /**
   * @param rmStatus
   *          the rmStatus to set
   */
  public void setRmStatus(PCSDaemonStatus rmStatus) {
    this.rmStatus = rmStatus;
  }

  /**
   * @return the wmStatus
   */
  public PCSDaemonStatus getWmStatus() {
    return wmStatus;
  }

  /**
   * @param wmStatus
   *          the wmStatus to set
   */
  public void setWmStatus(PCSDaemonStatus wmStatus) {
    this.wmStatus = wmStatus;
  }

  /**
   * 
   * @return This report's {@link #generationDate} in ISO 8601 String format.
   */
  public String getCreateDateIsoFormat() {
    Calendar cal = Calendar.getInstance();
    cal.setTime(this.generationDate);
    return DateUtils.toString(cal);
  }

}
