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
 * BREADCRUMB DISPLAY WIDGET
 *
 * Widget to display basic information about the current page. It allows
 * users to keep track of their locations within the website.
 *
 * These breadcrumbs will allow the user to return to parent pages when a
 * a link is provided.
 *
 * Initialization BreadcrumbsWidget:
 *      $bcw = new BreadcrumbsWidget( $options = array());
 *
 *      supported options:
 *        'separator' => "(string) specifying the html that should separate breadcrumbs"
 *
 *      *NOTE*: the class also recognizes the `breadcrumbs_separator` application config
 *              setting. If this setting is present within the application config.ini file,
 *              the value will be used as the separator;
 *
 *
 * Adding a label breadcrumb (no link):
 * 		$bcw->add('Label');
 *
 * Adding a linked label breadcrumb:
 * 	 	$bcw->add('Label','link_href');
 *
 *
 */

class BreadcrumbsWidget
	implements Ogr_Apache_Oodt_Balance_Interfaces_IApplicationWidget {

	protected $separator = '&nbsp;&rarr;&nbsp;';

	public function __construct($options = array()) {

		// Check if the breadcrumbs_separator config setting has been set
		if (isset(App::Get()->settings['breadcrumbs_separator'])) {
			$this->separator = App::Get()->settings['breadcrumbs_separator'];
		}

		// If the 'separator' option has been passed, prefer it
		if (isset($options['separator'])) {
			$this->separator = $options['separator'];
		}
	}

	public function add( $label = null, $link = null) {
		$breadcrumbs = App::Get()->response->data('breadcrumbs');
		if ( $label != null && $link != null ) {

			// will show up as a linked label
			$breadcrumbs[] = array($label,$link);
		} elseif ( $label != null ) {

			// will show up as just text
			$breadcrumbs[] = $label;
		} else
			return 0;

		App::Get()->response->data("breadcrumbs",$breadcrumbs);
	}

	public function render($bEcho = true) {
		$str 	= '';
		$data = App::Get()->response->data('breadcrumbs');
		if ( !empty($data) ) {
			foreach ($data as $bc) {
				if (is_array($bc)) {
					$str .= '<span class="crumb link"><a href="'.$bc[1].'">'.$bc[0]."</a></span>{$this->separator}";
				} else {
					$str .= '<span class="crumb text">'.$bc.'</span>';
				}
			}
		}

		if($bEcho) {
			echo $str;
		} else {
			return $str;
		}
	}
}
