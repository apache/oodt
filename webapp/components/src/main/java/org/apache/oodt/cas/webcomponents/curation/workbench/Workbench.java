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

package org.apache.oodt.cas.webcomponents.curation.workbench;

import java.util.concurrent.ConcurrentHashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.file.File;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

public class Workbench extends Panel {

  private static final long serialVersionUID = 3911179455208050261L;

  public Workbench(String id) {
    super(id);
  }

  public static Set<String> getImageFiles() {
    return getImageFiles(Workbench.class.getPackage().getName());
  }

  public static Set<String> getImageFiles(String packageName) {
    Pattern pattern = Pattern.compile(".*\\.(png|gif|jpg|jpeg|jp2)");
    Set<String> resources = new Reflections(packageName, new ResourcesScanner())
        .getResources(pattern);
    Set<String> filteredResources = new HashSet<String>();
    Map<String, Boolean> resMap = new ConcurrentHashMap<String, Boolean>();
    for (String res : resources) {
      String resName = new File(res).getName();
      if (!resMap.containsKey(resName)) {
        resMap.put(resName, true);
        filteredResources.add(resName);
      }
    }

    return filteredResources;
  }

}
