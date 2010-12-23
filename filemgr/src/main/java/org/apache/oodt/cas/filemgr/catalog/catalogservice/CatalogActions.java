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
package org.apache.oodt.cas.filemgr.catalog.catalogservice;

/**
 * 
 * @author bfoster
 *
 */
public class CatalogActions {

	/* KEY */
	public static final String CATALOG_ACTION_KEY = "urn:filemgr:CatalogAction";

	/* VALUES */
	public static final String STATUS_UPDATE = "STATUS_UPDATE";
		
	
	public static final String INGEST_PRODUCT = "INGEST_PRODUCT";
	
	public static final String REMOVE_PRODUCT = "REMOVE_PRODUCT";

	
	public static final String INGEST_METADATA = "INGEST_METADATA";
	
	public static final String REMOVE_METADATA = "REMOVE_METADATA";


	public static final String INGEST_REFERENCE = "INGEST_REFERENCE";
	
	public static final String REMOVE_REFERENCE = "REMOVE_REFERENCE";


}
