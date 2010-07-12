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

//JDK imports
import java.util.Collections;
import java.util.List;
import java.util.Vector;

/**
 * 
 * A pager for paging through query results.
 * 
 */
public class QueryPager extends IndexPager {

  protected List<TransactionReceipt> receipts;

  public QueryPager(List<TransactionReceipt> receipts) {
    super();
    this.receipts = new Vector<TransactionReceipt>(receipts);
    this.totalPages = this.caculateTotalPages();
    this.numOfHits = receipts.size();
  }

  protected int caculateTotalPages() {
    return (int) Math.ceil((double) receipts.size()
        / (double) this.getPageSize());
  }

  public void setPageInfo(PageInfo pageInfo) {
    this.pageSize = Math.max(pageInfo.getPageSize(), 0);
    this.totalPages = this.caculateTotalPages();
    if (this.totalPages == 0)
      this.pageNum = 0;
    else
      this.pageNum = (pageInfo.getPageNum() == PageInfo.LAST_PAGE || pageInfo
          .getPageNum() >= this.totalPages) ? this.totalPages : pageInfo
          .getPageNum();
  }

  public List<TransactionReceipt> getTransactionReceipts() {
    return Collections.unmodifiableList(this.receipts);
  }

  public List<TransactionReceipt> getCurrentPage() {
    List<TransactionReceipt> currentPage = new Vector<TransactionReceipt>();
    if (this.pageNum > 0)
      for (int i = (this.getPageNum() - 1) * this.getPageSize(); i < receipts
          .size()
          && i < this.getPageNum() * this.getPageSize(); i++)
        currentPage.add(receipts.get(i));
    return currentPage;
  }

}
