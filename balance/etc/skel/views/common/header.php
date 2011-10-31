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

/*
 * Application Header File
 *
 * This file represents a sample application header file that will be displayed
 * with every application view (unless the view has explicitly requested
 * a different header file, or no header file at all). To customize the header
 * file, you have two options:
 * 
 * 1) make changes to this file directly
 * 2) create a new header file, and update config.ini to point to it, instead
 */
?>
<DOCTYPE html>
<html>
<head>
<title>Untitled OODT Balance Application</title>

<!-- Base stylesheets -->
<link rel="stylesheet" type="text/css" href="<?php echo SITE_ROOT .'/static/css/balance/balance.css'?>"/>

<!-- Base Javascript -->

<!-- Dynamically Added Stylesheets -->
<!-- STYLESHEETS -->

<!-- Dynamically Added Javascripts -->
<!-- JAVASCRIPTS -->

<!-- Site specific stylesheet overrides -->
<link rel="stylesheet" type="text/css" href="<?php echo SITE_ROOT .'/static/css/site.css'?>"/>

</head>
<body>
<div class="container">
<?php
	// This retrieves any 'flash' messages that should be displayed to the user
	echo App::Get()->GetMessages();
?>
