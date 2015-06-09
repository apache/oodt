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
 * This view will expose the user groups and roles for the user.
 */
$module = App::Get()->loadModule();

// Get instance of authentication and authorization class
$authorization 	= App::Get()->getAuthorizationProvider();
$authentication = App::Get()->getAuthenticationProvider();

if ( $authorization != false) {
	$groups = $authorization->retrieveGroupsForUser($authentication->getCurrentUsername(),App::Get()->settings['authorization_ldap_group_dn']);
}

?>
	
	<div class="breadcrumbs">
		<a href="<?php echo SITE_ROOT?>/">Home</a>
	</div>
    
	<div class='span-22 append-1 prepend-1 last' id='profile_container'>
		<h1><?php echo $authentication->getCurrentUsername() ?>'s Groups </h1>
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

		<fieldset id='profile_fieldset'>
		<?php 
			
		if (count($groups) > 0) {
			echo "<div class='span-15 prepend-1'>";
			foreach ($groups as $g) {
				List($group, $role) =  explode("_", $g);
				
				if( $group != $groupIndex ) {
					echo "</ul></ul>";
					echo "<br class='space'>";
					echo "<h3>{$group}</h3>";
					echo "<ul><ul>";
					$groupIndex = $group;
				}		
			  	echo "<li>";
			  	echo $role;
				echo "</li>";
				echo "<br class='space'>";
			}
			
			echo "</div>";
		} else{
			echo "<h4> No Groups found!</h4>";
		}
		?>
		</fieldset>
		<br class='space'>
	</div>
	