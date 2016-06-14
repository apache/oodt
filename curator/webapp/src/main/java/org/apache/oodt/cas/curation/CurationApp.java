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

import org.apache.oodt.cas.curation.login.LoginPage;
import org.apache.oodt.cas.webcomponents.curation.workbench.Workbench;
import org.apache.wicket.*;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.target.coding.MixedParamUrlCodingStrategy;
import org.apache.wicket.util.file.File;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CurationApp extends WebApplication {

  private static final Logger LOG = Logger.getLogger(CurationApp.class
      .getName());

  public static final String PROJECT_DISPLAY_NAME = "org.apache.oodt.cas.curator.projectName";

  public static final String SSO_IMPL_CLASS = "org.apache.oodt.security.sso.implClass";

  public static final String CURATOR_HOMEPAGE = "curator.homepage";

  public static final String CURATOR_SKIN = "org.apache.oodt.cas.curator.skin";

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.wicket.protocol.http.WebApplication#init()
   */
  @Override
  protected void init() {
    super.init();
    Set<String> benchResources = Workbench.getImageFiles();
    String localPath = HomePage.class.getPackage().getName();
    Set<String> localResources = Workbench.getImageFiles(localPath);
    benchResources = filterBenchResources(benchResources, localResources,
        localPath);
    doImageMounts(benchResources, Workbench.class);
    doImageMounts(localResources, HomePage.class);

    MixedParamUrlCodingStrategy loginPageMount = new MixedParamUrlCodingStrategy(
        "auth", LoginPage.class, new String[] { "action" });
    mount(loginPageMount);
  }

  @Override
  public Class<? extends Page> getHomePage() {
    try {
      return (Class<? extends Page>) Class.forName(getHomePageClass());
    } catch (ClassNotFoundException e) {
      LOG.log(Level.SEVERE, e.getMessage());
      return HomePage.class;
    }
  }

  public String getHomePageClass() {
    return getServletContext().getInitParameter(CURATOR_HOMEPAGE);
  }

  public String getProjectName() {
    return getServletContext().getInitParameter(PROJECT_DISPLAY_NAME);
  }

  public String getSSOImplClass() {
    return getServletContext().getInitParameter(SSO_IMPL_CLASS);
  }

  public String getSkin() {
    return getServletContext().getInitParameter(CURATOR_SKIN);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.wicket.protocol.http.WebApplication#newSession(org.apache.wicket
   * .Request, org.apache.wicket.Response)
   */
  @Override
  public Session newSession(Request request, Response response) {
    CurationSession session = new CurationSession(request);
    String skin = getSkin();
    if (skin != null && !skin.equals("")) {
      LOG.log(Level.INFO, "Setting skin to: [" + skin + "]");
      session.setStyle(skin);
    }
    return session;
  }

  private Set<String> filterBenchResources(Set<String> bench,
      Set<String> local, String localPrefix) {
    if (local == null || (local.size() == 0)) {
      return bench;
    }
    if (bench == null || (bench.size() == 0)) {
      return bench;
    }
    Set<String> filtered = new HashSet<String>();
    for (String bResource : bench) {
      String localName = new File(bResource).getName();
      String compare = localPrefix + localName;
      if (!local.contains(compare)) {
        filtered.add(bResource);
      } else {
        LOG.log(Level.INFO, "Filtered conflicting bench resource: ["
            + bResource + "]");
      }

    }
    return filtered;
  }

  private void doImageMounts(Set<String> resources, Class<?> clazz) {
    if (resources != null) {
      for (String resource : resources) {
        String resName = new File(resource).getName();
        String resPath = "/images/" + resName;
        LOG.log(Level.INFO, "Mounting: [" + resPath + "] origName: [" + resName
            + "]: resource: [" + resource + "]");
        mountSharedResource(resPath,
            new ResourceReference(clazz, resName).getSharedResourceKey());
      }
    }
  }
}