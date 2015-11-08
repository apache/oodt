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
import org.apache.oodt.cas.metadata.exceptions.MetExtractionException;
import org.apache.oodt.cas.metadata.extractors.FilenameTokenMetExtractor;

import java.io.File;


/**
 * 
 * Wrap CAS-Metadata's {@link FilenameTokenMetExtractor} as a CAS-PGE
 * {@link PcsMetFileWriter}. First arg passed in is the config file full path.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class FilenameExtractorWriter extends PcsMetFileWriter {

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.pge.writers.PcsMetFileWriter#getSciPgeSpecificMetadata
   * (java.io.File, org.apache.oodt.cas.metadata.Metadata, java.lang.Object[])
   */
  @Override
  protected Metadata getSciPgeSpecificMetadata(File generatedFile,
      Metadata workflowMet, Object... args) throws MetExtractionException {
    String metConfFilePath = String.valueOf(args[0]);
    FilenameTokenMetExtractor extractor = new FilenameTokenMetExtractor();
    extractor.setConfigFile(metConfFilePath);
    return extractor.extractMetadata(generatedFile);
  }

}
