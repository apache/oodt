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
 * This is an abstract base class for a number of different criteria searches
 * such as term search, range search and free text search. Subclasses are added
 * to Query instances in order to search the Catalog.
 * </p>
 * 
 * <ul>
 * <lh>Known Subclasses:</lh>
 * <li>FreeTextQueryCriteria
 * <li>RangeQueryCriteria
 * <li>TermQueryCriteria
 * </ul>
 */
public abstract class QueryCriteria implements Cloneable {

    /**
     * Abstract accessor method for the Element name to search on.
     * 
     * @return The ElementName in the form of a String.
     */
    public abstract String getElementName();

    /**
     * Abstract mutator method for the Elment name to search on.
     *
     */
    public abstract void setElementName(String elementName);

    /**
     * Abstract method for converting the QueryCriteria to a human-parsable
     * String.
     */
    public abstract String toString();

}
