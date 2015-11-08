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

//OODT imports
import org.apache.oodt.cas.filemgr.structs.Query;
import org.apache.oodt.cas.filemgr.structs.QueryCriteria;

//JDK imports
import java.util.List;

/**
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * A Complex query allows for querying the filemgr across multiple product-types,
 * reducing the metadata queried for, filtering the results, and sorting the results
 * in a single query
 * </p>
 */
public class ComplexQuery extends Query {

    private List<String> reducedProductTypeNames;
    private List<String> reducedMetadata;
    private QueryFilter queryFilter;
    private String sortByMetKey;
    private String toStringResultFormat;
    
    public ComplexQuery() {
        super();
    }
    
    public ComplexQuery(List<QueryCriteria> criteria) {
        super(criteria);
    }
    
    public List<String> getReducedProductTypeNames() {
        return reducedProductTypeNames;
    }

    public void setReducedProductTypeNames(List<String> reducedProductTypeNames) {
        this.reducedProductTypeNames = reducedProductTypeNames;
    }

    public List<String> getReducedMetadata() {
        return reducedMetadata;
    }

    public void setReducedMetadata(List<String> reducedMetadata) {
        this.reducedMetadata = reducedMetadata;
        if (this.sortByMetKey != null && this.reducedMetadata != null 
                && !this.reducedMetadata.contains(this.sortByMetKey)) {
            this.reducedMetadata.add(this.sortByMetKey);
        }
    }

    public QueryFilter getQueryFilter() {
        return queryFilter;
    }

    public void setQueryFilter(QueryFilter queryFilter) {
        this.queryFilter = queryFilter;
    }
    
    public String getSortByMetKey() {
        return sortByMetKey;
    }

    public void setSortByMetKey(String sortByMetKey) {
        this.sortByMetKey = sortByMetKey;
        if (this.reducedMetadata != null && this.sortByMetKey != null 
                && !this.reducedMetadata.contains(this.sortByMetKey)) {
            this.reducedMetadata.add(this.sortByMetKey);
        }
    }
    
    public String getToStringResultFormat() {
        return this.toStringResultFormat;
    }

    public void setToStringResultFormat(String toStringResultFormat) {
        this.toStringResultFormat = toStringResultFormat;
    }
    
}
