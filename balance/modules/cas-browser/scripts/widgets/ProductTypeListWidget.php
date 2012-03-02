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

class ProductTypeListWidget 
	implements Org_Apache_Oodt_Balance_Interfaces_IApplicationWidget {
	
	public $productTypes;
	public $urlBase;
	
	public function __construct($options = array()) {
		$this->productTypes = $options['productTypes'];
		$this->urlBase      = isset($options['urlBase']) 
			? $options['urlBase']
			: '';
	}
	
	public function setUrlBase($base) {
		$this->urlBase = $base;
	}
	
	public function render($bEcho = true) {
		$str = '';
		$str.= "<table id=\"productTypeSearch\" class=\"dataTable\">
			  <thead>
			    <tr>";
		// Display the Column Headers
		foreach (App::Get()->settings['browser_pt_search_met'] as $metKey) {
			$str .= "<th>".ucwords($metKey)."</th>";
		}
		if (isset(App::Get()->settings['browser_pt_hidden_search_met'])) {
			foreach (App::Get()->settings['browser_pt_hidden_search_met'] as $metKey) {
				$str .= "<th class=\"hidden\">{$metKey}</th>";
			}
		}
		$str .= "</tr></thead><tbody>";

		// Display the Data
		foreach ($this->productTypes as $ptKey => $ptMetadata) {
			if (isset(App::Get()->settings['browser_product_type_ignores']) && 
				in_array($ptKey,App::Get()->settings['browser_product_type_ignores'])) { continue; }
			$str .= "<tr>";
			foreach (App::Get()->settings['browser_pt_search_met'] as $metKey) {
				if ($metKey == App::Get()->settings['browser_pt_search_linkkey']) {
					$str .= "<td><a href=\"{$this->urlBase}/products/{$ptKey}\">{$ptMetadata[$metKey][0]}</a>";
					if(count($ptMetadata[$metKey]) == 2){
						$str .= "&nbsp({$ptMetadata[$metKey][1]})";
					}
					$str .= "</td>";
				} else {
					if (count($ptMetadata[$metKey]) > 1) {
						$str .= "<td>" . implode(", ", $ptMetadata[$metKey]) . "</td>";
					} else {
						$str .= "<td>{$ptMetadata[$metKey][0]}</td>";	
					}
				}
			}
			if (isset(App::Get()->settings['browser_pt_hidden_search_met'])) {
				foreach (App::Get()->settings['browser_pt_hidden_search_met'] as $metKey) {
					if (count($ptMetadata[$metKey]) > 1) {
						$str .= "<td class=\"hidden\">" . implode(", ", $ptMetadata[$metKey]) . "</td>";
					} else {
						$str .= "<td class=\"hidden\">{$ptMetadata[$metKey][0]}</td>";	
					}
				}
			}
			$str .= "</tr>\r\n";
		}
		$str .= "</tbody></table>";	

		if ($bEcho) {
			echo $str;
		} else {
			return $str;
		}
	}
}