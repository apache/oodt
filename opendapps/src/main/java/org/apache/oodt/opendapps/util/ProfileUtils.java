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

package org.apache.oodt.opendapps.util;

import static org.apache.oodt.opendapps.config.OpendapConfigMetKeys.ENUM_ELEMENT_TYPE;
import static org.apache.oodt.opendapps.config.OpendapConfigMetKeys.PROF_ATTR_SPEC_TYPE;
import static org.apache.oodt.opendapps.config.OpendapConfigMetKeys.PROF_ELEM_SPEC_TYPE;
import static org.apache.oodt.opendapps.config.OpendapConfigMetKeys.RANGED_ELEMENT_TYPE;
import static org.apache.oodt.opendapps.config.OpendapConfigMetKeys.RES_ATTR_SPEC_TYPE;

//JDK imports
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

//OPENDAP imports
import opendap.dap.BaseType;
import opendap.dap.DArray;
import opendap.dap.DConnect;
import opendap.dap.DDS;
import opendap.dap.DGrid;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.oodt.opendapps.OpendapProfileElementExtractor;
import org.apache.oodt.opendapps.config.ConstantSpec;
import org.apache.oodt.opendapps.config.DatasetMetElem;
import org.apache.oodt.opendapps.config.OpendapConfig;
import org.apache.oodt.opendapps.config.RewriteSpec;
import org.apache.oodt.profile.EnumeratedProfileElement;
import org.apache.oodt.profile.Profile;
import org.apache.oodt.profile.ProfileAttributes;
import org.apache.oodt.profile.ProfileElement;
import org.apache.oodt.profile.ResourceAttributes;
import org.springframework.util.StringUtils;

/**
 * 
 * Static methods for unraveling and generating {@link ProfileElement}s,
 * {@link ProfileAttributes} and {@link ResourceAttributes} for a
 * {@link Profile}, derived from a set of OPeNDAP dataset information and an
 * {@link OpendapConfig}.
 * 
 * 
 */
public class ProfileUtils {
	
	static {
		// Note: must override the CAS PathUtils delimiter otherwise every sentence is split at the ',' as different metadata fields.
		// The delimiter must be a character that is not commonly used in the metadata values, 
		// and that it does not a special regular expression character.
		// Cannot use '#' as it is used in URL anchors, such as THREDDS urls.
		// Cannot user '?', '&' as they are used in URL query strings.
		// Cannot use '|' as it is used as multi-part separators in encoding of metadata fields.
		PathUtils.DELIMITER = "~";
	}
	
  // character separating multiple parts of the same metadata field,
  // when more than one piece of information needs to be stored in one field
  public final static String CHAR = "|";
  
  // HTTP mime types
  public final static String MIME_TYPE_THREDDS = "application/xml+thredds";   
  public final static String MIME_TYPE_NETCDF = "application/netcdf";
  public final static String MIME_TYPE_GRIDFTP = "application/gridftp";
  public final static String MIME_TYPE_FTP = "application/ftp";
  public final static String MIME_TYPE_LAS = "application/las";   
  public final static String MIME_TYPE_HTML = "text/html";
  public final static String MIME_TYPE_GOOGLE_EARTH = "application/vnd.google-earth.kmz";
  public final static String MIME_TYPE_HDF = "application/x-hdf";
  public final static String MIME_TYPE_OPENDAP = "application/opendap";
  public final static String MIME_TYPE_OPENDAP_DODS = "application/opendap-dods";
  public final static String MIME_TYPE_OPENDAP_DAS = "application/opendap-das";
  public final static String MIME_TYPE_OPENDAP_DDS = "application/opendap-dds";
  public final static String MIME_TYPE_OPENDAP_HTML = "application/opendap-html";
  public final static String MIME_TYPE_RSS = "application/rss+xml";
  public final static String MIME_TYPE_GIS = "application/gis";


  private static final Logger LOG = Logger.getLogger(ProfileUtils.class
      .getName());

