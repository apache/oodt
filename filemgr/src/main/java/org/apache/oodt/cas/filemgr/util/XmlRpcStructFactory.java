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

//OODT imports
import org.apache.oodt.cas.filemgr.structs.BooleanQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.ExtractorSpec;
import org.apache.oodt.cas.filemgr.structs.FileTransferStatus;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductPage;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.Query;
import org.apache.oodt.cas.filemgr.structs.QueryCriteria;
import org.apache.oodt.cas.filemgr.structs.RangeQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.Element;
import org.apache.oodt.cas.filemgr.structs.TermQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.exceptions.QueryFormulationException;
import org.apache.oodt.cas.filemgr.structs.query.ComplexQuery;
import org.apache.oodt.cas.filemgr.structs.query.QueryFilter;
import org.apache.oodt.cas.filemgr.structs.query.QueryResult;
import org.apache.oodt.cas.filemgr.structs.query.filter.FilterAlgor;
import org.apache.oodt.cas.filemgr.structs.type.TypeHandler;
import org.apache.oodt.cas.metadata.Metadata;

//JDK imports
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;
import java.util.List;

/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * A factory class for creating File Manager structures suitable for transfer
 * over the XML-RPC pipe, and for reading objects from the XML-RPC pipe into
 * File Manager structs.
 * </p>
 * 
 */
public final class XmlRpcStructFactory {

    private XmlRpcStructFactory() throws InstantiationException {
        throw new InstantiationException(
                "Don't instantiate XmlRpcStructFactories!");
    }

    public static Hashtable<String, Object> getXmlRpcFileTransferStatus(
            FileTransferStatus status) {
        Hashtable<String, Object> statusHash = new Hashtable<String, Object>();
        statusHash.put("bytesTransferred",Long.toString(status
                .getBytesTransferred()));
        statusHash.put("parentProduct", getXmlRpcProduct(status
                .getParentProduct()));
        statusHash.put("fileRef", getXmlRpcReference(status.getFileRef()));
        return statusHash;
    }

    @SuppressWarnings("unchecked")
    public static FileTransferStatus getFileTransferStatusFromXmlRpc(
            Hashtable<String, Object> statusHash) {
        FileTransferStatus status = new FileTransferStatus();
        status.setBytesTransferred(Long.parseLong(statusHash
                .get("bytesTransferred").toString()));
        status.setParentProduct(getProductFromXmlRpc((Hashtable<String, Object>) statusHash.get("parentProduct")));
        status.setFileRef(getReferenceFromXmlRpc((Hashtable<String, Object>) statusHash.get("fileRef")));
        return status;
    }

    public static Vector<Hashtable<String, Object>> getXmlRpcFileTransferStatuses(List<FileTransferStatus> statuses) {
        Vector<Hashtable<String, Object>> statusVector = new Vector<Hashtable<String, Object>>();

        if (statuses != null && statuses.size() > 0) {

            for (Iterator<FileTransferStatus> i = statuses.iterator(); i.hasNext();) {
                FileTransferStatus status = i.next();
                statusVector.add(getXmlRpcFileTransferStatus(status));
            }
        }

        return statusVector;
    }

    public static List<FileTransferStatus> getFileTransferStatusesFromXmlRpc(Vector<Hashtable<String, Object>> statusVector) {
        List<FileTransferStatus> statuses = new Vector<FileTransferStatus>();

        if (statusVector != null && statusVector.size() > 0) {
            for (Iterator<Hashtable<String, Object>> i = statusVector.iterator(); i.hasNext();) {
                Hashtable<String, Object> statusHash = i.next();
                FileTransferStatus status = getFileTransferStatusFromXmlRpc(statusHash);
                statuses.add(status);
            }
        }

        return statuses;
    }

