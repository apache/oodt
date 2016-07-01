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
  
  String CHANNEL_LINK_ATTR = "channelLink";
  
  String NAMESPACE_TAG = "namespace";
  
  String NAMESPACE_ATTR_PREFIX = "prefix";
  
  String NAMESPACE_ATTR_URI = "uri";

  String TAG_TAG = "tag";

  String TAG_ATTR_NAME = "name";

  String TAG_ATTR_SOURCE = "source";

  String ATTRIBUTE_TAG = "attribute";

  String ATTRIBUTE_ATTR_NAME = "name";

  String ATTRIBUTE_ATTR_VALUE = "value";

}
