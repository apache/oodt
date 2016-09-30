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

package org.apache.oodt.cas.filemgr.util;

import org.apache.oodt.cas.filemgr.structs.BooleanQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.Element;
import org.apache.oodt.cas.filemgr.structs.ExtractorSpec;
import org.apache.oodt.cas.filemgr.structs.FileTransferStatus;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductPage;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.Query;
import org.apache.oodt.cas.filemgr.structs.QueryCriteria;
import org.apache.oodt.cas.filemgr.structs.RangeQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.TermQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.exceptions.QueryFormulationException;
import org.apache.oodt.cas.filemgr.structs.query.ComplexQuery;
import org.apache.oodt.cas.filemgr.structs.query.QueryFilter;
import org.apache.oodt.cas.filemgr.structs.query.QueryResult;
import org.apache.oodt.cas.filemgr.structs.query.filter.FilterAlgor;
import org.apache.oodt.cas.filemgr.structs.type.TypeHandler;
import org.apache.oodt.cas.metadata.Metadata;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 *          <p/>
 *          <p> A factory class for creating File Manager structures suitable for transfer over the XML-RPC pipe, and
 *          for reading objects from the XML-RPC pipe into File Manager structs. </p>
 */
@Deprecated
public final class XmlRpcStructFactory {

  private XmlRpcStructFactory() throws InstantiationException {
    throw new InstantiationException(
        "Don't instantiate XmlRpcStructFactories!");
  }

  public static Map<String, Object> getXmlRpcFileTransferStatus(
      FileTransferStatus status) {
    Map<String, Object> statusHash = new Hashtable<String, Object>();
    statusHash.put("bytesTransferred", Long.toString(status
        .getBytesTransferred()));
    statusHash.put("parentProduct", getXmlRpcProduct(status
        .getParentProduct()));
    statusHash.put("fileRef", getXmlRpcReference(status.getFileRef()));
    return statusHash;
  }

  @SuppressWarnings("unchecked")
  public static FileTransferStatus getFileTransferStatusFromXmlRpc(
      Map<String, Object> statusHash) {
    FileTransferStatus status = new FileTransferStatus();
    status.setBytesTransferred(Long.parseLong(statusHash
        .get("bytesTransferred").toString()));
    status.setParentProduct(getProductFromXmlRpc((Hashtable<String, Object>) statusHash.get("parentProduct")));
    status.setFileRef(getReferenceFromXmlRpc((Hashtable<String, Object>) statusHash.get("fileRef")));
    return status;
  }

  public static Vector<Map<String, Object>> getXmlRpcFileTransferStatuses(List<FileTransferStatus> statuses) {
    Vector<Map<String, Object>> statusVector = new Vector<Map<String, Object>>();

    if (statuses != null && statuses.size() > 0) {

      for (FileTransferStatus status : statuses) {
        statusVector.add(getXmlRpcFileTransferStatus(status));
      }
    }

    return statusVector;
  }

  public static List<FileTransferStatus> getFileTransferStatusesFromXmlRpc(Vector<Map<String, Object>> statusVector) {
    List<FileTransferStatus> statuses = new Vector<FileTransferStatus>();

    if (statusVector != null && statusVector.size() > 0) {
      for (Map<String, Object> statusHash : statusVector) {
        FileTransferStatus status = getFileTransferStatusFromXmlRpc(statusHash);
        statuses.add(status);
      }
    }

    return statuses;
  }

  public static Map<String, Object> getXmlRpcProductPage(ProductPage page) {
    Hashtable<String, Object> productPageHash = new Hashtable<String, Object>();
    productPageHash.put("totalPages", page.getTotalPages());
    productPageHash.put("pageNum", page.getPageNum());
    productPageHash.put("pageSize", page.getPageSize());
    productPageHash.put("pageProducts", getXmlRpcProductList(page
        .getPageProducts()));
    return productPageHash;
  }

