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
 * @author woollard
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * A Term Criteria element for a Query to the Catalog.
 * </p>
 * 
 */
public class TermQueryCriteria extends QueryCriteria {

    private static final long serialVersionUID = 1L;

    private String elementName;

    private String value;

    /**
     * Default constructor.
     */
    public TermQueryCriteria() {
        elementName = null;
        value = null;
    }

    /**
     * Constructor for the TermQueryECriteria Class.
     * 
     * @param elementName
     *            The name of the element to search on.
     * @param v
     *            The value of the term.
     */
    public TermQueryCriteria(String elementName, String v) {
        this.elementName = elementName;
        value = v;
    }

    /**
     * Accessor method for the value of the element to search on.
     * 
     * @return The value of the element to search on as a String.
     */
    public String getValue() {
        return value;
    }

    /**
     * Mutator method for the value of the element to search on
     * 
     * @param value
     *            The value of the element to search on as a String.
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Implementation of the abstract method inherited from QueryCriteria for
     * accessing the element name to search on.
     * 
     * @return The element name to search on as a String.
     */
    public String getElementName() {
        return elementName;
    }

    /**
     * Implementation of the abstract method inherited from QueryCriteria for
     * mutating the element name to search on.
     * 
     * @param elementName
     *            The element name to search on as a String.
     */
    public void setElementName(String elementName) {
        this.elementName = elementName;
    }

    /**
     * Implementation of the abstract method inherited from QueryCriteria for
     * generating a human-parsable string version of the query criteria. Note
     * that the returned String follows the Lucene query language.
     * 
     * @return The query as a String.
     */
    public String toString() {
        return this.elementName + ":" + value;
    }

}
