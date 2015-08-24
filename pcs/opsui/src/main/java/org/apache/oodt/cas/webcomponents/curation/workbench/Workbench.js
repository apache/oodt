/*
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

// File Tree

var _staging = '/';	// current root path in staging area browser
var _catalog = '/';	// current root path in catalog browser
var _paths   = { "staging" :  '/', 
                 "catalog" :  '/',
                 "currentStagedFile" : '',
                 "currentCatalogedFile" : '',
                 "currentProductType" : ''}

if(jQuery) (function($){
	
	$.extend($.fn, {
		fileTree: function(o, h, i) {
			// Defaults
			if( !o ) var o = {};
			if( o.root == undefined ) o.root = '/';
			if( o.folderEvent == undefined ) o.folderEvent = 'dblclick';
			if( o.expandSpeed == undefined ) o.expandSpeed= 500;
			if( o.collapseSpeed == undefined ) o.collapseSpeed= 500;
			if( o.expandEasing == undefined ) o.expandEasing = null;
			if( o.collapseEasing == undefined ) o.collapseEasing = null;
			if( o.multiFolder == undefined ) o.multiFolder = true;
			if( o.loadMessage == undefined ) o.loadMessage = 'Loading...';
			
			// Understand which of the two browsers to apply nav actions to
			if (o.which == undefined) { alert('please specify \'which\' (options are: "staging"|"catalog") '); o.which = "staging";}
			
			// Ensure a script has been provided
			if (o.script == undefined) { alert('please specify a target script \'script\' in fileTree options'); }
			
			// Get a handle to the outer UL
			o.outerContainer = '#' + $(this).attr('id');
			if (o.outerContainer == undefined) { alert('container must have unique id') };
			
			$(this).each( function() {
				
				function showTree(c, t) {
					$(c).addClass('wait');
					$(".fileTree.start").remove();
					$.get(o.script, { path: t }, function(data) {
						$(c).find('.start').html('');
						$(c).removeClass('wait').html(data);
						if (o.which == "staging")
							_paths.staging = escape(t);
						else
							_paths.catalog = escape(t);
						bindTree(c);
						updateNav(o.which);
						clearMetadataWorkbenchContent(o.which);
						initDraggables();
					});
				}
				
				function bindTree(t) {
					$(o.outerContainer).find('UL LI A').bind('click', function() {
						if ($(this).parent().hasClass('productType')) {
							i($(this).attr('rel'));
						} else if ($(this).parent().hasClass('file')) {
							h($(this).attr('rel'));
						}
					}).bind('dblclick', function() {
						if ($(this).parent().hasClass('directory') ) {
							$(this).parent().find('UL').remove(); // cleanup
							showTree( $(o.outerContainer), escape($(this).attr('rel')) );
						}
					});
					
					// Prevent A from triggering the # on non-click events
					if( o.folderEvent.toLowerCase != 'click' ) $(t).find('LI A').bind('click', function() { return false; });
				}
				
				// Loading message
				$(this).html('<ul class="fileTree start"><li class="wait">' + o.loadMessage + '<li></ul>');
				
				// Get the initial file list
				showTree( $(this), escape((o.which == "staging" ) ? _paths.staging : _paths.catalog) );
			});
		}
	});
})(jQuery);

// Metadata editors
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
	
	$.post('../services/metadata/staging',
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

	$.post('../services/metadata/catalog',
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

	$.post('../services/metadata/productType',
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


/********************************************************************
 * INGESTION TASK SETUP
 */


// Keep track of dropped files. This is used to determine the files 
// that will be included in an ingestion task
var droppedFiles = Array();

// The currently selected policy. This is the policy that will
// be used in the ingestion task.
var itPolicy;

// The currently selected product type. This is the product typte that
// will be used in the ingestion task
var itProductType;


// The currently selected Metadata Extractor Configuration Id
// This is the configuration that will be sent with the ingestion task
var itMetExtractorConfigId = 'default';



function updateIngestionTaskMetExtractorConfigIds() {
	$.get('../services/metadata/extractor/config',
			{},
			function(data,textState) {
				$('#itMetExtractorConfigId').html(data);
				itMetExtractorConfigId = $('#itMetExtractorConfigId').val();
			});
}

function updateSelectedMetExtractorConfigId() {
	itMetExtractorConfigId = $('#itMetExtractorConfigId').val();
}

function createIngestionTask() {
	// The files to include are in droppedFiles
	var files       = droppedFiles;
	// The met extractor config id is in itMetExtractorConfigId
	var metExtCfgId = itMetExtractorConfigId;
	// The policy and product type are in itPolicy and itProductType
	var policy      = itPolicy;
	var productType = itProductType;
	// Other variables we'll need
	var numFiles = droppedFiles.length;
	
	// Send the information to the create ingestion task servlet
	$.get('../services/ingest/create',
			{
				'files'      : files.join(','),
				'numfiles'   : numFiles,
				'metExtCfgId': metExtCfgId,
				'policy'     : policy,
				'ptype'      : productType
			},
			function (data,textState) {
			   refreshIngestTaskList();		
			}); 
	
   
}

