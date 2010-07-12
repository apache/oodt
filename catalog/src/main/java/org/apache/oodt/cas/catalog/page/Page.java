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

package org.apache.oodt.cas.catalog.page;

//OODT imports
import org.apache.oodt.cas.catalog.query.QueryExpression;

//JDK imports
import java.util.List;
import java.util.Set;

/**
 * 
 * Describe your class here.
 * 
 */
public class Page {

  protected List<TransactionReceipt> receipts;
  protected ProcessedPageInfo processedPageInfo;
  protected QueryExpression queryExpression;
  protected Set<String> restrictToCatalogIds;

  public Page(ProcessedPageInfo pageInfo, QueryExpression queryExpression,
      Set<String> restrictToCatalogIds, List<TransactionReceipt> receipts) {
    this.processedPageInfo = pageInfo;
    this.queryExpression = queryExpression;
    this.restrictToCatalogIds = restrictToCatalogIds;
    this.receipts = receipts;
  }

  public List<TransactionReceipt> getReceipts() {
    return this.receipts;
  }

  public int getPageSize() {
    return this.processedPageInfo.getPageSize();
  }

  public int getPageNum() {
    return this.processedPageInfo.getPageNum();
  }

  public int getTotalPages() {
    return this.processedPageInfo.getTotalPages();
  }

  public int getNumOfHits() {
    return this.processedPageInfo.getNumOfHits();
  }

  public QueryExpression getQueryExpression() {
    return queryExpression;
  }

  public Set<String> getRestrictToCatalogIds() {
    return restrictToCatalogIds;
  }

  public boolean isLastPage() {
    return this.processedPageInfo.isLastPage();
  }

}
