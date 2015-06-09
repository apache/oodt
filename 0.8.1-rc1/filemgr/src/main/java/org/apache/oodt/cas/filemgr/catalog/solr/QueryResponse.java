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
package org.apache.oodt.cas.filemgr.catalog.solr;

import java.util.ArrayList;
import java.util.List;

import org.apache.oodt.cas.filemgr.structs.Product;

/**
 * Class holding information returned from querying the Solr server.
 * 
 * @author Luca Cinquini
 *
 */
public class QueryResponse {

	/**
	 * Total number of results matching the query
	 * (NOT the number of results returned, which is typically less).
	 */
	private int numFound = 0;
	
	/**
	 * Offset into returned results.
	 */
	private int start = 0;
	
	/**
	 * List of products matching the query,
	 * with associated metadata and references.
	 */
	private List<CompleteProduct> completeProducts;
	
	public QueryResponse() {
		completeProducts = new ArrayList<CompleteProduct>();
	}

	public void setNumFound(int numFound) {
		this.numFound = numFound;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public void setResults(List<CompleteProduct> results) {
		this.completeProducts = results;
	}

	public int getNumFound() {
		return numFound;
	}

	public int getStart() {
		return start;
	}

	public List<CompleteProduct> getCompleteProducts() {
		return completeProducts;
	}
	
	/**
	 * Utility method to return only the 'product' part of the complete results.
	 * @return
	 */
	public List<Product> getProducts() {
		List<Product> products = new ArrayList<Product>();
		for (CompleteProduct cp : completeProducts) {
			products.add(cp.getProduct());
		}
		return products;
	}
	
	
	
}
