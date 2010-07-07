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


package org.apache.oodt.cas.crawl.typedetection;

/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * XML Config file Met Keys for the {@link MimeExtractorConfig}
 * </p>.
 */
public interface MimeExtractorConfigMetKeys {

    public static final String MAGIC_ATTR = "magic";

    public static final String MIME_REPO_ATTR = "mimeRepo";

    public static final String DEFAULT_EXTRACTOR_TAG = "default";

    public static final String EXTRACTOR_TAG = "extractor";

    public static final String EXTRACTOR_CLASS_TAG = "extractorClass";

    public static final String EXTRACTOR_CONFIG_TAG = "config";

    public static final String EXTRACTOR_PRECONDITIONS_TAG = "preconditions";

    public static final String MIME_TAG = "mime";

    public static final String MIME_TYPE_ATTR = "type";

    public static final String CLASS_ATTR = "class";

    public static final String FILE_ATTR = "file";

    public static final String ENV_REPLACE_ATTR = "envReplace";
    
    public static final String PRECONDITION_COMPARATORS_TAG = "preCondComparators";
    
    public static final String PRECONDITION_COMPARATOR_TAG = "preCondComparator";
    
    public static final String ID_ATTR = "id";
    	
}
