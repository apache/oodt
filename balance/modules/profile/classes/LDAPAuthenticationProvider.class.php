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
 * LDAPAuthenticationProvider
 * 
 * This class provides an extension of the OODT Security 'Single-sign on'
 * functionality that complies with the contract specified in the OODT Balance
 * ApplicationAuthenticationProvider interface.
 * 
 * For more information on the functions available here, consult either the
 * OODT Security package or the Balance ApplicationAuthenticationProvider 
 * interface documentation.
 * 
 * Note: This class has a dependency on the OODT CAS-SSO package 
 *      (http://oodt.jpl.nasa.gov/repo/framework/cas-sso/trunk/src/php/pear)
 *      
 *      To build this dependency, check out the above project and then:
 *      1) cd into the checked out project (you should see a package.xml file)
 *      2) pear package
 *      3) (sudo) pear install --force Gov_Nasa_Jpl...tar.gz
 * 
 * @author s.khudikyan
 * @author ahart
 * 
 */

require("Org/Apache/Oodt/Security/SingleSignOn.php");

class LDAPAuthenticationProvider
	extends Org_Apache_Oodt_Security_SingleSignOn 
	implements Org_Apache_Oodt_Balance_Interfaces_IApplicationAuthenticationProvider {
	
	// The LDAP server name
	protected $ldapHost;
	
	// The port on which the LDAP server listens
	protected $ldapPort;


	/**
	 * Constructor
	 */
	public function __construct() {
		
		// set LDAP constants
		define("CAS_SECURITY",true);
		define("SSO_LDAP_HOST", App::Get()->settings['ldap_host']);
		define("SSO_LDAP_PORT", App::Get()->settings['ldap_port']);
		define("SSO_BASE_DN", App::Get()->settings['ldap_base_dn']);
		define("SSO_GROUPS_DN", App::Get()->settings['ldap_group_dn']);
		define("SSO_COOKIE_KEY", App::Get()->settings['cookie_key']);
		
		$this->ldapHost = SSO_LDAP_HOST;
		$this->ldapPort = SSO_LDAP_PORT;
	}
	
	/**
	 * Connect to the LDAP server
	 */
	public function connect() {
		return parent::connect($this->ldapHost,$this->ldapPort);
	}
	
	/**
	 * Disconnect from the LDAP server
	 */
	public function disconnect() {
		ldap_close($this->conn);
	}
	
	/**
	 * Determine whether or not a user has authenticated
	 * 
	 * @return boolean Whether or not the current user has authenticated
	 */
	public function isLoggedIn() {
		return parent::isLoggedIn();
	}
	
	/**
	 * Process a user login
	 * 
	 * @param string $username The provided username
	 * @param string $password The provided password
	 */
	public function login( $username, $password ) {
		return parent::login( $username, $password );
	}
	
	/**
	 * End an authenticated user's session
	 */
	public function logout() {
		parent::logout();
	}

	/**
	 * Return the unique username of the currently authenticated user
	 */
	public function getCurrentUsername() {
		return parent::getCurrentUsername();
	}
	
	/**
	 * Process a password change request for the currently authenticated
	 * user
	 * 
	 * @param string $newPassword The new password to associate with the user
	 */
	public function changePassword( $newPassword ) {
		if ( App::Get()->settings['auth_encryption_method'] ) {
			return parent::changePassword( $newPassword, App::Get()->settings['auth_encryption_method'] );
		}
		return parent::changePassword( $newPassword );
	}
	
	/**
	 * Attempt to validate a candidate value for new user password
	 * 
	 * @param string $newPass The candidate password value
	 * @param string $encryptionMethod The encryption method to use
	 */
	public function validateChangePassword( $newPass, $encryptionMethod = "SHA" ) {
		$isValid = true;
		$messages = array();
		// validate rules from config file
		$rules = App::Get()->settings['security_password_rules'];

		if ( isset($rules) ) {
			foreach( $rules as $rule ){
				
				// Separate the rule from the error message
				list($regularExpression,$errorMessage) = explode('|',$rule,2);
				
				// Test the rule
				$rulePassed = preg_match($regularExpression, $newPass);
				
				// If the rule failed, append the error message
				if (!$rulePassed) {
					$messages[] = $errorMessage;
					$isValid    = false;
				}
			}
		}

		if ($isValid && $this->connect(SSO_LDAP_HOST,SSO_LDAP_PORT)) {
		  $result = $this->changePassword($newPass,$encryptionMethod);
		  return true;
		} else
		  return $messages;
	}
	
	/**
	 * Obtain detailed information about the specified user. 
	 * 
	 * This function accepts both a username and an array of attributes 
	 * corresponding to the variables in the LDAP user record for which 
	 * values should be returned. 
	 * 
	 * @param string $username   The username to obtain information for
	 * @param array  $attributes The set of variables to return values for
	 */
	public function retrieveUserAttributes( $username, $attributes ) {
		$rawArray 		= parent::retrieveUserAttributes( $username, $attributes );
		$userAttributes = array();
		
		if ( count($rawArray) > 1 ) {
			$rawArray = $rawArray[0];
			// Get only necessary attributes to return
			foreach ( $rawArray as $key=>$keyValue ) {
				foreach ( $attributes as $value ) {
					if ( $key === $value ) {
						$userAttributes[$key] = $keyValue[0];
					}
				}
			}
		}
		return $userAttributes;
	}
	
	/**
	 * Add a user record to the LDAP directory
	 * 
	 * This function accepts an associative array of information about 
	 * a user. The keys in this array correspond to the named variables
	 * in the user object in the LDAP directory. At a minimum, this 
	 * array must contain a key 'uid' which must be unique across users
	 * so that a proper dn can be generated for the new user.
	 * 
	 * @param array $userInfo The information to include in the record
	 */
	public function addUser($userInfo) {
		$ldapconn = $this->connect(SSO_LDAP_HOST,SSO_LDAP_PORT);
		if ($ldapconn) {
			$user  = "uid={$userInfo[ "uid" ]}," . SSO_BASE_DN;
			return ldap_add($ldapconn,$user,$userInfo);
		}
		// connection failed
		return false;
	}
	
	/**
	 * Determine if the specified username is available
	 * 
	 * This function uses the value of the config setting 'username_attr' when
	 * determining which key in the LDAP user record to use as the username
	 * attribute. 
	 * 
	 * @param string $username The username to test for availability
	 */
	public function usernameAvailability( $username ) {
		$justthese = array( App::Get()->settings['username_attr'] );
		$profile = $this->retrieveUserAttributes($username, $justthese);
		if (count($profile) > 0) {
			return false;
		} else {
			// available
			return true;
		}
	}
	
	/**
	 * Update an existing user profile with new information
	 * 
	 * This function accepts an associative array containing named key/value
	 * pairs. The keys in this array correspond to the named variables
	 * in the user object in the LDAP directory. The function can only update
	 * the record for the currently authenticated user.
	 * 
	 * @param array $newInfo The updated information for the user's profile
	 */
	public function updateProfile($newInfo) {

		if ($this->isLoggedIn()) {
			$user  = "uid={$this->getCurrentUsername()}," . SSO_BASE_DN ;
			$ldapconn = $this->connect(SSO_LDAP_HOST,SSO_LDAP_PORT);
			
			if (ldap_mod_replace($ldapconn,$user,$newInfo)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
}