  @SuppressWarnings("unchecked")
  public static ProductPage getProductPageFromXmlRpc(Map<String, Object> productPageHash) {
    ProductPage page = new ProductPage();
    page.setPageNum((Integer) productPageHash.get("pageNum"));
    page
        .setPageSize((Integer) productPageHash.get("pageSize"));
    page.setTotalPages((Integer) productPageHash.get("totalPages"));
    page.setPageProducts(getProductListFromXmlRpc((Vector<Map<String, Object>>) productPageHash
        .get("pageProducts")));
    return page;
  }

  public static Map<String, Object> getXmlRpcComplexQuery(ComplexQuery complexQuery) {
    Map<String, Object> complexQueryHash = getXmlRpcQuery(complexQuery);
    if (complexQuery.getReducedProductTypeNames() != null) {
      complexQueryHash
          .put("reducedProductTypeNames", new Vector<String>(complexQuery.getReducedProductTypeNames()));
    } else {
      complexQueryHash.put("reducedProductTypeNames", new Vector<String>());
    }
    if (complexQuery.getReducedMetadata() != null) {
      complexQueryHash.put("reducedMetadata", new Vector<String>(complexQuery.getReducedMetadata()));
    } else {
      complexQueryHash.put("reducedMetadata", new Vector<String>());
    }
    if (complexQuery.getSortByMetKey() != null) {
      complexQueryHash.put("sortByMetKey", complexQuery.getSortByMetKey());
    }
    if (complexQuery.getToStringResultFormat() != null) {
      complexQueryHash.put("toStringResultFormat", complexQuery.getToStringResultFormat());
    }
    if (complexQuery.getQueryFilter() != null) {
      complexQueryHash.put("queryFilter", getXmlRpcQueryFilter(complexQuery.getQueryFilter()));
    }
    return complexQueryHash;
  }

  @SuppressWarnings("unchecked")
  public static ComplexQuery getComplexQueryFromXmlRpc(Map<String, Object> complexQueryHash) {
    ComplexQuery complexQuery = new ComplexQuery();
    complexQuery.setCriteria(getQueryFromXmlRpc(complexQueryHash).getCriteria());
    if (((Vector<String>) complexQueryHash.get("reducedProductTypeNames")).size() > 0) {
      complexQuery.setReducedProductTypeNames((Vector<String>) complexQueryHash.get("reducedProductTypeNames"));
    }
    if (((Vector<String>) complexQueryHash.get("reducedMetadata")).size() > 0) {
      complexQuery.setReducedMetadata((Vector<String>) complexQueryHash.get("reducedMetadata"));
    }
    complexQuery.setSortByMetKey((String) complexQueryHash.get("sortByMetKey"));
    complexQuery.setToStringResultFormat((String) complexQueryHash.get("toStringResultFormat"));
    if (complexQueryHash.get("queryFilter") != null) {
      complexQuery.setQueryFilter(
          getQueryFilterFromXmlRpc((Map<String, Object>) complexQueryHash.get("queryFilter")));
    }
    return complexQuery;
  }

  public static Map<String, Object> getXmlRpcQueryFilter(QueryFilter queryFilter) {
    Map<String, Object> queryFilterHash = new ConcurrentHashMap<String, Object>();
    queryFilterHash.put("startDateTimeMetKey", queryFilter.getStartDateTimeMetKey());
    queryFilterHash.put("endDateTimeMetKey", queryFilter.getEndDateTimeMetKey());
    queryFilterHash.put("priorityMetKey", queryFilter.getPriorityMetKey());
    queryFilterHash.put("filterAlgor", getXmlRpcFilterAlgor(queryFilter.getFilterAlgor()));
    queryFilterHash.put("versionConverterClass", queryFilter.getConverter().getClass().getCanonicalName());
    return queryFilterHash;
  }

