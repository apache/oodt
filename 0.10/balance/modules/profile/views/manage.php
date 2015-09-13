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
 * This view will expose the profile attributes specified in the config.ini 
 * (profile_modify_attributes) and will allow user to modify values in the LDAP directory.
 */
$module = App::Get()->loadModule();

function manageAttribute() {
	$userAttr = App::Get()->getAuthenticationProvider()->retrieveUserAttributes( 
				App::Get()->getAuthenticationProvider()->getCurrentUsername(), App::Get()->settings['profile_modify_attributes'] );
	$str = '';
 	foreach ($userAttr as $key=>$keyValue) {
 		foreach (App::Get()->settings['attr_titles'] as $attrTitle=>$value) {
 			if ( $key != App::Get()->settings['username_attr']) {

	 			if ( $key === $value) {
	 				$str .= '<div class="span-3 prepend-1"><label for="';
	 				$str .= $key . '"> ' . $attrTitle;
	 				$str .= '</label></div>';
	 				
	 				$str .= '<div class="span-12"><input class="profile_input" type="text" maxlength="100" id=';
	 				$str .= $key . ' name=' . $key . ' value=' . $keyValue;
	 				$str .= '></div>';
	 				$str .= '<br class="space"/>';
	 			}
 			}
 		}
 	}

 	return $str;
}
?>
	
	<div class="breadcrumbs">
		<a href="<?php echo SITE_ROOT?>/">Home</a>
	</div>
    
    <div class='span-22 append-1 prepend-1 last' id='profile_container'>
    	<h1>Manage Your Profile</h1>
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
		<form id="signupform" autocomplete="off" method="post" action="<?php echo $module->moduleRoot?>/manage.do">
	    	<br class="space"/>

			<?php echo manageAttribute()?>

	    	<br class="space"/>
	    	<br class="space"/>
	    	
	    	<div class="span-10" align="center">
	    		<input class="profile_input" type="submit" value="Submit" name="submit_button">
    		</div>
    		
	    	<br class="space"/>
    	</form>
		</fieldset>
		
    </div>
