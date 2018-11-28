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

class MetadataDisplayWidget
	implements Org_Apache_Oodt_Balance_Interfaces_IApplicationWidget {
	
	public $metadata;
	
	public function __construct($options = array()) {
		
	}
	
	public function loadMetadata($metadata) {
		$this->metadata = $metadata;
	}
	
	
	public function render($bEcho = true) {
		$str  = "<table class=\"metwidget\"><tbody>";
		$str .= $this->renderHelper($this->metadata);
		$str .= "</tbody></table>";
		if ($bEcho) {
			echo $str;
		} else {
			return $str;
		}
	}
	
	protected function renderHelper($metadata) {
		foreach ($metadata as $key => $values) {
			if ( !empty($values) ) {
				// Build nested metadata tables recursively
				$r .= "<tr><th>{$key}</th>";
				// Associative array means met contains subkeys
				if (is_array($values) && self::is_assoc($values)) {
					$r .= "<td>";
					$r .= "<table class=\"metwidget multivalue\"><tbody>";
					$r .= $this->renderHelper($values);
					$r .= "</tbody></table>";
				} 
				// Numeric array means met has multiple values
				else if (is_array($values)) {
					$r .= "<td>";
					$r .= "<table class=\"metwidget\"><tbody>";
					foreach ($values as $val) {
						if (is_array($val) && self::is_assoc($val)) {
							$r .= "<tr class=\"multivalue\"><td>";
							$r .= "<table class=\"metwidget\">";
							$r .= $this->renderHelper($val);
							$r .= "</table>";
						} else {
							$r .= "<tr><td class=\"value\">";
							$r .= "<div>". wordwrap($val, 80, "<br />", true) . "</div>";
						}
						$r .= "</td></tr>";
					}
					$r .= "</tbody></table>";
				} 
				// Scalar value means met has one value 
				else {
					$r .= "<td class=\"value\">";
					$r .= "<div>{$values}</div>";
				}
				$r .= "</td></tr>";
			}
		}
		return $r;
	}
	
	protected static function is_assoc($array) {
    	return (is_array($array) && 
    		(count($array)==0 || 
    			0 !== count(array_diff_key($array, array_keys(array_keys($array))) )));
	}
}