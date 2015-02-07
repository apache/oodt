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
 * LDAPAuthorizationProvider
 * 
 * This class provides an implementation of the OODT Balance Application 
 * Authorization Provider interface that makes use of the Lightweight
 * Directory Access Protocol (LDAP).
 * 
 * For more information on the functions available here, consult the
 * OODT Balance ApplicationAuthenticationProvider interface documentation.
 *
 * @author s.khudikyan
 * @author ahart
 * 
 */

class LDAPAuthorizationProvider 
	implements Org_Apache_Oodt_Balance_Interfaces_IApplicationAuthorizationProvider {

	
	// The LDAP server name
	protected $ldapHost;
	
	// The port on which the LDAP server listens
	protected $ldapPort;
	
	/**
	 * Constructor
	 */
	public function __construct() {
		
		// Set LDAP constants
		define("AUTH_BASE_DN",   App::Get()->settings['authorization_ldap_base_dn']);
		define("AUTH_GROUPS_DN", App::Get()->settings['authorization_ldap_group_dn']);
		define("AUTH_LDAP_HOST", App::Get()->settings['authorization_ldap_host']);
		define("AUTH_LDAP_PORT", App::Get()->settings['authorization_ldap_port']);
		
		$this->ldapHost = AUTH_LDAP_HOST;
		$this->ldapPort = AUTH_LDAP_PORT;
	}
	
	/**
	 * Obtain the groups/roles for the current username
	 * 
	 * This function searches {$searchDirectory} for groupOfUniqueName objects whose
	 * uniqueMember attribute contains {$username}. The cn attribute of all matching
	 * groups is returned as a numeric array.
	 * 
	 * @param string $username The username to test for. Note that just the username 
	 *                         portion should be specified, as both 'uid=' and AUTH_BASE_DN
	 *                         are added to the value before searching.
	 * @param string $searchDirectory The fully qualified DN (e.g.: ou=system,ou=groups)
	 *                         of the LDAP directory in which to search for groups
	 */
	public function retrieveGroupsForUser($username,$searchDirectory = AUTH_GROUPS_DN) {
		
		// attempt to connect to ldap server 
		$ldapconn = $this->connect(AUTH_LDAP_HOST,AUTH_LDAP_PORT);
		$groups   = array();
		if ($ldapconn) {
			$filter = "(&(objectClass=groupOfUniqueNames)"
				."(uniqueMember=uid={$username}," . AUTH_BASE_DN . "))";
			$result = ldap_search($ldapconn,$searchDirectory,$filter,array('cn'));
			
			if ($result) {
				$entries = ldap_get_entries($ldapconn,$result);
				foreach ($entries as $rawGroup) {
					if (isset($rawGroup['cn'][0]) 
					&& $rawGroup['cn'][0] != '') {
						$groups[] = $rawGroup['cn'][0];
					}
				}
			}
		} 
		return $groups;
	}
			
	/**
	 * Connect to the LDAP server
	 */
	public function connect() {
		if ($conn = ldap_connect($this->ldapHost,$this->ldapPort)) {
			
			// Connection established
			$this->connectionStatus = 1;
			ldap_set_option($conn, LDAP_OPT_PROTOCOL_VERSION, 3);
			ldap_set_option($conn, LDAP_OPT_DEBUG_LEVEL, 7);
			ldap_set_option($conn, LDAP_OPT_REFERRALS, 0);	
			$this->conn = $conn;
			return $conn;
		} else {
			// Connection failed
			return false;
		}
	}		
}