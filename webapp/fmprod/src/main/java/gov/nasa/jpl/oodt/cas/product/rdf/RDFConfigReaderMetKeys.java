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


package gov.nasa.jpl.oodt.cas.product.rdf;

/**
 * 
 * Met keys for the {@link RDFConfigReader}.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public interface RDFConfigReaderMetKeys {

  public static final String NS_OUTER_TAG = "namespaces";

  public static final String NS_TAG = "ns";

  public static final String NS_NAME_ATTR = "name";

  public static final String NS_VALUE_ATTR = "value";

  public static final String REWRITE_OUTER_TAG = "rewrite";

  public static final String REWRITE_KEY_TAG = "key";

  public static final String REWRITE_FROM_ATTR = "from";

  public static final String REWRITE_TO_ATTR = "to";

  public static final String RESOURCE_LINK_TAG = "resourcelinks";

  public static final String RESLINK_KEY_TAG = "key";

  public static final String RESLINK_KEY_TAG_NAME_ATTR = "name";

  public static final String RESLINK_KEY_TAG_LINK_ATTR = "link";

  public static final String KEY_NSMAP_TAG = "keynsmap";

  public static final String KEY_NSMAP_DEFAULT_ATTR = "default";

  public static final String KEY_NSMAP_KEY_TAG = "key";

  public static final String KEY_NSMAP_KEY_TAG_NAME_ATTR = "name";

  public static final String KEY_NSMAP_KEY_TAG_NS_ATTR = "ns";

  public static final String TYPE_NSMAP_TAG = "typesnsmap";

  public static final String TYPE_NSMAP_DEFAULT_ATTR = "default";

  public static final String TYPE_NSMAP_TYPE_TAG = "type";

  public static final String TYPE_NSMAP_TYPE_NAME_ATTR = "name";

  public static final String TYPE_NSMAP_TYPE_NS_ATTR = "ns";
  
}