function refreshIngestTaskList(){
    $.get('../services/ingest/list',
            { 'format': 'html'
            },
            function (data,textState) {
                showIngestionTaskList(data);
            });         
}

function showIngestionTaskList(data){
    $('#ingestionTaskListItems > tbody').html(data);
}

function registerIngestionTaskListener(){
    $.PeriodicalUpdater('../services/ingest/list', {
        method: 'get',         
        data: { 'format:': 'html'},          
        minTimeout: 1000,
        maxTimeout: 60000,
        multiplier: 2
      }, function (data) {
          showIngestionTaskList(data);
         });

}

function createIngestionTaskRow(data) {    
    $('#ingestionTaskListItems > tbody').replace(data);
}

function removeIngestionTask(id) {
	$.get('../services/ingest/remove',
			{ 'taskId' : id },
			refreshIngestTaskList()
			);
}

function startIngestionTask(id) {
	$.get('../services/ingest/start',
			{ 'taskId' : id },
			function(data,textState) {
				$('#'+id+'_Status').html(data);	// 'started' or 'error'
				refreshIngestTaskList();
			});
}

/********************************************************************
 * DRAG AND DROP INITIALIZATION AND CONFIGURATION
 * 
 */
function initDraggables() {
	$('.draggy').draggable({
		revert:true, 
		zIndex:'5000', 
		appendTo:'body'
		, 
		helper: function() { 
			return $('<li>')
				.addClass('helper')
				.addClass('file').addClass('ext_tiff')
				.html($(this).children().clone());
		}
	});
}
function initDragAndDropTarget() {
	$('#droppedFileTarget').droppable( {
		drop: function(event, ui) {
			droppedFiles.push(ui.draggable.children('a')[0].rel);
			updateDroppedFilesList();
		}
	});
}
function updateDroppedFilesList() {
	var i = droppedFiles.length -1;
	$('#droppedFileList > ul').append(
		'<li rel="'+droppedFiles[i]+'">'+droppedFiles[i]+'</li>'
	);
}
function clearDroppedFilesList() {
	droppedFiles = Array();				// clear the js array
	$('#droppedFileList ul').html('');	// clear the interface display
}


/********************************************************************
 * MAIN INITIALIZATION FUNCTION (ON DOM READY)
 */
$(document).ready( function() {

	// Initialize the staging area browser
	initStagingAreaBrowser();
	
	// Initialize the staging area navigation buttons
	$('#stagingNavUpOne')
    	.dblclick(stagingNavigateUpOne)
    	.click( function(event) {event.preventDefault();});
    $('#stagingNavUpRoot')
    	.dblclick(stagingNavigateUpRoot)
    	.click( function(event) {event.preventDefault();});


	// Initialize the File Manager browser
	initFileManagerBrowser();

	// Initialize the catalog navigation buttons
	$('#catalogNavUpOne')
		.dblclick(catalogNavigateUpOne)
    	.click( function(event) {event.preventDefault();});
	$('#catalogNavUpRoot')
		.dblclick(catalogNavigateUpRoot)
    	.click( function(event) {event.preventDefault();});


	// Make overlaid info messages semi-transparent
	$('.info.overlay').fadeTo(0,0.8);
	
	// Connect staging met extractor refresh button
	$('#stagingMetExtractorRefreshButton').click(refreshStagedMetadata);
	
});

function updateNav(which) {
	if (which == 'staging') 
		$('#browseStagingPath').html('Path: ' + _paths.staging);
		if ('/' == _paths.staging) {
			// We are at the root, don't show . and ..
			$('#browseStagingNav').hide();
		} else {
			$('#browseStagingNav').show();
		}
	if (which == 'catalog') {
		// Catalog navigation is a little more complex
		$('#browseCatalogPath').html('Path: ' + _paths.catalog);
		if ('/' == _paths.catalog) {
			// We are listing policies, don't show . and ..
			$('#browseCatalogNav').hide();
		} else {
			$('#browseCatalogNav').show();
		}

	}
}

function clearMetadataWorkbenchContent(which) {
	if (which == 'staging') {
		$('#infoNoMetadataExtractorDefined').hide();
		$('#stagedMetadataWorkbenchContent').html('');
		hideStagingMetExtractorSelection();
		_paths.currentStagedFile = '';
	} else if (which == 'catalog') {
		$('#catalogMetadataWorkbenchContent').html('');
		_paths.currentCatalogedFile = '';
		_paths.currentProductType   = '';
	}
}



/********************************************************************
 * STAGING AREA BROWSER FUNCTIONS
 */

var stagingMetExtractorConfigId = '';

 
function initStagingAreaBrowser() {
	 $('#browseStagingContents').fileTree(
    	{ root:   '/',
          which:  'staging',
          script: '../services/directory/staging' 
        }, function(file) {
        	getMetadataForStagedFile(file, false);
      });
     updateNav('staging');
}

function stagingNavigateUpOne() {
	var pieces = _paths.staging.split('/');
	var newpath = '';
	for (var i=0; i < (pieces.length-2); i++) {
		newpath += (pieces[i] + '/');
	}
	if ('' == newpath) {
		newpath = '/';
	}
	_paths.staging = newpath;
	initStagingAreaBrowser();
	clearMetadataWorkbenchContent('staging');
	return false;
}

