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


package org.apache.oodt.cas.pushpull.config;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Met keys for the PushPullFramework {@link Config}
 * </p>.
 */
public interface ConfigMetKeys {

    public static final String STAGING_AREA_PREFIX = "Staging_Area";

    public static final String EXTERNAL_PROPERTIES_FILES = "org.apache.oodt.cas.pushpull.config.external.properties.files";

    public static final String PROTOCOL_FACTORY_INFO_FILES = "org.apache.oodt.cas.pushpull.config.protocolfactory.info.files";

    public static final String PARSER_INFO_FILES = "org.apache.oodt.cas.pushpull.config.parser.info.files";
    
    public static final String INGESTER_CLASS = "org.apache.oodt.cas.filemgr.ingester";
    
    public static final String INGESTER_DATA_TRANSFER = "org.apache.oodt.cas.filemgr.datatransfer.factory";
    
    public static final String INGESTER_FM_URL = "org.apache.oodt.cas.filemgr.url";
    
    public static final String NO_FM_SPECIFIED = "N/A";
    
    public static final String CACHE_FACTORY_CLASS = "org.apache.oodt.cas.filemgr.ingest.cache.factory";

    public static final String TYPE_DETECTION_FILE = "org.apache.oodt.cas.pushpull.config.type.detection.file";

    public static final String MET_LIST_TO_PRINT = "org.apache.oodt.cas.pushpull.metadata.list.to.print";

    public static final String ALLOW_ONLY_DEFINED_TYPES = "org.apache.oodt.cas.pushpull.allow.only.defined.types";

    public static final String USE_TRACKER = "org.apache.oodt.cas.pushpull.crawler.use.tracker";

    public static final String FILE_RET_SYSTEM_REC_THREAD_COUNT = "org.apache.oodt.cas.pushpull.file.retrieval.system.recommended.thread.count";

    public static final String FILE_RET_SYSTEM_MAX_ALLOWED_FAIL_DOWNLOADS = "org.apache.oodt.cas.pushpull.file.retrieval.system.max.number.allowed.failed.downloads";

    public static final String MET_FILE_EXT = "org.apache.oodt.cas.pushpull.met.file.extension";

    public static final String PROTOCOL_TIMEOUT_MS = "org.apache.oodt.cas.pushpull.protocol.timeout.milliseconds";

    public static final String PROTOCOL_PAGE_SIZE = "org.apache.oodt.cas.pushpull.protocol.page_size";

    public static final String DATA_FILE_BASE_STAGING_AREA = "org.apache.oodt.cas.pushpull.data.files.base.staging.area";

    public static final String WRITE_MET_FILE = "org.apache.oodt.cas.pushpull.write.met.file";
    
}
