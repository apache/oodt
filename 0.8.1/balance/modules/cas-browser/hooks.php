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
 * HOOKS.PHP
 * 
 * Hooks provide the ability, as the name implies, to hook into various parts of 
 * the view rendering process and insert customizations. The contents of these 
 * functions are run at the appropriate time *EVERY* time a view is rendered, i.e.
 * the content of the hooks is not by default view-specific but rather will be 
 * applied to all views. (However, there is nothing that prevents developers from 
 * inserting conditional logic inside a hook that then causes view-specific
 * them to exhibit view-specific behavior).
 * 
 * Take a look at the docblock descriptions of each hook to get a sense of where
 * in the view rendering process the hook is invoked.
 * 
 * @author ahart
 */

/**
 * hook_before_header
 * 
 * This hook is executed before the contents of the header file are processed.
 */
function hook_before_header() {} 

/**
 * hook_before_view
 * 
 * This hook is executed before the contents of the main view are processed.
 */
function hook_before_view() {
	require_once( 'scripts/widgets/BreadcrumbsWidget.php' );
	
	$module = App::Get()->loadModule();
	// Include JavaScript files to be shown with every view in this module
	App::Get()->response->addJavascript($module->moduleStatic.'/js/jquery-1.4.2-min.js');
	App::Get()->response->addJavascript($module->moduleStatic.'/js/jcorner.jquery.js');
	
	// Include CAS-Browser default CSS stylesheets to be shown with every view in this module
	App::Get()->response->addStylesheet($module->moduleStatic.'/css/cas-browser.css');
	App::Get()->response->addStylesheet($module->moduleStatic.'/css/dataTables.css');
}

/**
 * hook_before_footer
 * 
 * This hook is executed before the contents of the footer are processed
 */
function hook_before_footer() {}

/**
 * hook_before_render
 * 
 * This hook is after all of the view components (header, view, footer) have been
 * processed but before the processed results are sent out across the wire to the 
 * browser.
 */
function hook_before_send() {}