  public static QueryFilter getQueryFilterFromXmlRpc(Map<String, Object> queryFilterHash) {
    String startDateTimeMetKey = (String) queryFilterHash.get("startDateTimeMetKey");
    String endDateTimeMetKey = (String) queryFilterHash.get("endDateTimeMetKey");
    String priorityMetKey = (String) queryFilterHash.get("priorityMetKey");
    @SuppressWarnings("unchecked")
    FilterAlgor filterAlgor = getFilterAlgorFromXmlRpc((Map<String, Object>) queryFilterHash.get("filterAlgor"));
    QueryFilter queryFilter = new QueryFilter(startDateTimeMetKey, endDateTimeMetKey, priorityMetKey, filterAlgor);
    queryFilter.setConverter(GenericFileManagerObjectFactory
        .getVersionConverterFromClassName((String) queryFilterHash.get("versionConverterClass")));
    return queryFilter;
  }

  public static Map<String, Object> getXmlRpcFilterAlgor(FilterAlgor filterAlgor) {
    Map<String, Object> filterAlgorHash = new ConcurrentHashMap<String, Object>();
    filterAlgorHash.put("class", filterAlgor.getClass().getCanonicalName());
    filterAlgorHash.put("epsilon", Long.toString(filterAlgor.getEpsilon()));
    return filterAlgorHash;
  }

  public static FilterAlgor getFilterAlgorFromXmlRpc(Map<String, Object> filterAlgorHash) {
    FilterAlgor filterAlgor =
        GenericFileManagerObjectFactory.getFilterAlgorFromClassName((String) filterAlgorHash.get("class"));
    if (filterAlgor != null) {
      filterAlgor.setEpsilon(Long.parseLong((String) filterAlgorHash.get("epsilon")));
    }
    return filterAlgor;
  }

  public static Vector<Map<String, Object>> getXmlRpcQueryResults(List<QueryResult> queryResults) {
    Vector<Map<String, Object>> queryResultHashVector = new Vector<Map<String, Object>>();
    for (QueryResult queryResult : queryResults) {
      queryResultHashVector.add(getXmlRpcQueryResult(queryResult));
    }
    return queryResultHashVector;
  }

  public static List<QueryResult> getQueryResultsFromXmlRpc(Vector<Map<String, Object>> queryResultHashVector) {
    List<QueryResult> queryResults = new Vector<QueryResult>();
    for (Map<String, Object> queryResultHash : queryResultHashVector) {
      queryResults.add(getQueryResultFromXmlRpc(queryResultHash));
    }
    return queryResults;
  }

  public static Map<String, Object> getXmlRpcQueryResult(QueryResult queryResult) {
    Hashtable<String, Object> queryResultHash = new Hashtable<String, Object>();
    if (queryResult.getToStringFormat() != null) {
      queryResultHash.put("toStringFormat", queryResult.getToStringFormat());
    }
    queryResultHash.put("product", getXmlRpcProduct(queryResult.getProduct()));
    queryResultHash.put("metadata", queryResult.getMetadata().getHashTable());
    return queryResultHash;
  }


  @SuppressWarnings("unchecked")
  public static QueryResult getQueryResultFromXmlRpc(Map<String, Object> queryResultHash) {
    Product product = getProductFromXmlRpc((Map<String, Object>) queryResultHash.get("product"));
    Metadata metadata = new Metadata();
    metadata.addMetadata((Map<String, Object>) queryResultHash.get("metadata"));
    QueryResult queryResult = new QueryResult(product, metadata);
    queryResult.setToStringFormat((String) queryResultHash.get("toStringFormat"));
    return queryResult;
  }

