/**
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

package org.apache.oodt.security.sso.opensso;

/**
 * 
 * Met keys for dealing with {@link SSOProxy}.
 * 
 */
public interface SSOMetKeys {

  /* service endpoints */
  String AUTHENTICATE_ENDPOINT = "https://host/opensso/identity/authenticate";

  String IDENTITY_READ_ENDPOINT = "https://host/opensso/identity/read";

  String IDENTITY_ATTRIBUTES_ENDPOINT = "https://host/opensso/identity/attributes";

  String LOGOUT_ENDPOINT = "https://host/opensso/identity/logout";

  /* cookie names */

  String SSO_COOKIE_KEY = "iPlanetDirectoryPro";

  String USER_COOKIE_KEY = "curationWebapp";

  /* Identity Details response object */

  String IDENTITY_DETAILS_NAME = "identitydetails.name";

  String IDENTITY_DETAILS_TYPE = "identitydetails.type";

  String IDENTITY_DETAILS_REALM = "identitydetails.realm";

  String IDENTITY_DETAILS_GROUP = "identitydetails.group";

  String IDENTITY_DETAILS_ATTR_NAME = "identitydetails.attribute.name";

  String IDENTITY_DETAILS_ATTR_VALUE = "identitydetails.attribute.value";
  
  String IDENTITY_DETAILS_ATTR_SKIP_LINE = "identitydetails.attribute=";

  /* User Details response object */
  String USER_DETAILS_TOKEN = "userdetails.token.id";

  String USER_DETAILS_ROLE = "userdetails.role=id";

  String USER_DETAILS_ATTR_NAME = "userdetails.attribute.name";

  String USER_DETAILS_ATTR_VALUE = "userdetails.attribute.value";
  
  String UID_ATTRIBUTE_NAME = "uid";

  /* commands available from SSOProxy command line */
  String AUTH_COMMAND = "authenticate";

  String IDENTITY_COMMAND = "identity";

  String ATTRIBUTES_COMMAND = "attributes";
  
  String LOGOUT_COMMAND = "logout";

  /* general stuff */
  String UNKNOWN_USER = "Unknown";

}
