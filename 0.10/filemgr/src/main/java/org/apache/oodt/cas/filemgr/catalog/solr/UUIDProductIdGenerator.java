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

import java.util.UUID;

import org.apache.oodt.cas.filemgr.structs.Product;

/**
 * Implementation of {@link ProductIdGenerator} that assigns the product
 * identifier as a newly generated UUID.
 * Note that this generator will cause multiple submissions of the same named product
 * to result in as many records in the Solr index.
 * 
 * @author Luca Cinquini
 *
 */
public class UUIDProductIdGenerator implements ProductIdGenerator {

	@Override
	public String generateId(Product product) {
		return UUID.randomUUID().toString();
	}

}
