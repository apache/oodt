/*
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

package org.apache.oodt.cas.filemgr.structs;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A metadata element.
 * </p>
 * 
 */
public class Element {

    /* the element id */
    private String elementId = null;

    /* the element name */
    private String elementName = null;

    /* the corresponding DC element for this CAS element */
    private String dcElement = null;

    /* the element's string description. */
    private String description = null;

    /**
     * <p>
     * Default constructor
     * </p>
     * 
     */
    public Element() {
    }

    /**
     * <p>
     * Constructs a new CAS element
     * </p>
     * 
     * @param elementId
     *            The element's Id from the database.
     * @param elementName
     *            The element name.
     * @param element
     *            The DC element that corresponds to this archive element.
     * 
     * @param desc
     *            The element's description.
     * 
     */
    public Element(String elementId, String elementName,
            String elemMetadataColumn, String element, 
            String desc, String typeHandler) {
        this.elementId = elementId;
        this.elementName = elementName;
        this.dcElement = element;
        this.description = desc;
    }

    @Override
    public int hashCode() {
       return this.elementId.hashCode();
    }
    
    /**
     * @return Returns the dcElement.
     */
    public String getDCElement() {
        return dcElement;
    }

    /**
     * @param element
     *            The dcElement to set.
     */
    public void setDCElement(String element) {
        dcElement = element;
    }

    /**
     * @return Returns the elementId.
     */
    public String getElementId() {
        return elementId;
    }

    /**
     * @param elementId
     *            The elementId to set.
     */
    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    /**
     * @return Returns the elementName.
     */
    public String getElementName() {
        return elementName;
    }

    /**
     * @param elementName
     *            The elementName to set.
     */
    public void setElementName(String elementName) {
        this.elementName = elementName;
    }

    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description
     *            The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }  
    
    public static Element blankElement(){
      Element e = new Element();
      e.setDCElement("");
      e.setDescription("blank");
      e.setElementId("");
      e.setElementName("blank");
      return e;
      
    }

}
