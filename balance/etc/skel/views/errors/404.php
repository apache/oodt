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
$bcw->add('Not Found');
?>

<div id="page_content">
        <h2> 404 Not Found </h2>
        <h5> The requested resource (<?php echo App::Get()->request->uri?>) does not exist.</h5>
</div>
