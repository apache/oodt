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
	$.get('./services/metadata/extractor/config',
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
	$.get('./services/ingest/create',
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
    $.get('./services/ingest/list',
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
    $.PeriodicalUpdater('./services/ingest/list', {
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
	$.get('./services/ingest/remove',
			{ 'taskId' : id },
			refreshIngestTaskList()
			);
}

function startIngestionTask(id) {
	$.get('./services/ingest/start',
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
          script: './services/directory/staging' 
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
	$.get('./services/metadata/staging',
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
	$.get('./services/metadata/extractor/config',
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
          script: './services/policy/browse'
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
	$.get('./services/metadata/catalog',
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
	$.get('./services/metadata/productType',
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

