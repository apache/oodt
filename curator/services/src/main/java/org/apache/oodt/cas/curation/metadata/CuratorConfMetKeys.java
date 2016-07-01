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


package org.apache.oodt.cas.curation.metadata;

/**
 * 
 * Met keys used in the <code>context.xml</code> file to configure the CAS
 * curator webapp.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public interface CuratorConfMetKeys {

  String MET_EXTRACTOR_CONF_UPLOAD_PATH = "org.apache.oodt.cas.curator.metExtractorConf.uploadPath";

  String POLICY_UPLOAD_PATH = "org.apache.oodt.cas.curator.dataDefinition.uploadPath";

  String FM_URL = "org.apache.oodt.cas.fm.url";

  String DEFAULT_TRANSFER_FACTORY = "org.apache.oodt.cas.filemgr.datatransfer.LocalDataTransferFactory";

  String CRAWLER_CONF_FILE = "classpath:/org.apache/oodt/cas/crawl/crawler-config.xml";

  String STAGING_AREA_PATH = "org.apache.oodt.cas.curator.stagingAreaPath";

  String MET_AREA_PATH = "org.apache.oodt.cas.curator.metAreaPath";
  
  String MET_EXTENSION = "org.apache.oodt.cas.curator.metExtension";
  
  String FM_PROPS = "org.apache.oodt.cas.curator.fmProps";
  
  String CATALOG_FACTORY_CLASS = "org.apache.oodt.cas.curator.catalogFactoryClass";

}
