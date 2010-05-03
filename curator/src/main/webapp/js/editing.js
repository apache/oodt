/**
 * Metadata editors
 *
 */

function makeStagedMetadataEditable() {
	makeMetadataEditable(
		$("#stagedMetadataWorkbenchContent > table > tbody > tr >  td"),
		updateStagedMetadata);
}

function makeProductMetadataEditable() {
	makeMetadataEditable(
		$("#catalogMetadataWorkbenchContent > table > tbody > tr > td"),
		updateProductMetadata);		
}

function makeProductTypeMetadataEditable() {
	makeMetadataEditable(
		$("#liveProductTypeWorkbenchContents div.ptwbMetadataList > table > tbody > tr > td"),
		updateProductTypeMetadata);
}

function updateStagedMetadata() {
	// Data is the collection of <tr/> elements that contain metadata key/value pairs
	var data = getMetadataFromSource($('#stagedMetadataWorkbenchContent > table > tbody > tr'));
	var qstr = getQueryStringFromMetadata(_paths.currentStagedFile, data);
	
	$.post('./services/metadata/staging',
		qstr,
		function(data, textStatus) {
			// alert(data);
		}
	);
}

function updateProductMetadata() {
	// Data is the collection of <tr/> elements that contain metadata key/value pairs
	var data = getMetadataFromSource($('#catalogMetadataWorkbenchContent > table > tbody > tr'));
	var qstr = getQueryStringFromMetadata(_paths.currentCatalogedFile, data);

	$.post('./services/metadata/catalog',
		qstr,
		function(data, textStatus) {
			// alert(data);
		}
	);
}

function updateProductTypeMetadata() {
	// Data is the collection of <tr/> elements that contain metadata key/value pairs
	var data = getMetadataFromSource($('#liveProductTypeWorkbenchContents div.ptwbMetadataList > table > tbody > tr'));
	var qstr = getQueryStringFromMetadata(_paths.currentProductType, data);

	$.post('./services/metadata/productType',
		qstr,
		function(data, textStatus) {
			// alert(data);
		}
	);
}



/**
 * Utility functions to encapsulate common functionality
 *
 */

// source:   the collection of <td/> objects containing metadata values
// callback: the function to be called to persist the edits
function makeMetadataEditable(source, callback) {
	// Set up the i/f to display the icon only when the user is hovering over
	// the field
	source.click(function() {
		var $met = $(this);
		var metValue = $met; // the <td> itself
			var metKey = $(this).prev().text();
			jPromptMulti(
					metKey,
					metValue,
					'Update Metadata',
					function(r) {

						if (r) {
							// turn r into a comma-separated string of values
							htmlResult = '<span>' + r.join('</span>, <span>') + '</span>';
							// alert( 'You entered ' + htmlResult )
							$met.html(htmlResult);
							callback();
						}
					});
		});
}

function getMetadataFromSource( source ) {
	
	// Keys are 1 per row in <th/> elements
	// Values are 1 per row in <td/> elements
	
	var $table = source;
	var data   = Array();
	$table.each( function () {
		// Create a key/value mapping of the staged metadata
		var $valueElmt = $(this).children('td');
		// All values are wrapped in <span/> elements and there can be
		// an unlimited number of them, so build an array.
		var $values    = $valueElmt.children('span');
		var valueData  = Array();
		for (var i = 0; i < $values.length; i ++ ) {
			valueData.push($values[i].textContent);
		}
		// Assign the value data to the correct element
		data.push( { 'key' : $(this).children('th').text(), 'value' : valueData } );
	});
	
	return data;
}

function getQueryStringFromMetadata(id,data) {
	// Build the query string that will be sent to the server
	var qstr      = '';
	qstr         += 'id=' + id;
	for ( var i = 0; i < data.length; i++ ) {
		for ( var j = 0; j < data[i].value.length; j++ ) {
			qstr += '&metadata.' + data[i].key + '=' + data[i].value[j];
		}
	}
	
	return qstr;
}