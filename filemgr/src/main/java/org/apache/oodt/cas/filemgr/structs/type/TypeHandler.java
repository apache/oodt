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

package org.apache.oodt.cas.filemgr.structs.type;

//JDK imports
import java.util.LinkedList;

//OODT imports
import org.apache.oodt.cas.filemgr.structs.BooleanQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.Query;
import org.apache.oodt.cas.filemgr.structs.QueryCriteria;
import org.apache.oodt.cas.filemgr.structs.RangeQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.TermQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.exceptions.QueryFormulationException;
import org.apache.oodt.cas.metadata.Metadata;

/**
 * A Metadata modification class that intercepts all Metadata flowing in and out
 * of the Catalog.  Allows the Catalog to contain additional Metadata or modified
 * Metadata while keeping it invisible to the user.  The purpose it to create 
 * String queriable Metadata so that the Catalog can be queries accurately and
 * without extra knowledge required on the user end.
 * 
 * @author bfoster
 * @version $Revision$
 * 
 */
public abstract class TypeHandler {
    
    protected String elementName;

    /**
     * Sets the Element name that this TypeHandler is responsible for handling
     * @param elementName The Element name for this TypeHandler
     */
    public void setElementName(String elementName) {
        this.elementName = elementName;
    }
    
    /**
     * Get the Element name that this TypeHandler is responsible for handling
     * @return The Element name for this TypeHandler
     */
    public String getElementName() {
        return this.elementName;
    }
    
    /**
     * Converts this TypeHandler's element in the given Query into a Query
     * with the necessary elements and values so the Catalog can be queried.
     *    
     *    NOTE: Original Query is modified . . . the argument query becomes
     *    equal to the returned query (return of query is a convenience).
     *    
     * @param query Query for which the Catalog Query will be returned
     * @return A Query with Catalog element values
     * @throws QueryFormulationException
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     */
    public Query preQueryHandle(Query query) throws QueryFormulationException {
        LinkedList<QueryCriteria> qcList = new LinkedList<QueryCriteria>();
        for (QueryCriteria qc : query.getCriteria()) {
            qcList.add(this.handleQueryCriteria(qc));
        }
        query.setCriteria(qcList);
        return query;
    }
    
    private QueryCriteria handleQueryCriteria(QueryCriteria qc) throws QueryFormulationException {
        if (qc instanceof BooleanQueryCriteria) {
            LinkedList<QueryCriteria> qcList = new LinkedList<QueryCriteria>();
            for (QueryCriteria criteria : ((BooleanQueryCriteria) qc).getTerms()) {
                qcList.add(this.handleQueryCriteria(criteria));
            }
            BooleanQueryCriteria bqc = new BooleanQueryCriteria();
            bqc.setOperator(((BooleanQueryCriteria) qc).getOperator());
            bqc.setElementName(qc.getElementName());
            for (QueryCriteria criteria : qcList) {
                bqc.addTerm(criteria);
            }
            return bqc;
        }else if (qc.getElementName().equals(elementName) && qc instanceof TermQueryCriteria) {
            return this.handleTermQueryCriteria((TermQueryCriteria) qc);
        }else if (qc.getElementName().equals(elementName) && qc instanceof RangeQueryCriteria) {
            return this.handleRangeQueryCriteria((RangeQueryCriteria) qc);
        }else {
            return qc;
        }
    }
    
    /**
     * Handles converting any Catalog metadata element values to their original values.  
     * Metadata elements can be added, modified, or replaced.
     * 
     * @param metadata The Catalog Metadata
     */
    public abstract void postGetMetadataHandle(Metadata metadata);
    
    /**
     * Handles converting any metadata element to Catalog element values.  
     * Metadata elements can be added, modified, or replaced.
     * 
     * @param metadata The Original Metadata
     */
    public abstract void preAddMetadataHandle(Metadata metadata);
    
    /**
     * Converts the given RangeQueryCriteria into a QueryCriteria with the necessary
     * Catalog elements and values to perform the query
     * 
     * 
     * @param qc
     * @return
     */
    protected abstract QueryCriteria handleRangeQueryCriteria(RangeQueryCriteria qc);
    
    /**
     * Converts the given TermQueryCriteria into a QueryCriteria with the necessary
     * Catalog elements and values to perform the query
     * 
     * @param qc
     * @return
     */
    protected abstract QueryCriteria handleTermQueryCriteria(TermQueryCriteria qc);

}