    public static Hashtable<String, Object> getXmlRpcProductPage(ProductPage page) {
        Hashtable<String, Object>productPageHash = new Hashtable<String, Object>();
        productPageHash.put("totalPages", new Integer(page.getTotalPages()));
        productPageHash.put("pageNum", new Integer(page.getPageNum()));
        productPageHash.put("pageSize", new Integer(page.getPageSize()));
        productPageHash.put("pageProducts", getXmlRpcProductList(page
                .getPageProducts()));
        return productPageHash;
    }

    @SuppressWarnings("unchecked")
    public static ProductPage getProductPageFromXmlRpc(Hashtable<String, Object> productPageHash) {
        ProductPage page = new ProductPage();
        page.setPageNum(((Integer) productPageHash.get("pageNum")).intValue());
        page
                .setPageSize(((Integer) productPageHash.get("pageSize"))
                        .intValue());
        page.setTotalPages(((Integer) productPageHash.get("totalPages"))
                .intValue());
        page.setPageProducts(getProductListFromXmlRpc((Vector<Hashtable<String, Object>>) productPageHash
                .get("pageProducts")));
        return page;
    }
    
    public static Hashtable<String, Object> getXmlRpcComplexQuery(ComplexQuery complexQuery) {
        Hashtable<String, Object> complexQueryHash = getXmlRpcQuery(complexQuery);
        if (complexQuery.getReducedProductTypeNames() != null)
            complexQueryHash.put("reducedProductTypeNames", new Vector<String>(complexQuery.getReducedProductTypeNames()));
        else 
            complexQueryHash.put("reducedProductTypeNames", new Vector<String>());
        if (complexQuery.getReducedMetadata() != null)
            complexQueryHash.put("reducedMetadata", new Vector<String>(complexQuery.getReducedMetadata()));
        else 
            complexQueryHash.put("reducedMetadata", new Vector<String>());
        if (complexQuery.getSortByMetKey() != null)
            complexQueryHash.put("sortByMetKey", complexQuery.getSortByMetKey());
        if (complexQuery.getToStringResultFormat() != null)
            complexQueryHash.put("toStringResultFormat", complexQuery.getToStringResultFormat());
        if (complexQuery.getQueryFilter() != null)
            complexQueryHash.put("queryFilter", getXmlRpcQueryFilter(complexQuery.getQueryFilter()));
        return complexQueryHash;
    }
    
    @SuppressWarnings("unchecked")
    public static ComplexQuery getComplexQueryFromXmlRpc(Hashtable<String, Object> complexQueryHash) {
        ComplexQuery complexQuery = new ComplexQuery();
        complexQuery.setCriteria(getQueryFromXmlRpc(complexQueryHash).getCriteria());
        if (((Vector<String>) complexQueryHash.get("reducedProductTypeNames")).size() > 0)
            complexQuery.setReducedProductTypeNames((Vector<String>) complexQueryHash.get("reducedProductTypeNames"));
        if (((Vector<String>) complexQueryHash.get("reducedMetadata")).size() > 0)
            complexQuery.setReducedMetadata((Vector<String>) complexQueryHash.get("reducedMetadata"));
        complexQuery.setSortByMetKey((String) complexQueryHash.get("sortByMetKey"));
        complexQuery.setToStringResultFormat((String) complexQueryHash.get("toStringResultFormat"));
        if (complexQueryHash.get("queryFilter") != null)
            complexQuery.setQueryFilter(getQueryFilterFromXmlRpc((Hashtable<String, Object>) complexQueryHash.get("queryFilter")));
        return complexQuery;
    }
    
    public static Hashtable<String, Object> getXmlRpcQueryFilter(QueryFilter queryFilter) {
        Hashtable<String, Object> queryFilterHash = new Hashtable<String, Object>();
        queryFilterHash.put("startDateTimeMetKey", queryFilter.getStartDateTimeMetKey());
        queryFilterHash.put("endDateTimeMetKey", queryFilter.getEndDateTimeMetKey());
        queryFilterHash.put("priorityMetKey", queryFilter.getPriorityMetKey());
        queryFilterHash.put("filterAlgor", getXmlRpcFilterAlgor(queryFilter.getFilterAlgor()));
        queryFilterHash.put("versionConverterClass", queryFilter.getConverter().getClass().getCanonicalName());
        return queryFilterHash;
    }
    
