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

package org.apache.oodt.cas.catalog.pagination;

/**
 * 
 * A pager for paging through a index
 * 
 */
public class IndexPager {

  protected int pageSize;
  protected int pageNum;
  protected int totalPages;
  protected int numOfHits;

  public IndexPager() {
    this.pageSize = 20;
    this.pageNum = 1;
    this.totalPages = 0;
  }

  public IndexPager(ProcessedPageInfo processedPageInfo) {
    this.pageSize = processedPageInfo.getPageSize();
    this.pageNum = processedPageInfo.getPageNum();
    this.totalPages = processedPageInfo.getTotalPages();
    this.numOfHits = processedPageInfo.getNumOfHits();
  }

  public ProcessedPageInfo getProcessedPageInfo() {
    return new ProcessedPageInfo(this.pageSize, this.pageNum, this.numOfHits);
  }

  public int getPageSize() {
    return this.pageSize;
  }

  public int getPageNum() {
    return this.pageNum;
  }

  public int getTotalPages() {
    return this.totalPages;
  }

  public int getNumOfHits() {
    return this.numOfHits;
  }

  public void incrementPageNumber() {
    if (this.pageNum + 1 <= this.totalPages)
      this.pageNum++;
  }

  public boolean isLastPage() {
    return this.getProcessedPageInfo().isLastPage();
  }

}
