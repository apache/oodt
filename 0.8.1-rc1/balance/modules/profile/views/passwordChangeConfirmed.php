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
 * Display confirmation of password change and allow user to log back in
 */
$module = App::Get()->loadModule();

?>
	
	<div class="breadcrumbs">
		<a href="<?php echo SITE_ROOT?>/">Home</a>
	</div>
    
    <div class='span-22 append-1 prepend-1 last' id="profile_container">
    	<div class="span-24">
    		<br>
			<h3>Password has been changed.</h3>
			<br>
    	</div>
		
		<div class="span-22">
			<h5><a href="<?php echo $module->moduleRoot?>/login">Please log in with new password.</a></h5>
		</div>
		<br class="space"/>
		
    </div>
    