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
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

//OPENDAP imports
import opendap.dap.Attribute;
import opendap.dap.AttributeTable;
import opendap.dap.DAS;
import opendap.dap.DConnect;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.opendapps.util.ProfileUtils;

/**
 * Implementation of {@link MetadataExtractor} to extract metadata from an
 * OpenDAP DAS source. Currently this class only extracts metadata from the
 * NetCDF global attributes of type String, disregarding all others.
 * 
 * @author Luca Cinquini
 * 
 */
public class DasMetadataExtractor implements MetadataExtractor {

  // prefix for all NetCDF global attributes
  public static final String NC_GLOBAL = "NC_GLOBAL";

  // NetCDF data types
  public static final int INT32_TYPE = 6;
  public static final int INT64_TYPE = 7;
  public static final int FLOAT32_TYPE = 8;
  public static final int FLOAT64_TYPE = 9;
  public static final int STRING_TYPE = 10;

  private static Logger LOG = Logger.getLogger(DasMetadataExtractor.class
      .getName());

  /**
   * The DAS stream which is the metadata source.
   */
  private final DConnect dConn;

  public DasMetadataExtractor(DConnect dConn) {
    this.dConn = dConn;
  }

  /**
   * The main metadata extraction method.
   * 
   * @param metadata
   *          : the metadata target, specifically the CAS metadata container.
   */
  public void extract(Metadata metadata) {

    LOG.log(Level.INFO, "Parsing DAS metadata from: " + dConn.URL());

    try {
      DAS das = dConn.getDAS();
      @SuppressWarnings("unchecked")
      Enumeration<String> names = das.getNames();
      while (names.hasMoreElements()) {
        String attName = (String) names.nextElement();
        LOG.log(Level.FINE, "Extracting DAS attribute: " + attName);

        AttributeTable at = das.getAttributeTable(attName);
        Enumeration e = at.getNames();
        while (e.hasMoreElements()) {
          String key = (String) e.nextElement();
          Attribute att = at.getAttribute(key);
          LOG.log(Level.FINER,
              "\t" + att.getName() + " value=" + att.getValueAt(0) + "type="
                  + att.getType());

          // store NetCDF global attributes
          if (attName.equals(NC_GLOBAL)) {
            if (att.getType() == STRING_TYPE) {
              ProfileUtils.addIfNotExisting(metadata, key, att.getValues());
            }
          }
        }

      }
    } catch (Exception e) {
      LOG.log(Level.WARNING, "Error parsing DAS metadata: " + e.getMessage());
    }

  }

}
