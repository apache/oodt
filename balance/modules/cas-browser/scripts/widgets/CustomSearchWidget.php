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
 * 
 * 
 * This widget is configured by the customSearchConfig array in the config file.  The
 * widget expects the specifications for the criteria to search in sequence.  Each
 * criteria should start with either 'term' or 'range' as an element in the config
 * array, followed by the name of the metadata element that will be searched with the
 * value given in the field corresponding to that criteria.  Finally the next element
 * or two elements in the config array will be occupied by labels for the inout fields,
 * depending upon the criteria type (term or range).
 * Here is an exmaple of a config array for a term criterion and a range criterion in order:
 * 
 * customSearchConfig[]=term
 * customSearchConfig[]=EUFile
 * customSearchConfig[]=EUFile
 * customSearchConfig[]=range
 * customSearchConfig[]=VideoLength
 * customSearchConfig[]=Min. Video Length
 * customSearchConfig[]=Max. Video Length
 * 
 */
class CustomSearchWidget 
	implements Org_Apache_Oodt_Balance_Interfaces_IApplicationWidget {
	
	// This array will specify what criteria we use in our search, what types they are
	// and how they should be labeled.
	public $criteria;
	
	// The ProductType that the widget is filtering.  This must be set before calling
	// render() or renderScript().
	public $productType;
	
	// The id of the HTMLElement that will be modified by filter results.  This must be set before
	// calling renderScript().
	public $htmlID;
	
	// The url of the site base
	public $siteUrl;
	
	// Will results be returned in html or json
	public $resultFormat;
	
	public function __construct($options = array()){
		
		if(isset($options['productType'])){
			$this->productType = $options['productType'];
		}
		if(isset($options['htmlID'])){
			$this->htmlID = $options['htmlID'];
		}
		if(isset($options['siteUrl'])){
			$this->siteUrl = $options['siteUrl'];
		}
		if(isset($options['resultFormat'])){
			$this->resultFormat = $options['resultFormat'];
		}
		
		if(!isset(App::Get()->settings['customSearchConfig'])){
			echo '<div class="error">No criteria config was found for the CustomSearchWidget.</div>';
			return;
		}
		$criteriaConfig = App::Get()->settings['customSearchConfig'];
		
		// Build a representation of the criteria to be searched by the widget.
		// If, during this process, we encounter config values that don't make
		// sense or an incomplete config for a criterion, we'll simply skip to
		// the next index in the config array.
		$this->criteria = array();
		$configIndex = 0;
		while(isset($criteriaConfig[$configIndex])){
			
			$newCriterion = array();
			
			// Look for a criteria type
			$newType = strtolower($criteriaConfig[$configIndex]);
			if($newType == 'term' or $newType == 'range'){
				$newCriterion['type'] = $newType;
				$configIndex++;
			}else{
				continue;	// Let's skip this one and hope that we
							// find one that makes more sense later.
			}
			
			// Extract the metadata element associated with the criterion
			$newElement = $criteriaConfig[$configIndex];
			if(isset($newElement)){
				$newCriterion['element'] = $newElement;
				$configIndex++;
			}else{
				continue;	// This criterion wasn't finished.
			}
			
			// Extract the labels for the input field(s)
			$newLabel = $criteriaConfig[$configIndex];
			if(isset($newLabel)){
				if($newType == 'term'){
					$newCriterion['termLabel'] = $newLabel;
				}else{
					$newCriterion['minLabel'] = $newLabel;
					$configIndex++;
					$newLabel = $criteriaConfig[$configIndex];
					if(isset($newLabel)){
						$newCriterion['maxLabel'] = $newLabel;
					}else{
						continue;	// This criterion wasn't finished.
					}
				}
			}else{
				continue;	// This criterion wasn't finished.
			}
			
			array_push($this->criteria, $newCriterion);
			$configIndex++;
			
		}
	}
	
	// Using the criteria representation at this->criteria, we create the fields for
	// specifying the criteria values.  We put a hidden input tag with an id of the
	// form termX or rangeX before the specification fields.  The JS will find these
	// hidden tags and use them to understand whether the fields specify term or
	// range criteria.  The number following the criterion type is only so that they
	// have unique IDs.
	public function render($bEcho = true){
		$str = '';
		for($i = 0; $i < count($this->criteria); $i++){
			if($this->criteria[$i]['type'] == 'term'){
				$str .= '<input id="term' . $i . '" type="hidden" value="' . $this->criteria[$i]['element'] . '">';
				$str .= $this->criteria[$i]['termLabel'] . ':&nbsp;<input type="text" size="18" id="inputTerm' . $i . '"><br/>';
			}else{
				$str .= '<input id="range' . $i . '" type="hidden" value="' . $this->criteria[$i]['element'] . '">';
				$str .= $this->criteria[$i]['minLabel'] . ':&nbsp;<input type="text" size="18" id="inputRangeMin' . $i . '">';
				$str .= '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;';
				$str .= $this->criteria[$i]['maxLabel'] . ':&nbsp;<input type="text" size="18" id="inputRangeMax' . $i . '"><br/>';
			}
		}
		$str .= '<input type="button" value="Search" onclick="customQuery()" />';
		
		if ($bEcho) {
			echo $str;
		} else {
			return $str;
		}
	}
	
	public function renderScript($bEcho = true){
		$module = App::Get()->loadModule();
		$str = '';
		$str .= '<script type="text/javascript">var htmlID = "' . $this->htmlID . '";</script>';
		$str .= '<script type="text/javascript">var ptName = "' . $this->productType->getName() . '";</script>';
		$str .= '<script type="text/javascript">var siteUrl = "' . $this->siteUrl . '";</script>';
		$str .= '<script type="text/javascript">var resultFormat = "' . $this->resultFormat . '";</script>';
		$str .= '<script type="text/javascript" src="' . $module->moduleStatic . '/js/querywidget.js"></script>';
		$str .= '<script type="text/javascript" src="' . $module->moduleStatic . '/js/customsearchwidget.js"></script>';
		
		if ($bEcho) {
			echo $str;
		} else {
			return $str;
		}
	}
}
?>
