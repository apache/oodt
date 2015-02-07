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
 * PROFILE MANAGER - PASSWORD CHANGE
 * 
 * Attempt to change password. This script uses config file to 
 * check for password restrictions.
 * 
 * @author s.khudikyan
**/
$module = App::Get()->loadModule();

// Get instance of authentication class
$authentication = App::Get()->getAuthenticationProvider();


if ( !isset($_POST["password_confirm"]) || $_POST["password_confirm"] == "" ) {
	
	// password confirm is required
	App::Get()->SetMessage("Please confirm password.",CAS_MSG_ERROR);
	App::Get()->Redirect($module->moduleRoot . "/changePwd" );
	
} elseif ( (isset($_POST["password"]) || $_POST["password"] != "") && 
		 ( $_POST["password"] == $_POST["password_confirm"] ) ) {

		 	$message = $authentication->validateChangePassword( $_POST["password"] );

		 	if( is_array($message) ) {
 		
		 		foreach ($message as $value) {
		 			App::Get()->setMessage($value,CAS_MSG_ERROR);
		 		}
		   		
		   		App::Get()->Redirect($module->moduleRoot . "/changePwd" );
		 	} else{
		 		
		 		// Log the user out
				$authentication->logout();
				
				// End user session
				App::Get()->EndUserSession();
				
				// Redirect to confirmation page
		   		App::Get()->Redirect($module->moduleRoot . "/passwordChangeConfirmed" ); // password change successful
   		
   			}
		 } else {
	
			// password is required and must match
			App::Get()->SetMessage("Password cannot be blank and must match",CAS_MSG_ERROR);
			App::Get()->Redirect($module->moduleRoot . "/changePwd" );
		}
		