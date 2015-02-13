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
 * PROFILE MANAGER - CREATE USER
 * 
 * Create new user, required attributes:
 * 
 * 	1. First name
 * 	2. Last name
 * 	3. Username
 * 	4. Email 
 * 
 * 	- password
 * 	- confirm password
 * 
 * @author s.khudikyan
**/
$module = App::Get()->loadModule();
$allSet = true;


if ( isset($_POST["firstname"]) && $_POST["firstname"] != "" ) {
	$info[ App::Get()->settings['firstname_attr'] ] = $_POST["firstname"];
} else {
	// first name is required
	App::Get()->SetMessage("First name cannot be blank",CAS_MSG_ERROR);
	$allSet = FALSE;
}

if ( isset($_POST["lastname"]) && $_POST["lastname"] != "" ) {
	$info[ App::Get()->settings['lastname_attr'] ] = $_POST["lastname"];
} else {
	// last name is required
	App::Get()->SetMessage("Last name cannot be blank",CAS_MSG_ERROR);
	$allSet = FALSE;
}

if ( isset($_POST["username"]) && $_POST["username"] != "" ) {
	
	// check username availability
	$isAvailable = App::Get()->getAuthenticationProvider()->usernameAvailability($_POST["username"]);	
	if ( $isAvailable ) {
		$info[ App::Get()->settings['username_attr'] ] = $_POST["username"];
	} else{
		App::Get()->SetMessage("Username has been taken",CAS_MSG_ERROR);
		$allSet = FALSE;
	}
} else {
	// username is required
	App::Get()->SetMessage("Username cannot be blank",CAS_MSG_ERROR);
	$allSet = FALSE;
}

if ( isset($_POST["email"]) && $_POST["email"] != "") {
	$info[ App::Get()->settings['email_attr'] ] = $_POST["email"];
} else {
	// email is required
	App::Get()->SetMessage("Email cannot be blank",CAS_MSG_ERROR);
	$allSet = FALSE;
}

if ( !isset($_POST["password_confirm"]) || $_POST["password_confirm"] == "" ) {
	
	// password confirm is required
	App::Get()->SetMessage("Password cannot be blank and must match",CAS_MSG_ERROR);
	$allSet = FALSE;
} elseif ( (isset($_POST["password"]) || $_POST["password"] != "") && 
		   ( $_POST["password"] == $_POST["password_confirm"] ) ) {
		   	
	$info['userPassword'] = $_POST["password"];
} else {
	
	// password is required and must match
	App::Get()->SetMessage("Password cannot be blank and must match",CAS_MSG_ERROR);
	$allSet = FALSE;
}

if ($allSet) {
	
	$info[ App::Get()->settings['commonname_attr'] ] = $_POST["firstname"] . " " . $_POST["lastname"];
	$info[ "objectClass" ] = "inetOrgPerson";
	if ( App::Get()->getAuthenticationProvider()->addUser($info) ) {
		
		App::Get()->Redirect($module->moduleRoot . "/login" ); // add account successful	
	} else {
		App::Get()->setMessage("Could not add user.",CAS_MSG_ERROR);
	}
} else {
	
	App::Get()->Redirect($module->moduleRoot . "/createUser" );
}
