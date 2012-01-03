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

//JDK imports
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

//OPeNDAP imports
import opendap.dap.BaseType;
import opendap.dap.DArray;
import opendap.dap.DConnect;
import opendap.dap.DDS;
import opendap.dap.DGrid;

//OODT imports
import static org.apache.oodt.opendapps.config.OpendapConfigMetKeys.ENUM_ELEMENT_TYPE;
import static org.apache.oodt.opendapps.config.OpendapConfigMetKeys.PROF_ATTR_SPEC_TYPE;
import static org.apache.oodt.opendapps.config.OpendapConfigMetKeys.PROF_ELEM_SPEC_TYPE;
import static org.apache.oodt.opendapps.config.OpendapConfigMetKeys.RANGED_ELEMENT_TYPE;
import static org.apache.oodt.opendapps.config.OpendapConfigMetKeys.RES_ATTR_SPEC_TYPE;
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

  private static final Logger LOG = Logger.getLogger(ProfileUtils.class
      .getName());

  public static ResourceAttributes getResourceAttributes(OpendapConfig conf,
      String opendapUrl, DConnect dConn, Metadata datasetMet) {
    ResourceAttributes resAttr = new ResourceAttributes();
    for (ConstantSpec spec : conf.getConstSpecs()) {
      if (spec.getType().equals(RES_ATTR_SPEC_TYPE)) {
        try {
          setResourceAttributes(resAttr, spec.getName(), PathUtils
              .replaceEnvVariables(spec.getValue(), datasetMet, true));
        } catch (Exception e) {
          e.printStackTrace();
          LOG.log(Level.WARNING, "Error setting field: [" + spec.getName()
              + "] in resource attributes: Message: " + e.getMessage());
        }
      }
    }

    // set the identifier
    if (resAttr.getIdentifier() == null
        || (resAttr.getIdentifier() != null && (resAttr.getIdentifier().equals(
            "") || resAttr.getIdentifier().equals("UNKNOWN")))) {
      resAttr.setIdentifier(UUID.randomUUID().toString());
    }

    // set res location
    if (resAttr.getResLocations() == null
        || (resAttr.getResLocations() != null && resAttr.getResLocations()
            .size() == 0)) {
      resAttr.getResLocations().add(opendapUrl);
    }

    return resAttr;
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
      			LOG.log(Level.INFO, "Extracting Darray variable: "+varName);
      		} else if (variable instanceof DGrid) {
      			LOG.log(Level.INFO, "Extracting Dgrid variable: "+varName);
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

    if (datasetMet != null) {
      for (DatasetMetElem datasetSpec : conf.getDatasetMetSpecs()) {
        EnumeratedProfileElement epe = getEnumeratedProfileElement(datasetSpec
            .getProfileElementName(), profile);
        List<String> epeVals = datasetMet
            .getAllMetadata(datasetSpec.getValue());
        if (epeVals != null && epeVals.size() > 0)
          epe.getValues().addAll(epeVals);
        profElements.put(datasetSpec.getProfileElementName(), epe);
      }
    }
    
    // now add const prof elems
    for (ConstantSpec spec : conf.getConstSpecs()) {
      if (spec.getType().equals(PROF_ELEM_SPEC_TYPE)) {
        try {
          EnumeratedProfileElement epe = getEnumeratedProfileElement(spec.getName(), profile);  
          String replaceVal = PathUtils.replaceEnvVariables(spec.getValue(), datasetMet);
          List<String> epeVals = Arrays.asList(replaceVal.split(PathUtils.DELIMITER));
          if (epeVals != null && epeVals.size() > 0)
            epe.getValues().addAll(epeVals);
          profElements.put(spec.getName(), epe);
        } catch (Exception e) {
          e.printStackTrace();
          LOG.log(Level.WARNING, "Error setting field: [" + spec.getName()
              + "] in resource attributes: Message: " + e.getMessage());
        }
      }
    }

    return profElements;

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
    
    if (propName.equals("Identifier")) {
      resAttr.setIdentifier(value);
    } else if (propName.equals("Title")) {
      resAttr.setTitle(value);
    } else if (propName.equals("Format")) {
      resAttr.getFormats().addAll(Arrays.asList(value.split(PathUtils.DELIMITER)));
    } else if (propName.equals("Description")) {
      resAttr.setDescription(value);
    } else if (propName.equals("Creator")) {
      resAttr.getCreators().addAll(Arrays.asList(value.split(PathUtils.DELIMITER)));
    } else if (propName.equals("Subject")) {
      resAttr.getSubjects().addAll(Arrays.asList(value.split(PathUtils.DELIMITER)));
    } else if (propName.equals("Publisher")) {
      resAttr.getPublishers().addAll(Arrays.asList(value.split(PathUtils.DELIMITER)));
    } else if (propName.equals("Contributor")) {
      resAttr.getContributors().addAll(Arrays.asList(value.split(PathUtils.DELIMITER)));
    } else if (propName.equals("Date")) {
      resAttr.getDates().addAll(Arrays.asList(value.split(PathUtils.DELIMITER)));
    } else if (propName.equals("Type")) {
      resAttr.getTypes().addAll(Arrays.asList(value.split(PathUtils.DELIMITER)));
    } else if (propName.equals("Source")) {
      resAttr.getSources().addAll(Arrays.asList(value.split(PathUtils.DELIMITER)));
    } else if (propName.equals("Language")) {
      resAttr.getLanguages().addAll(Arrays.asList(value.split(PathUtils.DELIMITER)));
    } else if (propName.equals("Relation")) {
      resAttr.getRelations().addAll(Arrays.asList(value.split(PathUtils.DELIMITER)));
    } else if (propName.equals("Coverage")) {
      resAttr.getCoverages().addAll(Arrays.asList(value.split(PathUtils.DELIMITER)));
    } else if (propName.equals("Rights")) {
      resAttr.getRights().addAll(Arrays.asList(value.split(PathUtils.DELIMITER)));
    } else if (propName.equals("resContext")) {
      resAttr.getResContexts().addAll(Arrays.asList(value.split(PathUtils.DELIMITER)));
    } else if (propName.equals("resAggregation")) {
      resAttr.setResAggregation(value);
    } else if (propName.equals("resClass")) {
      resAttr.setResClass(value);
    } else if (propName.equals("resLocation")) {
      resAttr.getResLocations().addAll(Arrays.asList(value.split(PathUtils.DELIMITER)));
    }
  }

  private static EnumeratedProfileElement getEnumeratedProfileElement(
      String name, Profile profile) {
    EnumeratedProfileElement pe = new EnumeratedProfileElement(profile);
    pe.setName(name);
    return pe;
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

}
