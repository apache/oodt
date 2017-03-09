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

package org.apache.oodt.cas.filemgr.tools;


//Lucene imports
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.util.CharArraySet;

//JDK imports
import java.io.Reader;
import java.util.Set;


/**
 * 
 * @author woollard
 * @version $Revision$
 * 
 * <p>
 * An analyzer used in the CatalogSearch program.
 * </p>
 * 
 */
public class CASAnalyzer extends Analyzer {
    private Set stopSet;

    /**
     * An array containing some common English words that are usually not useful
     * for searching.
     */
    public static final CharArraySet STOP_WORDS = StopAnalyzer.ENGLISH_STOP_WORDS_SET;
    private Reader reader;

    /** Builds an analyzer. */
    public CASAnalyzer() {
        this(STOP_WORDS);
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        TokenStream result = new WhitespaceTokenizer(/*reader*/);
        result = new StandardFilter(result);
        result = new StopFilter(result, STOP_WORDS);


        //TODO FIX
        try {
            throw new Exception("needs fixing");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; //new TokenStreamComponents();
    }

    public void tokenStreams(String fname, Reader reader){
        this.reader = reader;
    }
    /** Builds an analyzer with the given stop words. */
    public CASAnalyzer(CharArraySet stopWords) {
        stopSet = StopFilter.makeStopSet(stopWords.toArray(new String[stopWords.size()]));

    }

    /**
     * Constructs a {@link org.apache.lucene.analysis.standard.StandardTokenizer} filtered by a {@link
     * StandardFilter}, a {@link LowerCaseFilter} and a {@link StopFilter}.
     */
    /*4public TokenStream tokenStream(String fieldName, Reader reader) {
        TokenStream result = new WhitespaceTokenizer(reader);
        result = new StandardFilter(result);
        result = new StopFilter(result, stopSet);
        return result;
    }*/

}
