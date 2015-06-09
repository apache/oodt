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
