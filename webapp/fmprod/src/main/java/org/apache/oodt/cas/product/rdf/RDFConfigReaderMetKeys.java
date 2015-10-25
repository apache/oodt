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


package org.apache.oodt.cas.product.rdf;

/**
 * 
 * Met keys for the {@link RDFConfigReader}.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public interface RDFConfigReaderMetKeys {

  String NS_OUTER_TAG = "namespaces";

  String NS_TAG = "ns";

  String NS_NAME_ATTR = "name";

  String NS_VALUE_ATTR = "value";

  String REWRITE_OUTER_TAG = "rewrite";

  String REWRITE_KEY_TAG = "key";

  String REWRITE_FROM_ATTR = "from";

  String REWRITE_TO_ATTR = "to";

  String RESOURCE_LINK_TAG = "resourcelinks";

  String RESLINK_KEY_TAG = "key";

  String RESLINK_KEY_TAG_NAME_ATTR = "name";

  String RESLINK_KEY_TAG_LINK_ATTR = "link";

  String KEY_NSMAP_TAG = "keynsmap";

  String KEY_NSMAP_DEFAULT_ATTR = "default";

  String KEY_NSMAP_KEY_TAG = "key";

  String KEY_NSMAP_KEY_TAG_NAME_ATTR = "name";

  String KEY_NSMAP_KEY_TAG_NS_ATTR = "ns";

  String TYPE_NSMAP_TAG = "typesnsmap";

  String TYPE_NSMAP_DEFAULT_ATTR = "default";

  String TYPE_NSMAP_TYPE_TAG = "type";

  String TYPE_NSMAP_TYPE_NAME_ATTR = "name";

  String TYPE_NSMAP_TYPE_NS_ATTR = "ns";
  
}
