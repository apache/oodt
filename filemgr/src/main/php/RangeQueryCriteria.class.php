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

require_once("XML/RPC.php");

/**
 * @author resneck
 *
 * <p>A PHP port of the core filemgr data structure.</p>
 */
 class CAS_Filemgr_RangeQueryCriteria {
 
 	public $elementName;
    public $startValue;
    public $endValue;
    public $inclusive;
 
 	/**
	 * <p>
	 * Default Constructor
	 * </p>.
	 */
	function __construct(){
		$this->elementName = '';
		$this->startValue = '';
		$this->endValue = '';
		$this->inclusive = true;
	}
	
	/**
     * @param elementName
     *            The name of the element to search on.
     * @param start
     *            The start value for the range search as a String.
     * @param end
     *            The end value for the range search as a String.
     * @param inclusive
     *            Boolean: true for inclusive, false for exclusive.
     */
	function __init($elementName, $start, $end, $inclusive){
		$this->elementName = $elementName;
		$this->startValue = $start;
		$this->endValue = $end;
		$this->inclusive = $inclusive;
	}
	
	function __initXmlRpc($xmlRpcData){
		$this->elementName = (isset($xmlRpcData['elementName']))
			? $xmlRpcData['elementName'] 
			: '';
		$this->value = (isset($xmlRpcData['start']))
			? $xmlRpcData['start'] 
			: '';
		$this->value = (isset($xmlRpcData['end']))
			? $xmlRpcData['end'] 
			: '';
		$this->value = (isset($xmlRpcData['inclusive']))
			? (bool)$xmlRpcData['inclusive'] 
			: true;
	}
	
	/**
     * Accessor method for the start value of the element to search on.
     * 
     * @return The start value of the element to search on as a String.
     */
    function getStartValue() {
        return $this->startValue;
    }

    /**
     * Mutator method for the start value fo the element to search on.
     * 
     * @param value
     *            The start value of the range as a String.
     */
    function setStartValue($value) {
        $this->startValue = $value;
    }

    /**
     * Accessor method for the end value of the element to search on.
     * 
     * @return The end value of the element to search on as a String.
     */
    function getEndValue() {
        return $this->endValue;
    }

    /**
     * Mutator method for the end value fo the element to search on.
     * 
     * @param value
     *            The end value of the range as a String.
     */
    function setEndValue($value) {
        $this->endValue = $value;
    }

    /**
     * Accessor method for the inclusive setting for the range.
     * 
     * @return The boolean inclusive/exclusive flag.
     */
    function getInclusive() {
        return $this->inclusive;
    }

    /**
     * Mutator method for the inclusive setting for the range. Note that flag
     * should be set to true for inclusive, false for exclusive.
     * 
     * @param inclusive
     *            The boolean inclusive/exclusive flag.
     */
    function setInclusive($flag) {
        $this->inclusive = $flag;
    }

    /**
     * Implementation of the abstract method inherited from QueryCriteria for
     * accessing the element name to search on.
     * 
     * @return The element name to search on as a String.
     */
    function getElementName() {
        return $this->elementName;
    }

    /**
     * Implementation of the abstract method inherited from QueryCriteria for
     * mutating the element name to search on.
     * 
     * @param elementName
     *            The element name to search on as a String.
     */
    function setElementName($elementName) {
        $this->elementName = $elementName;
    }
    
    function toAssocArray(){
    	return array(
			'startValue' => $this->startValue,
			'endValue' => $this->endValue,
			'elementName' => $this->elementName,
			'inclusive' => $this->inclusive);
    }
    
    function toXmlRpcStruct(){
    	$inclusiveStr = ($this->inclusive)
			? 'true'
			: 'false';
    	return new XML_RPC_Value(array(
    		'class' => new XML_RPC_Value('org.apache.oodt.cas.filemgr.structs.RangeQueryCriteria', 'string'),
			'elementStartValue' => new XML_RPC_Value($this->startValue,'string'),
			'elementEndValue' => new XML_RPC_Value($this->endValue,'string'),
			'elementName' => new XML_RPC_Value($this->elementName,'string'),
			'inclusive' => new XML_RPC_Value($inclusiveStr, 'string')), 'struct');
    }
 
 }
 
 ?>
