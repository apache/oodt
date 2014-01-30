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
 * PROFILE MANAGER - User Status Display
 * 
 * This widget displays basic information about the user. It provides a link
 * to log in when user is logged out and displays username with a log-out link
 * when user is logged in.
 * 
 * This widget also allows the developer to decide whether or not the users will
 * have access to manage their account through the Profile Manager interface.
 * 
 * @author s.khudikyan
 * 
 */

class UserStatusWidget 
	implements Org_Apache_Oodt_Balance_Interfaces_IApplicationWidget {
		
	public $isLoggedIn;
	public $username;
	public $profileLink;
	protected $moduleName;
	
	/**
	 * Pass boolean true or false as the first parameter in the options array
	 * to generate either a login link or a logout link, whichever is appropriate.
	 *  	True:  user is logged in, show logout link
	 *  	False: user is not logged in, show login link
	 * 
	 * Pass username as the second paramerter in the options array
	 * 
	 * Pass boolean true or false as the third parameter in the options array
	 * to generate the username as a link to the porfile module.
	 *  	True:  link will be created- *Default*
	 *  	False: link will not be created
	 *   
	 * Pass module name as the fourth paramerter in the options array. This way
	 * the developer has the option to name the module anything.
	 * 		Default: 'profile'
	 */ 
	public function __construct($options = array()) {
		$this->isLoggedIn  = ( isset($options[0])   && $options[0] === true );
		$this->username    = ( isset($options[1]) ) ?  $options[1] : false;
		$this->profileLink = ( isset($options[2]) ) ?  $options[2] : true;
		$this->moduleName  = ( isset($options[3]) ) ?  $options[3] : 'profile';
	}
	
	public function render($bEcho = true) {
		
		$str 	= '';
		$module = App::Get()->loadModule($this->moduleName);
		
		// Display the appropriate information about the user
		if($this->isLoggedIn) {
			$str .= "Logged in as ";
			if ($this->profileLink) {
				$str .= '<a href="' . $module->moduleRoot . '/">' . $this->username . '</a>&nbsp;|&nbsp;'
					.'<a href="' . $module->moduleRoot . '/logout.do">Log Out</a>';
			} else {
				$str .=  $this->username . '&nbsp;|&nbsp;'
						.'<a href="' . $module->moduleRoot . '/logout.do">Log Out</a>';
			}
		} else {
			$str .= '<a href="' . $module->moduleRoot . '/login">Log In</a>';
		}
		
		if($bEcho) {
			echo $str;
		} else {
			return $str;
		}
	}
	
}