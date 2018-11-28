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
package org.apache.oodt.cas.catalog.page;

/**
 * @author bfoster
 * @version $Revision$
 *
 */
public class PageInfo {

	protected int pageSize;
	protected int pageNum;
	
	public static final int LAST_PAGE = Integer.MAX_VALUE;
	public static final int FIRST_PAGE = 1;

	public PageInfo(int pageSize, int pageNum) {
		this.pageSize = pageSize;
		if (pageNum < 1)
			this.pageNum = 1;
		else
			this.pageNum = pageNum;
	}

	public int getPageSize() {
		return pageSize;
	}

	public int getPageNum() {
		return pageNum;
	}
	
}
