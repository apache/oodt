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


package org.apache.oodt.product;

/**
 * Retrievers retrieve products.
 *
 * @author Kelly
 * @version $Revision: 1.1.1.1 $
 */
public interface Retriever {
	/**
	 * Retrieve a chunk from a large product.
	 *
	 * @param productID Product ID.
	 * @param offset Where in the product to retrieve the data.
	 * @param length How much data to get.
	 * @return The data.
	 * @throws ProductException if an error occurs.
	 */
	byte[] retrieveChunk(String productID, long offset, int length) throws ProductException;

	/**
	 * Close off a large product.
	 *
	 * @param productID Product ID.
	 * @throws ProductException if an error occurs.
	 */
	void close(String productID);
}
