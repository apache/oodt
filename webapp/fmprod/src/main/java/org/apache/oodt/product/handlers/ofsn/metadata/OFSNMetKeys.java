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


package org.apache.oodt.product.handlers.ofsn.metadata;

/**
 * Met keys for use in OFSN handler definition.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public interface OFSNMetKeys {

  String LISTING_CMD = "listing";

  String GET_CMD = "get";
  
  String OFSN_XML_CONF_FILE_KEY = "org.apache.oodt.product.handlers.ofsn.xmlConfigFilePath";
  
  String OFSN_COMPUTE_DIR_SIZE = "org.apache.oodt.product.handlers.ofsn.computeDirSize";
  
  String OFSN_COMPUTE_FILE_SIZE = "org.apache.oodt.product.handlers.ofsn.computeFileSize";


}
