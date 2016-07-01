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

    String MAGIC_ATTR = "magic";

    String MIME_REPO_ATTR = "mimeRepo";

    String DEFAULT_EXTRACTOR_TAG = "default";

    String EXTRACTOR_TAG = "extractor";

    String NAMING_CONVENTION_TAG = "namingConvention";

    String EXTRACTOR_CLASS_TAG = "extractorClass";

    String EXTRACTOR_CONFIG_TAG = "config";

    String EXTRACTOR_PRECONDITIONS_TAG = "preconditions";

    String MIME_TAG = "mime";

    String MIME_TYPE_ATTR = "type";

    String CLASS_ATTR = "class";

    String FILE_ATTR = "file";

    String ENV_REPLACE_ATTR = "envReplace";
    
    String PRECONDITION_COMPARATORS_TAG = "preCondComparators";
    
    String PRECONDITION_COMPARATOR_TAG = "preCondComparator";
    
    String ID_ATTR = "id";
    	
}
