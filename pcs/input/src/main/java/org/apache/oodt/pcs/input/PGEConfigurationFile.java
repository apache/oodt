// Licensed to the Apache Software Foundation (ASF) under one or more contributor
// license agreements.  See the NOTICE.txt file distributed with this work for
// additional information regarding copyright ownership.  The ASF licenses this
// file to you under the Apache License, Version 2.0 (the "License"); you may not
// use this file except in compliance with the License.  You may obtain a copy of
// the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
// License for the specific language governing permissions and limitations under
// the License.

package org.apache.oodt.pcs.input;

//OCO imports
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * 
 * <p>
 * A configuration file to record input similar to HDF format. The configuration
 * file is a set of named {@link PGEGroup}s.
 * </p>
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class PGEConfigurationFile implements PGEConfigFileKeys {

  /* the name of the PGE that uses this configuration file */
  private PGEScalar pgeName = null;

  /* the list of data product files for tihs PGE */
  private PGEGroup inputProductFiles = null;

  /* the list of static file identification files */
  private PGEGroup staticFileIdentificationFiles = null;

  /* the list of ancillary files identified by the PCS */
  private PGEGroup dynamicAuxiliaryInputFiles = null;

  /*
   * the list of ancillary files that are not used by the PGE directly, but are
   * recorded in the AncillaryDataDescriptors metadata element
   */
  private PGEGroup recordedAuxiliaryInputFiles = null;

  /* the location to put any output products */
  private PGEScalar productPath = null;

  /* the location to which to save monitor files */
  private PGEScalar monitorPath = null;

  /* the filename format of the monitor files */
  private PGEScalar monitorFilenameFormat = null;

  private PGEGroup monitorLevelGroup = null;

  /* the pge specific groups in the configuration file */
  private Map<String, PGEGroup> pgeSpecificGroups = null;

  /**
   * <p>
   * Constructs a new PGEConfigurationFile with no groups.
   * </p>
   */
  public PGEConfigurationFile() {
    this.pgeSpecificGroups = new ConcurrentHashMap<String, PGEGroup>();
    this.inputProductFiles = new PGEGroup(INPUT_PRODUCT_FILES_GROUP);
    this.staticFileIdentificationFiles = new PGEGroup(SFIF_FILE_GROUP);
    this.dynamicAuxiliaryInputFiles = new PGEGroup(
        DYNAMIC_AUX_INPUT_FILES_GROUP);
    this.recordedAuxiliaryInputFiles = new PGEGroup(
        RECORDED_AUX_INPUT_FILES_GROUP);
    this.monitorLevelGroup = new PGEGroup(MONITOR_LEVEL_GROUP);
  }

  /**
   * @return the pgeName
   */
  public PGEScalar getPgeName() {
    return pgeName;
  }

  /**
   * @param pgeName
   *          the pgeName to set
   */
  public void setPgeName(PGEScalar pgeName) {
    this.pgeName = pgeName;
  }

  /**
   * @return the inputProductFiles
   */
  public PGEGroup getInputProductFiles() {
    return inputProductFiles;
  }

  /**
   * @param inputProductFiles
   *          the inputProductFiles to set
   */
  public void setInputProductFiles(PGEGroup inputProductFiles) {
    this.inputProductFiles = inputProductFiles;
  }

  /**
   * @return the staticFileIdentificationFiles
   */
  public PGEGroup getStaticFileIdentificationFiles() {
    return staticFileIdentificationFiles;
  }

  /**
   * @param staticFileIdentificationFiles
   *          the staticFileIdentificationFiles to set
   */
  public void setStaticFileIdentificationFiles(
      PGEGroup staticFileIdentificationFiles) {
    this.staticFileIdentificationFiles = staticFileIdentificationFiles;
  }

  /**
   * @return the dynamicAuxiliaryInputFiles
   */
  public PGEGroup getDynamicAuxiliaryInputFiles() {
    return dynamicAuxiliaryInputFiles;
  }

  /**
   * @param dynamicAuxiliaryInputFiles
   *          the dynamicAuxiliaryInputFiles to set
   */
  public void setDynamicAuxiliaryInputFiles(PGEGroup dynamicAuxiliaryInputFiles) {
    this.dynamicAuxiliaryInputFiles = dynamicAuxiliaryInputFiles;
  }

  /**
   * @return the recordedAuxiliaryInputFiles
   */
  public PGEGroup getRecordedAuxiliaryInputFiles() {
    return recordedAuxiliaryInputFiles;
  }

  /**
   * @param recordedAuxiliaryInputFiles
   *          the recordedAuxiliaryInputFiles to set
   */
  public void setRecordedAuxiliaryInputFiles(
      PGEGroup recordedAuxiliaryInputFiles) {
    this.recordedAuxiliaryInputFiles = recordedAuxiliaryInputFiles;
  }

  /**
   * @return the productPath
   */
  public PGEScalar getProductPath() {
    return productPath;
  }

  /**
   * @param productPath
   *          the productPath to set
   */
  public void setProductPath(PGEScalar productPath) {
    this.productPath = productPath;
  }

  /**
   * @return the monitorPath
   */
  public PGEScalar getMonitorPath() {
    return monitorPath;
  }

  /**
   * @param monitorPath
   *          the monitorPath to set
   */
  public void setMonitorPath(PGEScalar monitorPath) {
    this.monitorPath = monitorPath;
  }

  /**
   * @return the monitorFilenameFormat
   */
  public PGEScalar getMonitorFilenameFormat() {
    return monitorFilenameFormat;
  }

  /**
   * @param monitorFilenameFormat
   *          the monitorFilenameFormat to set
   */
  public void setMonitorFilenameFormat(PGEScalar monitorFilenameFormat) {
    this.monitorFilenameFormat = monitorFilenameFormat;
  }

  /**
   * @return the monitorLevelGroup
   */
  public PGEGroup getMonitorLevelGroup() {
    return monitorLevelGroup;
  }

  /**
   * @param monitorLevelGroup
   *          the monitorLevelGroup to set
   */
  public void setMonitorLevelGroup(PGEGroup monitorLevelGroup) {
    this.monitorLevelGroup = monitorLevelGroup;
  }

  /**
   * @return the pgeSpecificGroups
   */
  public Map<String, PGEGroup> getPgeSpecificGroups() {
    return pgeSpecificGroups;
  }

  /**
   * @param pgeSpecificGroups
   *          the pgeSpecificGroups to set
   */
  public void setPgeSpecificGroups(Map<String, PGEGroup> pgeSpecificGroups) {
    this.pgeSpecificGroups = pgeSpecificGroups;
  }
  
}
