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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author woollard
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * A Free Text Criteria element for a Query to the Catalog.
 * </p>
 * 
 */
public class FreeTextQueryCriteria extends QueryCriteria {

    private static final long serialVersionUID = 1L;

    private String elementName;

    private List<String> values;

    private static final String[] noiseWords = { "a", "all", "am", "an", "and",
            "any", "are", "as", "at", "be", "but", "can", "did", "do", "does",
            "for", "from", "had", "has", "have", "here", "how", "i", "if",
            "in", "is", "it", "no", "not", "of", "on", "or", "so", "that",
            "the", "then", "there", "this", "to", "too", "up", "use", "what",
            "when", "where", "who", "why", "you" };

    private static HashSet<String> noiseWordHash;

    /**
     * Default constructor.
     */
    public FreeTextQueryCriteria() {
        elementName = "";
        values = new ArrayList<String>();

        noiseWordHash = new HashSet<String>();
        Collections.addAll(noiseWordHash, noiseWords);
    }

    /**
     * Constructor for the FreeTextQueryECriteria Class.
     * 
     * @param v
     *            A List of terms to search for.
     */
    public FreeTextQueryCriteria(String elementName, List<String> v) {
        this.elementName = elementName;
        values = v;

        noiseWordHash = new HashSet<String>();
        Collections.addAll(noiseWordHash, noiseWords);
    }

    /**
     * Accessor method for the values of the element to search on.
     * 
     * @return The values of the element to search on as a List of Strings.
     */
    public List<String> getValues() {
        return values;
    }

    /**
     * Mutator method for the values of the element to search on. This method
     * should be used when keywords have been parsed out of user-entered free
     * text. The query will JOIN on all of these values. In order to add
     * unparsed free text to a Query, see the addFreeText method of this class.
     *
     */
    public void setValue(List<String> v) {
        this.values = v;
    }

    /**
     * A method for adding a value to search on to the list of values.
     * 
     * @param v
     *            The value to be added to the search as a String.
     */
    public void addValue(String v) {
        values.add(v);
    }

    /**
     * A method for adding unparsed free text to the FreeTextCriteria. Free text
     * entered as a string is tokenized and punctuation and common words are
     * dropped before the values are added to the query. In order to query for
     * pre-parsed keywords, see the setValues method of this class.
     * 
     * @param text
     *            The free text to be parsed and searched on.
     */
    public void addFreeText(String text) {
        // remove punctuation from the text
        text = text.replaceAll("\\p{Punct}+", "");

        // tokenize string using default delimiters
        StringTokenizer tok = new StringTokenizer(text);
        String token;

        // filter noise words and add to values vector
        while (tok.hasMoreElements()) {
            token = tok.nextToken();
            if (!noiseWordHash.contains(token)) {
                values.add(token);
            }
        }
    }

    /**
     * Implementation of the abstract method inherited from QueryCriteria for
     * accessing the element ID to search on.
     * 
     * @return The element ID to search on as a String.
     */
    public String getElementName() {
        return elementName;
    }

    /**
     * Implementation of the abstract method inherited from QueryCriteria for
     * mutating the element ID to search on.
     *
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
        StringBuilder serial = new StringBuilder(elementName + ":(");
        for (String value : values) {
            serial.append("+").append(value);
        }
        serial.append(")");
        return serial.toString();
    }

}