  public static Map<String, Object> getXmlRpcProduct(Product product) {
    Map<String, Object> productHash = new Hashtable<String, Object>();
    if (product.getProductId() != null) {
      productHash.put("id", product.getProductId());
    }
    if (product.getProductName() != null) {
      productHash.put("name", product.getProductName());
    }
    if (product.getProductType() != null) {
      productHash.put("type", getXmlRpcProductType(product.getProductType()));
    }
    if (product.getProductStructure() != null) {
      productHash.put("structure", product.getProductStructure());
    }
    if (product.getTransferStatus() != null) {
      productHash.put("transferStatus", product.getTransferStatus());
    }
    if (product.getProductReferences() != null) {
      productHash.put("references", getXmlRpcReferences(product
          .getProductReferences()));
    }
    if (product.getRootRef() != null) {
      productHash.put("rootReference", getXmlRpcReference(product
          .getRootRef()));
    }
    return productHash;
  }

  @SuppressWarnings("unchecked")
  public static Product getProductFromXmlRpc(Map<?, ?> productHash) {
    Product product = new Product();
    product.setProductId((String) productHash.get("id"));
    product.setProductName((String) productHash.get("name"));
    if (productHash.get("type") != null) {
      product.setProductType(getProductTypeFromXmlRpc(
          (Map<String, Object>) productHash.get("type")));
    }
    product.setProductStructure((String) productHash.get("structure"));
    product.setTransferStatus((String) productHash.get("transferStatus"));
    if (productHash.get("references") != null) {
      product.setProductReferences(getReferencesFromXmlRpc(
          (Vector<Map<String, Object>>) productHash
              .get("references")));
    }
    if (productHash.get("rootReference") != null) {
      product.setRootRef(getReferenceFromXmlRpc(
          (Map<String, Object>) productHash.get("rootReference")));
    }
    return product;
  }

  public static List<Product> getProductListFromXmlRpc(Vector<Map<String, Object>> productVector) {
    List<Product> productList = new Vector<Product>();

    for (Map<String, Object> productHash : productVector) {
      Product product = getProductFromXmlRpc(productHash);
      productList.add(product);
    }

    return productList;
  }


  public static Vector<Map<String, Object>> getXmlRpcProductList(List<Product> products) {
    Vector<Map<String, Object>> productVector = new Vector<Map<String, Object>>();

    if (products == null) {
      return productVector;
    }

    for (Product product : products) {
      Map<String, Object> productHash = getXmlRpcProduct(product);
      productVector.add(productHash);
    }

    return productVector;
  }

  public static Vector<Map<String, Object>> getXmlRpcProductTypeList(List<ProductType> productTypes) {
    Vector<Map<String, Object>> productTypeVector = new Vector<Map<String, Object>>();

    if (productTypes == null) {
      return productTypeVector;
    }

    for (ProductType type : productTypes) {
      Map<String, Object> typeHash = getXmlRpcProductType(type);
      productTypeVector.add(typeHash);
    }
    return productTypeVector;
  }

  public static List<ProductType> getProductTypeListFromXmlRpc(Vector<Map<String, Object>> productTypeVector) {
    List<ProductType> productTypeList = new Vector<ProductType>();
    for (Map<String, Object> productTypeHash : productTypeVector) {
      ProductType type = getProductTypeFromXmlRpc(productTypeHash);
      productTypeList.add(type);
    }

    return productTypeList;
  }

  public static Map<String, Object> getXmlRpcProductType(ProductType type) {
    Map<String, Object> productTypeHash = new Hashtable<String, Object>();
    // TODO(bfoster): ProductType ID is currently required by XmlRpcFileManager.
    productTypeHash.put("id", type.getProductTypeId());
    if (type.getName() != null) {
      productTypeHash.put("name", type.getName());
    }
    if (type.getDescription() != null) {
      productTypeHash.put("description", type.getDescription());
    }
    if (type.getProductRepositoryPath() != null) {
      productTypeHash.put("repositoryPath", type.getProductRepositoryPath());
    }
    if (type.getVersioner() != null) {
      productTypeHash.put("versionerClass", type.getVersioner());
    }
    if (type.getTypeMetadata() != null) {
      productTypeHash.put("typeMetadata", type.getTypeMetadata().getHashTable());
    }
    if (type.getExtractors() != null) {
      productTypeHash.put("typeExtractors", getXmlRpcTypeExtractors(type.getExtractors()));
    }
    if (type.getHandlers() != null) {
      productTypeHash.put("typeHandlers", getXmlRpcTypeHandlers(type.getHandlers()));
    }
    return productTypeHash;
  }

