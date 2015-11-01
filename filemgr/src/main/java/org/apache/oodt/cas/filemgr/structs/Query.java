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

//JDK imports
import java.util.List;
import java.util.Vector;

/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * A Query is a {@link List} of {@link QueryCriteria}.
 * </p>
 * 
 */
public class Query {

    /* the set of {@link QueryCriteria} for this Query */
    private List<QueryCriteria> criteria = null;
    
    /**
     * <p>
     * Default Constructor
     * </p>.
     */
    public Query() {
        criteria = new Vector<QueryCriteria>();
    }

    /**
     * @param criteria
     */
    public Query(List<QueryCriteria> criteria) {
        if (criteria == null) {
            this.criteria = new Vector<QueryCriteria>();
        } else {
            this.criteria = criteria;
        }
    }

    /**
     * @return Returns the criteria.
     */
    public List<QueryCriteria> getCriteria() {
        return criteria;
    }

    /**
     * @param criteria
     *            The criteria to set.
     */
    public void setCriteria(List<QueryCriteria> criteria) {
        if (criteria != null) {
            this.criteria = criteria;
        }
    }

    public void addCriterion(QueryCriteria qc) {
        criteria.add(qc);
    }
    
    /**
     * @return A String representation of this Query.
     */
    public String toString() {
        StringBuilder rStr = new StringBuilder();

        rStr.append("q=");

        int numCriteria = criteria.size();
        for (int i = 0; i < numCriteria; i++) {
            QueryCriteria c = (QueryCriteria) criteria.get(i);
            rStr.append(c.toString());
            if (i != numCriteria - 1) {
                rStr.append(" AND ");
            }
        }

        return rStr.toString();
    }

}
