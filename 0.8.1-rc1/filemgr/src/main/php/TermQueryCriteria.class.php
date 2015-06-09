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
class CAS_Filemgr_TermQueryCriteria {

	public $elementName;
	public $value;
	
	/**
	 * <p>
	 * Default Constructor
	 * </p>.
	 */
	function __construct(){
		$this->elementName = '';
		$this->value = '';
	}
	
	/**
     * @param elementName
     *            The name of the element to search on.
     * @param v
     *            The value of the term.
     */
	function __init($elementName, $value){
		$this->elementName = $elementName;
		$this->value = $value;
	}
	
	function __initXmlRpc($xmlRpcData){
		$this->elementName = (isset($xmlRpcData['elementName']))
			? $xmlRpcData['elementName'] 
			: '';
		$this->value = (isset($xmlRpcData['value']))
			? $xmlRpcData['value'] 
			: '';
	}
	
	/**
     * Accessor method for the value of the element to search on.
     * 
     * @return The value of the element to search on as a String.
     */
    function getValue() {
        return $this->value;
    }

    /**
     * Mutator method for the value of the element to search on
     * 
     * @param value
     *            The value of the element to search on as a String.
     */
    function setValue($value) {
        $this->value = $value;
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
			'value' => $this->value,
			'elementName' => $this->elementName);
    }
    
    function toXmlRpcStruct(){
    	return new XML_RPC_Value(array(
    		'class' => new XML_RPC_Value('org.apache.oodt.cas.filemgr.structs.TermQueryCriteria', 'string'),
			'elementValue' => new XML_RPC_Value($this->value,'string'),
			'elementName' => new XML_RPC_Value($this->elementName,'string')), 'struct');
    }

}

?>
