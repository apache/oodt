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

package org.apache.oodt.cas.pge.writers;

import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.exceptions.CasMetadataException;
import org.apache.oodt.cas.metadata.exceptions.MetExtractionException;
import org.apache.oodt.cas.metadata.exceptions.MetExtractorConfigReaderException;
import org.apache.oodt.cas.metadata.extractors.ExternConfigReader;
import org.apache.oodt.cas.metadata.extractors.ExternMetExtractor;
import org.apache.oodt.commons.exceptions.CommonsException;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;



/**
 * 
 * Wraps the OODT CAS {@link ExternMetExtractor} and exposes it as a CAS-PGE
 * {@link PcsMetFileWriter}.
 * 
 */
public class ExternExtractorMetWriter extends PcsMetFileWriter {

  @Override
  protected Metadata getSciPgeSpecificMetadata(File sciPgeConfigFilePath, Metadata inputMetadata, Object... customArgs)
      throws MetExtractorConfigReaderException, MetExtractionException, FileNotFoundException,
      ParseException, CommonsException, CasMetadataException {
    ExternMetExtractor extractor = new ExternMetExtractor();
    extractor.setConfigFile(new ExternConfigReader().parseConfigFile(new File(
        (String) customArgs[0])));
    Metadata m = new Metadata();
    m.addMetadata(extractor.extractMetadata(sciPgeConfigFilePath)
                           .getMap(), true);
    return m;  }
}
