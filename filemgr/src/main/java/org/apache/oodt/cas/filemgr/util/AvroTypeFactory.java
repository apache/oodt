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

import org.apache.oodt.cas.filemgr.structs.*;
import org.apache.oodt.cas.filemgr.structs.avrotypes.*;
import org.apache.oodt.cas.filemgr.structs.exceptions.QueryFormulationException;
import org.apache.oodt.cas.filemgr.structs.query.ComplexQuery;
import org.apache.oodt.cas.filemgr.structs.query.QueryFilter;
import org.apache.oodt.cas.filemgr.structs.query.QueryResult;
import org.apache.oodt.cas.filemgr.structs.query.filter.FilterAlgor;
import org.apache.oodt.cas.filemgr.structs.type.TypeHandler;
import org.apache.oodt.cas.metadata.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author radu
 *
 * <p>
 * A factory class for creating File Manager Avro types suitable for transfer
 * over the Avro IPC pipe.
 * </p>
 */
public class AvroTypeFactory {

    private static final Logger logger = LoggerFactory.getLogger(AvroTypeFactory.class);

    public static AvroReference getAvroReference(Reference reference){

        AvroReference avroReference = new AvroReference();

        avroReference.setOrigReference(reference.getOrigReference());

        if (reference.getDataStoreReference() != null)
            avroReference.setDataStoreReference(reference.getDataStoreReference());
        else
            avroReference.setDataStoreReference("");

        avroReference.setFileSize(reference.getFileSize());

        if(reference.getMimeType() != null)
            avroReference.setMimeTypeName(reference.getMimeType().getName());
        return avroReference;
    }

    public static Reference getReference(AvroReference avroReference) {
        Reference reference = new Reference();

        reference.setOrigReference(avroReference.getOrigReference());
        reference.setDataStoreReference(avroReference.getDataStoreReference());
        reference.setFileSize(avroReference.getFileSize());
        reference.setMimeType(avroReference.getMimeTypeName());

        return reference;

    }

    public static AvroExtractorSpec getAvroExtractorSpec(ExtractorSpec extractorSpec){

        AvroExtractorSpec avroExtractorSpec = new AvroExtractorSpec();

        avroExtractorSpec.setClassName(extractorSpec.getClassName());

        Properties props = extractorSpec.getConfiguration();
        if (props != null && props.keySet().size() > 0)
            avroExtractorSpec.setConfiguration((Map) props);

        return avroExtractorSpec;
    }

    public static ExtractorSpec getExtractorSpec(AvroExtractorSpec avroExtractorSpec){
        Properties properties = new Properties();

        Map configuration = avroExtractorSpec.getConfiguration();
        if (configuration != null && configuration.keySet().size() > 0) {
            for (Iterator<String> i = configuration.keySet().iterator(); i.hasNext();) {
                String propName = i.next();
                String propValue =(String) configuration.get(propName);
                properties.setProperty(propName, propValue);
            }
        }

        return new ExtractorSpec(avroExtractorSpec.getClassName(),properties);
    }

    public static AvroMetadata getAvroMetadata(Metadata metadata){

        AvroMetadata avroMetadata = new AvroMetadata();
        Hashtable met = metadata.getHashTable();
        Map<String,List<String>> hashMapMetadata = new HashMap<String,List<String>>();

        if (met != null && met.size() > 0){
            for (Iterator i = met.keySet().iterator(); i.hasNext();){
                String key =(String) i.next();
                hashMapMetadata.put(key, (List<String>) met.get(key));
            }
        }
        return new AvroMetadata(hashMapMetadata);
    }
    public static Metadata getMetadata(AvroMetadata metadata){
        Metadata met = new Metadata();

        Map<String,List<String>> hashMapMet = metadata.getTableMetadata();

        if (hashMapMet != null){
            Iterator iMet = hashMapMet.keySet().iterator();
            while(iMet.hasNext()){
                String i =(String) iMet.next();
                met.addMetadata(i,hashMapMet.get(i));
            }
        }

        return met;

    }

    public static AvroTypeHandler getAvroTypeHandler(TypeHandler typeHandler){
        AvroTypeHandler avroTypeHandler = new AvroTypeHandler();
        avroTypeHandler.setClassName(typeHandler.getClass().getCanonicalName());
        avroTypeHandler.setElementName(typeHandler.getElementName());
        return avroTypeHandler;
    }

