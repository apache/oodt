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
 * A Range Criteria element for a Query to the Catalog.
 * </p>
 * 
 */
public class RangeQueryCriteria extends QueryCriteria {

    private static final long serialVersionUID = 1L;

    private String elementName;

    private String startValue;

    private String endValue;

    private boolean inclusive;

    /**
     * Default constructor.
     */
    public RangeQueryCriteria() {
        elementName = null;
        startValue = null;
        endValue = null;
        inclusive = true;
    }

    /**
     * Constructor for the RangeQuerycriteria class. Note that this default
     * range is inclusive.
     * 
     * @param elementName
     *            The name of the element to search on.
     * @param start
     *            The start value for the range search as a String.
     * @param end
     *            The end value for the range search as a String.
     */
    public RangeQueryCriteria(String elementName, String start, String end) {
        this.elementName = elementName;
        startValue = start;
        endValue = end;
        inclusive = true;
    }

    /**
     * Constructor for the RangeQueryCriteria clas that can be used to specify
     * both inclusive and exclusive ranges.
     * 
     * @param elementName
     *            The name of the element to search on.
     * @param start
     *            The start value for the range search as a String.
     * @param end
     *            The end value for the range search as a String.
     * @param inclusive
     *            Boolean: true for inclusive, false for exclusive.
     */
    public RangeQueryCriteria(String elementName, String start, String end,
            boolean inclusive) {
        this.elementName = elementName;
        startValue = start;
        endValue = end;
        this.inclusive = inclusive;
    }

    /**
     * Accessor method for the start value of the element to search on.
     * 
     * @return The start value of the element to search on as a String.
     */
    public String getStartValue() {
        return startValue;
    }

    /**
     * Mutator method for the start value fo the element to search on.
     * 
     * @param value
     *            The start value of the range as a String.
     */
    public void setStartValue(String value) {
        startValue = value;
    }

    /**
     * Accessor method for the end value of the element to search on.
     * 
     * @return The end value of the element to search on as a String.
     */
    public String getEndValue() {
        return endValue;
    }

    /**
     * Mutator method for the end value fo the element to search on.
     * 
     * @param value
     *            The end value of the range as a String.
     */
    public void setEndValue(String value) {
        endValue = value;
    }

    /**
     * Accessor method for the inclusive setting for the range.
     * 
     * @return The boolean inclusive/exclusive flag.
     */
    public boolean getInclusive() {
        return inclusive;
    }

    /**
     * Mutator method for the inclusive setting for the range. Note that flag
     * should be set to true for inclusive, false for exclusive.
     *
     */
    public void setInclusive(boolean flag) {
        inclusive = flag;
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
        return elementName + ":[" + startValue + " TO " + endValue + "]";
    }

}
