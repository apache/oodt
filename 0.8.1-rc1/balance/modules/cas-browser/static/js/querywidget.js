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

var criteriaNum = 0;
var allCriteria = new Array();
var criteriaRoot = 0;
var pCurrPage = 1;
var pTotalPages = 0;
var pPageSize = 0;
var pNumPageProducts = 0;
var pTotalProducts = 0;

function encodeRequestData(key, value){
	return escape(key) + "=" + value;
}

function displayPageInfo(){
	pageInfo = '<div class="pp_pageLinks">';
	if(pNumPageProducts > 0){
		prodRangeStart = (pCurrPage - 1) * pPageSize + 1;
		prodRangeEnd = (pCurrPage - 1) * pPageSize + pNumPageProducts;
		pageInfo += 'Page ' + pCurrPage + ' of ' + pTotalPages + '&nbsp;';
		pageInfo += '(Products ' + prodRangeStart + ' to ' + prodRangeEnd;
		if(pTotalProducts > 0){
			pageInfo += ', out of ' + pTotalProducts;
		}
		pageInfo += ')&nbsp;&nbsp;';
		if(pCurrPage > 1){
			pageInfo += '<a href="#" onclick="getPrevPage()">&lt;&lt;&nbsp;Previous Page</a>&nbsp;&nbsp;';
		}
		if(pCurrPage < pTotalPages){
			pageInfo += '<a href="#" onclick="getNextPage()">Next Page&nbsp;&gt;&gt;</a>';
		}
	}else{
		pageInfo += 'No products meet given criteria.';
	}
	pageInfo += '</div>';
	$("#" + htmlID).prepend(pageInfo);
}

function formatCriteria(index){
	if(allCriteria[index].type == 'term'){
		requestData = encodeRequestData("Criteria[" + index + "][CriteriaType]", "Term");
		requestData += '&' + encodeRequestData("Criteria[" + index + "][ElementName]", allCriteria[index].element);
		requestData += '&' + encodeRequestData("Criteria[" + index + "][Value]", allCriteria[index].value);
	}else if(allCriteria[index].type == 'range'){
		requestData = encodeRequestData("Criteria[" + index + "][CriteriaType]", "Range");
		requestData += '&' + encodeRequestData("Criteria[" + index + "][ElementName]", allCriteria[index].element);
		requestData += '&' + encodeRequestData("Criteria[" + index + "][Min]", allCriteria[index].min);
		requestData += '&' + encodeRequestData("Criteria[" + index + "][Max]", allCriteria[index].max);
	}else if(allCriteria[index].type == 'boolean'){
		requestData = encodeRequestData("Criteria[" + index + "][CriteriaType]", "Boolean");
		requestData += '&' + encodeRequestData("Criteria[" + index + "][Operator]", allCriteria[index].operator);
		for(i = 0; i < allCriteria[index].criteria.length; i++){
			requestData += '&' + encodeRequestData("Criteria[" + index + "][CriteriaTerms][" + i + "]", allCriteria[index].criteria[i]);
		}
		for(i = 0; i < allCriteria[index].criteria.length; i++){
			requestData +=  '&' + formatCriteria(allCriteria[index].criteria[i]);
		}
	}
	return requestData;
}

function formatQueryRequest(expectedType){
	showLoadingIcon();
	requestData = encodeRequestData("Types[0]", ptName);
	requestData += '&' + encodeRequestData("PagedResults", "1");
	requestData += '&' + encodeRequestData("PageNum", pCurrPage);
	requestData += '&' + encodeRequestData("RootIndex", criteriaRoot);
	requestData += '&' + encodeRequestData("OutputFormat", expectedType);
	requestData += '&' + formatCriteria(criteriaRoot);
	displayPermalink();
	$.post(siteUrl + "/queryScript.do",
		requestData,
		function(data){
			hideLoadingIcon();
			if(expectedType == "html"){
				$("#" + htmlID).html(data);
				currPage = parseInt($("#page_num").val());
				totalPages = parseInt($("#total_pages").val());
				pageSize = parseInt($("#page_size").val());
				numPageProducts = $("#product_list > li").length;
				totalTypeProducts = $("#total_type_products").val();
			}else if(expectedType == "json"){
				if(parseInt(data['Error']) == 1){
					$("#" + htmlID).html('<div class="error">' + data['ErrorMsg'] + '</div>');
					pCurrPage = 0;
					pTotalPages = 0;
					pPageSize = 0;
					pNumPageProducts = 0;
					pTotalProducts = 0;
				}else{
					renderJsonOutput(data);
					pTotalPages = data['totalPages'];
					pPageSize = data['pageSize'];
					pNumPageProducts = data['results'].length;
					pTotalProducts = data['totalProducts'];
				}
			}
			displayPageInfo();
		},
		expectedType);
}

function formatCrossTypeQueryRequest(){
	showLoadingIcon();
	requestData = encodeRequestData("Types[0]", "*");
	requestData += '&' + encodeRequestData("RootIndex", criteriaRoot);
	requestData += '&' + formatCriteria(criteriaRoot);
	requestData += '&' + encodeRequestData("PageNum", pCurrPage);
	requestData += '&' + encodeRequestData("PageSize", pPageSize);
	displayPermalink();
	$.post(siteUrl + "/crossTypeQueryScript.do",
		requestData,
		function(data){
		hideLoadingIcon();
			if(parseInt(data['Error']) == 1){
				$("#" + htmlID).html('<div class="error">' + data['ErrorMsg'] + '</div>');
			}else{
				renderJsonOutput(data);
				pTotalPages = data['totalPages'];
				pTotalProducts = data['totalProducts'];
				pNumPageProducts = data['results'].length;
				displayPageInfo();
			}
		},
		"json");
}

