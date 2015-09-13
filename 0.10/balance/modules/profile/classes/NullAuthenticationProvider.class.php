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
 * NullAuthenticationProvider
 * 
 * An implementation of the iApplicationAuthenticationProvider interface with
 * no external dependencies (and also limited functionality) intended to be used
 * as a development tool for quickly testing and debugging applications without
 * having to set up detailed auth&auth dependencies. 
 * 
 * This class utilizes the PHP Session construct for storing a provided username.
 * Any username / password combination is treated as valid.
 * 
 */
class Org_Apache_Oodt_Balance_Providers_Authentication_NullAuthenticationProvider
	implements Org_Apache_Oodt_Balance_Interfaces_IApplicationAuthenticationProvider {

	public function __construct() {}
	
	public function connect() {
		return true;
	}
	
	public function disconnect() {
		return true;
	}
	
	public function isLoggedIn() {
		return isset($_SESSION['_auth']);
	}
	
	public function login( $username, $password ) {
        $_SESSION['_auth'] = array(
            'username' => $username
        );
	}
	
	public function logout() { 
        unset($_SESSION['_auth']);
	}

	public function getCurrentUsername() {
		return ($this->isLoggedIn())
            ? $_SESSION['_auth']['username']
            : false;
	}
	
	public function changePassword( $newPassword ) {
	       return true;
	}
	
	public function validateChangePassword( $newPass, $encryptionMethod = "SHA" ) {
	       return true;
	}
	
	public function retrieveUserAttributes( $username, $attributes ) {
		return ($this->isLoggedIn())
            ? array(
		       "cn"        => $this->getCurrentUsername(),
		       "givenname" => $this->getCurrentUsername(),
		       "sn"        => '',
		       "uid"       => $this->getCurrentUsername(),
		       "mail"      => 'guest@example.org'
		    )
            : false;
	}
	
	public function addUser($userInfo) {
	       return false;
	}
	
	public function usernameAvailability( $username ) {
	       return false;
	}
	
	public function updateProfile($newInfo) {
	       return false;

	}	
}