    public static TypeHandler getTypeHandler(AvroTypeHandler avroTypeHandler){
        try {
            TypeHandler th = (TypeHandler)Class.forName(avroTypeHandler.getClassName()).newInstance();
            th.setElementName(avroTypeHandler.getElementName());
            return th;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
       return null;
    }


    public static AvroProductType getAvroProductType(ProductType productType){
        if (productType == null){
            return null;
        }

        AvroProductType avroProductType =  new AvroProductType();

        if(productType.getProductTypeId() != null)
            avroProductType.setProductTypeId(productType.getProductTypeId());
        if(productType.getName() != null)
            avroProductType.setName(productType.getName());
        if(productType.getDescription() != null)
            avroProductType.setDescription(productType.getDescription());
        if(productType.getProductRepositoryPath() != null)
            avroProductType.setProductRepositoryPath(productType.getProductRepositoryPath());
        if(productType.getVersioner() != null)
            avroProductType.setVersioner(productType.getVersioner());
        if(productType.getTypeMetadata() != null)
            avroProductType.setTypeMetadata(getAvroMetadata(productType.getTypeMetadata()));

        List<ExtractorSpec> extractorSpecs = productType.getExtractors();
        if (extractorSpecs != null && extractorSpecs.size() > 0 ) {
            List<AvroExtractorSpec> avroExtractorSpecs = new ArrayList<AvroExtractorSpec>();
            for (ExtractorSpec es : extractorSpecs) {
                avroExtractorSpecs.add(getAvroExtractorSpec(es));
            }
            avroProductType.setExtractors(avroExtractorSpecs);
        }
        List<TypeHandler> typeHandlers =  productType.getHandlers();
        if (typeHandlers != null && typeHandlers.size() > 0 ) {
            List<AvroTypeHandler> avroTypeHandlers = new ArrayList<AvroTypeHandler>();
            for (TypeHandler th : typeHandlers) {
                avroTypeHandlers.add(getAvroTypeHandler(th));
            }
            avroProductType.setHandlers(avroTypeHandlers);
        }
        return avroProductType;
    }

    public static ProductType getProductType(AvroProductType avroProductType){

        ProductType productType = new ProductType();

        productType.setDescription(avroProductType.getDescription());
        productType.setName(avroProductType.getName());
        productType.setProductRepositoryPath(avroProductType.getProductRepositoryPath());
        productType.setProductTypeId(avroProductType.getProductTypeId());
        productType.setVersioner(avroProductType.getVersioner());
        if (avroProductType.getTypeMetadata() != null) {
            productType.setTypeMetadata(AvroTypeFactory.getMetadata(avroProductType.getTypeMetadata()));
        }

        List<AvroExtractorSpec> avroExtractorSpecs = avroProductType.getExtractors();
        List<ExtractorSpec> extractorSpecs = new ArrayList<ExtractorSpec>();
        if (avroExtractorSpecs != null && avroExtractorSpecs.size() > 0) {
            for (AvroExtractorSpec aes : avroExtractorSpecs){
                extractorSpecs.add(AvroTypeFactory.getExtractorSpec(aes));
            }
            productType.setExtractors(extractorSpecs);
        }

        List<AvroTypeHandler> avroTypeHandlers = avroProductType.getHandlers();
        List<TypeHandler> typeHandlers = new ArrayList<TypeHandler>();
        if (avroTypeHandlers != null && avroTypeHandlers.size() > 0) {
            for (AvroTypeHandler aes : avroTypeHandlers){
                typeHandlers.add(AvroTypeFactory.getTypeHandler(aes));
            }
            productType.setHandlers(typeHandlers);
        }

        return productType;
    }

    public static AvroProduct getAvroProduct(Product product){

        AvroProduct avroProduct =  new AvroProduct();

        if (product.getProductId() != null)
            avroProduct.setProductId(product.getProductId());

        if (product.getProductName() != null)
            avroProduct.setProductName(product.getProductName());

        if (product.getProductType() != null)
            avroProduct.setProductType(getAvroProductType(product.getProductType()));

        if (product.getProductType() != null)
            avroProduct.setProductStructure(product.getProductStructure());

        //referince
        List<Reference> references = product.getProductReferences();
        if (references != null){
            List<AvroReference> avroReferences = AvroTypeFactory.getAvroReferences(product.getProductReferences());
            avroProduct.setReferences(avroReferences);

        }

        if (product.getTransferStatus() != null)
            avroProduct.setTransferStatus(product.getTransferStatus());

        if (product.getRootRef() != null)
            avroProduct.setRootRef(getAvroReference(product.getRootRef()));

        return avroProduct;
    }

    public static Product getProduct(AvroProduct avroPoduct){
        Product product = new Product();
        product.setProductName(avroPoduct.getProductName());
        if (avroPoduct.getProductType() != null)
        product.setProductType(getProductType(avroPoduct.getProductType()));
        product.setProductStructure(avroPoduct.getProductStructure());
        product.setTransferStatus(avroPoduct.getTransferStatus());
        //references
        if (avroPoduct.getReferences() != null)
            product.setProductReferences(getReferences(avroPoduct.getReferences()));
        product.setProductId(avroPoduct.getProductId());
        if (avroPoduct.getRootRef() != null)
            product.setRootRef(getReference(avroPoduct.getRootRef()));
        return product;
    }

    public static AvroFileTransferStatus getAvroFileTransferStatus(FileTransferStatus fileTransferStatus){
        return new AvroFileTransferStatus(
                getAvroReference(fileTransferStatus.getFileRef()),
                fileTransferStatus.getBytesTransferred(),
                getAvroProduct(fileTransferStatus.getParentProduct()));
    }

    public static FileTransferStatus getFileTransferStatus(AvroFileTransferStatus avroFileTransferStatus){
        return new FileTransferStatus(
                getReference(avroFileTransferStatus.getFileRef()),
                0,
                avroFileTransferStatus.getBytesTransferred(),
                getProduct(avroFileTransferStatus.getParentProduct())
        );
    }

    public static AvroQueryCriteria getAvroQueryCriteria(QueryCriteria queryCriteria){
        AvroQueryCriteria avroQueryCriteria = new AvroQueryCriteria();
            avroQueryCriteria.setClassName(queryCriteria.getClass().getCanonicalName());
        if (queryCriteria instanceof TermQueryCriteria){
            avroQueryCriteria.setElementName(queryCriteria.getElementName());
            avroQueryCriteria.setElementValue(((TermQueryCriteria) queryCriteria).getValue());
        } else if (queryCriteria instanceof RangeQueryCriteria){
            avroQueryCriteria.setElementName(queryCriteria.getElementName());
            avroQueryCriteria.setElementStartValue(((RangeQueryCriteria) queryCriteria).getStartValue());
            avroQueryCriteria.setElementEndValue(((RangeQueryCriteria) queryCriteria).getEndValue());
            avroQueryCriteria.setInclusive(((RangeQueryCriteria) queryCriteria).getInclusive());
        } else if(queryCriteria instanceof BooleanQueryCriteria){
            List<AvroQueryCriteria> avroQueryCriterias = new ArrayList<AvroQueryCriteria>();
            List<QueryCriteria> queryCriterias = ((BooleanQueryCriteria)queryCriteria).getTerms();
            if(queryCriteria != null && queryCriterias.size() > 0){
                for(QueryCriteria qc : queryCriterias){
                    avroQueryCriterias.add(getAvroQueryCriteria(qc));
                }
            }
            avroQueryCriteria.setOperator(((BooleanQueryCriteria)queryCriteria).getOperator());
            avroQueryCriteria.setTerms(avroQueryCriterias);
        }
        return avroQueryCriteria;
    }

    public static QueryCriteria getQueryCriteria(AvroQueryCriteria avroQueryCriteria){
        QueryCriteria queryCriteria = null;
        if(avroQueryCriteria.getClassName().equals(TermQueryCriteria.class.getCanonicalName())){
            queryCriteria = new TermQueryCriteria();
            queryCriteria.setElementName(avroQueryCriteria.getElementName());
            ((TermQueryCriteria)queryCriteria).setValue(avroQueryCriteria.getElementValue());
        }else if (avroQueryCriteria.getClassName().equals(RangeQueryCriteria.class.getCanonicalName())){
            queryCriteria = new RangeQueryCriteria();
            queryCriteria.setElementName(avroQueryCriteria.getElementName());
            ((RangeQueryCriteria)queryCriteria).setStartValue(avroQueryCriteria.getElementStartValue());
            ((RangeQueryCriteria)queryCriteria).setEndValue(avroQueryCriteria.getElementEndValue());
            ((RangeQueryCriteria)queryCriteria).setInclusive(avroQueryCriteria.getInclusive());
        }else if(avroQueryCriteria.getClassName().equals(BooleanQueryCriteria.class.getCanonicalName())){
            queryCriteria = new BooleanQueryCriteria();
            try{
                ((BooleanQueryCriteria)queryCriteria).setOperator(avroQueryCriteria.getOperator());
            } catch (QueryFormulationException e){
                System.out.println("Error generating Boolean Query.");
            }
            List<AvroQueryCriteria> avroQueryCriterias = avroQueryCriteria.getTerms();

            if (avroQueryCriterias != null && avroQueryCriterias.size() > 0)
            for (AvroQueryCriteria aqc : avroQueryCriterias){
                try {
                    ((BooleanQueryCriteria)queryCriteria).addTerm(getQueryCriteria(aqc));
                } catch (QueryFormulationException e) {
                    System.out.println("Error generating Boolean Query.");
                }
            }
        }
        return queryCriteria;
    }

    public static AvroQuery getAvroQuery(Query query){
        List<AvroQueryCriteria> avroQueryCriterias = new ArrayList<AvroQueryCriteria>();
        for (QueryCriteria qc : query.getCriteria()){
            avroQueryCriterias.add(getAvroQueryCriteria(qc));
        }
        return new AvroQuery(avroQueryCriterias);
    }

    public static Query getQuery(AvroQuery avroQuery){
        List<QueryCriteria> queryCriterias = new ArrayList<QueryCriteria>();
        for (AvroQueryCriteria qc : avroQuery.getCriteria()){
            queryCriterias.add(getQueryCriteria(qc));
        }

        return new Query(queryCriterias);
    }

    public static AvroProductPage getAvroProductPage(ProductPage productPage){
        List<AvroProduct> avroProducts = new ArrayList<AvroProduct>();
        for (Product ap : productPage.getPageProducts()){
             avroProducts.add(getAvroProduct(ap));
        }

        return new AvroProductPage(productPage.getPageNum(),
                productPage.getTotalPages(),
                productPage.getPageSize(),
                avroProducts,
                productPage.getNumOfHits());
    }

    public static ProductPage getProductPage(AvroProductPage avroProductPage){
        List<Product> products = new ArrayList<Product>();
        for (AvroProduct ap : avroProductPage.getPageProducts()){
            products.add(getProduct(ap));
        }

        ProductPage pp = new ProductPage(avroProductPage.getPageNum(),
                avroProductPage.getTotalPages(),
                avroProductPage.getPageSize(),
                products
                );
        pp.setNumOfHits(avroProductPage.getNumOfHits());
        return pp;
    }

    public static AvroElement getAvroElement(Element element){
        return new AvroElement(
                element.getElementId(),
                element.getElementName(),
                element.getDCElement(),
                element.getDescription());
    }

    public static Element getElement(AvroElement avroElement){
        return new Element(
                avroElement.getElementId(),
                avroElement.getElementName(),
                null,
                avroElement.getDcElement(),
                avroElement.getDescription(),
                null);
    }

    public static AvroQueryResult getAvroQueryResult(QueryResult queryResult){
        return new AvroQueryResult(getAvroProduct(queryResult.getProduct()),
                getAvroMetadata(queryResult.getMetadata()),
                queryResult.getToStringFormat());
    }

    public static QueryResult getQueryResult(AvroQueryResult avroQueryResult){
        QueryResult qr = new QueryResult(
                getProduct(avroQueryResult.getProduct()),
                getMetadata(avroQueryResult.getMetadata())
        );
        qr.setToStringFormat(avroQueryResult.getToStringFormat());

        return qr;
    }
// sa schimb mai sun la get class get name !!!!!

    public static AvroFilterAlgor getAvroFilterAlgor(FilterAlgor filterAlgor){
        return new AvroFilterAlgor(filterAlgor.getClass().getName(),filterAlgor.getEpsilon());
    }

    public static FilterAlgor getFilterAlgor(AvroFilterAlgor avroFilterAlgor){

        FilterAlgor fa = null;
        try {
            fa = (FilterAlgor) Class.forName(avroFilterAlgor.getClassName()).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        fa.setEpsilon(avroFilterAlgor.getEpsilon());
        return fa;
    }


    public static AvroQueryFilter getAvroQueryFilter(QueryFilter queryFilter){


        AvroQueryFilter avroQueryFilter = new AvroQueryFilter(
                queryFilter.getStartDateTimeMetKey(),
                queryFilter.getEndDateTimeMetKey(),
                queryFilter.getPriorityMetKey(),
                getAvroFilterAlgor(queryFilter.getFilterAlgor()));

        return avroQueryFilter;
    }

    public static QueryFilter getQueryFilter(AvroQueryFilter avroQueryFilter){
        return new QueryFilter(avroQueryFilter.getStartDateTimeMetKey(),
                avroQueryFilter.getEndDateTimeMetKey(),
                avroQueryFilter.getPriorityMetKey(),
                getFilterAlgor(avroQueryFilter.getFilterAlgor()));
    }

    public static AvroComplexQuery getAvroComplexQuery(ComplexQuery complexQuery){
        List<AvroQueryCriteria> avroQueryCriterias = new ArrayList<AvroQueryCriteria>();
        for (QueryCriteria aqc : complexQuery.getCriteria()){
            avroQueryCriterias.add(getAvroQueryCriteria(aqc));
        }
        AvroComplexQuery avroComplexQuery = new AvroComplexQuery();

        avroComplexQuery.setCriteria(avroQueryCriterias);
        avroComplexQuery.setReducedProductTypeNames(complexQuery.getReducedProductTypeNames());
        avroComplexQuery.setReducedMetadata(complexQuery.getReducedMetadata());
        if(complexQuery.getQueryFilter() != null)
        avroComplexQuery.setQueryFilter(getAvroQueryFilter(complexQuery.getQueryFilter()));
        avroComplexQuery.setSortByMetKey(complexQuery.getSortByMetKey());
        avroComplexQuery.setToStringResultFormat(complexQuery.getToStringResultFormat());
        return avroComplexQuery;
    }

    public static ComplexQuery getComplexQuery(AvroComplexQuery avroComplexQuery){
        List<QueryCriteria> queryCriterias = new ArrayList<QueryCriteria>();
        List<AvroQueryCriteria> avroQueryCriterias = avroComplexQuery.getCriteria();

        if (avroQueryCriterias != null && avroQueryCriterias.size() > 0) {
            for (AvroQueryCriteria aqc : avroQueryCriterias){
                queryCriterias.add(getQueryCriteria(aqc));
            }
        }
        ComplexQuery complexQuery = new ComplexQuery();

        complexQuery.setCriteria(queryCriterias);
        complexQuery.setReducedProductTypeNames(avroComplexQuery.getReducedProductTypeNames());
        complexQuery.setReducedMetadata(avroComplexQuery.getReducedMetadata());
        if(avroComplexQuery.getQueryFilter() != null)
            complexQuery.setQueryFilter(getQueryFilter(avroComplexQuery.getQueryFilter()));
        complexQuery.setSortByMetKey(avroComplexQuery.getSortByMetKey());
        complexQuery.setToStringResultFormat(avroComplexQuery.getToStringResultFormat());
        return complexQuery;
    }


    private static List<Reference> getReferences(List<AvroReference> avroReferences){
        List<Reference> references = new ArrayList<Reference>();

        for(AvroReference ar : avroReferences){
            references.add(AvroTypeFactory.getReference(ar));
        }
        return references;
    }

    private static List<AvroReference> getAvroReferences(List<Reference> references){
        List<AvroReference> avroReferences = new ArrayList<AvroReference>();

        for (Reference r : references){
            avroReferences.add(getAvroReference(r));
        }
        return avroReferences;
    }

}
