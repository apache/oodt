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

class FilterWidget 
	implements Org_Apache_Oodt_Balance_Interfaces_IApplicationWidget {

	// The ProductType that the widget is filtering.  This must be set before calling
	// render() or renderScript().
	public $productType;
	
	// The ID of the HTMLElement that will be modified by filter results.  This must be set before
	// calling renderScript().
	public $htmlID;
	
	// The ID of the HTMLElement that contains the loading icon that will be
	// displayed while getting results
	public $loadingID;
	
	// The url of the site base
	public $siteUrl;
	
	// Whether filtered results will be displayed all at once or in a paged format
	public $pagedResults;
	
	// Will results be returned in html or json
	public $resultFormat;

	public function __construct($options = array()){
		if(isset($options['productType'])){
			$this->productType = $options['productType'];
		}
		if(isset($options['htmlID'])){
			$this->htmlID = $options['htmlID'];
		}
		if(isset($options['loadingID'])){
			$this->loadingID = $options['loadingID'];
		}
		if(isset($options['siteUrl'])){
			$this->siteUrl = $options['siteUrl'];
		}
		if(isset($options['pagedResults'])){
			$this->pagedResults = $options['pagedResults'];
		}
		if(isset($options['resultFormat'])){
			$this->resultFormat = $options['resultFormat'];
		}
		
	}
	
	public function setProductType($productType){
		$this->productType = $productType;
	}
	
	public function setHtmlId($htmlID){
		$this->htmlID = $htmlID;
	}
	
	public function setSiteUrl($siteUrl){
		$this->siteUrl = $siteUrl;
	}
	
	public function setPagedResults($pagedResults){
		$this->pagedResults = $pagedResults;
	}
	
	public function setResultFormat($resultFormat){
		$this->resultFormat = resultFormat;
	}
	
	public function render($bEcho = true){
		$str = '';
		$str .= '<select id="filterKey">';
		$filterKeys = Utils::getMetadataElements(array($this->productType));
		natcasesort($filterKeys);
		foreach($filterKeys as $label){
			$str .= '<option value="' . $label . '">' . $label . '</option>';
		}
		$str .= '</select>&nbsp;=&nbsp;';
		$str .= '<input type="text" id="filterValue" size="18" alt="filterValue">&nbsp;';
		$str .= '<input type="button" value="Add" onclick="addFilter()" />';
		$str .= '<table id="filters"></table>';
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
		$str .= 'var ptName = "' . $this->productType->getName() . '";';
		$str .= 'var ptID = "' . $this->productType->getId() . '";';
		$str .= 'var siteUrl = "'. $this->siteUrl . '";';
		$str .= 'var resultFormat = "' . $this->resultFormat  . '";';
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
		$str .= '<script type="text/javascript" src="' . App::Get()->request->moduleStatic . '/js/filterwidget.js"></script>';
	
		if ($bEcho) {
			echo $str;
		} else {
			return $str;
		}
	}
}
?>
