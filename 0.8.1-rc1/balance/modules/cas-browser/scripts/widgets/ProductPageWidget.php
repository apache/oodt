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

/**
 * Display a page of product results.
 * @author ahart
 *
 */
class ProductPageWidget 
	implements Org_Apache_Oodt_Balance_Interfaces_IApplicationWidget {
	
	public $page;
	public $pageMetadata;
	public $productTypeId;
	public $returnPage;
	public $urlBase;
	
	private $pageNum;
	private $pageProducts;
	private $pageSize;
	private $totalPages;
	
	public function __construct($options = array() ) {
		$this->page = false;
		$this->productTypeId = $options['productTypeId']; 
		$this->returnPage    = (isset($options['returnPage']))
			? $options['returnPage']
			: 1;
		$this->urlBase       = $options['urlBase'];
	}
	
	public function load($productPage, $productMetadata = NULL) {
		$this->page 		= $productPage;
		$this->pageMetadata = $productMetadata;
		$this->pageNum 		= $this->page->getPageNum();
		$this->pageProducts = $this->page->getPageProducts();
		$this->pageSize 	= $this->page->getPageSize();
		$this->totalPages 	= $this->page->getTotalPages();
	}
	
	public function render($bEcho = true) {
		$str = '';
		if ($this->page) {
			$str .= "<table><tr><th>Product Name</th>";
			$displayedElements = App::Get()->settings['browser_products_met'];
			foreach($displayedElements as $elementName){
				$str .= "<th>{$elementName}</th>";
			}
			$str .= "</tr>";
			
			if ( !App::Get()->settings['browser_private_products_visibility'] ) {
			// Get a CAS-Browser XML/RPC client
			$browser  = new CasBrowser();
			$client   = $browser->getClient();
			foreach ($this->pageMetadata as $key => $value) {
				if ( $browser->getProductVisibilityLevel($key) == "deny") {
					unset($this->pageMetadata[$key]);
					foreach ($this->pageProducts as $product) {
						if ($product->id == $key) {
							$productKey = array_search($product, $this->pageProducts);
							unset($this->pageProducts[$productKey]);
						}
					}
				}
			}
			}
			foreach($this->pageProducts as $product){
				$str .= "<tr><td><a href=\"".$this->urlBase."/product/{$product->getId()}/{$this->returnPage}\">" 
				  . urlDecode(basename($product->getName())) . "</a></td>";
				foreach($displayedElements as $elementName){
					$str .= "<td>" . $this->pageMetadata[$product->getId()]->getMetadata($elementName) . "</td>";
				}
				$str .= "</tr>";
			}
			$str .= "</table>";
		} 
		if ($bEcho) {
			echo $str;
		} else {
			return $str;
		}
	}
	
	public function renderPageDetails($bEcho = true) {
		// Variables to determine product range displayed and total count
		$pageNum    = ($this->pageNum == -1)    ? 0 : $this->pageNum;
		$totalPages = ($this->totalPages == -1) ? 0 : $this->totalPages;
		
		$displayCountStart = ($totalPages == 0) ? 0 : $this->pageSize * ($pageNum - 1) + 1;
		$displayCountEnd   = ($totalPages == 0) ? 0 : $displayCountStart + count($this->pageProducts) - 1;
		$displayCountStart = ($totalPages > 0 && $displayCountStart == 0) ? 1 : $displayCountStart;
		

		// 'Previous' and 'Next' page links
		$linkBase    = App::Get()->loadModule()->moduleRoot . "/products/{$this->productTypeId}/page";
		$prevPageNum = $this->pageNum -1;
		$nextPageNum = $this->pageNum +1;
		 
		$prevPageLink = ($prevPageNum >= 1) 
			? "<a href=\"{$linkBase}/{$prevPageNum}\">&lt;&lt;&nbsp;Previous Page</a>"
			: '';
		$nextPageLink = ($nextPageNum <= $this->totalPages)
			? "<a href=\"{$linkBase}/{$nextPageNum}\">Next Page&nbsp;&gt;&gt;</a>"
			: '';
	
		$rangeInfo = "<span class=\"pp_detail\">Page {$pageNum} of {$totalPages} "
			."(products {$displayCountStart} - {$displayCountEnd})</span>";

		
		$str = "<div class=\"pp_pageLinks\">{$rangeInfo}&nbsp;&nbsp;{$prevPageLink}&nbsp;&nbsp;{$nextPageLink}</div>\r\n";	
	
		if ($bEcho) {
			echo $str;
		} else {
			return $str;
		}
	}
}
