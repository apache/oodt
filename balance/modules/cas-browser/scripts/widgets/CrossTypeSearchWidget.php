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

class CrossTypeSearchWidget 
	implements Org_Apache_Oodt_Balance_Interfaces_IApplicationWidget {
	
	// The id of the HTMLElement that will be modified by filter results.  This must be set before
	// calling renderScript().
	public $htmlID;
	
	// The ID of the HTMLElement that contains the loading icon that will be
	// displayed while getting results
	public $loadingID;
	
	// The url of the site base
	public $siteUrl;

	public function __construct($options = array()){
		if(isset($options['htmlID'])){
			$this->htmlID = $options['htmlID'];
		}
		if(isset($options['loadingID'])){
			$this->loadingID = $options['loadingID'];
		}
		if(isset($options['siteUrl'])){
			$this->siteUrl = $options['siteUrl'];
		}
	}
	
	public function setHtmlId($htmlID){
		$this->htmlID = $htmlID;
	}
	
	public function setSiteUrl($siteUrl){
		$this->siteUrl = $siteUrl;
	}
	
	public function render($bEcho = true){
		$str = '<div id="filter_widget_exclusive_container">';
		$str .= '<label>Exclusive:</label><input id="exclusive" type="checkbox" checked="checked" onclick="changeExclusive()"/></div>';
		$str .= '<select id="filterKey">';
		$filterKeys = Utils::getFacets();
		natcasesort($filterKeys);
		foreach($filterKeys as $label){
			$str .= '<option value="' . $label . '">' . $label . '</option>';
		}
		$str .= '</select>&nbsp;=&nbsp;';
		$str .= '<input type="text" id="filterValue" size="18" alt="filterValue">&nbsp;';
		$str .= '<input type="button" value="Add" onclick="addFilter()" />';
		$str .= '<table id="filters"></table>';
		$str .= '';
		$str .= '<div id="permalink"></div>';
		
		if ($bEcho) {
			echo $str;
		} else {
			return $str;
		}
	}
	
	public function renderScript($bEcho = true){
		$str = '<script type="text/javascript">';
		$str .= 'var htmlID = "' . $this->htmlID . '";';
		$str .= 'var loadingID = "' . $this->loadingID . '";';
		$str .= 'var siteUrl = "'. $this->siteUrl . '";';
		$str .= 'var resultFormat = "json";';
		$str .= 'var defaultShowEverything = ' . App::Get()->settings['default_show_all'] . ';';
		$str .= 'var displayedMetadata = new Array(';
		if(isset(App::Get()->settings['browser_products_met'])){
			$metList = '';
			foreach(App::Get()->settings['browser_products_met'] as $met){
				if(strlen($metList) > 0){
					$metList .= ',';
				}
				$metList .= '"' . $met . '"';
			}
			$str .= $metList;
		}
		$str .= ');</script>';
		$str .= '<script type="text/javascript" src="' . App::Get()->request->moduleStatic . '/js/querywidget.js"></script>';
		$str .= '<script type="text/javascript" src="' . App::Get()->request->moduleStatic . '/js/crosstypesearchwidget.js"></script>';
		
		if ($bEcho) {
			echo $str;
		} else {
			return $str;
		}
	}
	
	public static function parseSegments(){
		$results = array();
		$segments = App::Get()->request->segments;
		if(isset($segments[1]) && $segments[1] != ""){
			$filterParams = array();
			for($index = 1; isset($segments[$index]) && $segments[$index] != ""; $index = $index + 2){
				array_push($filterParams, array($segments[$index], $segments[$index + 1]));
			}
			$results['filterParams'] = $filterParams;
		}
		if(isset($segments[0]) && $segments[0] != ""){
			if(intval($segments[0]) == 1){
				$results['exclusive'] = array('bool'=>'or', 'checked'=>false);
			}else{
				$results['exclusive'] = array('bool'=>'and', 'checked'=>true);
			}
		}
		return $results;
	}
}
?>