  public static ResourceAttributes getResourceAttributes(OpendapConfig conf,
      String opendapUrl, DConnect dConn, Metadata datasetMet) {
    ResourceAttributes resAttr = new ResourceAttributes();
    for (ConstantSpec spec : conf.getConstSpecs()) {
      if (spec.getType().equals(RES_ATTR_SPEC_TYPE)) {
        try {        	
        	      	        	
        	// first process expanded '[@...]' instructions
        	List<String> values = multipleEnvVariablesReplacement(spec.getValue(), datasetMet);
        	
        	// then process standard '[...]' instructions
        	for (String value : values) {
          	String _value = PathUtils.replaceEnvVariables(value, datasetMet, true);          		        		
        		if (StringUtils.hasText(_value)) {
        			setResourceAttributes(resAttr, spec.getName(), _value);
        		}
        		
        	}
 
        } catch (Exception e) {
          e.printStackTrace();
          LOG.log(Level.WARNING, "Error setting field: [" + spec.getName()
              + "] in resource attributes: Message: " + e.getMessage());
        }
      }
    }


    return resAttr;
  }
  
  /**
   * Utility method to process environment replacement instructions of the form '[@key]'
   * resulting in as many output values as there are values for the environment variable 'key'.
   * Note that currently only one such pattern '[@key']' can be processed.
   * 
   * @param value
   * @param metadata
   * @return
   */
  private static List<String> multipleEnvVariablesReplacement(String value, Metadata metadata) {
  	
  	List<String> newValues = new ArrayList<String>();

  	// regexp matching found > replace values
  	int start = value.indexOf("[@");
  	if (start>=0) {
  		
  			int end = value.indexOf("]",start+2);
  			// remove '[@',']' to obtain environment variable key
  			String envKey = value.substring(start+2,end);
    		List<String> envValues = metadata.getAllMetadata(envKey);
    		if (envValues!=null) {
      		for (String envValue : envValues) {
      			// create new metadata value for this environment replacement
      			String newValue = value.replaceAll("\\[@"+envKey+"\\]", envValue);
      			newValues.add(newValue);
      		}
  		}
  		
    // regexp matching not found > return original value
  	} else {
  		newValues.add(value);
  	}
  	
  	return newValues;
  	
  }

  public static ProfileAttributes getProfileAttributes(OpendapConfig conf, Metadata datasetMet) {
    ProfileAttributes profAttr = new ProfileAttributes();
    for (ConstantSpec spec : conf.getConstSpecs()) {
      if (spec.getType().equals(PROF_ATTR_SPEC_TYPE)) {
        setProfileAttributesProperty(profAttr, spec.getName(), PathUtils
            .replaceEnvVariables(spec.getValue(), datasetMet, true));
      }
    }

    return profAttr;

  }