  @SuppressWarnings("unchecked")
  public static ProductType getProductTypeFromXmlRpc(Map<String, Object> productTypeHash) {
    ProductType type = new ProductType();
    type.setDescription((String) productTypeHash.get("description"));
    type.setName((String) productTypeHash.get("name"));
    type.setProductRepositoryPath((String) productTypeHash.get("repositoryPath"));
    type.setProductTypeId((String) productTypeHash.get("id"));
    type.setVersioner((String) productTypeHash.get("versionerClass"));
    if (productTypeHash.get("typeMetadata") != null) {
      Metadata typeMet = new Metadata();
      typeMet.addMetadata((Map<String, Object>) productTypeHash.get("typeMetadata"));
      type.setTypeMetadata(typeMet);
    }
    if (productTypeHash.get("typeExtractors") != null) {
      type.setExtractors(getTypeExtractorsFromXmlRpc(
          (Vector<Map<String, Object>>) productTypeHash
              .get("typeExtractors")));
    }
    if (productTypeHash.get("typeHandlers") != null) {
      type.setHandlers(getTypeHandlersFromXmlRpc(
          (Vector<Map<String, Object>>) productTypeHash
              .get("typeHandlers")));
    }
    return type;
  }

  public static Vector<Map<String, Object>> getXmlRpcTypeExtractors(List<ExtractorSpec> extractors) {
    Vector<Map<String, Object>> extractorsVector = new Vector<Map<String, Object>>();

    if (extractors != null && extractors.size() > 0) {
      for (ExtractorSpec spec : extractors) {
        extractorsVector.add(getXmlRpcExtractorSpec(spec));
      }
    }

    return extractorsVector;
  }

  public static Map<String, Object> getXmlRpcExtractorSpec(ExtractorSpec spec) {
    Map<String, Object> extractorHash = new Hashtable<String, Object>();
    extractorHash.put("className", spec.getClassName());
    extractorHash.put("config",
        getXmlRpcProperties(spec.getConfiguration()));
    return extractorHash;
  }

  public static Vector<Map<String, Object>> getXmlRpcTypeHandlers(List<TypeHandler> typeHandlers) {
    Vector<Map<String, Object>> handlersVector = new Vector<Map<String, Object>>();

    if (typeHandlers != null && typeHandlers.size() > 0) {
      for (TypeHandler typeHandler : typeHandlers) {
        handlersVector.add(getXmlRpcTypeHandler(typeHandler));
      }
    }

    return handlersVector;
  }

  public static Map<String, Object> getXmlRpcTypeHandler(TypeHandler typeHandler) {
    Map<String, Object> handlerHash = new Hashtable<String, Object>();
    handlerHash.put("className", typeHandler != null ?
                                 typeHandler.getClass().getCanonicalName() : "");
    handlerHash.put("elementName", typeHandler != null ?
                                   typeHandler.getElementName() : "");
    return handlerHash;
  }

  public static List<ExtractorSpec> getTypeExtractorsFromXmlRpc(Vector<Map<String, Object>> extractorsVector) {
    List<ExtractorSpec> extractors = new Vector<ExtractorSpec>();

    if (extractorsVector != null && extractorsVector.size() > 0) {
      for (Map<String, Object> extractorSpecHash : extractorsVector) {
        extractors.add(getExtractorSpecFromXmlRpc(extractorSpecHash));
      }
    }

    return extractors;
  }

  @SuppressWarnings("unchecked")
  public static ExtractorSpec getExtractorSpecFromXmlRpc(
      Map<String, Object> extractorSpecHash) {
    ExtractorSpec spec = new ExtractorSpec();
    spec.setClassName((String) extractorSpecHash.get("className"));
    spec
        .setConfiguration(getPropertiesFromXmlRpc((Map<String, String>) extractorSpecHash
            .get("config")));
    return spec;
  }

