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

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;

/**
 * Interface for extracting metadata from a generic web accessible resource into
 * a CAS metadata container. Each implementation class must be responsible for
 * instantiating and accessing the specific metadata source as appropriate.
 * 
 * @author Luca Cinquini
 * 
 */
public interface MetadataExtractor {

  /**
   * Method to (further) populate the metadata container. Any extracted metadata
   * is added to the current metadata content.
   * 
   * @param metadata
   */
  void extract(Metadata metadata);

}
