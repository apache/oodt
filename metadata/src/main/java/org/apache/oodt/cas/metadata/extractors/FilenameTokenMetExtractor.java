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
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.exceptions.MetExtractionException;
import org.apache.oodt.commons.date.DateUtils;

import java.io.File;
import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Logger;

//OODT imports

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

  private static final Logger LOG = Logger
      .getLogger(FilenameTokenMetExtractor.class.getName());

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
   * @see org.apache.oodt.cas.metadata.AbstractMetExtractor#extrMetadata(java.io
   * .File)
   */
  @Override
  protected Metadata extrMetadata(File file) throws MetExtractionException {
    Metadata met = new Metadata();
    String filename = file.getName();
    if (((FilenameTokenConfig) this.config).hasTokenNameList()) {
      List<String> metKeyTokens = ((FilenameTokenConfig) this.config)
          .getTokenMetKeyNames();
      String[] filenameToks = filename.split("\\.")[0]
          .split(((FilenameTokenConfig) this.config).getTokenDelimeterScalar());
      for (int i = 0; i < filenameToks.length; i++) {
        String keyName = metKeyTokens.get(i);
        String keyVal = filenameToks[i];
        if (keyName.equals("ProductionDateTime")) {
          Calendar cal = GregorianCalendar.getInstance();
          try {
            cal.setTime(((FilenameTokenConfig) this.config).getDateFormatter()
                .parse(keyVal));
          } catch (ParseException e) {
            throw new MetExtractionException(e.getMessage());
          }
          keyVal = DateUtils.toString(cal);
        }

        met.addMetadata(keyName, keyVal);
      }
    }

    Metadata commonMet = ((FilenameTokenConfig) this.config).getCommonMet();
    met.addMetadata(commonMet.getMap());
    met.addMetadata(((FilenameTokenConfig) this.config)
        .getSubstringOffsetMet(file));

    met.addMetadata("Filename", file.getName());
    met.addMetadata("FileLocation", file.getParentFile().getAbsolutePath());
    return met;
  }

  public static void main(String[] args) throws Exception {
    processMain(args, new FilenameTokenMetExtractor());
  }

}
