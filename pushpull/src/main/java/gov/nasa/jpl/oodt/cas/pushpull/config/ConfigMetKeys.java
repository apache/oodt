//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.pushpull.config;

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

    public static final String EXTERNAL_PROPERTIES_FILES = "gov.nasa.jpl.oodt.cas.pushpull.config.external.properties.files";

    public static final String PROTOCOL_FACTORY_INFO_FILES = "gov.nasa.jpl.oodt.cas.pushpull.config.protocolfactory.info.files";

    public static final String PARSER_INFO_FILES = "gov.nasa.jpl.oodt.cas.pushpull.config.parser.info.files";
    
    public static final String INGESTER_CLASS = "gov.nasa.jpl.oodt.cas.filemgr.ingester";
    
    public static final String INGESTER_DATA_TRANSFER = "gov.nasa.jpl.oodt.cas.filemgr.datatransfer.factory";
    
    public static final String INGESTER_FM_URL = "gov.nasa.jpl.oodt.cas.filemgr.url";
    
    public static final String NO_FM_SPECIFIED = "N/A";
    
    public static final String CACHE_FACTORY_CLASS = "gov.nasa.jpl.oodt.cas.filemgr.ingest.cache.factory";

    public static final String TYPE_DETECTION_FILE = "gov.nasa.jpl.oodt.cas.pushpull.config.type.detection.file";

    public static final String MET_LIST_TO_PRINT = "gov.nasa.jpl.oodt.cas.pushpull.metadata.list.to.print";

    public static final String ALLOW_ONLY_DEFINED_TYPES = "gov.nasa.jpl.oodt.cas.pushpull.allow.only.defined.types";

    public static final String USE_TRACKER = "gov.nasa.jpl.oodt.cas.pushpull.crawler.use.tracker";

    public static final String FILE_RET_SYSTEM_REC_THREAD_COUNT = "gov.nasa.jpl.oodt.cas.pushpull.file.retrieval.system.recommended.thread.count";

    public static final String FILE_RET_SYSTEM_MAX_ALLOWED_FAIL_DOWNLOADS = "gov.nasa.jpl.oodt.cas.pushpull.file.retrieval.system.max.number.allowed.failed.downloads";

    public static final String MET_FILE_EXT = "gov.nasa.jpl.oodt.cas.pushpull.met.file.extension";

    public static final String PROTOCOL_TIMEOUT_MS = "gov.nasa.jpl.oodt.cas.pushpull.protocol.timeout.milliseconds";

    public static final String PROTOCOL_PAGE_SIZE = "gov.nasa.jpl.oodt.cas.pushpull.protocol.page_size";

    public static final String DATA_FILE_BASE_STAGING_AREA = "gov.nasa.jpl.oodt.cas.pushpull.data.files.base.staging.area";

    public static final String WRITE_MET_FILE = "gov.nasa.jpl.oodt.cas.pushpull.write.met.file";
    
}
