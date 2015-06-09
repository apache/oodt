/**
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

package org.apache.oodt.pcs.opsui;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebResponse;

/**
*
* Extension of WebPage for customized features
*
* @author riverma
* @version $Revision$
*
*/
public class OPSUIWebPage extends WebPage {

	  /**
	   * Necessary to alleviate 'Page expired' caching issue.
	   * See: http://www.richardnichols.net/2010/03/apache-wicket-force-page-reload-to-fix-ajax-back/
	   */
	  @Override
	  protected void configureResponse() {
	      super.configureResponse();
	      WebResponse response = getWebRequestCycle().getWebResponse();
	      response.setHeader("Cache-Control", 
	            "no-cache, max-age=0,must-revalidate, no-store");
	  } 
	
}
