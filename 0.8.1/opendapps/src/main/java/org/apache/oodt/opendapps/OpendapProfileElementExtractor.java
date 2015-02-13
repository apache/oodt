/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.oodt.opendapps;

//JDK imports
import java.util.Arrays;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

//OPeNDAP/THREDDS imports
import opendap.dap.Attribute;
import opendap.dap.AttributeTable;
import opendap.dap.DAS;
import opendap.dap.NoSuchAttributeException;

//OODT imports
import org.apache.oodt.opendapps.config.OpendapConfig;
import org.apache.oodt.profile.EnumeratedProfileElement;
import org.apache.oodt.profile.Profile;
import org.apache.oodt.profile.ProfileElement;
import org.apache.oodt.profile.RangedProfileElement;

import static org.apache.oodt.opendapps.DapNames.*;

/**
 * 
 * 
 * This class is used to set custom functionality for scraping data into
 * different types of objects. The class looks at the {@link OpendapConfig} and
 * then tries to stuff what's in each &lt;var&gt; into
 * {@link RangedProfileElement} or {@link EnumeratedProfileElement}. The class
 * is designed with extensibility in mind in case new {@link ProfileElement}
 * types are created in the future.
 * 
 */
public class OpendapProfileElementExtractor {

  private static final Logger LOG = Logger
      .getLogger(OpendapProfileElementExtractor.class.getName());

  private OpendapConfig conf;

  public OpendapProfileElementExtractor(OpendapConfig conf) {
    this.conf = conf;
  }

  public RangedProfileElement extractRangedProfileElement(String elemName, String varname,
      Profile profile, DAS das) throws NoSuchAttributeException {
    RangedProfileElement elem = new RangedProfileElement(profile);
    elem.setName(elemName);
    AttributeTable attTable = null;
    try {
      attTable = das.getAttributeTable(varname);
      
      // make variable names case insensitive
      if(attTable == null) attTable = das.getAttributeTable(varname.toLowerCase());
      if(attTable == null) attTable = das.getAttributeTable(varname.toUpperCase());
      if(attTable == null) throw new NoSuchAttributeException("Att table for ["+varname+"] is null!");
    } catch (NoSuchAttributeException e) {
      e.printStackTrace();
      LOG.log(Level.WARNING, "Error extracting attribute table for element: ["
          + elemName + "]: Message: " + e.getMessage());
      throw e;

    }

    Enumeration attributeNames = attTable.getNames();

    while (attributeNames.hasMoreElements()) {
      String attrName = (String) attributeNames.nextElement();
      Attribute attr = attTable.getAttribute(attrName);
     
      if (!attr.isContainer()) {
      	 Enumeration attrValues = null;
        
        	try {
            attrValues = attr.getValues();
          } catch (NoSuchAttributeException e) {
            e.printStackTrace();
            LOG.log(Level.WARNING, "Attempt to resolve attribute: [" + attrName
                + "] failed: Message: " + e.getMessage());
            continue;
         }
  
        while (attrValues.hasMoreElements()) {
          String attrValue = (String) attrValues.nextElement();
          if (attrName.equals(ACTUAL_RANGE)) {
            elem.setMinValue(attrValue);
            if (attrValues.hasMoreElements()) {
              elem.setMaxValue((String) attrValues.nextElement());
            }
          } else if (attrName.equals(UNITS)) {
            elem.setUnit(attrValue);
          } else if (attrName.equals(START)) {
            elem.setMinValue(attrValue);
          } else if (attrName.equals(END)) {
            elem.setMaxValue(attrValue);
          }
        }
      }
      
    } // not a container attribute
    
    return elem;
  }

  public EnumeratedProfileElement extractEnumeratedProfileElement(String elemName, String varname,
      Profile profile, DAS das)
      throws NoSuchAttributeException {
    EnumeratedProfileElement elem = new EnumeratedProfileElement(profile);
    elem.setName(elemName);

    AttributeTable attTable = null;
    try {
      attTable = das.getAttributeTable(elemName);
    } catch (NoSuchAttributeException e) {
      LOG.log(Level.WARNING, "Error extracting attribute table for element: ["
          + elemName + "]: Message: " + e.getMessage());
      throw e;

    }

    Enumeration attributeNames = attTable.getNames();
    while (attributeNames.hasMoreElements()) {
      String attrName = (String) attributeNames.nextElement();
      Attribute attr = attTable.getAttribute(attrName);
      Enumeration attrValues = null;
      try {
        attrValues = attr.getValues();
      } catch (NoSuchAttributeException e) {
        LOG.log(Level.WARNING, "Attempt to resolve attribute: [" + attrName
            + "] failed: Message: " + e.getMessage());
        continue;
      }

      while (attrValues.hasMoreElements()) {
        String attrValue = (String) attrValues.nextElement();
        if (attrName.equals(ACTUAL_RANGE)) {
          String[] vals = attrValue.split(" ");
          elem.getValues().addAll(Arrays.asList(vals));
        } else if (attrName.equals(UNITS)) {
          elem.setUnit(attrValue);
        } else {
          elem.getValues().add(attrValue);
        }
      }

    }

    return elem;
  }

}
