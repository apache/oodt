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

package gov.nasa.jpl.oodt.cas.curation.policymgr;

//OODT imports
import gov.nasa.jpl.oodt.cas.filemgr.repository.XMLRepositoryManager;
import gov.nasa.jpl.oodt.cas.filemgr.structs.ProductType;
import gov.nasa.jpl.oodt.cas.metadata.SerializableMetadata;

//JDK imports
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

//APACHE imports
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

/**
 * 
 * 
 * A management interface for dealing with CAS filemgr policy, and for allowing
 * for browsing and traversal of the staging area used in ingestion.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class CurationPolicyManager {

  private static final Logger LOG = Logger
      .getLogger(CurationPolicyManager.class.getName());

  private String stagingAreaPath;

  private String policyDirectoryPath;

  public CurationPolicyManager() {
    this(null, null);
  }

  public CurationPolicyManager(String policyDirectoryPath,
      String stagingAreaPath) {
    this.policyDirectoryPath = policyDirectoryPath;
    this.stagingAreaPath = stagingAreaPath;
  }

  public Map<String, ProductType> getProductTypes(String dsCollection) {
    Map<String, ProductType> typeMap = new HashMap<String, ProductType>();
    XMLRepositoryManager xmlRepo = getRepo(dsCollection);
    List<ProductType> types = safeGetProductTypes(xmlRepo);

    for (ProductType type : types) {
      typeMap.put(type.getName(), type);
    }

    return typeMap;
  }

  public List<ProductType> typesToList(Map<String, ProductType> types) {
    List<ProductType> typeList = new Vector<ProductType>();
    for (String typeStr : types.keySet()) {
      typeList.add(types.get(typeStr));
    }

    return typeList;
  }

  public String getDirectoryAreaAsJSON(String base, String directory,
      boolean showFiles) {
    String startingPath = (base + "/" + directory);
    startingPath = StringUtils.replace(startingPath, "source", "/");
    String f[] = getFilesInDirectory(startingPath, showFiles);

    List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
    for (int i = 0; i < f.length; i++) {
      Map<String, Object> entry = new HashMap<String, Object>();
      String children[] = getFilesInDirectory(startingPath + "/" + f[i],
          showFiles);
      entry.put("text", f[i]);
      entry.put("id", directory + "/" + f[i]);
      entry.put("expanded", false);
      entry.put("hasChildren", children != null && (children.length > 0));
      entry.put("isFile", new File(startingPath + "/" + f[i]).isFile());
      items.add(entry);
    }

    return JSONArray.fromObject(items).toString();
  }

  public String getExistingPoliciesAsHTML() {
    String f[] = getFilesInDirectory(this.policyDirectoryPath, false);

    List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
    for (int i = 0; i < f.length; i++) {
      Map<String, Object> entry = new HashMap<String, Object>();
      entry
          .put("text", "<a href=\\\"" + f[i]
              + "\\\" onclick=\\\"return treeSelection(this);\\\">" + f[i]
              + "</a>");
      entry.put("id", f[i].toString());
      entry.put("expanded", false);
      entry.put("hasChildren", false);
    }

    return JSONArray.fromObject(items).toString();
  }

  public String getDatasetsByPolicyAsJSON(String dsCollection) {
    XMLRepositoryManager xmlRepo = getRepo(dsCollection);
    List<ProductType> types = safeGetProductTypes(xmlRepo);
    
    Iterator<ProductType> it = types.iterator();

    List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
    while (it.hasNext()) {
      ProductType t = it.next();
      Map<String, Object> entry = new HashMap<String, Object>();
      entry.put("text", "<a href=\\\"" + t.getName()
          + "\\\" onclick=\\\"return treeSelection(this);\\\">" + t.getName()
          + "</a>");
      entry.put("id", t.getName());
      entry.put("expanded", false);
      entry.put("hasChildren", false);
    }

    return JSONArray.fromObject(items).toString();
  }

  public String getMetFileAsJSON(String base, String file) {
    Map<String, Object> responseMap = new HashMap<String, Object>();
    if (file != null) {
      responseMap.put("file", file);
    }

    try {
      SerializableMetadata metadata = new SerializableMetadata("UTF-8", false);
      metadata
          .loadMetadataFromXmlStream(new FileInputStream(base + "/" + file));
      responseMap.put("metadata", metadata.getHashtable());
    } catch (InstantiationException e) {
      // no op
    } catch (FileNotFoundException e) {
      // no op
    } catch (IOException e) {
      // no op
    }

    JSONObject metJSON = JSONObject.fromObject(responseMap);
    return metJSON.toString();
  }

  public void writeMetFileFromJSON(String base, String json) {

  }

  /**
   * @return the stagingAreaPath
   */
  public String getStagingAreaPath() {
    return stagingAreaPath;
  }

  /**
   * @param stagingAreaPath
   *          the stagingAreaPath to set
   */
  public void setStagingAreaPath(String stagingAreaPath) {
    this.stagingAreaPath = stagingAreaPath;
  }

  /**
   * @return the policyDirectoryPath
   */
  public String getPolicyDirectoryPath() {
    return policyDirectoryPath;
  }

  /**
   * @param policyDirectoryPath
   *          the policyDirectoryPath to set
   */
  public void setPolicyDirectoryPath(String policyDirectoryPath) {
    this.policyDirectoryPath = policyDirectoryPath;
  }

  private List<ProductType> safeGetProductTypes(XMLRepositoryManager xmlRepo) {
    try {
      return xmlRepo.getProductTypes();
    } catch (Exception ignore) {
      return null;
    }

  }

  private XMLRepositoryManager getRepo(String dsCollection) {
    XMLRepositoryManager xmlRepo = null;
    String url = "file://" + this.policyDirectoryPath + "/" + dsCollection;

    try {
      xmlRepo = new XMLRepositoryManager(Collections.singletonList(url));
    } catch (Exception e) {
      LOG.log(Level.WARNING, "Error reading product types from: [" + url
          + "]: Message: " + e.getMessage());
    }

    return xmlRepo;
  }

  private String[] getFilesInDirectory(String directory, final boolean showFiles) {
    File dir = new File(directory);
    FilenameFilter filter = new FilenameFilter() {

      public boolean accept(File dir, String name) {

        return !name.startsWith(".")
            && (showFiles || !new File(dir, name).isFile());
      }
    };
    return dir.list(filter);
  }
}
