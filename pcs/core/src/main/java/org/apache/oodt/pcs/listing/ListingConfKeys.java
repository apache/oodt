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

package org.apache.oodt.pcs.listing;

/**
 * 
 * Met keys required to read the {@link ListingConf}.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public interface ListingConfKeys {

  String MET_FIELD_COLS_GROUP = "MetFieldColumns";
  
  String MET_FIELDS_ORDER_VECTOR = "OrderedMetKeys";

  String COLLECTION_FIELDS_GROUP = "CollectionFields";

  String COLLECTION_FIELDS_NAMES = "FieldNames";

  String EXCLUDED_PRODUCT_TYPE_GROUP = "ExcludedProductTypeList";

  String EXCLUDED_VECTOR = "ProductTypes";

}