  public static Map<String, ProfileElement> getProfileElements(
      OpendapConfig conf, DConnect dConn, Metadata datasetMet, Profile profile) throws Exception {
  	
    OpendapProfileElementExtractor pe = new OpendapProfileElementExtractor(conf);
    Map<String, ProfileElement> profElements = new HashMap<String, ProfileElement>();

    // extracts all variables defined in DDS
    try {
      	
    		DDS dds = dConn.getDDS();
      	      	
      	// loop over all variables found
      	Enumeration variables = dds.getVariables();
      	while (variables.hasMoreElements()) {
      		
      		BaseType variable = (BaseType)variables.nextElement();
      		String varName = variable.getName();
      		if (variable instanceof DArray) {
      			LOG.log(Level.FINE, "Extracting Darray variable: "+varName);
      		} else if (variable instanceof DGrid) {
      			LOG.log(Level.FINE, "Extracting Dgrid variable: "+varName);
      		}     		

      		RewriteSpec spec = getProfileElementSpec(varName, conf);
      		if (spec!=null) {
      			// use configuration to set variable re-name and type
      			String peName = spec.getRename() != null && !spec.getRename().equals("") ? spec.getRename() : spec.getOrigName();
            if (spec.getElementType().equals(RANGED_ELEMENT_TYPE)) {
              profElements.put(peName, pe.extractRangedProfileElement(peName, spec.getOrigName(), profile, dConn.getDAS()));
            } else if (spec.getElementType().equals(ENUM_ELEMENT_TYPE)) {
              profElements.put(peName, pe.extractEnumeratedProfileElement(peName, spec.getOrigName(), profile, dConn.getDAS()));
            }
      		} else {
      			// if not explicitly configured, assume variable if of RANGED_ELEMENT_TYPE
      			profElements.put(varName, pe.extractRangedProfileElement(varName, varName, profile, dConn.getDAS()));
      		}
      		
      	}
      	
    } catch(Exception e) {
      e.printStackTrace();
      LOG.log(Level.WARNING, "Error extracting metadata from DDS ("+dConn.URL()+") :"  +e.getMessage());
      // rethrow the exception so that this dataset is not harvested
      throw e;
    }

    // add profile elements from <datasetMetadata> specification
    if (datasetMet != null) {
      for (DatasetMetElem datasetSpec : conf.getDatasetMetSpecs()) {
      	// retrieve values from metadata container
        List<String> values = datasetMet.getAllMetadata(datasetSpec.getValue());
      	addValuesToEnumeratedProfileElement(datasetSpec.getProfileElementName(), values, profile, profElements);
      }
    }
    
    // add profile elements from <constants> specification
    for (ConstantSpec spec : conf.getConstSpecs()) {
      if (spec.getType().equals(PROF_ELEM_SPEC_TYPE)) {
      	// retrieve value from XML configuration file, replace with value from metadata container if required,
      	// split according to delimiter
        String replaceVal = PathUtils.replaceEnvVariables(spec.getValue(), datasetMet);
        List<String> values = Arrays.asList(replaceVal.split(PathUtils.DELIMITER));
      	addValuesToEnumeratedProfileElement(spec.getName(), values, profile, profElements);
      }
    }

    return profElements;

  }
  
  /**
   * Method to add one or more values to an EnumeratedProfileElement, creating the element if not existing already.
   * The supplied values are added only if valid.
   */
  private static void addValuesToEnumeratedProfileElement(final String name, final List<String> values, Profile profile, Map<String, ProfileElement> profElements) {
  	
  	// try retrieve existing profile element
  	ProfileElement epe = profElements.get(name);
  	// or create a new one if not found
  	if (epe==null) {
  		 epe = new EnumeratedProfileElement(profile);
  	   epe.setName(name);
  	} 
  	if (values!=null) {
      for (String value : values) {
      	if (StringUtils.hasText(value) && !value.equalsIgnoreCase("null")) {
      		epe.getValues().add(value);
      	}
      }
  	}
    // only save profile elements with one or more values
    if (epe.getValues().size()>0) profElements.put(name, epe);
  	
  }

  private static void setProfileAttributesProperty(ProfileAttributes attr,
      String propName, String value) {
    if (propName.equals("profId")) {
      attr.setID(value);
    } else if (propName.equals("profVersion")) {
      attr.setVersion(value);
    } else if (propName.equals("profType")) {
      attr.setType(value);
    } else if (propName.equals("profStatusId")) {
      attr.setStatusID(value);
    } else if (propName.equals("profSecurityType")) {
      attr.setSecurityType(value);
    } else if (propName.equals("profParentId")) {
      attr.setParent(value);
    } else if (propName.equals("profRegAuthority")) {
      attr.setRegAuthority(value);
    } else if (propName.equals("profChildId")) {
      attr.getChildren().addAll(Arrays.asList(value.split(PathUtils.DELIMITER)));
    } else if (propName.equals("profRevisionNote")) {
      attr.getRevisionNotes().addAll(Arrays.asList(value.split(PathUtils.DELIMITER)));
    }

  }

