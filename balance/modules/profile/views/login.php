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
 * Profile Manager:
 * Login page
 */
$module = App::Get()->loadModule();

// If a user is already logged in, redirect to home page
if( App::Get()->getAuthenticationProvider()->isLoggedIn() ) App::Get()->Redirect(SITE_ROOT . "/"); 

?>
	
	<div class="breadcrumbs">
		<a href="<?php echo SITE_ROOT?>/">Home</a>
	</div>
    
	<div class='span-22 append-1 prepend-1 last' id='profile_container'> 
		<h1>Please log in to continue...</h1>
		
		<br class="space"/>
		
		<fieldset id='profile_fieldset'>
		<form id="signupform" autocomplete="off" method="post" action="<?php echo $module->moduleRoot ?>/login.do">
			<br class="space"/>
		 	<br class="space"/>
		 	
			<div class="span-2 prepend-1">
				<label for="username">Username</label>
			</div>
			<div class="span-15">
				<input class="profile_input" id="username" name="username" type="text" value="" maxlength="50" />
			</div>
			
			<br class="space"/>
			
			<div class="span-2 prepend-1">
	    		<label for="password">Password</label>
	    	</div>
	    	<div class="span-15">
	    		<input class="profile_input" id="password" name="password" type="password" maxlength="50" value="" />
	    	</div>
	    	
	    	<br class="space"/>
	    	<br class="space"/>
	    	
	    	<div class="span-10" align="center">
	    		<input class="profile_input" id="button_new_account" type="submit" value="Submit">
    		</div>
    		
    		<br class="space"/>
    		<br class="space"/>
		</form>
		</fieldset>
	</div>