    public static QueryFilter getQueryFilterFromXmlRpc(Hashtable<String, Object> queryFilterHash) {
        String startDateTimeMetKey = (String) queryFilterHash.get("startDateTimeMetKey");
        String endDateTimeMetKey = (String) queryFilterHash.get("endDateTimeMetKey");
        String priorityMetKey = (String) queryFilterHash.get("priorityMetKey");
        @SuppressWarnings("unchecked")
        FilterAlgor filterAlgor = getFilterAlgorFromXmlRpc((Hashtable<String, Object>) queryFilterHash.get("filterAlgor"));
        QueryFilter queryFilter = new QueryFilter(startDateTimeMetKey, endDateTimeMetKey, priorityMetKey, filterAlgor);
        queryFilter.setConverter(GenericFileManagerObjectFactory.getVersionConverterFromClassName((String) queryFilterHash.get("versionConverterClass")));
        return queryFilter;
    }

    public static Hashtable<String, Object> getXmlRpcFilterAlgor(FilterAlgor filterAlgor) {
        Hashtable<String, Object> filterAlgorHash = new Hashtable<String, Object>();
        filterAlgorHash.put("class", filterAlgor.getClass().getCanonicalName());
        filterAlgorHash.put("epsilon", Long.toString(filterAlgor.getEpsilon()));
        return filterAlgorHash;
    }
    
    public static FilterAlgor getFilterAlgorFromXmlRpc(Hashtable<String, Object> filterAlgorHash) {
        FilterAlgor filterAlgor = GenericFileManagerObjectFactory.getFilterAlgorFromClassName((String) filterAlgorHash.get("class"));
        filterAlgor.setEpsilon(Long.parseLong((String) filterAlgorHash.get("epsilon")));
        return filterAlgor;
    }
    
    public static Vector<Hashtable<String, Object>> getXmlRpcQueryResults(List<QueryResult> queryResults) {
        Vector<Hashtable<String, Object>> queryResultHashVector = new Vector<Hashtable<String, Object>>();
        for (QueryResult queryResult : queryResults)
            queryResultHashVector.add(getXmlRpcQueryResult(queryResult));
        return queryResultHashVector;
    }
    
    public static List<QueryResult> getQueryResultsFromXmlRpc(Vector<Hashtable<String, Object>> queryResultHashVector) {
        List<QueryResult> queryResults = new Vector<QueryResult>();
        for (Hashtable<String, Object> queryResultHash : queryResultHashVector)
            queryResults.add(getQueryResultFromXmlRpc(queryResultHash));
        return queryResults;
    }
        
    public static Hashtable<String, Object> getXmlRpcQueryResult(QueryResult queryResult) {
        Hashtable<String, Object> queryResultHash = new Hashtable<String, Object>();
        if (queryResult.getToStringFormat() != null)
            queryResultHash.put("toStringFormat", queryResult.getToStringFormat());
        queryResultHash.put("product", getXmlRpcProduct(queryResult.getProduct()));
        queryResultHash.put("metadata", queryResult.getMetadata().getHashtable());
        return queryResultHash;
    }
    
    @SuppressWarnings("unchecked")
    public static QueryResult getQueryResultFromXmlRpc(Hashtable<String, Object> queryResultHash) {
        Product product = getProductFromXmlRpc((Hashtable<String, Object>) queryResultHash.get("product"));
        Metadata metadata = new Metadata();
        metadata.addMetadata((Hashtable<String, Object>) queryResultHash.get("metadata"));
        QueryResult queryResult = new QueryResult(product, metadata);
        queryResult.setToStringFormat((String) queryResultHash.get("toStringFormat"));
        return queryResult;
    }
    
