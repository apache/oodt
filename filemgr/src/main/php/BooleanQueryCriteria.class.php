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
 class CAS_Filemgr_BooleanQueryCriteria {
 
 	// These variables will function as constants to define boolean operators
 	public static $AND_OP = 0;
    public static $OR_OP = 1;
    public static $NOT_OP = 2;
    
    public $operator;
    public $terms;
    
     /**
     * Default constructor. Uses the AND operator.
     */
	function __construct(){
    	$this->operator = self::$AND_OP;
    	$this->terms = array();
	}
	
	/**
     * This query is a boolean combination of term,
     * range, and other boolean queries. The supported operators are AND, OR and
     * NOT. Note that the NOT operator can only be used with one (1) term. This
     * function will not operate if more than one term is used with the NOT
     * operator.
     * 
     * @param terms
     *            The criteria onto which to apply the boolean operator
     * @param op
     *            The boolean operator to be applied
     */
	function __init($terms, $op){
		if(!($op == self::$NOT_OP && count($terms) > 1)){
			$this->terms = $terms;
			$this->operator = $op;
		}
	}
	
	function __initXmlRpc($xmlRpcData){
    	$this->operator = (isset($xmlRpcData['operator']))
			? intval($xmlRpcData['operator']) 
			: self::$AND_OP;
		$this->terms = (isset($xmlRpcData['terms']))
			? $xmlRpcData['terms'] 
			: array();
	}
	
	/**
     * Method to add a term to the boolean query. Note that a NOT operator can
     * only be applied to one term. This function will not operate if more than 
     * one term is used with the NOT operator.
     * 
     * @param t
     *            Term to be added to the query
     */
	function addTerm($t){
		if(!($this->operator == self::$NOT_OP && count($this->terms))){
			array_push($this->terms, $t);
		}
	}
	
	/**
     * Accessor method for the list of terms in the query.
     * 
     * @return The list of terms
     */
    function getTerms() {
        return $this->terms;
    }

    /**
     * Mutator method for the boolean operator. This function will not operate if more than 
     * one term is used with the NOT operator.
     * 
     * @param op
     *            Boolean operator
     */
    function setOperator($op){
    	if(!($op == self::$NOT_OP && count($this->terms) > 1)){
    		$this->operator = $op;
    	}
    }

    /**
     * Accessor method for the boolean operator.
     * 
     * @return the boolean operator
     */
    function getOperator() {
        return $this->operator;
    }
    
    function toAssocArray(){
		$assocArray = array(
			'operator' => $this->operator,
			'terms' => array());
		foreach($this->terms as $t){
			array_push($assocArray['terms'], $t->toAssocArray());
		}
		return $assocArray;
	}
	
	function toXmlRpcStruct(){
		$termArray = array();
		foreach($this->terms as $t){
			array_push($termArray, $t->toXmlRpcStruct());
		}
		return new XML_RPC_Value(array(
			'class' => new XML_RPC_Value('org.apache.oodt.cas.filemgr.structs.BooleanQueryCriteria', 'string'),
			'terms' => new XML_RPC_Value($termArray, 'array'),
			'operator' => new XML_RPC_Value($this->operator, 'int')), 'struct');
	}
 
 }
 
 ?>