function stagingNavigateUpRoot() {
	// Re-Initialize the staging area browser
	_paths.staging = '/';
	initStagingAreaBrowser();
	clearMetadataWorkbenchContent('staging');
    return false;
}

function getMetadataForStagedFile(file, overwrite) {
	$.get('../services/metadata/staging',
			{ 'id' : file,
		      'configId' : stagingMetExtractorConfigId,
		      'overwrite' : overwrite
			},
			function(data,textStatus) {
				displayStagedFileMetadata(file,data);
			}, "html");
}

function displayStagedFileMetadata(file,data) {
	showStagingMetExtractorSelection();
	_paths.currentStagedFile = file;
	if (! data.match('tr')) {
		if (stagingMetExtractorConfigId == '') {
			$('#infoNoMetadataExtractorDefined').show();
		}
	} else { 
		$('#stagedMetadataWorkbenchContent').html(data);
		makeStagedMetadataEditable();
	}
}

function updateStagingMetExtractorConfigIds() {
	$.get('../services/metadata/extractor/config',
		{'current' : stagingMetExtractorConfigId },
		function(data,textState) {
			$('#stagingMetExtractorConfigList').html(data);
			stagingMetExtractorConfigId = $('#stagingMetExtractorConfigList').val();
		});
}

function showStagingMetExtractorSelection() {
	$('#stagedMetadataWorkbenchContent').css('height','323px');
	$('#stagingMetExtractorSelection').fadeIn('fast');
	updateStagingMetExtractorConfigIds();
}

function hideStagingMetExtractorSelection() {
	$('#stagingMetExtractorSelection').fadeOut('fast');
	$('#stagedMetadataWorkbenchContent').css('height','350px');
}

function refreshStagedMetadata() {
	stagingMetExtractorConfigId = $('#stagingMetExtractorConfigList').val();
	getMetadataForStagedFile(_paths.currentStagedFile, true);
}
/********************************************************************
 * FILE MANAGER BROWSER FUNCTIONS
 */

//
// paths in this area are "virtual", that is, they reflect the logical
// organization of the file manager catalog rather than the physical
// organization of the products in the repository. As such, the virtual
// path to a product is obtained by specifying both the 'policy' and 
// 'productType':
//
// dem00001.tiff exists at "virtual" path: /Lmmp/DEM where
// LMMP is the policy
// and DEM is a product type defined within that policy
//

 
function initFileManagerBrowser() {
	 $('#browseCatalogContents').fileTree(
    	{ root:   '/',
          which:  'catalog',
          script: '../services/policy/browse'
        }, function(file) {
        	getMetadataForCatalogedFile(file);
        }, function(productType) {
          getProductTypeWorkbench(productType);
     });
     updateNav('catalog');
     
}

function catalogNavigateUpOne() {
	
	var pieces = _paths.catalog.split('/');
	var newpath = '';
	for (var i=0; i < (pieces.length-2); i++) {
		newpath += (pieces[i] + '/');
	}
	if ('' == newpath) {
		newpath = '/';
	}
	_paths.catalog = newpath;
	initFileManagerBrowser();
	clearMetadataWorkbenchContent('catalog');
	return false;
}

function catalogNavigateUpRoot() {
	// Re-Initialize the catalog browser
	_paths.catalog = '/';
	initFileManagerBrowser();
	clearMetadataWorkbenchContent('catalog');
	return false;
}
 
function getMetadataForCatalogedFile(file) {
	$.get('../services/metadata/catalog',
			{ 'id' : file },
			function(data,textStatus) {
				displayCatalogedFileMetadata(file,data);
				}, "html");
}
 
function displayCatalogedFileMetadata(file,data) {
	$('#catalogMetadataWorkbenchContent').html(data);
	_paths.currentCatalogedFile = file;
	makeProductMetadataEditable();
}

function getProductTypeWorkbench(productType) {
	var pieces = productType.split('/');
	var policy = pieces[1];
	var ptype  = pieces[2];
	$.get('../services/metadata/productType',
			{ 'policy' : policy, 'productType' : ptype, 'id' : productType },
			function(data,textStatus) {
				displayProductTypeWorkbench(productType,data);
			},"html");
	itPolicy      = policy;
	itProductType = ptype;
}

function displayProductTypeWorkbench(productType,data) {
	$('#catalogMetadataWorkbenchContent').html('');	// clear the old contents
	$('.ptwbMetadataList').html(data);				// add metadata from service		
	
	$('#productTypeWorkbenchContents')				// display pt workbench
		.clone()
		.attr('id','liveProductTypeWorkbenchContents')
		.appendTo('#catalogMetadataWorkbenchContent')
		.show()
		.tabs();
	clearDroppedFilesList();						// clear dropped files list
	initDragAndDropTarget();						// set up the d&d target box
	_paths.currentProductType = productType;		// set the path for the ptype
	makeProductTypeMetadataEditable();				// allow ptype met editing
	updateIngestionTaskMetExtractorConfigIds();
}


