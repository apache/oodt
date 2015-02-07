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
 * PROFILE MANAGER - MANAGE USER PROFILE
 * 
 * Process user's changes to profile information. 
 * 
 * @author s.khudikyan
**/
$module = App::Get()->loadModule();
$allSet = true;

foreach ($_POST as $key=>$value) {

	if ( $key != submit_button) {
		if ( isset($value) && $value != "" ) {
			$info[$key] = $value;
		} else {
			// set error message
			App::Get()->SetMessage( 
				array_search( $key, App::Get()->settings['attr_titles'] ) 
				. " cannot be left blank.", CAS_MSG_ERROR );
			$allSet = false;
		}
	}
}

if ($allSet) {
	// method to edit user
	if( App::Get()->getAuthenticationProvider()->updateProfile($info) ){
 		
		// user info change successful
   		App::Get()->Redirect($module->moduleRoot . "/" );
   	} else{
   		
   		// if not logged in - cannot change pwd
   		App::Get()->SetMessage("Invalid entry",CAS_MSG_ERROR);
   		App::Get()->Redirect($module->moduleRoot . "/manage" );
   	}
} else {
	
	App::Get()->Redirect($module->moduleRoot . "/manage" );
}
