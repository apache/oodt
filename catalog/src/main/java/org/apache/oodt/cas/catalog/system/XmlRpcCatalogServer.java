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

package org.apache.oodt.cas.catalog.system;

//JDK imports
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Set;

//Apache imports
import org.apache.xmlrpc.WebServer;

//OODT imports
import org.apache.oodt.cas.catalog.metadata.TransactionalMetadata;
import org.apache.oodt.cas.catalog.pagination.CatalogReceipt;
import org.apache.oodt.cas.catalog.pagination.Page;
import org.apache.oodt.cas.catalog.pagination.PageInfo;
import org.apache.oodt.cas.catalog.pagination.QueryPager;
import org.apache.oodt.cas.catalog.query.QueryExpression;
import org.apache.oodt.cas.catalog.struct.Dictionary;
import org.apache.oodt.cas.catalog.struct.Index;
import org.apache.oodt.cas.catalog.struct.TransactionId;
import org.apache.oodt.cas.catalog.system.Catalog;
import org.apache.oodt.cas.metadata.Metadata;

/**
 * 
 * The XML-RPC Catalog Server.
 * 
 */
public class XmlRpcCatalogServer {

  private WebServer webServer;

  public XmlRpcCatalogServer() {
    super();
  }

  public void startup() throws Exception {
    this.webServer = new WebServer(this.port);
    this.webServer.addHandler(this.getClass().getSimpleName(), this);
    this.webServer.start();
  }

  public boolean shutdown() throws Exception {
    this.shutdown();
    this.webServer.shutdown();
    this.webServer = null;
    return true;
  }

  public boolean addCatalog(String catalogObject) throws Exception {
    this.addCatalog(this.serializer.deserializeObject(Catalog.class,
        catalogObject));
    return true;
  }

  public boolean replaceCatalog(String catalogObject) throws Exception {
    this.replaceCatalog(this.serializer.deserializeObject(Catalog.class,
        catalogObject));
    return true;
  }

  public boolean addCatalog(String catalogId, String indexObject)
      throws Exception {
    this.addCatalog(catalogId, this.serializer.deserializeObject(Index.class,
        indexObject));
    return true;
  }

  public boolean addCatalog(String catalogId, String indexObject,
      String dictionariesObject) throws Exception {
    this.addCatalog(catalogId, this.serializer.deserializeObject(Index.class,
        indexObject), this.serializer.deserializeObject(List.class,
        dictionariesObject));
    return true;
  }

  public boolean addCatalog(String catalogId, String indexObject,
      String dictionariesObject, String restrictQueryPermissionObject,
      String restrictIngestPermissionObject) throws Exception {
    this.addCatalog(catalogId, this.serializer.deserializeObject(Index.class,
        indexObject), this.serializer.deserializeObject(List.class,
        dictionariesObject), this.serializer.deserializeObject(Boolean.class,
        restrictQueryPermissionObject), this.serializer.deserializeObject(
        Boolean.class, restrictIngestPermissionObject));
    return true;
  }

  public boolean addDictionary(String catalogId, String dictionaryObject)
      throws Exception {
    this.addDictionary(catalogId, this.serializer.deserializeObject(
        Dictionary.class, dictionaryObject));
    return true;
  }

  public boolean replaceDictionaries(String catalogId, String dictionariesObject)
      throws Exception {
    this.replaceDictionaries(catalogId, this.serializer.deserializeObject(
        List.class, dictionariesObject));
    return true;
  }

  public boolean replaceIndex(String catalogId, String indexObject)
      throws Exception {
    this.replaceIndex(catalogId, this.serializer.deserializeObject(Index.class,
        indexObject));
    return true;
  }

  public boolean modifyIngestPermission(String catalogId,
      String restrictIngestPermissionObject) throws Exception {
    this.modifyIngestPermission(catalogId, this.serializer.deserializeObject(
        Boolean.class, restrictIngestPermissionObject));
    return true;
  }

  public boolean modifyQueryPermission(String catalogId,
      String restrictQueryPermissionObject) throws Exception {
    this.modifyIngestPermission(catalogId, this.serializer.deserializeObject(
        Boolean.class, restrictQueryPermissionObject));
    return true;
  }

  public boolean delete(String metadataObject) throws Exception {
    this.delete(this.serializer.deserializeObject(Metadata.class,
        metadataObject));
    return true;
  }

  public String getPluginUrls() throws Exception {
    return this.serializer.serializeObject(this.getPluginUrls());
  }

  public boolean addPluginUrls(String pluginUrlsObject) throws Exception {
    this.addPluginUrls(this.serializer.deserializeObject(List.class,
        pluginUrlsObject));
    return true;
  }

  public String getPluginStorageDir() throws Exception {
    return this.serializer.serializeObject(this.getPluginStorageDir());
  }

  public boolean transferFile(String filePath, byte[] fileData, int offset,
      int numBytes) throws Exception {
    FileOutputStream fOut = null;
    try {
      File outFile = new File(filePath);
      if (outFile.exists())
        fOut = new FileOutputStream(outFile, true);
      else
        fOut = new FileOutputStream(outFile, false);

      fOut.write(fileData, (int) offset, (int) numBytes);
    } catch (Exception e) {
      throw e;
    } finally {
      try {
        fOut.close();
      } catch (Exception e) {
      }
    }
    return true;
  }

