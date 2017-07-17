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
 * 
 * Met Keys for the {@link org.apache.oodt.product.handlers.ofsn.OFSNFileHandlerConfigurationReader}
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public interface OFSNXMLConfigMetKeys {

  String OFSN_CFG_ID_ATTR = "id";

  String OFSN_CFG_NAME_ATTR = "name";

  String OFSN_PRODUCT_ROOT_ATTR = "productRoot";

  String HANDLER_TAG = "handler";

  String HANDLER_CLASSNAME_ATTR = "class";

  String HANDLER_NAME_ATTR = "name";

  String HANDLER_TYPE_ATTR = "type";

  String PROPERTY_TAG = "property";

  String PROPERTY_NAME_ATTR = "name";

  String PROPERTY_VALUE_ATTR = "value";
  
  /* optional handler property attributes supported by all handlers */
  String PROPERTY_MIMETYPE_ATTR = "mimeType";

}
