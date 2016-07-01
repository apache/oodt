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
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.opendapps.config.OpendapConfig;
import org.apache.oodt.opendapps.config.OpendapConfigMetKeys;
import org.apache.oodt.opendapps.config.OpendapProfileMetKeys;
import org.apache.oodt.opendapps.config.ProcessingInstructions;
import org.apache.oodt.opendapps.util.ProfileUtils;

import org.springframework.util.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import opendap.dap.Attribute;
import opendap.dap.AttributeTable;
import opendap.dap.DAS;
import opendap.dap.DConnect;

//OPENDAP imports
//OODT imports

/**
 * Implementation of {@link MetadataExtractor} to extract metadata from an
 * OpenDAP DAS source. Currently this class only extracts metadata from the
 * NetCDF global attributes of type String, disregarding all others.
 * 
 * @author Luca Cinquini
 * 
 */
public class DasMetadataExtractor implements MetadataExtractor {

  // constants from NetCDF metadata convention
  public static final String NC_GLOBAL = "NC_GLOBAL";
  public static final String LONG_NAME = "long_name";
  public static final String STANDARD_NAME = "standard_name";

  // NetCDF data types
  public static final int INT32_TYPE = 6;
  public static final int INT64_TYPE = 7;
  public static final int FLOAT32_TYPE = 8;
  public static final int FLOAT64_TYPE = 9;
  public static final int STRING_TYPE = 10;
  
  // output format for all global attributes interpreted as dates
  private final static String OUTPUT_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
  private final static DateFormat outputDatetimeFormat = new SimpleDateFormat(OUTPUT_DATETIME_FORMAT);

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
	public void extract(Metadata metadata, OpendapConfig config) {
	
	  LOG.log(Level.INFO, "Parsing DAS metadata from: " + dConn.URL());
	  
	  // list of excluded variables
	  Set<String> excludedVariables 
	  	= config.getProcessingInstructions().getInstructionValues(OpendapConfigMetKeys.EXCLUDE_VARIABLES_ATTR);
	
	  try {
	    DAS das = dConn.getDAS();
	    @SuppressWarnings("unchecked")
	    Enumeration<String> names = das.getNames();
	    while (names.hasMoreElements()) {
	      String attName = names.nextElement();
	      LOG.log(Level.FINE, "Extracting DAS attribute: " + attName);
	      AttributeTable at = das.getAttributeTable(attName);
	      Enumeration<String> e = at.getNames();
	      
	      // NetCDF global attributes
	      // store attribute name, all values for ALL attributes (strings and numerics)
	      ProcessingInstructions processingInstructions = config.getProcessingInstructions();
	      if (attName.equals(NC_GLOBAL)) {
	      	while (e.hasMoreElements()) {
	      		String key = e.nextElement();
	      		Attribute att = at.getAttribute(key);
	      		// convert all DAS attribute names to lower case
	      		String lkey = key.toLowerCase();
	      		
	      		// look for global attribute name in date/time configuration specification
	      		String dateTimeFormatKey = OpendapConfigMetKeys.DATETIME_FORMAT_ATTR + ":" + lkey;
	      		String dateTimeFormatValue = processingInstructions.getInstructionValue(dateTimeFormatKey);
	      		// add this attribute as properly formatted date/time
	      		if (StringUtils.hasText(dateTimeFormatValue)) {
	      			DateFormat inFormat = new SimpleDateFormat(dateTimeFormatValue);
	      			Enumeration<String> edt = att.getValues();
	      			while (edt.hasMoreElements()) {
	      				String value = edt.nextElement();
	      				try {
  	      				Date date = inFormat.parse(value);
  	      				ProfileUtils.addIfNotNull(metadata, lkey, outputDatetimeFormat.format(date));
	      				} catch(ParseException pe) {
	      					LOG.log(Level.WARNING, 
	      							    "Error parsing date/time from DAS attribute: "+key+" value="+value+" error="+pe.getMessage());
	      				}
	      			}
	      		// add this global attribute as string
	      		} else {
	      			ProfileUtils.addIfNotExisting(metadata, lkey, att.getValues());
	      		}
	      	}
	        
	      // NetCDF coordinates
	      } else {
	      	
	      	if (   attName.equalsIgnoreCase("lat") || attName.equalsIgnoreCase("latitude")
	      			|| attName.equalsIgnoreCase("lon") || attName.equalsIgnoreCase("longitude")
	      			|| attName.equalsIgnoreCase("time")
	      			|| attName.equalsIgnoreCase("alt") || attName.equalsIgnoreCase("altitude")
	      			|| attName.equalsIgnoreCase("lev") || attName.equalsIgnoreCase("level")
	      			|| attName.equalsIgnoreCase("depth")
	      			) {
	      		
	      		if (!excludedVariables.contains(attName)) {
	      			// store coordinate name
	      			ProfileUtils.addIfNotNull(metadata, OpendapProfileMetKeys.COORDINATES, attName);
	      		}
	        	
				} else {
	      		
	      		if (!excludedVariables.contains(attName)) {
	          	// store variable name
	          	ProfileUtils.addIfNotNull(metadata, OpendapProfileMetKeys.VARIABLES, attName);
	          	// store "standard_name", "long_name"
	          	while (e.hasMoreElements()) {
	          		String key = e.nextElement();
	          		Attribute att = at.getAttribute(key);
	          		if (key.equalsIgnoreCase(STANDARD_NAME)) {
	          			ProfileUtils.addIfNotNull(metadata, OpendapProfileMetKeys.CF_STANDARD_NAMES, att.getValueAt(0));
	          		} else if (key.equalsIgnoreCase(LONG_NAME)) {
	          			ProfileUtils.addIfNotNull(metadata, OpendapProfileMetKeys.VARIABLES_LONG_NAMES, att.getValueAt(0));
	          		}       		
	          	}	
	      		}
	        	
	      	}
	      }
	
	    }
	    
	  } catch (Exception e) {
	    LOG.log(Level.WARNING, "Error parsing DAS metadata: " + e.getMessage());
	  }
	
	}

}