  public static List<TypeHandler> getTypeHandlersFromXmlRpc(Vector<Map<String, Object>> handlersVector) {
    List<TypeHandler> handlers = new Vector<TypeHandler>();

    if (handlersVector != null && handlersVector.size() > 0) {
      for (Map<String, Object> typeHandlerHash : handlersVector) {
        handlers.add(getTypeHandlerFromXmlRpc(typeHandlerHash));
      }
    }

    return handlers;
  }

  public static TypeHandler getTypeHandlerFromXmlRpc(
      Map<String, Object> typeHandlerHash) {
    TypeHandler typeHandler = GenericFileManagerObjectFactory
        .getTypeHandlerFromClassName((String) typeHandlerHash.get("className"));
    if (typeHandler != null) {
      typeHandler.setElementName((String) typeHandlerHash.get("elementName"));
    }
    return typeHandler;
  }

  public static Properties getPropertiesFromXmlRpc(Map<String, String> propHash) {
    Properties props = new Properties();

    if (propHash != null && propHash.keySet().size() > 0) {
      for (Map.Entry<String, String> propName : propHash.entrySet()) {
        String propValue = propName.getValue();
        props.setProperty(propName.getKey(), propValue);
      }
    }

    return props;
  }

  public static Map<String, String> getXmlRpcProperties(Properties props) {
    Map<String, String> propHash = new Hashtable<String, String>();

    if (props != null && props.keySet().size() > 0) {
      for (Object o : props.keySet()) {
        String propName = (String) o;
        String propValue = props.getProperty(propName);
        propHash.put(propName, propValue);
      }
    }

    return propHash;
  }


  public static Vector<Map<String, Object>> getXmlRpcReferences(List<Reference> references) {
    Vector<Map<String, Object>> refVector = new Vector<Map<String, Object>>();

    if (references == null) {
      return refVector;
    }

    for (Reference reference : references) {
      Map<String, Object> refHash = getXmlRpcReference(reference);
      refVector.add(refHash);
    }

    return refVector;
  }

  public static List<Reference> getReferencesFromXmlRpc(Vector<Map<String, Object>> referenceVector) {
    List<Reference> references = new Vector<Reference>();
    for (Map<String, Object> aReferenceVector : referenceVector) {
      Reference r = getReferenceFromXmlRpc(aReferenceVector);
      references.add(r);
    }
    return references;
  }

  public static Map<String, Object> getXmlRpcReference(Reference reference) {
    Map<String, Object> referenceHash = new Hashtable<String, Object>();
    referenceHash.put("origReference", reference.getOrigReference());
    referenceHash.put("dataStoreReference", reference
                                                .getDataStoreReference() != null ? reference
                                                .getDataStoreReference() : "");
    referenceHash.put("fileSize",
        Long.toString(reference.getFileSize()));
    referenceHash.put("mimeType", (reference.getMimeType() == null) ? ""
                                                                    : reference.getMimeType().getName());
    return referenceHash;
  }

  public static Reference getReferenceFromXmlRpc(Map<String, Object> referenceHash) {
    Reference reference = new Reference();
    reference.setDataStoreReference((String) referenceHash
        .get("dataStoreReference"));
    reference.setOrigReference((String) referenceHash.get("origReference"));
    reference.setFileSize(Long.parseLong(referenceHash.get("fileSize").toString()));
    reference.setMimeType((String) referenceHash.get("mimeType"));
    return reference;
  }

  public static Reference getReferenceFromXmlRpcHashtable(Map<String, Object> referenceHash) {
    Reference reference = new Reference();
    reference.setDataStoreReference((String) referenceHash
        .get("dataStoreReference"));
    reference.setOrigReference((String) referenceHash.get("origReference"));
    reference.setFileSize(Long.parseLong(referenceHash.get("fileSize").toString()));
    reference.setMimeType((String) referenceHash.get("mimeType"));
    return reference;
  }

