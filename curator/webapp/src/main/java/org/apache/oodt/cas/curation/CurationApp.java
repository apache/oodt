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

package org.apache.oodt.cas.curation;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.oodt.cas.webpcomponents.curation.workbench.Workbench;
import org.apache.wicket.Page;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.file.File;

public class CurationApp extends WebApplication {
  
  private static final Logger LOG = Logger.getLogger(CurationApp.class.getName());

	/* (non-Javadoc)
   * @see org.apache.wicket.protocol.http.WebApplication#init()
   */
  @Override
  protected void init() {
    super.init();
    Set<String> resources = Workbench.getImageFiles();
    if (resources != null){
      for (String resource: resources){
        String resName = new File(resource).getName();
        String resPath = "/images/"+resName;
        LOG.log(Level.INFO, "Mounting: ["+resPath+"]");
        mountSharedResource(resPath,
            new ResourceReference(Workbench.class,
                resName).getSharedResourceKey());
      }
    }
  }

  @Override
	public Class<? extends Page> getHomePage() {
		try {
			return (Class<? extends Page>) Class.forName(getHomePageClass());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return HomePage.class;
		}
	}

	public String getHomePageClass() {
		return getServletContext().getInitParameter("curator.homepage");
	}

}