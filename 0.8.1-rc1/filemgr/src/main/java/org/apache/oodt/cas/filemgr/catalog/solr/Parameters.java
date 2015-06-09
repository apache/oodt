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

import java.text.DateFormat;
import java.text.SimpleDateFormat;


/**
 * Class holding parameters for Solr implementation of CAS File Manager.
 * 
 * @author Luca Cinquini
 *
 */
public class Parameters {
	
	// the Solr unique identifier field
	public final static String ID = "id";
	
	public final static String NS = "CAS.";
		
	public final static String PRODUCT_ID = NS+"ProductId";
	public final static String PRODUCT_NAME = NS+"ProductName";
	public final static String PRODUCT_STRUCTURE = NS+"ProductStructure";
	public final static String PRODUCT_TRANSFER_STATUS = NS+"ProductTransferStatus";
	public final static String PRODUCT_RECEIVED_TIME = NS+"ProductReceivedTime";	
	public final static String PRODUCT_TYPE_NAME = NS+"ProductTypeName";
	public final static String PRODUCT_TYPE_ID = NS+"ProductTypeId";
		
	public final static String REFERENCE_ORIGINAL = NS+"ReferenceOriginal";
	public final static String REFERENCE_DATASTORE = NS+"ReferenceDatastore";
	public final static String REFERENCE_FILESIZE = NS+"ReferenceFileSize";
	public final static String REFERENCE_MIMETYPE = NS+"ReferenceMimeType";
	
	public final static String ROOT = "Root";
	public final static String ROOT_REFERENCE_ORIGINAL = NS+ROOT+REFERENCE_ORIGINAL;
	public final static String ROOT_REFERENCE_DATASTORE = NS+ROOT+REFERENCE_DATASTORE;
	public final static String ROOT_REFERENCE_FILESIZE = NS+ROOT+REFERENCE_FILESIZE;
	public final static String ROOT_REFERENCE_MIMETYPE = NS+ROOT+REFERENCE_MIMETYPE;

  // required date/time format for Solr documents
  public static final String SOLR_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
  public static final DateFormat SOLR_DATE_TIME_FORMATTER = new SimpleDateFormat(SOLR_DATE_FORMAT);

  // possible mime types for communication with Solr
  public static final String MIME_TYPE_XML = "application/xml";
  public static final String MIME_TYPE_JSON = "application/json";
  
  // page size for pagination
  public static final int PAGE_SIZE = 20;
	
	// special value that indicates that the metadata field must be deleted
	public final static String NULL = "__NULL__";
	
	private Parameters() {}
	
}
