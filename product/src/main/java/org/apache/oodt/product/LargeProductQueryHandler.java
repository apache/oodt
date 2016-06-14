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
 * Handle requests for products that are too large to fit in an {@link
 * org.apache.oodt.xmlquery.XMLQuery} object.
 *
 * @author Kelly
 * @version $Revision: 1.1.1.1 $
 */
public interface LargeProductQueryHandler extends QueryHandler {
	/**
	 * Retrieve a chunk of a product.
	 *
	 * The product is identified by a string ID.  The query handler should return a
	 * binary chunk of the product using the given offset and length.  If the ID isn't
	 * recognized, it should return null.  It should throw an exception if retrieval
	 * fails for some reason.
	 *
	 * @param id Product ID.
	 * @param offset Where in the product to get a chunk of it.
	 * @param length How much of the product to get.
	 * @return A chunk, or null if the <var>id</var> is unknown.
	 * @throws ProductException if an error occurs.
	 */
	byte[] retrieveChunk(String id, long offset, int length) throws ProductException;

	/**
	 * Close off a product.
	 *
	 * This method indicates that the product is no longer required and its resources
	 * can be freed by the query handler.  If the ID is unknown, no untoward action is
	 * required.  It should throw an exception if there is an error during the
	 * resource release (such as an {@link java.io.IOException} when closing a file.
	 *
	 * @param id Product ID.
	 * @throws ProductException if an error occurs.
	 */
	void close(String id);
}
