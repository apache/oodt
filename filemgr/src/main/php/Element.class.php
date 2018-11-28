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
class CAS_Filemgr_Element {

	/* the element id */
	public $elementId;

    /* the element name */
    public $elementName;

    /* the corresponding DC element for this CAS element */
    public $dcElement;

    /* the element's string description. */
    public $description;
    
    /**
	 * <p>Default Constructor.</p>
	 */
	function __construct(){
		$this->elementId = null;
		$this->elementName = null;
		$this->dcElement = null;
		$this->description = null;
	}
	
	function __init($elementId, $elementName, $dcElement, $description){
		$this->elementId = $elementId;
		$this->elementName = $elementName;
		$this->dcElement = $dcElement;
		$this->description = $description;
	}
	
	function __initXmlRpc($xmlRpcData){
		$this->elementId = (isset($xmlRpcData['id']))
			? $xmlRpcData['id']
			: null;
		$this->elementName = (isset($xmlRpcData['name']))
			? $xmlRpcData['name']
			: null;
		$this->dcElement = (isset($xmlRpcData['dcElement']))
			? $xmlRpcData['dcElement']
			: null;
		$this->description = (isset($xmlRpcData['description']))
			? $xmlRpcData['description']
			: null;
	}
	
	/**
     * @return The ID of this Element.
     */
	function getElementID(){
		return $this->elementId;
	}
	
	/**
     * @param elementId
     *            The new ID for this Element.
     */
	function setElementID($elementId){
		$this->elementId = $elementId;
	}
	
	/**
     * @return The name of this Element.
     */
	function getElementName(){
		return $this->elementName;
	}
	
	/**
     * @param elementName
     *            The new name for this Element.
     */
	function setElementName($elementName){
		$this->elementName = $elementName;
	}

	/**
     * @return The dcElement of this Element...whatever that means.
     */
	function getDcElement(){
		return $this->dcElement;
	}
	
	/**
     * @param dcElement
     *            The new dcElement for this Element.
     */
	function setDcElement($dcElement){
		$this->dcElement = $dcElement;
	}
	
	/**
     * @return The description of this Element.
     */
	function getDescription(){
		return $this->description;
	}
	
	/**
     * @param description
     *            The new description for this Element.
     */
	function setDescription($description){
		$this->description = $description;
	}
	
	function toAssocArray(){
    	return array(
			'elementId' => $this->elementId,
			'elementName' => $this->elementName,
			'dcElement' => $this->dcElement,
			'description' => $this->description);
    }
	
	function toXmlRpcStruct(){
    	return new XML_RPC_Value(array(
			'elementId' => new XML_RPC_Value($this->elementId,'string'),
			'elementName' => new XML_RPC_Value($this->elementName,'string'),
			'dcElement' => new XML_RPC_Value($this->dcElement,'string'),
			'description' => new XML_RPC_Value($this->description, 'string')), 'struct');
    }

}

?>
