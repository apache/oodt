/**
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

package org.apache.oodt.cas.catalog.query.filter;

//OODT imports
import org.apache.oodt.cas.catalog.metadata.TransactionalMetadata;

//JDK imports
import java.util.List;

/**
 * 
 * A Filter interface for filtering queries.
 * 
 */
public abstract class QueryFilter<FilterType> {

  protected FilterAlgorithm<FilterType> filterAlgorithm;

  public QueryFilter() {
  }

  public QueryFilter(FilterAlgorithm<FilterType> filterAlgorithm) {
    super();
    this.filterAlgorithm = filterAlgorithm;
  }

  public void setFilterAlgorithm(FilterAlgorithm<FilterType> filterAlgorithm) {
    this.filterAlgorithm = filterAlgorithm;
  }

  public FilterAlgorithm<FilterType> getFilterAlgorithm() {
    return this.filterAlgorithm;
  }

  public List<TransactionalMetadata> filterMetadataList(
      List<TransactionalMetadata> metadataToFilter) {
    return this.filterTypeToMetadata(this.filterAlgorithm.filter(this
        .metadataToFilterType(metadataToFilter)));
  }

  protected abstract List<FilterType> metadataToFilterType(
      List<TransactionalMetadata> metadataList);

  protected abstract List<TransactionalMetadata> filterTypeToMetadata(
      List<FilterType> filterObjects);

}
