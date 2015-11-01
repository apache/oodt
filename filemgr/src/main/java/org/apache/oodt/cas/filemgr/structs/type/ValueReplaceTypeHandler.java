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

//OODT imports
import org.apache.oodt.cas.filemgr.structs.QueryCriteria;
import org.apache.oodt.cas.filemgr.structs.RangeQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.TermQueryCriteria;
import org.apache.oodt.cas.metadata.Metadata;

/**
 * A Subset-TypeHandler which only allows for metadata value modification.
 * It will automatically detected and replace the metadata values for 
 * the metadata element which it is responsible for.  All the developer
 * needs to supply is the convertion method between the original value
 * and the Catalog value
 * 
 * @author bfoster
 * @version $Revision$
 * 
 */
public abstract class ValueReplaceTypeHandler extends TypeHandler {

    /**
     * {@inheritDoc}
     */
    @Override
    public void postGetMetadataHandle(Metadata metadata) {
        if (metadata.containsKey(elementName)) {
            metadata.replaceMetadata(elementName, 
                this.getOrigValue(metadata.getMetadata(elementName)));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void preAddMetadataHandle(Metadata metadata) {
        if (metadata.containsKey(elementName)) {
            metadata.replaceMetadata(elementName, 
                this.getCatalogValue(metadata.getMetadata(elementName)));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected QueryCriteria handleRangeQueryCriteria(RangeQueryCriteria rqc) {
        if (rqc.getEndValue() != null) {
            rqc.setEndValue(this.getCatalogValue(rqc.getEndValue()));
        }
        if (rqc.getStartValue() != null) {
            rqc.setStartValue(this.getCatalogValue(rqc.getStartValue()));
        }
        return rqc;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected QueryCriteria handleTermQueryCriteria(TermQueryCriteria tqc) {
        if (tqc.getValue() != null) {
            tqc.setValue(this.getCatalogValue(tqc.getValue()));
        }
        return tqc;
    }
    
    /**
     * Converts the metadata element, for this TypeHandler, value to the 
     * Catalog value
     * @param origValue The value for the element which this TypeHandler
     *  is responsible for
     * @return The Catalog value
     */
    protected abstract String getCatalogValue(String origValue);
    
    /**
     * Converts the metadata element, for this TypeHandler, value from the 
     * Catalog value to the original value
     * @return The original value
     */
    protected abstract String getOrigValue(String databaseValue);

}