  public String getAllPages(String queryPagerObject) throws Exception {
    return this.serializer.serializeObject(this.getAllPages(this.serializer
        .deserializeObject(QueryPager.class, queryPagerObject)));
  }

  public String getCalalogProperties() throws Exception {
    return this.serializer.serializeObject(this.getCalalogProperties());
  }

  public String getCalalogProperties(String catalogUrn) throws Exception {
    return this.serializer.serializeObject(this
        .getCalalogProperties(catalogUrn));
  }

  public String getCatalogServiceTransactionId(
      String catalogTransactionIdObject, String catalogUrn) throws Exception {
    return this.serializer.serializeObject(this.getCatalogServiceTransactionId(
        this.serializer.deserializeObject(TransactionId.class,
            catalogTransactionIdObject), catalogUrn));
  }

  public String getCatalogServiceTransactionId2(String catalogReceiptObject,
      String generateNewObject) throws Exception {
    return this.serializer.serializeObject(this.getCatalogServiceTransactionId(
        this.serializer.deserializeObject(CatalogReceipt.class,
            catalogReceiptObject), this.serializer.deserializeObject(
            Boolean.class, generateNewObject)));
  }

  public String getCatalogServiceTransactionIds(
      String catalogTransactionIdsObject, String catalogUrn) throws Exception {
    return this.serializer.serializeObject(this
        .getCatalogServiceTransactionIds(this.serializer.deserializeObject(
            List.class, catalogTransactionIdsObject), catalogUrn));
  }

  public String getCurrentCatalogIds() throws Exception {
    return this.serializer.serializeObject(this.getCurrentCatalogIds());
  }

  public String getMetadataFromTransactionIdStrings(
      String catalogServiceTransactionIdStringsObject) throws Exception {
    return this.serializer.serializeObject(this
        .getMetadataFromTransactionIdStrings(this.serializer.deserializeObject(
            List.class, catalogServiceTransactionIdStringsObject)));
  }

  public String getMetadataFromTransactionIds(
      String catalogServiceTransactionIdsObject) throws Exception {
    return this.serializer.serializeObject(this
        .getMetadataFromTransactionIds(this.serializer.deserializeObject(
            List.class, catalogServiceTransactionIdsObject)));
  }

  public String getNextPage(String queryPagerObject) throws Exception {
    return this.serializer.serializeObject(this.getNextPage(this.serializer
        .deserializeObject(QueryPager.class, queryPagerObject)));
  }

  public String getProperty(String key) throws Exception {
    return this.serializer.serializeObject(this.getProperty(key));
  }

  public String ingest(String metadataObject) throws Exception {
    return this.serializer.serializeObject(this.ingest(this.serializer
        .deserializeObject(Metadata.class, metadataObject)));
  }

  public String isRestrictIngestPermissions() throws Exception {
    return this.serializer.serializeObject(new Boolean(this
        .isRestrictIngestPermissions()));
  }

  public String isRestrictQueryPermissions() throws Exception {
    return this.serializer.serializeObject(new Boolean(this
        .isRestrictQueryPermissions()));
  }

  public String query(String queryExpressionObject) throws Exception {
    return this.serializer.serializeObject(this.query(this.serializer
        .deserializeObject(QueryExpression.class, queryExpressionObject)));
  }

  public String query(String queryExpressionObject, String catalogIdsObject)
      throws Exception {
    return this.serializer.serializeObject(this.query(this.serializer
        .deserializeObject(QueryExpression.class, queryExpressionObject),
        this.serializer.deserializeObject(Set.class, catalogIdsObject)));
  }

  public String getNextPage2(String pageObject) throws Exception {
    return this.serializer.serializeObject(this.getNextPage(this.serializer
        .deserializeObject(Page.class, pageObject)));
  }

  public String getPage(String pageInfoObject, String queryExpressionObject)
      throws Exception {
    return this.serializer.serializeObject(this.getPage(this.serializer
        .deserializeObject(PageInfo.class, pageInfoObject), this.serializer
        .deserializeObject(QueryExpression.class, queryExpressionObject)));
  }

  public String getPage(String pageInfoObject, String queryExpressionObject,
      String catalogIdsObject) throws Exception {
    return this.serializer.serializeObject(this.getPage(this.serializer
        .deserializeObject(PageInfo.class, pageInfoObject), this.serializer
        .deserializeObject(QueryExpression.class, queryExpressionObject),
        this.serializer.deserializeObject(Set.class, catalogIdsObject)));
  }

  public String getMetadata(String pageObject) throws Exception {
    return this.serializer.serializeObject(this.getMetadata(this.serializer
        .deserializeObject(Page.class, pageObject)));
  }

  public boolean removeCatalog(String catalogUrn) throws Exception {
    this.removeCatalog(catalogUrn);
    return true;
  }

}