  public static Vector<Map<String, Object>> getXmlRpcElementListHashtable(List<Element> elementList) {
    Vector<Map<String, Object>> elementVector = new Vector<Map<String, Object>>(elementList.size());
    for (Element element : elementList) {
      Map<String, Object> elementHash = getXmlRpcElementHashTable(element);
      elementVector.add(elementHash);
    }
    return elementVector;
  }

  public static Vector<Map<String, Object>> getXmlRpcElementList(List<Element> elementList) {
    Vector<Map<String, Object>> elementVector = new Vector<Map<String, Object>>(elementList.size());
    for (Element element : elementList) {
      Map<String, Object> elementHash = getXmlRpcElement(element);
      elementVector.add(elementHash);
    }
    return elementVector;
  }

  public static List<Element> getElementListFromXmlRpc(Vector<Map<String, Object>> elementVector) {
    List<Element> elementList = new Vector<Element>(elementVector.size());
    for (Map<String, Object> elementHash : elementVector) {
      Element element = getElementFromXmlRpc(elementHash);
      elementList.add(element);
    }
    return elementList;
  }

  public static Map<String, Object> getXmlRpcElement(Element element) {
    Map<String, Object> elementHash = new HashMap<String, Object>();

    elementHash.put("id", element.getElementId());
    elementHash.put("name", element.getElementName());
    elementHash.put("dcElement", element.getDCElement() != null ? element
        .getDCElement() : "");
    elementHash.put("description",
        element.getDescription() != null ? element.getDescription()
                                         : "");

    return elementHash;
  }

  public static Map<String, Object> getXmlRpcElementHashTable(Element element) {
    Map<String, Object> elementHash = new Hashtable<String, Object>();

    elementHash.put("id", element.getElementId());
    elementHash.put("name", element.getElementName());
    elementHash.put("dcElement", element.getDCElement() != null ? element
        .getDCElement() : "");
    elementHash.put("description",
        element.getDescription() != null ? element.getDescription()
                                         : "");

    return elementHash;
  }

  public static Element getElementFromXmlRpc(Map<String, Object> elementHash) {
    Element element = new Element();
    element.setElementId((String) elementHash.get("id"));
    element.setElementName((String) elementHash.get("name"));
    element.setDescription((String) elementHash.get("description"));
    element.setDCElement((String) elementHash.get("dcElement"));

    return element;
  }

  public static Map<String, Object> getXmlRpcQuery(Query query) {
    Map<String, Object> queryHash = new Hashtable<String, Object>();
    Vector<Map<String, Object>> criteriaVector = getXmlRpcQueryCriteriaList(query.getCriteria());
    queryHash.put("criteria", criteriaVector);
    return queryHash;
  }

  public static Query getQueryFromXmlRpc(Map<String, Object> queryHash) {
    Query query = new Query();
    @SuppressWarnings("unchecked")
    List<QueryCriteria> criteria = getQueryCriteriaListFromXmlRpc((Vector<Map<String, Object>>) queryHash
        .get("criteria"));
    query.setCriteria(criteria);
    return query;
  }

  public static Vector<Map<String, Object>> getXmlRpcQueryCriteriaList(List<QueryCriteria> criteriaList) {
    Vector<Map<String, Object>> criteriaVector = new Vector<Map<String, Object>>(criteriaList.size());
    for (QueryCriteria criteria : criteriaList) {
      Map<String, Object> criteriaHash = getXmlRpcQueryCriteria(criteria);
      criteriaVector.add(criteriaHash);
    }

    return criteriaVector;
  }

  public static List<QueryCriteria> getQueryCriteriaListFromXmlRpc(Vector<Map<String, Object>> criteriaVector) {

    List<QueryCriteria> criteriaList = new Vector<QueryCriteria>(criteriaVector.size());
    for (Map<String, Object> criteriaHash : criteriaVector) {
      QueryCriteria criteria = getQueryCriteriaFromXmlRpc(criteriaHash);
      criteriaList.add(criteria);
    }
    return criteriaList;
  }

