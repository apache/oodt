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


package gov.nasa.jpl.oodt.product.handlers.ofsn.metadata;

/**
 * Met keys for use in OFSN handler definition.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public interface OFSNMetKeys {

  public static final String LISTING_CMD = "listing";

  public static final String GET_CMD = "get";
  
  public static final String OFSN_XML_CONF_FILE_KEY = "gov.nasa.jpl.oodt.product.handlers.ofsn.xmlConfigFilePath";
  
  public static final String OFSN_COMPUTE_DIR_SIZE = "gov.nasa.jpl.oodt.product.handlers.ofsn.computeDirSize";

}
