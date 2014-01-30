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


package org.apache.oodt.cas.product.rss;

/**
 * 
 * Met keys used by the {@link RSSConfigReader}.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public interface RSSConfigReaderMetKeys {
  
  public static final String CHANNEL_LINK_ATTR = "channelLink";
  
  public static final String NAMESPACE_TAG = "namespace";
  
  public static final String NAMESPACE_ATTR_PREFIX = "prefix";
  
  public static final String NAMESPACE_ATTR_URI = "uri";

  public static final String TAG_TAG = "tag";

  public static final String TAG_ATTR_NAME = "name";

  public static final String TAG_ATTR_SOURCE = "source";

  public static final String ATTRIBUTE_TAG = "attribute";

  public static final String ATTRIBUTE_ATTR_NAME = "name";

  public static final String ATTRIBUTE_ATTR_VALUE = "value";

}
