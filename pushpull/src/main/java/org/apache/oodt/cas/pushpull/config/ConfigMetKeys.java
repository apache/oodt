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

    String STAGING_AREA_PREFIX = "Staging_Area";

    String EXTERNAL_PROPERTIES_FILES = "org.apache.oodt.cas.pushpull.config.external.properties.files";

    String PROTOCOL_FACTORY_INFO_FILES = "org.apache.oodt.cas.pushpull.config.protocolfactory.info.files";

    String PARSER_INFO_FILES = "org.apache.oodt.cas.pushpull.config.parser.info.files";
    
    String INGESTER_CLASS = "org.apache.oodt.cas.filemgr.ingester";
    
    String INGESTER_DATA_TRANSFER = "org.apache.oodt.cas.filemgr.datatransfer.factory";
    
    String INGESTER_FM_URL = "org.apache.oodt.cas.filemgr.url";
    
    String NO_FM_SPECIFIED = "N/A";
    
    String CACHE_FACTORY_CLASS = "org.apache.oodt.cas.filemgr.ingest.cache.factory";

    String TYPE_DETECTION_FILE = "org.apache.oodt.cas.pushpull.config.type.detection.file";

    String MET_LIST_TO_PRINT = "org.apache.oodt.cas.pushpull.metadata.list.to.print";

    String ALLOW_ONLY_DEFINED_TYPES = "org.apache.oodt.cas.pushpull.allow.only.defined.types";

    String USE_TRACKER = "org.apache.oodt.cas.pushpull.crawler.use.tracker";

    String FILE_RET_SYSTEM_REC_THREAD_COUNT = "org.apache.oodt.cas.pushpull.file.retrieval.system.recommended.thread.count";

    String FILE_RET_SYSTEM_MAX_ALLOWED_FAIL_DOWNLOADS = "org.apache.oodt.cas.pushpull.file.retrieval.system.max.number.allowed.failed.downloads";

    String MET_FILE_EXT = "org.apache.oodt.cas.pushpull.met.file.extension";

    String PROTOCOL_TIMEOUT_MS = "org.apache.oodt.cas.pushpull.protocol.timeout.milliseconds";

    String PROTOCOL_PAGE_SIZE = "org.apache.oodt.cas.pushpull.protocol.page_size";

    String DATA_FILE_BASE_STAGING_AREA = "org.apache.oodt.cas.pushpull.data.files.base.staging.area";

    String WRITE_MET_FILE = "org.apache.oodt.cas.pushpull.write.met.file";
    
}
