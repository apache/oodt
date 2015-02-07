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

// When the document is ready, we need to create a representation of the criteria
// tree in javascript.  This is done by looking for hidden input tags with ids that
// start with either 'term' or 'range'.  These precede the text input fields to
// specify criteria of the corresponding type.  We store the ids of the input fields
// so we can use thier values when the search button is clicked.  Create criteria
// that have blank values since they haven't been filled in yet; these values will
// be updated when the user clicks the search button.
$(document).ready(function(){
	var parentIndex = addBooleanCriteria('and', null);
	$('input[id^="term"]').each(function(){
		var newTermIndex = addTermCriteria($(this).val(), '', parentIndex);
		allCriteria[newTermIndex].termInputId = $(this).next().attr("id");
	})
	$('input[id^="range"]').each(function(){
		var newRangeIndex = addRangeCriteria($(this).val(), '', '', parentIndex);
		allCriteria[newRangeIndex].minInputId = $(this).next().attr("id");
		allCriteria[newRangeIndex].maxInputId = $(this).next().next().attr("id");
	})
});

// Since the criteria in our query tree are originally created with empty values, we
// must grab the values from the input fields at the stored ids.
function customUpdateCriteria(index){
	if(allCriteria[index].type == 'term'){
		allCriteria[index].value = $('#' + allCriteria[index].termInputId).val();
		$('#' + allCriteria[index].termInputId).val('');
	}else if(allCriteria[index].type == 'range'){
		allCriteria[index].min = $('#' + allCriteria[index].minInputId).val();
		$('#' + allCriteria[index].minInputId).val('');
		allCriteria[index].max = $('#' + allCriteria[index].maxInputId).val();
		$('#' + allCriteria[index].maxInputId).val('');
	}else if(allCriteria[index].type == 'boolean'){
		for(i = 0; i < allCriteria[index].criteria.length; i++){
			customUpdateCriteria(allCriteria[index].criteria[i]);
		}
	}
	return;
}

// This function is called when the search button is clicked.
function customQuery(){
	$("#page_num").val(1);	// Set the value of the desired page to 1 (first in
							// the set).  The user can fetch later pages by
							// clicking the links for next and previous pages.
	customUpdateCriteria(criteriaRoot);	// Update the criteria in the query tree
										// with the values from the input fields.
	formatQueryRequest("html");	// Perform the query
}

function renderJsonOutput(data){
	output = '<ul class="pp_productList" id="product_list">';
	for(i = 0; i < data['productList'].length; i++){
		output += '<li><a href="' + siteUrl + '/product/' + data['productList'][i]['id'] + '">';
		output += data['productList'][i]['name'] + '</li>';
	}
	output += '</ul>';
	output += '<input type="hidden" id="total_pages" value="' + data['totalPages'] + '">';
	output += '<input type="hidden" id="page_size" value="' + data['pageSize'] + '">';
	output += '<input type="hidden" id="total_type_products" value="' + data['totalTypeProducts'] + '">';
	$("#" + htmlID).html(output);
}