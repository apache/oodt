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

package org.apache.oodt.pcs.listing;

//OODT imports
import org.apache.oodt.pcs.input.PGEConfigFileException;
import org.apache.oodt.pcs.input.PGEConfigFileReader;
import org.apache.oodt.pcs.input.PGEConfigurationFile;

//JDK imports
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.List;

import static org.apache.oodt.pcs.listing.ListingConfKeys.*;


/**
 * 
 * The configuration for the {@link org.apache.oodt.pcs.tools.PCSLongLister}.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class ListingConf {

  private PGEConfigurationFile conf;

  /**
   * Constructs a new ListingConf with the given {@link File}.
   * 
   * @param file
   *          The configuration file.
   * @throws FileNotFoundException
   *           If the config file cannot be found.
   * @throws InstantiationException
   *           If there is some error reading the config file.
   */
  public ListingConf(File file) throws FileNotFoundException,
      InstantiationException {
    try {
      this.conf = new PGEConfigFileReader().read(new FileInputStream(file));
    } catch (PGEConfigFileException e) {
      throw new InstantiationException(e.getMessage());
    } finally {
      if (this.conf == null) {
        throw new InstantiationException("Configuration is null!");
      }
    }
  }

  /**
   * Returns the set of excluded product types.
   * 
   * @return The set of excluded product types.
   */
  public List<String> getExcludedTypes() {
    if (this.conf.getPgeSpecificGroups().get(EXCLUDED_PRODUCT_TYPE_GROUP) == null) {
      return Collections.EMPTY_LIST;
    }

    return (List<String>) (List<?>) this.conf.getPgeSpecificGroups().get(
        EXCLUDED_PRODUCT_TYPE_GROUP).getVector(EXCLUDED_VECTOR).getElements();

  }

  /**
   * Gets the set of Header column met key names for the long lister.
   * 
   * @return The set of Header column met key names for the long lister.
   */
  public List<String> getHeaderColKeys() {
    if (this.conf.getPgeSpecificGroups().get(MET_FIELD_COLS_GROUP) == null) {
      return Collections.EMPTY_LIST;
    }

    return (List<String>) (List<?>) this.conf.getPgeSpecificGroups().get(
        MET_FIELD_COLS_GROUP).getVector(MET_FIELDS_ORDER_VECTOR).getElements();
  }

  /**
   * Returns the display name for a particular header col key.
   * 
   * @param headerColKey
   *          The header col met key to look up the display name for.
   * @return The header col met key's display name.
   */
  public String getHeaderColDisplayName(String headerColKey) {
    if (this.conf.getPgeSpecificGroups().get(MET_FIELD_COLS_GROUP) == null) {
      return "";
    }

    return this.conf.getPgeSpecificGroups().get(MET_FIELD_COLS_GROUP)
        .getScalar(headerColKey).getValue();
  }

  /**
   * Tester method to determine if an output field is a collection field, and
   * needs special handling.
   * 
   * @param colName
   *          The field name to check.
   * @return True if it's a collection field, false if not, or if the collection
   *         fields group doesn't exist in the conf.
   */
  public boolean isCollectionField(String colName) {
    if (this.conf.getPgeSpecificGroups().get(COLLECTION_FIELDS_GROUP) == null) {
      return false;
    }

    return this.conf.getPgeSpecificGroups().get(COLLECTION_FIELDS_GROUP)
        .getVector(COLLECTION_FIELDS_NAMES).getElements().contains(colName);
  }

}
