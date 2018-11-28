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

// Prepare BreadcrumbsWigdet
require_once( HOME . '/scripts/widgets/BreadcrumbsWidget.php');
$bcw = new BreadcrumbsWidget();
$bcw->add('Home',SITE_ROOT . '/');
$bcw->add('Access Forbidden');
?>

<div id="page_content">
	<h2> 403 Access Forbidden </h2>
	<h5> You are not authorized to view the requested resource. If you feel this is in error, please
             contact the webmaster for this site </h5>
</div>