  private static void setResourceAttributes(ResourceAttributes resAttr,
      String propName, String value) {
    if (StringUtils.hasText(value) && !value.equalsIgnoreCase("null")) {
      if (propName.equals("Identifier")) {
        resAttr.setIdentifier(value);
      } else if (propName.equals("Title")) {
        resAttr.setTitle(value);
      } else if (propName.equals("Format")) {
        resAttr.getFormats().addAll( parseValues(value) );
      } else if (propName.equals("Description")) {
        resAttr.setDescription(value);
      } else if (propName.equals("Creator")) {
        resAttr.getCreators().addAll( parseValues(value) );
      } else if (propName.equals("Subject")) {
        resAttr.getSubjects().addAll( parseValues(value) );
      } else if (propName.equals("Publisher")) {
        resAttr.getPublishers().addAll( parseValues(value) );
      } else if (propName.equals("Contributor")) {
        resAttr.getContributors().addAll( parseValues(value) );
      } else if (propName.equals("Date")) {
        resAttr.getDates().addAll( parseValues(value) );
      } else if (propName.equals("Type")) {
        resAttr.getTypes().addAll( parseValues(value) );
      } else if (propName.equals("Source")) {
        resAttr.getSources().addAll( parseValues(value) );
      } else if (propName.equals("Language")) {
        resAttr.getLanguages().addAll( parseValues(value) );
      } else if (propName.equals("Relation")) {
        resAttr.getRelations().addAll( parseValues(value) );
      } else if (propName.equals("Coverage")) {
        resAttr.getCoverages().addAll( parseValues(value) );
      } else if (propName.equals("Rights")) {
        resAttr.getRights().addAll( parseValues(value) );
      } else if (propName.equals("resContext")) {
        resAttr.getResContexts().addAll( parseValues(value) );
      } else if (propName.equals("resAggregation")) {
        resAttr.setResAggregation(value);
      } else if (propName.equals("resClass")) {
        resAttr.setResClass(value);
      } else if (propName.equals("resLocation")) {
        resAttr.getResLocations().addAll( parseValues(value) );
      }
    }
  }
  
  /**
   * Utility method to discover the rewrite specification for a named variable, if available.
   * @param name
   * @param conf
   */
  private static RewriteSpec getProfileElementSpec(String origName, OpendapConfig conf) {
  	for (RewriteSpec spec : conf.getRewriteSpecs()) {
  		if (spec.getOrigName().equals(origName)) {
  			return spec;
  		}
  	}
  	return null;
  }
  
  /**
   * Utility method to split a metadata field value according to the known delimiter.
   * @param value
   * @return
   */
  public static List<String> parseValues(String value) {
  	List<String> values = new ArrayList<String>();
  	for (String val : value.split(PathUtils.DELIMITER)) {
  		if (StringUtils.hasText(val) && !val.equalsIgnoreCase("null")) {
  			values.add(val);
  		}
  	}
  	
  	return values;
  }
  
  /**
   * Method to add a (name,value) pair to the metadata container if the value is not null or empty,
   * and doesn't exist already.
   * @param met
   * @param field
   * @param value
   */
  public static void addIfNotNull(Metadata met, String key, String value) {
  	// do not add a null value
  	if (StringUtils.hasText(value) && !value.equalsIgnoreCase("null")) {
  		// do not add the same value twice for the same metadata key
  		if (!met.containsKey(key) || !met.getAllMetadata(key).contains(value)) {
  			met.addMetadata(key, value);
  		}
    }
  }
  
	/**
	 * Method to add multiple (key, value) pairs to the metadata container if not existing already.
	 * @param met
	 * @param field
	 * @param value
	 */
	public static void addIfNotExisting(Metadata metadata, String key, Enumeration<String> values) {
		if (StringUtils.hasText(key) && !metadata.containsKey(key)) {
			while (values.hasMoreElements()) {
				String value = values.nextElement();
				if (StringUtils.hasText(value) && !value.equalsIgnoreCase("null")) {
					metadata.addMetadata(key,value);
				}
			}
		}
	}
  
  // inspired from ASLv2 code at:
  // http://www.java2s.com/Code/Java/Data-Type/ISO8601dateparsingutility.htm
  public static String toISO8601(Date date) {
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    TimeZone tz = TimeZone.getTimeZone("UTC");
    df.setTimeZone(tz);
    return df.format(date);
  }

}
