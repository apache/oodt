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

class BasicSearchWidget 
	implements Org_Apache_Oodt_Balance_Interfaces_IApplicationWidget {
	
	public function __construct($options = array()){}
	
	public function render($bEcho = true){
		$module = App::Get()->loadModule();
		$str = '';
		$str .= '<form action="' . $module->moduleRoot . '/queryScript.do" method="POST">';
		$str .= '<input type="hidden" name="Types[0]" value="*"/>';
		$str .= '<input type="hidden" name="Criteria[0][CriteriaType]" value="Term"/>';
		$str .= '<input type="hidden" name="Criteria[0][ElementName]" value="*"/>';
		$str .= '<input type="text" name ="Criteria[0][Value]"/>';
		$str .= '<input type="submit" value="Search"/>';
		$str .= '</form>';
		
		if ($bEcho) {
			echo $str;
		} else {
			return $st;
		}
	}
}
?>