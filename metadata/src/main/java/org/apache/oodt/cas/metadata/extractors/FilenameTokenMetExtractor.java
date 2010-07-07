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
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.exceptions.MetExtractionException;
import org.apache.oodt.cas.metadata.extractors.CmdLineMetExtractor;
import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.oodt.pcs.input.PGEConfigurationFile;
import org.apache.oodt.pcs.input.PGEGroup;
import org.apache.oodt.pcs.input.PGEScalar;
import org.apache.oodt.pcs.input.PGEVector;

/**
 * 
 * Extracts out {@link Metadata} using the {@link File#getName()} and a
 * {@link FilenameTokenConfig} to specify what pieces of the filename map to
 * particular Metadata key names.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class FilenameTokenMetExtractor extends CmdLineMetExtractor implements
    FilenameTokenExtractorMetKeys {
  
  private static final Logger LOG = Logger.getLogger(FilenameTokenMetExtractor.class.getName());

  /**
   * Default constructor.
   */
  public FilenameTokenMetExtractor() {
    super(new FilenameTokenConfigReader());
    // TODO Auto-generated constructor stub
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.metadata.AbstractMetExtractor#extrMetadata(java.io
   * .File)
   */
  @Override
  protected Metadata extrMetadata(File file) throws MetExtractionException {
    Metadata met = getCoreMet(file);
    addCommonMetadata(met);
    return met;
  }
  
  public static void main(String [] args) throws Exception{
    processMain(args, new FilenameTokenMetExtractor());
  }

  private Metadata getCoreMet(File file) {
    PGEGroup substrOffsetGroup = getConf().getPgeSpecificGroups().get(
        SUBSTRING_OFFSET_GROUP);
    Metadata met = new Metadata();
    String filename = file.getName();

    for (PGEVector vec : substrOffsetGroup.getVectors().values()) {
      String metKeyName = vec.getName();
      LOG.log(Level.FINE, "Extracting key: ["+metKeyName+"]");
      int offset = Integer.valueOf((String) vec.getElements().get(0)) - 1;
      int length = Integer.valueOf((String) vec.getElements().get(1));
      String metVal = filename.substring(offset, offset + length).trim();
      met.addMetadata(metKeyName, metVal);
    }

    return met;
  }

  private void addCommonMetadata(Metadata met) {
    PGEGroup commonMetGroup = this.getConf().getPgeSpecificGroups().get(
        COMMON_METADATA_GROUP);

    for (PGEScalar metScalar : commonMetGroup.getScalars().values()) {
      met.addMetadata(metScalar.getName(), PathUtils.replaceEnvVariables(
          metScalar.getValue(), met));
    }
  }

  private PGEConfigurationFile getConf() {
    return ((FilenameTokenConfig) this.config).getConf();
  }

}
