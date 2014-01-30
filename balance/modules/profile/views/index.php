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
 * This view will expose the profile attributes specified in the config.ini (profile_attributes).
 */
$module = App::Get()->loadModule();

function displayAttributes($userAttr) {

	$str = '';
 	foreach ($userAttr as $key=>$keyValue) {
 		foreach (App::Get()->settings['attr_titles'] as $attrTitle=>$value) {
 			if ( $key === $value) {
 				$str .= '<div class="span-13 prepend-1"><h4 align="left">';
 				$str .= $attrTitle;
 				$str .= '</h4></div>';
 				
 				$str .= '<div class="span-5"><h6 align="left">';
 				$str .= $keyValue;
 				$str .= '</h6></div>';
 			}
 		}
 	}
 	return $str;	
}

	// Get user attributes
	$userAttr = App::Get()->getAuthenticationProvider()->retrieveUserAttributes( 
				App::Get()->getAuthenticationProvider()->getCurrentUsername(), App::Get()->settings['profile_attributes'] );

?>
	
	<div class="breadcrumbs">
		<a href="<?php echo SITE_ROOT?>/">Home</a>
	</div>
    
	<div class='span-22 append-1 prepend-1 last' id='profile_container'>
		<h1>Welcome <?php echo $userAttr[ App::Get()->settings['firstname_attr'] ] ?>! </h1>
		<div id="submenu">
			<p>
				<a href="<?php echo $module->moduleRoot ?>/manage"> Manage Profile</a>
				&nbsp;&nbsp;|&nbsp;&nbsp;
				<a href="<?php echo $module->moduleRoot?>/changePwd">Change Password</a>
				&nbsp;&nbsp;|&nbsp;&nbsp;
				<a href="<?php echo $module->moduleRoot?>/groups">Groups</a>
			</p>
		</div>
		
 		<br class="space"/>

		<h3>Profile</h3>		
 		<fieldset id='profile_fieldset'>
 			<br class="space"/>
 			<br class="space"/>
			
			<?php 	echo displayAttributes($userAttr) ?>
			
			<br class="space"/>
		</fieldset>
		
    	<br class="space"/>
    	
	</div>
	
