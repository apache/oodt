<?php
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

/**
 * 
 *  @package Org_Apache_Oodt_Security
 *  @author  Andrew F. Hart
 *  @version $Revision$
 * 
 *  PHP Single Sign On Authentication Library and Utilities
 * 
 */

/**
 * LDAP Host URI
 * 
 * The path to the host where the LDAP directory server is running
 * @var string
 */
define("SSO_LDAP_HOST", "ldaps://host.domain");

/**
 * LDAP Port
 * 
 * The port on @link{SSO_LDAP_HOST} where the LDAP directory server
 * is listening.
 * 
 * Default ports:
 *   389: Standard (non-ssl) LDAP (ldap://...)
 *   636: Secure LDAP (ldaps://...)
 * @var string
 */
define("SSO_LDAP_PORT", 636);

/**
 * Base DN
 * 
 * The base domain name to use when interacting with the LDAP server
 *
 * @var string
 */
define("SSO_BASE_DN", "dc=sample, dc=jpl, dc=nasa, dc=gov");

/**
 * Cookie Key
 * 
 * The unique string to use in identifying the security cookie
 * 
 */
define("SSO_COOKIE_KEY", "__ac__sc__");
