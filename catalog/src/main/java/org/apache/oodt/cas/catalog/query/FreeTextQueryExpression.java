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
package org.apache.oodt.cas.catalog.query;

//JDK imports
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * @author woollard
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * A Free Text TermQueryExpression
 * </p>
 * 
 */
public class FreeTextQueryExpression extends TermQueryExpression {
	
	private HashSet<String> noiseWordHash = new HashSet<String>(
		Arrays.asList(new String[] { "a", "all", "am", "an", "and",
        "any", "are", "as", "at", "be", "but", "can", "did", "do", "does",
        "for", "from", "had", "has", "have", "here", "how", "i", "if",
        "in", "is", "it", "no", "not", "of", "on", "or", "so", "that",
        "the", "then", "there", "this", "to", "too", "up", "use", "what",
        "when", "where", "who", "why", "you" }));

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
        String token = null;

        // filter noise words and add to values vector
        List<String> values = new Vector<String>();
        while (tok.hasMoreElements()) {
            token = tok.nextToken();
            if (!noiseWordHash.contains(token))
                values.add(token);
        }
        if (values.size() > 0) {
        	values.addAll(this.term.getValues());
        	this.term.setValues(values);
        }
    }

    /**
     * Implementation of the abstract method inherited from QueryCriteria for
     * generating a human-parsable string version of the query criteria. Note
     * that the returned String follows the Lucene query language.
     * 
     * @return The query as a String.
     */
    public String toString() {
        String serial = "({" + this.bucketNames + "} " + this.term.getName() + " :|";
        for (String value : this.term.getValues())
            serial += "+" + value;
        serial += "|: )";
        return serial;
    }
    
	@Override
	public FreeTextQueryExpression clone() {
		FreeTextQueryExpression ftQE = new FreeTextQueryExpression();
		ftQE.noiseWordHash = new HashSet<String>(this.noiseWordHash);
		ftQE.setTerm(this.term.clone());
		ftQE.setBucketNames(this.bucketNames);
		return ftQE;
	}

}
