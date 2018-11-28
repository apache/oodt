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
 * This view allows user to change password
 */
$module = App::Get()->loadModule();

?>
	
	<div class="breadcrumbs">
		<a href="<?php echo SITE_ROOT?>/">Home</a>
	</div>
    
    <div class='span-22 append-1 prepend-1 last' id='profile_container'>
		<h1>Change Password</h1>
		<div id="submenu">
			<p>
				<a href="<?php echo $module->moduleRoot?>/manage"> Manage Profile</a>
				&nbsp;&nbsp;|&nbsp;&nbsp;
				<a href="<?php echo $module->moduleRoot?>/changePwd">Change Password</a>
				&nbsp;&nbsp;|&nbsp;&nbsp;
				<a href="<?php echo $module->moduleRoot?>/groups">Groups</a>
			</p>
		</div>
		
		<br class="space"/>
		
		<fieldset id='profile_fieldset'>
		<form id="signupform" autocomplete="off" method="post" action="<?php echo $module->moduleRoot?>/changePwd.do">
    		<br class="space"/>
    			
        	<div class="span-4 prepend-1">
	    		<label for="password">Choose a password</label>
	    	</div>
	    	<div id="form_input" class="span-12">
	    		<input class="profile_input" id="password" name="password" type="password" maxlength="50" value="" />
	    	</div>
	    	
	    	<br class="space"/>
	    	
	    	<div class="span-4 prepend-1">
	    		<label for="password_confirm">Confirm password</label>
	    	</div>
	    	<div id="form_input" class="span-12">
	    		<input class="profile_input" id="password_confirm" name="password_confirm" type="password" maxlength="50" value="" />
	    	</div>
	    	
	    	<br class="space"/>
	    	<br class="space"/>
	    	
	    	<div class="span-10" align="center">
	    		<input class="profile_input" id="button_passwdReset" type="submit" value="Submit">
    		</div>
    		
			<br class="space"/>
    	</form>
    	</fieldset>
		
    </div>
