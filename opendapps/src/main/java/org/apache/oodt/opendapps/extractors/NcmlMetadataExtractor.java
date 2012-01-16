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

package org.apache.oodt.opendapps.extractors;

//JDK imports
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;

/**
 * Implementation of {@link MetadataExtractor} that parses an NcML XML document.
 * Currently this class is simply a stub that doesn't do anything.
 * 
 * @author Luca Cinquini
 * 
 */
public class NcmlMetadataExtractor implements MetadataExtractor {

  private final String ncmlUrl;

  private static Logger LOG = Logger.getLogger(NcmlMetadataExtractor.class
      .getName());

  public NcmlMetadataExtractor(String ncmlUrl) {
    this.ncmlUrl = ncmlUrl;
  }

  /**
   * Stub implementation of interface method.
   */
  public void extract(Metadata metadata) {

    LOG.log(Level.INFO, "Parsing NcML metadata from: " + ncmlUrl);

  }

}