  public static Map<String, Object> getXmlRpcQueryCriteria(QueryCriteria criteria) {
    Map<String, Object> criteriaHash = new Hashtable<String, Object>();
    criteriaHash.put("class", criteria.getClass().getCanonicalName());

    if (criteria instanceof TermQueryCriteria) {
      criteriaHash.put("elementName", criteria.getElementName());
      criteriaHash.put("elementValue", ((TermQueryCriteria) criteria).getValue());
    } else if (criteria instanceof RangeQueryCriteria) {
      criteriaHash.put("elementName", criteria.getElementName());
      criteriaHash.put("elementStartValue", ((RangeQueryCriteria) criteria).getStartValue() != null ?
                                            ((RangeQueryCriteria) criteria).getStartValue() : "");
      criteriaHash.put("elementEndValue", ((RangeQueryCriteria) criteria).getEndValue() != null ?
                                          ((RangeQueryCriteria) criteria).getEndValue() : "");
      criteriaHash.put("inclusive", Boolean.toString(((RangeQueryCriteria) criteria).getInclusive()));
    } else if (criteria instanceof BooleanQueryCriteria) {
      BooleanQueryCriteria boolQuery = (BooleanQueryCriteria) criteria;
      criteriaHash.put("operator", boolQuery.getOperator());
      Vector<Map<String, Object>> termsHash = new Vector<Map<String, Object>>();
      List<QueryCriteria> terms = boolQuery.getTerms();

      for (QueryCriteria term : terms) {
        Map<String, Object> termHash = getXmlRpcQueryCriteria(term);
        termsHash.add(termHash);
      }
      criteriaHash.put("terms", termsHash);

    }
    return criteriaHash;
  }

  public static QueryCriteria getQueryCriteriaFromXmlRpc(Map<String, Object> criteriaHash) {
    QueryCriteria criteria = null;
    if (criteriaHash.get("class").equals(TermQueryCriteria.class.getCanonicalName())) {
      criteria = new TermQueryCriteria();
      criteria.setElementName((String) criteriaHash.get("elementName"));
      ((TermQueryCriteria) criteria).setValue((String) criteriaHash.get("elementValue"));
    } else if (criteriaHash.get("class").equals(RangeQueryCriteria.class.getCanonicalName())) {
      criteria = new RangeQueryCriteria();
      criteria.setElementName((String) criteriaHash.get("elementName"));
      String startVal = criteriaHash.get("elementStartValue").equals("") ?
                        null : (String) criteriaHash.get("elementStartValue");
      String endVal = criteriaHash.get("elementEndValue").equals("") ?
                      null : (String) criteriaHash.get("elementEndValue");
      ((RangeQueryCriteria) criteria).setStartValue(startVal);
      ((RangeQueryCriteria) criteria).setEndValue(endVal);
      ((RangeQueryCriteria) criteria).setInclusive(Boolean.parseBoolean((String) criteriaHash.get("inclusive")));
    } else if (criteriaHash.get("class").equals(BooleanQueryCriteria.class.getCanonicalName())) {
      criteria = new BooleanQueryCriteria();
      try {
        ((BooleanQueryCriteria) criteria).setOperator((Integer) criteriaHash.get("operator"));
      } catch (QueryFormulationException e) {
        System.out.println("Error generating Boolean Query.");
      }
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> terms = (List<Map<String, Object>>) criteriaHash.get("terms");
      for (Map<String, Object> term : terms) {
        QueryCriteria termCriteria = getQueryCriteriaFromXmlRpc(term);
        try {
          ((BooleanQueryCriteria) criteria).addTerm(termCriteria);
        } catch (QueryFormulationException e) {
          System.out.println("Error generating Boolean Query.");
        }
      }

    }

    return criteria;

  }

}
