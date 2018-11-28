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
 *  @author Chris A. Mattmann
 *  @author Andrew F. Hart
 *  @version $Revision$
 * 
 *  PHP Single Sign On Library for EDRN PHP-based products.
 * 
 */

class Org_Apache_Oodt_Security_SingleSignOn {

	private $connectionStatus;
	
	private $conn;

	public function __construct() {
		$this->connectionStatus = 1;
	}

	public function getCurrentUsername() {
		return $this->getSingleSignOnUsername();
	}

	public function isLoggedIn() {
		return ($this->getSingleSignOnUsername() != null);
	}

	public function login($username, $password) {
		// first check to see if we are already signed in
		if ($this->getSingleSignOnUsername() <> "" 
			&& strcmp($this->getSingleSignOnUsername(), $username) == 0) {
			// we're logged in already
			return true;
		} else {
			// log in via LDAP
			$ldaprdn = "uid=" . $username . ',' . SSO_BASE_DN;
			$ldappass = $password;
			
			// connect to ldap server 
			$ldapconn = $this->connect(SSO_LDAP_HOST, SSO_LDAP_PORT);
			if ($ldapconn) {
				
				// binding to ldap server 
				$ldapbind = @ ldap_bind($ldapconn, $ldaprdn, $ldappass);

				// verify binding 
				if ($ldapbind) {
					$this->createSingleSignOnCookie($username, $password);
					return true;
				} else {
					return false;
				}

			} else {
				$this->connectionStatus = 0;
				return false;
			}
		}

	}

	public function logout() {
		$this->clearSingleSignOnInfo();
	}

	public function getLastConnectionStatus() {
		return ($this->connectionStatus == 1);
	}
	
	public function retrieveGroupsForUser($username,$searchDirectory = SSO_BASE_DN) {
		// attempt to connect to ldap server 
		$ldapconn = $this->connect(SSO_LDAP_HOST,SSO_LDAP_PORT);
		$groups   = array();
		if ($ldapconn) {
			$filter = "(&(objectClass=groupOfUniqueNames)"
				."(uniqueMember=uid={$username}," . SSO_BASE_DN . "))";
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
	 * 
	 * retrieves the set of attributes from the users ldap entry 
	 * @param string $username user for which attributes will be returned
	 * @param array $attributes ldap attributes to retrieve
	 * @param string $searchDirectory optional path to users ldap entry 
	 */
	public function retrieveUserAttributes($username,$attributes,$searchDirectory = SSO_BASE_DN) {
		// attempt to connect to ldap server 
		$ldapconn = $this->connect(SSO_LDAP_HOST,SSO_LDAP_PORT);
		$attr = array();
		
		if ($ldapconn) {
			// get user attributes
			$filter = "uid=".$username;
			$result = ldap_search($ldapconn,$searchDirectory,$filter,$attributes);
			if ($result) {
				$entries = ldap_get_entries($ldapconn,$result);
				return $entries;
			} else {
				return array();
			}
		}
	}
	
	
	public function changePassword($newPass,$encryptionMethod = "SHA") {
		if ($this->isLoggedIn()) {
			$user  = "uid={$this->getSingleSignOnUsername()}," . SSO_BASE_DN ;
			$entry = array();
			
			switch (strtoupper($encryptionMethod)) {
				case "SHA": 
					$entry['userPassword'] = "{SHA} " . base64_encode(pack("H*",sha1($newPass))); 
					break;
				case "MD5": 
					$entry['userPassword'] = "{MD5} " . base64_encode(pack("H*",md5($newPass)));  
					break;
				default:
					throw new Exception("Unsupported encryption method requested");
			}
			
			if (ldap_mod_replace($this->conn,$user,$entry)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
	public function connect($server,$port) {
		if ($conn = ldap_connect($server,$port)) {
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

	private function clearSingleSignOnInfo() {
		$oldCookie = $_COOKIE[SSO_COOKIE_KEY];
		setcookie(SSO_COOKIE_KEY, $oldCookie, 1, "/");
	}

	private function getSingleSignOnUsername() {
		$theCookie = $_COOKIE[SSO_COOKIE_KEY];
		if ($theCookie <> "") {
			$userpass = base64_decode(urldecode($theCookie));
			$userpassArr = explode(":", $userpass);
			return $userpassArr[0];
		} else
			return null;
	}

	private function createSingleSignOnCookie($username, $password) {
		if (!isset ($_COOKIE[SSO_COOKIE_KEY])) {
			$theCookieStrUnencoded = $username . ":" . $password;
			$theCookieStrEncoded = "\"".base64_encode($theCookieStrUnencoded)."\"";
			setcookie(SSO_COOKIE_KEY, $theCookieStrEncoded, time() + (86400 * 7), "/"); // expire in 1 day
		}
	}
}
?>