    public static Hashtable<String, Object> getXmlRpcProduct(Product product) {
        Hashtable<String, Object> productHash = new Hashtable<String, Object>();
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
    public static Product getProductFromXmlRpc(Hashtable<?, ?> productHash) {
        Product product = new Product();
        product.setProductId((String) productHash.get("id"));
        product.setProductName((String) productHash.get("name"));
        if (productHash.get("type") != null) {
           product.setProductType(getProductTypeFromXmlRpc(
                 (Hashtable<String, Object>) productHash.get("type")));
        }
        product.setProductStructure((String) productHash.get("structure"));
        product.setTransferStatus((String) productHash.get("transferStatus"));
        if (productHash.get("references") != null) {
           product.setProductReferences(getReferencesFromXmlRpc(
                 (Vector<Hashtable<String, Object>>) productHash
                        .get("references")));
        }
        if (productHash.get("rootReference") != null) {
           product.setRootRef(getReferenceFromXmlRpc(
                 (Hashtable<String, Object>) productHash.get("rootReference")));
        }
        return product;
    }

    public static List<Product> getProductListFromXmlRpc(Vector<Hashtable<String, Object>> productVector) {
        List<Product> productList = new Vector<Product>();

        for (Iterator<Hashtable<String, Object>> i = productVector.iterator(); i.hasNext();) {
            Hashtable<String, Object> productHash = i.next();
            Product product = getProductFromXmlRpc(productHash);
            productList.add(product);
        }

        return productList;
    }

    public static Vector<Hashtable<String, Object>> getXmlRpcProductList(List<Product> products) {
        Vector<Hashtable<String, Object>> productVector = new Vector<Hashtable<String, Object>>();

        if (products == null) {
            return productVector;
        }

        for (Iterator<Product> i = products.iterator(); i.hasNext();) {
            Product product = i.next();
            Hashtable<String, Object> productHash = getXmlRpcProduct(product);
            productVector.add(productHash);
        }

        return productVector;
    }

    public static Vector<Hashtable<String, Object>> getXmlRpcProductTypeList(List<ProductType> productTypes) {
        Vector<Hashtable<String, Object>> productTypeVector = new Vector<Hashtable<String, Object>>();

        if (productTypes == null) {
            return productTypeVector;
        }

        for (Iterator<ProductType> i = productTypes.iterator(); i.hasNext();) {
            ProductType type = i.next();
            Hashtable<String, Object> typeHash = getXmlRpcProductType(type);
            productTypeVector.add(typeHash);
        }
        return productTypeVector;
    }

    public static List<ProductType> getProductTypeListFromXmlRpc(Vector<Hashtable<String, Object>> productTypeVector) {
        List<ProductType> productTypeList = new Vector<ProductType>();
        for (Iterator<Hashtable<String, Object>> i = productTypeVector.iterator(); i.hasNext();) {
            Hashtable<String, Object> productTypeHash = i.next();
            ProductType type = getProductTypeFromXmlRpc(productTypeHash);
            productTypeList.add(type);
        }

        return productTypeList;
    }

    public static Hashtable<String, Object> getXmlRpcProductType(ProductType type) {
        Hashtable<String, Object> productTypeHash = new Hashtable<String, Object>();
        // TODO(bfoster): ProductType ID is currently required by XmlRpcFileManagerServer.
        productTypeHash.put("id", type.getProductTypeId());
        if (type.getName() != null) {
           productTypeHash.put("name", type.getName());
        }
        if (type.getDescription() != null) {
           productTypeHash.put("description", type.getDescription());  
        }
        if (type.getProductRepositoryPath() != null) {
           productTypeHash.put("repositoryPath",type.getProductRepositoryPath());
        }
        if (type.getVersioner() != null) {
           productTypeHash.put("versionerClass", type.getVersioner());
        }
        if (type.getTypeMetadata() != null) {
           productTypeHash.put("typeMetadata", type.getTypeMetadata().getHashtable());
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
    public static ProductType getProductTypeFromXmlRpc(Hashtable<String, Object> productTypeHash) {
        ProductType type = new ProductType();
        type.setDescription((String) productTypeHash.get("description"));
        type.setName((String) productTypeHash.get("name"));
        type.setProductRepositoryPath((String) productTypeHash.get("repositoryPath"));
        type.setProductTypeId((String) productTypeHash.get("id"));
        type.setVersioner((String) productTypeHash.get("versionerClass"));
        if (productTypeHash.get("typeMetadata") != null) {
           Metadata typeMet = new Metadata();
           typeMet.addMetadata((Hashtable<String, Object>) productTypeHash.get("typeMetadata"));
           type.setTypeMetadata(typeMet);
        }
        if (productTypeHash.get("typeExtractors") != null) {
            type.setExtractors(getTypeExtractorsFromXmlRpc(
                  (Vector<Hashtable<String, Object>>) productTypeHash
                     .get("typeExtractors")));
        }
        if (productTypeHash.get("typeHandlers") != null) {
            type.setHandlers(getTypeHandlersFromXmlRpc(
                  (Vector<Hashtable<String, Object>>) productTypeHash
                        .get("typeHandlers")));
        }
        return type;
    }

    public static Vector<Hashtable<String, Object>> getXmlRpcTypeExtractors(List<ExtractorSpec> extractors) {
        Vector<Hashtable<String, Object>> extractorsVector = new Vector<Hashtable<String, Object>>();

        if (extractors != null && extractors.size() > 0) {
            for (Iterator<ExtractorSpec> i = extractors.iterator(); i.hasNext();) {
                ExtractorSpec spec = i.next();
                extractorsVector.add(getXmlRpcExtractorSpec(spec));
            }
        }

        return extractorsVector;
    }

    public static Hashtable<String, Object> getXmlRpcExtractorSpec(ExtractorSpec spec) {
        Hashtable<String, Object> extractorHash = new Hashtable<String, Object>();
        extractorHash.put("className", spec.getClassName());
        extractorHash.put("config",
                getXmlRpcProperties(spec.getConfiguration()));
        return extractorHash;
    }
    
    public static Vector<Hashtable<String, Object>> getXmlRpcTypeHandlers(List<TypeHandler> typeHandlers) {
        Vector<Hashtable<String, Object>> handlersVector = new Vector<Hashtable<String, Object>>();

        if (typeHandlers != null && typeHandlers.size() > 0) {
            for (Iterator<TypeHandler> i = typeHandlers.iterator(); i.hasNext();) {
                TypeHandler typeHandler = i.next();
                handlersVector.add(getXmlRpcTypeHandler(typeHandler));
            }
        }

        return handlersVector;
    }
    
    public static Hashtable<String, Object> getXmlRpcTypeHandler(TypeHandler typeHandler) {
        Hashtable<String, Object> handlerHash = new Hashtable<String, Object>();
        handlerHash.put("className", typeHandler != null ? 
            typeHandler.getClass().getCanonicalName():"");
        handlerHash.put("elementName", typeHandler != null ? 
            typeHandler.getElementName():"");
        return handlerHash;
    }

    public static List<ExtractorSpec> getTypeExtractorsFromXmlRpc(Vector<Hashtable<String, Object>> extractorsVector) {
        List<ExtractorSpec> extractors = new Vector<ExtractorSpec>();

        if (extractorsVector != null && extractorsVector.size() > 0) {
            for (Iterator<Hashtable<String, Object>> i = extractorsVector.iterator(); i.hasNext();) {
                Hashtable<String, Object> extractorSpecHash = i.next();
                extractors.add(getExtractorSpecFromXmlRpc(extractorSpecHash));
            }
        }

        return extractors;
    }

    @SuppressWarnings("unchecked")
    public static ExtractorSpec getExtractorSpecFromXmlRpc(
            Hashtable<String, Object> extractorSpecHash) {
        ExtractorSpec spec = new ExtractorSpec();
        spec.setClassName((String) extractorSpecHash.get("className"));
        spec
                .setConfiguration(getPropertiesFromXmlRpc((Hashtable<String, String>) extractorSpecHash
                        .get("config")));
        return spec;
    }
    
    public static List<TypeHandler> getTypeHandlersFromXmlRpc(Vector<Hashtable<String, Object>> handlersVector) {
        List<TypeHandler> handlers = new Vector<TypeHandler>();

        if (handlersVector != null && handlersVector.size() > 0) {
            for (Iterator<Hashtable<String, Object>> i = handlersVector.iterator(); i.hasNext();) {
                Hashtable<String, Object> typeHandlerHash = i.next();
                handlers.add(getTypeHandlerFromXmlRpc(typeHandlerHash));
            }
        }

        return handlers;
    }
    
    public static TypeHandler getTypeHandlerFromXmlRpc(
            Hashtable<String, Object> typeHandlerHash) {
        TypeHandler typeHandler = GenericFileManagerObjectFactory
            .getTypeHandlerFromClassName((String) typeHandlerHash.get("className"));
        if(typeHandler != null)
          typeHandler.setElementName((String) typeHandlerHash.get("elementName"));
        return typeHandler;
    }

    public static Properties getPropertiesFromXmlRpc(Hashtable<String, String> propHash) {
        Properties props = new Properties();

        if (propHash != null && propHash.keySet().size() > 0) {
            for (Iterator<String> i = propHash.keySet().iterator(); i.hasNext();) {
                String propName = i.next();
                String propValue = propHash.get(propName);
                props.setProperty(propName, propValue);
            }
        }

        return props;
    }

    public static Hashtable<String, String> getXmlRpcProperties(Properties props) {
        Hashtable<String, String> propHash = new Hashtable<String, String>();

        if (props != null && props.keySet().size() > 0) {
            for (Iterator<Object> i = props.keySet().iterator(); i.hasNext();) {
                String propName = (String) i.next();
                String propValue = props.getProperty(propName);
                propHash.put(propName, propValue);
            }
        }

        return propHash;
    }

    public static Vector<Hashtable<String, Object>> getXmlRpcReferences(List<Reference> references) {
        Vector<Hashtable<String, Object>> refVector = new Vector<Hashtable<String, Object>>();

        if (references == null) {
            return refVector;
        }

        for (Iterator<Reference> i = references.iterator(); i.hasNext();) {
            Hashtable<String, Object> refHash = getXmlRpcReference(i.next());
            refVector.add(refHash);
        }

        return refVector;
    }

    public static List<Reference> getReferencesFromXmlRpc(Vector<Hashtable<String, Object>> referenceVector) {
        List<Reference> references = new Vector<Reference>();
        for (Iterator<Hashtable<String, Object>> i = referenceVector.iterator(); i.hasNext();) {
            Reference r = getReferenceFromXmlRpc(i.next());
            references.add(r);
        }
        return references;
    }

    public static Hashtable<String, Object> getXmlRpcReference(Reference reference) {
        Hashtable<String, Object> referenceHash = new Hashtable<String, Object>();
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

    public static Reference getReferenceFromXmlRpc(Hashtable<String, Object> referenceHash) {
        Reference reference = new Reference();
        reference.setDataStoreReference((String) referenceHash
                .get("dataStoreReference"));
        reference.setOrigReference((String) referenceHash.get("origReference"));
        reference.setFileSize(Long.parseLong(referenceHash.get("fileSize").toString()));
        reference.setMimeType((String) referenceHash.get("mimeType"));
        return reference;
    }

    public static Vector<Hashtable<String, Object>> getXmlRpcElementList(List<Element> elementList) {
        Vector<Hashtable<String, Object>> elementVector = new Vector<Hashtable<String, Object>>(elementList.size());
        for (Iterator<Element> i = elementList.iterator(); i.hasNext();) {
            Element element = i.next();
            Hashtable<String, Object> elementHash = getXmlRpcElement(element);
            elementVector.add(elementHash);
        }
        return elementVector;
    }

    public static List<Element> getElementListFromXmlRpc(Vector<Hashtable<String, Object>> elementVector) {
        List<Element> elementList = new Vector<Element>(elementVector.size());
        for (Iterator<Hashtable<String, Object>> i = elementVector.iterator(); i.hasNext();) {
            Hashtable<String, Object> elementHash = i.next();
            Element element = getElementFromXmlRpc(elementHash);
            elementList.add(element);
        }
        return elementList;
    }

    public static Hashtable<String, Object> getXmlRpcElement(Element element) {
        Hashtable<String, Object> elementHash = new Hashtable<String, Object>();

        elementHash.put("id", element.getElementId());
        elementHash.put("name", element.getElementName());
        elementHash.put("dcElement", element.getDCElement() != null ? element
                .getDCElement() : "");
        elementHash.put("description",
                element.getDescription() != null ? element.getDescription()
                        : "");

        return elementHash;
    }

    public static Element getElementFromXmlRpc(Hashtable<String, Object> elementHash) {
        Element element = new Element();
        element.setElementId((String) elementHash.get("id"));
        element.setElementName((String) elementHash.get("name"));
        element.setDescription((String) elementHash.get("description"));
        element.setDCElement((String) elementHash.get("dcElement"));

        return element;
    }

    public static Hashtable<String, Object> getXmlRpcQuery(Query query) {
        Hashtable<String, Object> queryHash = new Hashtable<String, Object>();
        Vector<Hashtable<String, Object>> criteriaVector = getXmlRpcQueryCriteriaList(query.getCriteria());
        queryHash.put("criteria", criteriaVector);
        return queryHash;
    }

    public static Query getQueryFromXmlRpc(Hashtable<String, Object> queryHash) {
        Query query = new Query();
        @SuppressWarnings("unchecked")
        List<QueryCriteria> criteria = getQueryCriteriaListFromXmlRpc((Vector<Hashtable<String, Object>>) queryHash
                .get("criteria"));
        query.setCriteria(criteria);
        return query;
    }

    public static Vector<Hashtable<String, Object>> getXmlRpcQueryCriteriaList(List<QueryCriteria> criteriaList) {
        Vector<Hashtable<String, Object>> criteriaVector = new Vector<Hashtable<String, Object>>(criteriaList.size());
        for (Iterator<QueryCriteria> i = criteriaList.iterator(); i.hasNext();) {
            QueryCriteria criteria = i.next();
            Hashtable<String, Object> criteriaHash = getXmlRpcQueryCriteria(criteria);
            criteriaVector.add(criteriaHash);
        }

        return criteriaVector;
    }

    public static List<QueryCriteria> getQueryCriteriaListFromXmlRpc(Vector<Hashtable<String, Object>> criteriaVector) {

        List<QueryCriteria> criteriaList = new Vector<QueryCriteria>(criteriaVector.size());
        for (Iterator<Hashtable<String, Object>> i = criteriaVector.iterator(); i.hasNext();) {
            Hashtable<String, Object> criteriaHash = i.next();
            QueryCriteria criteria = getQueryCriteriaFromXmlRpc(criteriaHash);
            criteriaList.add(criteria);
        }
        return criteriaList;
    }

    public static Hashtable<String, Object> getXmlRpcQueryCriteria(QueryCriteria criteria) {
        Hashtable<String, Object> criteriaHash = new Hashtable<String, Object>();
        criteriaHash.put("class",criteria.getClass().getCanonicalName());
        
        if(criteria instanceof TermQueryCriteria){  
            criteriaHash.put("elementName", criteria.getElementName());
            criteriaHash.put("elementValue", ((TermQueryCriteria)criteria).getValue());
        } else if(criteria instanceof RangeQueryCriteria){
            criteriaHash.put("elementName", criteria.getElementName());
            criteriaHash.put("elementStartValue", ((RangeQueryCriteria)criteria).getStartValue() != null ?
                    ((RangeQueryCriteria)criteria).getStartValue():"");
            criteriaHash.put("elementEndValue", ((RangeQueryCriteria)criteria).getEndValue() != null ?
                    ((RangeQueryCriteria)criteria).getEndValue():"");
            criteriaHash.put("inclusive", Boolean.toString(((RangeQueryCriteria) criteria).getInclusive())); 
        } else if(criteria instanceof BooleanQueryCriteria){
            BooleanQueryCriteria boolQuery = (BooleanQueryCriteria) criteria;
            criteriaHash.put("operator", new Integer(boolQuery.getOperator()));
            Vector<Hashtable<String, Object>> termsHash = new Vector<Hashtable<String, Object>>();
            List<QueryCriteria> terms = boolQuery.getTerms();
            
            for(int i=0;i<terms.size();i++){
                QueryCriteria term = terms.get(i);
                Hashtable<String, Object> termHash = getXmlRpcQueryCriteria(term);
                termsHash.add(termHash);
            }
            criteriaHash.put("terms", termsHash);
            
        } else {
            //should not happen
        }
        return criteriaHash;
    }
    
    public static QueryCriteria getQueryCriteriaFromXmlRpc(Hashtable<String, Object> criteriaHash) {
        QueryCriteria criteria = null;
        if(((String)criteriaHash.get("class")).equals(TermQueryCriteria.class.getCanonicalName())){
            criteria = new TermQueryCriteria();
            criteria.setElementName((String) criteriaHash.get("elementName"));
            ((TermQueryCriteria)criteria).setValue((String) criteriaHash.get("elementValue"));
        } else if(((String)criteriaHash.get("class")).equals(RangeQueryCriteria.class.getCanonicalName())){
            criteria = new RangeQueryCriteria();
            criteria.setElementName((String) criteriaHash.get("elementName"));
            String startVal = criteriaHash.get("elementStartValue").equals("") ? 
                    null : (String)criteriaHash.get("elementStartValue");
            String endVal = criteriaHash.get("elementEndValue").equals("") ?
                    null : (String)criteriaHash.get("elementEndValue");
            ((RangeQueryCriteria)criteria).setStartValue(startVal);
            ((RangeQueryCriteria)criteria).setEndValue(endVal);
            ((RangeQueryCriteria)criteria).setInclusive(Boolean.parseBoolean((String) criteriaHash.get("inclusive")));
        } else if(((String)criteriaHash.get("class")).equals(BooleanQueryCriteria.class.getCanonicalName())){
            criteria = new BooleanQueryCriteria();
            try{
              ((BooleanQueryCriteria)criteria).setOperator( ((Integer)criteriaHash.get("operator")).intValue() );
            } catch (QueryFormulationException e){
                System.out.println("Error generating Boolean Query.");
            }
            @SuppressWarnings("unchecked")
            List<Hashtable<String, Object>> terms = (List<Hashtable<String, Object>>) criteriaHash.get("terms");
            for(int i=0;i<terms.size();i++){
                Hashtable<String, Object> term = terms.get(i);
                QueryCriteria termCriteria = getQueryCriteriaFromXmlRpc(term);
                try{
                    ((BooleanQueryCriteria)criteria).addTerm(termCriteria);
                } catch (QueryFormulationException e){
                    System.out.println("Error generating Boolean Query.");
                }
            }
            
        }

        return criteria;        
        
    }

}
