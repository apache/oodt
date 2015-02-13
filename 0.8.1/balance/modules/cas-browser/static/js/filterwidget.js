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

function createFilter(key, value){
	if(!allCriteria[criteriaRoot]){
		criteriaRoot = addBooleanCriteria('and', null);
	}
	index = addTermCriteria(key, value, criteriaRoot);
	var filterText = '<tr id="filter' + index + '">';
	filterText += '<td>' + key + '</td><td>=</td><td>' + value + '</td>';
	filterText += '<td align="right">';
	filterText += '<input type="button" value="Remove" onclick="removeFilter(\'' + index + '\')" />';
	filterText += '</td></tr>';
	$("#filters").append(filterText);
}

function addFilter(){
	key = $("#filterKey").val();
	value = $("#filterValue").val();
	pCurrPage = 1;
	if(value!=""){
		$("#permalink").html("");
		createFilter(key, value);
		formatQueryRequest(resultFormat);
		$("#filterValue").val("");
    }
}

function removeFilter(filterIndex){
	pCurrPage = 1;
	$("#filter" + filterIndex).remove();
	$("#permalink").html("");
	removeCriteria(filterIndex);
	sendRequest(resultFormat);
}

function renderJsonOutput(data){
	output = '';
	if(displayedMetadata.length > 0){
		output = '<table><thead><tr><th>Product Name</th>';
		for(i = 0; i < displayedMetadata.length; i++){
			output += '<th>' + displayedMetadata[i] + '</th>';
		}
		output += '</tr></thead><tbody>';
		for(i = 0; i < data['results'].length; i++){
			output += '<tr><td><a href="' + siteUrl + '/product/' + data['results'][i]['id'] + '">';
			output += data['results'][i]['name'] + '</a></td>';
			for(j = 0; j < displayedMetadata.length; j++){
				output += '<td>' + data['results'][i]['metadata'][displayedMetadata[j]] + '</td>';
			}
			output += '</tr>';
		}
		output += '</tbody></table>';
	}else{
		output = '<ul class="pp_productList" id="product_list">';
		for(i = 0; i < data['results'].length; i++){
			output += '<li><a href="' + siteUrl + '/product/' + data['results'][i]['id'] + '">';
			output += data['results'][i]['name'] + '</a></li>';
		}
		output += '</ul>';
	}
	$("#" + htmlID).html(output);
}

function displayPermalink(){
	output = 'To see these filters again, go to:<br />' + siteUrl + '/products/' + ptID + '/';
	for(i = 0; i < allCriteria[criteriaRoot].criteria.length; i++){
		termIndex = allCriteria[criteriaRoot].criteria[i];
		termKey = allCriteria[termIndex].element;
		termValue = allCriteria[termIndex].value;
		output += termKey + '/' + termValue + '/';
	}
	$("#permalink").html(output);
}

function getNextPage(){
	pCurrPage = pCurrPage + 1;
	sendRequest("json");
}

function getPrevPage(){
	pCurrPage = pCurrPage - 1;
	sendRequest("json");
}