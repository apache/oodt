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

package org.apache.oodt.cas.filemgr.metadata;


/**
 * 
 * Met key field names used to augment {@link org.apache.oodt.cas.filemgr.structs.Product}
 * {@link org.apache.oodt.cas.metadata.Metadata} in
 * {@link org.apache.oodt.cas.filemgr.system.FileManagerClient#getMetadata(org.apache.oodt.cas.filemgr.structs.Product)}
 * and in
 * {@link org.apache.oodt.cas.filemgr.system.FileManagerClient#getReducedMetadata(org.apache.oodt.cas.filemgr.structs.Product, java.util.List)}
 * .
 * 
 * @see http://issues.apache.org/jira/browse/OODT-72
 * 
 */
public interface ProductMetKeys {

  String PRODUCT_ID = "ProductId";

  String PRODUCT_NAME = "ProductName";

  String PRODUCT_STRUCTURE = "ProductStructure";

  String PRODUCT_TRANSFER_STATUS = "ProductTransferStatus";

  String PRODUCT_ROOT_REFERENCE = "ProductRootReference";

  String PRODUCT_DATASTORE_REFS = "ProductDataStoreReferences";

  String PRODUCT_ORIG_REFS = "ProductOrigReferences";

  String PRODUCT_MIME_TYPES = "ProductMimeType";

  String PRODUCT_FILE_SIZES = "ProductFileSize";

}
