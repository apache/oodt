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
 * @author mattmann
 * @version $Revision$
 * 
 * A Page of {@link Product}s returned from the <code>File Manager</code>.
 * 
 */
class CAS_Filemgr_ProductPage {

	/* the number of this page */
	private $pageNum = -1;

	/* the total number of pages in the set */
	private $totalPages = -1;
	
	/* the number of total hits for the query */
	private $numOfHits = -1;
	
	/* the size of the number of products on this page */
	private $pageSize = -1;

	/* the list of produdcts associated with this page */
	private $pageProducts = null;

	/**
	 * <p>
	 * Default Constructor
	 * </p>.
	 */
	function __construct() {
		$this->pageProducts = array ();
	}

	/**
	 * @param pageNum
	 *            The number of this page.
	 * @param totalPages
	 *            The total number of pages in the set.
	 * @param pageSize
	 *            The size of this page.
	 * @param pageProducts
	 *            The products associated with this page.
	 */
	function __init($pageNum, $totalPages, $numOfHits, $pageSize, $pageProducts) {
		$this->pageNum = $pageNum;
		$this->totalPages = $totalPages;
		$this->numOfHits = $numOfHits;
		$this->pageSize = $pageSize;
		$this->pageProducts = $pageProducts;
	}

    function __initXmlRpc($xmlRpcData){
		$this->pageNum = (isset($xmlRpcData['pageNum']))
			? intval($xmlRpcData['pageNum'])
			: -1;
		$this->totalPages = (isset($xmlRpcData['totalPages']))
			? intval($xmlRpcData['totalPages']) 
			: -1;
		$this->numOfHits = (isset($xmlRpcData['numOfHits']))
			? intval($xmlRpcData['numOfHits']) 
			: -1;
		$this->pageSize = (isset($xmlRpcData['pageSize']))
			? intval($xmlRpcData['pageSize']) 
			: -1;
		$this->pageProducts = array();
		if(isset($xmlRpcData['pageProducts'])){
			foreach($xmlRpcData['pageProducts'] as $p){
				array_push($this->pageProducts, new CAS_Filemgr_Product($p));
			}
		}
    }
    
    
	/**
	 * @return Returns the pageNum.
	 */
	public function getPageNum() {
		return $this->pageNum;
	}

	/**
	 * @param pageNum
	 *            The pageNum to set.
	 */
	public function setPageNum($pageNum) {
		$this->pageNum = $pageNum;
	}

	/**
	 * @return Returns the pageProducts.
	 */
	public function getPageProducts() {
		return $this->pageProducts;
	}

	/**
	 * @param pageProducts
	 *            The pageProducts to set.
	 */
	public function setPageProducts($pageProducts) {
		$this->pageProducts = $pageProducts;
	}

	/**
	 * @return Returns the pageSize.
	 */
	public function getPageSize() {
		return $this->pageSize;
	}

	/**
	 * @param pageSize
	 *            The pageSize to set.
	 */
	public function setPageSize($pageSize) {
		$this->pageSize = $pageSize;
	}

	/**
	 * @return Returns the totalPages.
	 * If totalPages is not set check to see if numOfHits is used instead
	 */
	public function getTotalPages() {
		if ($this->totalPages != -1 ) {
			return $this->totalPages;
		} elseif ( $this->numOfHits > 0 ) {
			return (ceil($this->numOfHits/$this->pageSize));
		}
		return $this->totalPages;
	}

	/**
	 * @param totalPages
	 *            The totalPages to set.
	 */
	public function setTotalPages($totalPages) {
		$this->totalPages = $totalPages;
	}

	/**
	 * @return Returns the numOfHits.
	 */
	public function getNumOfHits() {
		return $this->numOfHits;
	}

	/**
	 * @param totalPages
	 *            The totalPages to set.
	 */
	public function setNumOfHits($numOfHits) {
		$this->numOfHits = $numOfHits;
	}	

	/**
	 * 
	 * @return True if this is the last page in the set, false otherwise.
	 */
	public function isLastPage() {
		return $this->pageNum == $this->totalPages;
	}

	/**
	 * 
	 * @return True if this is the fist page of the set, false otherwise.
	 */
	public function isFirstPage() {
		return $this->pageNum == 1;
	}

	public function toXmlRpcStruct() {
	  return new XML_RPC_Value(array(
					 'pageNum' => new XML_RPC_Value($this->pageNum,'int'),
					 'pageSize' => new XML_RPC_Value($this->pageSize,'int'),
					 'totalPages' => new XML_RPC_Value($this->totalPages,'int'),
					 'numOfHits' => new XML_RPC_Value($this->numOfHits,'int'),
	  				 'pageProducts' => $this->toXmlRpcProductList($this->pageProducts)), 'struct');
	}

	/**
	 * 
	 * @return A blank, unpopulated {@link ProductPage}.
	 */
	public static function blankPage() {
		$blank = new CAS_Filemgr_ProductPage();
		$blank->setPageNum(0);
		$blank->setTotalPages(0);
		$blank->setNumOfHits(0);
		$blank->setPageSize(0);
		return $blank;
	}
	
	
	private function toXmlRpcProductList($prodList){
		$prodEncodedArr = array();
		
		foreach ($prodList as $product){
			$prodEncodedArr[] = $product->toXmlRpcStruct();
		}
		
		return new XML_RPC_Value($prodEncodedArr, 'array');
	}

}
?>