function formatAllTypeRequest(){
	showLoadingIcon();
	requestData = encodeRequestData("PageNum", pCurrPage);
	requestData += '&' + encodeRequestData("PageSize", pPageSize);
	$.post(siteUrl + "/allTypeScript.do",
		requestData,
		function(data){
			hideLoadingIcon();
			if(parseInt(data['Error']) == 1){
				$("#" + htmlID).html('<div class="error">' + data['ErrorMsg'] + '</div>');
			}else{
				renderJsonOutput(data);
				pTotalPages = data['totalPages'];
				pTotalProducts = data['totalProducts'];
				pNumPageProducts = data['results'].length;
				displayPageInfo();
			}
		},
		"json");
}

function formatProductPageRequest(expectedType){
	showLoadingIcon();
	requestData = encodeRequestData("Type", ptName);
	requestData += '&' + encodeRequestData("PageNum", pCurrPage);
	requestData += '&' + encodeRequestData("OutputFormat", expectedType);
	$.post(siteUrl + "/pageScript.do", 
		requestData, 
		function(data){
			hideLoadingIcon();
			if(expectedType == "html"){
				$("#" + htmlID).html(data);
				currPage = parseInt($("#page_num").val());
				totalPages = parseInt($("#total_pages").val());
				pageSize = parseInt($("#page_size").val());
				numPageProducts = $("#product_list > li").length;
				totalTypeProducts = 0;
			}else if(expectedType == "json"){
				if(parseInt(data['Error']) == 1){
					$("#" + htmlID).html('<div class="error">' + data['ErrorMsg'] + '</div>');
					pCurrPage = 0;
					pTotalPages = 0;
					pPageSize = 0;
					pNumPageProducts = 0;
					pTotalProducts = 0;
				}else{
					renderJsonOutput(data);
					pTotalPages = data['totalPages'];
					pPageSize = data['pageSize'];
					pNumPageProducts = data['results'].length;
					pTotalProducts = data['totalProducts'];
				}
			}
			displayPageInfo();
		},
		expectedType);
}

// Determine whether to request a page or query, depending upon the presence
// of any non-boolean criteria.  Returns 0 for a page and 1 for a query.
function determineRequest(index){
	if(allCriteria[index] == null){
		return 0;
	}
	if(allCriteria[index].type != 'boolean'){
		return 1;
	}
	for(i = 0; i < allCriteria[index].criteria.length; i++){
		if(determineRequest(allCriteria[index].criteria[i])){
			return 1;
		}
	}
	return 0;
}

function sendRequest(expectedType){
	if(determineRequest(criteriaRoot)){
		formatQueryRequest(expectedType);
	}else{
		formatProductPageRequest(expectedType);
	}
}

function sendCrossTypeRequest(){
	if(determineRequest(criteriaRoot)){
		formatCrossTypeQueryRequest();
	}else{
		if(parseInt(defaultShowEverything) == 1){
			formatAllTypeRequest();
		}else{
			clearResults();
		}
	}
}

function addCriteria(){
	index = criteriaNum;
	criteriaNum++;
	allCriteria[index] = new Object();
	return index;
}

function addTermCriteria(elementName, value, parentIndex){
	index = addCriteria();
	allCriteria[index].type = 'term';
	allCriteria[index].parentIndex = parentIndex;
	allCriteria[index].element = elementName;
	allCriteria[index].value = value;
	if(parentIndex != null){
		allCriteria[parentIndex].criteria.push(index);
	}
	return index;
}

function addRangeCriteria(elementName, min, max, parentIndex){
	index = addCriteria();
	allCriteria[index].type = 'range';
	allCriteria[index].parentIndex = parentIndex;
	allCriteria[index].element = elementName;
	allCriteria[index].min = min;
	allCriteria[index].max = max;
	if(parentIndex != null){
		allCriteria[parentIndex].criteria.push(index);
	}
	return index;
}

function addBooleanCriteria(operator, parentIndex){
	index = addCriteria();
	allCriteria[index].type = 'boolean';
	allCriteria[index].parentIndex = parentIndex;
	allCriteria[index].operator = operator;
	allCriteria[index].criteria = new Array();
	if(parentIndex != null){
		allCriteria[parentIndex].criteria.push(allCriteria[index]);
	}
	return index;
}

function removeCriteria(index){
	if(allCriteria[index].parentIndex != null){
		parentCriteria = allCriteria[allCriteria[index].parentIndex];
		for(i = 0; i < parentCriteria.criteria.length; i++){
			if(parentCriteria.criteria[i] == index){
				parentCriteria.criteria.splice(i, 1);
				i = parentCriteria.criteria.length;
			}
		}
	}
	allCriteria[index] = null;
}

function showLoadingIcon(){
	$("#" + htmlID).css("display", "none");
	$("#" + loadingID).css("display", "block");
}

function hideLoadingIcon(){
	$("#" + htmlID).css("display", "block");
	$("#" + loadingID).css("display", "none");
}

function setExclusiveQuery(exclusive){
	if(allCriteria[criteriaRoot]){
		if(exclusive == true){
			allCriteria[criteriaRoot].operator = 'and';
		}else{
			allCriteria[criteriaRoot].operator = 'or';
		}
	}
}
