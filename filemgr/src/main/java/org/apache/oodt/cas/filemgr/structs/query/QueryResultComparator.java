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

package org.apache.oodt.cas.filemgr.structs.query;

//JDK imports
import java.util.Comparator;

/**
 * 
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * A Comparator that allows for sorting of QueryResults based on a 
 * Metadata element
 * <p>
 */
public class QueryResultComparator implements Comparator<QueryResult> {

    private String sortByMetKey;

    public String getSortByMetKey() {
        return sortByMetKey;
    }

    public void setSortByMetKey(String sortByMetKey) {
        this.sortByMetKey = sortByMetKey;
    }

    public int compare(QueryResult qr1, QueryResult qr2) {
        String m1 = qr1.getMetadata().getMetadata(sortByMetKey);
        String m2 = qr2.getMetadata().getMetadata(sortByMetKey);
        if (m1 == m2) {
            return 0;
        }
        // return null last, since they are the least interesting
        if (m1 == null) {
            return 1;
        }
        if (m2 == null) {
            return -1;
        }
        return m1.compareTo(m2);
    }

}
