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
package org.apache.oodt.cas.filemgr.util;

//JDK imports
import java.util.List;
import java.util.Vector;

//APACHE imports
import org.apache.commons.lang.StringUtils;

//OODT imports
import org.apache.oodt.cas.filemgr.structs.query.QueryResult;


/**
 * @author bfoster
 * @version $Revsion$
 * 
 * <p>
 * Utility Class
 * </p>
 * 
 */
public class QueryUtils {

    public static String getQueryResultsAsString(List<QueryResult> queryResults) {
        return getQueryResultsAsString(queryResults, null);
    }
    
    public static String getQueryResultsAsString(List<QueryResult> queryResults, String resultsDelimiter) {
    	return getQueryResultsAsString(queryResults, null, resultsDelimiter);
    }
    
    public static String getQueryResultsAsString(List<QueryResult> queryResults, String valueDelimiter, String resultsDelimiter) {
    	if (valueDelimiter == null)
    		valueDelimiter = ",";
    	if (resultsDelimiter == null)
    		resultsDelimiter = "//n";    	
        StringBuffer returnString = new StringBuffer("");
        boolean firstRun = true;
        for (QueryResult queryResult : queryResults) {
        	List<String> keys = queryResult.getMetadata().getAllKeys();
        	if (keys.size() > 0) {
        		if (!firstRun) 
                    returnString.append(resultsDelimiter);
        		else
        			firstRun = false;
        		returnString.append(queryResult.getMetadata().getAllMetadata(keys.get(0)));
        		for (int i = 1; i < keys.size(); i++)
            		returnString.append(valueDelimiter + queryResult.getMetadata().getAllMetadata(keys.get(i)));
        	}
        }
        return returnString.toString();
    }
    
    public static String getQueryResultsAsFormattedString(List<QueryResult> queryResults, String format, String delimiter) {
        if (format == null)
            return getQueryResultsAsString(queryResults, delimiter);
        Vector<String> stringResults = new Vector<String>();
        for (QueryResult queryResult : queryResults) {
            String outputString = format;
            for (String key : queryResult.getMetadata().getAllKeys())
            	outputString = outputString.replaceAll("\\$" + key, StringUtils.join(queryResult.getMetadata().getAllMetadata(key).iterator(), ","));
            stringResults.add(outputString);
        }
        return StringUtils.join(stringResults.iterator(), delimiter);
    }

}
