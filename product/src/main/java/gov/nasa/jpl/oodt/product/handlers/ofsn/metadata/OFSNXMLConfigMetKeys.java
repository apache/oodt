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

// OODT imports
import gov.nasa.jpl.oodt.product.handlers.ofsn.OFSNFileHandlerConfigurationReader; //javadoc

/**
 * 
 * Met Keys for the {@link OFSNFileHandlerConfigurationReader}
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public interface OFSNXMLConfigMetKeys {

  public static final String OFSN_CFG_ID_ATTR = "id";

  public static final String OFSN_CFG_NAME_ATTR = "name";

  public static final String OFSN_PRODUCT_ROOT_ATTR = "productRoot";

  public static final String HANDLER_TAG = "handler";

  public static final String HANDLER_CLASSNAME_ATTR = "class";

  public static final String HANDLER_NAME_ATTR = "name";

  public static final String HANDLER_TYPE_ATTR = "type";

  public static final String PROPERTY_TAG = "property";

  public static final String PROPERTY_NAME_ATTR = "name";

  public static final String PROPERTY_VALUE_ATTR = "value";

}
