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

package org.apache.oodt.cas.metadata.extractors;

//JDK imports
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.List;

//OODT imports
import org.apache.oodt.cas.metadata.MetExtractorConfig;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.util.PathUtils;
import static org.apache.oodt.cas.metadata.extractors.FilenameTokenExtractorMetKeys.*;
import org.apache.oodt.pcs.input.PGEConfigFileException;
import org.apache.oodt.pcs.input.PGEConfigFileReader;
import org.apache.oodt.pcs.input.PGEConfigurationFile;
import org.apache.oodt.pcs.input.PGEGroup;
import org.apache.oodt.pcs.input.PGEScalar;
import org.apache.oodt.pcs.input.PGEVector;

/**
 * 
 * Wraps a {@link PGEConfigurationFile} as a {@link MetExtractorConfig}.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class FilenameTokenConfig implements MetExtractorConfig {

  private PGEConfigurationFile conf;

  public FilenameTokenConfig() {
    this.conf = null;
  }

  public FilenameTokenConfig(PGEConfigurationFile conf) {
    this.conf = conf;
  }

  public void setConfig(String confFilePath) throws FileNotFoundException,
      PGEConfigFileException {
    this.conf = new PGEConfigFileReader().read(new FileInputStream(new File(
        confFilePath)));
  }

  public SimpleDateFormat getDateFormatter() {
    return new SimpleDateFormat(this.conf.getPgeSpecificGroups().get(
        PRODUCTION_DATE_TIME_GROUP).getScalar(DATETIME_SCALAR).getValue());
  }

  public boolean hasTokenNameList() {
    return this.conf.getPgeSpecificGroups().get(TOKEN_LIST_GROUP) != null;
  }

  public String getTokenDelimeterScalar() {
    return this.conf.getPgeSpecificGroups().get(TOKEN_LIST_GROUP).getScalar(
        TOKEN_DELIMETER_SCALAR).getValue();
  }

  public List<String> getTokenMetKeyNames() {
    return (List<String>) (List<?>) this.conf.getPgeSpecificGroups().get(
        TOKEN_LIST_GROUP).getVector(TOKEN_MET_KEYS_VECTOR).getElements();
  }

  public Metadata getSubstringOffsetMet(File file) {
    PGEGroup substrOffsetGroup = this.conf.getPgeSpecificGroups().get(
        SUBSTRING_OFFSET_GROUP);
    Metadata met = new Metadata();
    if (substrOffsetGroup == null) {
      return met;
    }
    String filename = file.getName();

    for (PGEVector vec : substrOffsetGroup.getVectors().values()) {
      String metKeyName = vec.getName();
      int offset = Integer.valueOf((String) vec.getElements().get(0)) - 1;
      int length = Integer.valueOf((String) vec.getElements().get(1));
      String metVal = filename.substring(offset, offset + length).trim();
      met.addMetadata(metKeyName, metVal);
    }

    return met;
  }

  public Metadata getCommonMet() {
    PGEGroup commonMetGroup = this.conf.getPgeSpecificGroups().get(
        COMMON_METADATA_GROUP);
    Metadata met = new Metadata();
    for (String scalarName : commonMetGroup.getScalars().keySet()) {
      PGEScalar scalar = commonMetGroup.getScalar(scalarName);
      met.addMetadata(scalar.getName(), PathUtils.replaceEnvVariables(scalar
          .getValue()));
    }

    for (String vecName : commonMetGroup.getVectors().keySet()) {
      PGEVector vec = commonMetGroup.getVector(vecName);
      for (String val : (List<String>) (List<?>) vec.getElements()) {
        met.addMetadata(vecName, PathUtils.replaceEnvVariables(val));
      }
    }

    return met;
  }

  /**
   * @return the conf
   */
  public PGEConfigurationFile getConf() {
    return conf;
  }

  /**
   * @param conf
   *          the conf to set
   */
  public void setConf(PGEConfigurationFile conf) {
    this.conf = conf;
  }

}
