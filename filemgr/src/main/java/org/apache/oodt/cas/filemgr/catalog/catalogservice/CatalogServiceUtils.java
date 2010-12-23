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
package org.apache.oodt.cas.filemgr.catalog.catalogservice;

//OODT imports
import org.apache.oodt.cas.catalog.exception.CatalogServiceException;
import org.apache.oodt.cas.catalog.metadata.TransactionalMetadata;
import org.apache.oodt.cas.catalog.page.CatalogReceipt;
import org.apache.oodt.cas.catalog.page.Page;
import org.apache.oodt.cas.catalog.page.QueryPager;
import org.apache.oodt.cas.catalog.query.ComparisonQueryExpression;
import org.apache.oodt.cas.catalog.query.CustomQueryExpression;
import org.apache.oodt.cas.catalog.query.NotQueryExpression;
import org.apache.oodt.cas.catalog.query.QueryExpression;
import org.apache.oodt.cas.catalog.query.QueryLogicalGroup;
import org.apache.oodt.cas.catalog.system.CatalogService;
import org.apache.oodt.cas.catalog.term.Term;
import org.apache.oodt.cas.catalog.term.TermBucket;
import org.apache.oodt.cas.filemgr.metadata.CoreMetKeys;
import org.apache.oodt.cas.filemgr.structs.BooleanQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.Element;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductPage;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.QueryCriteria;
import org.apache.oodt.cas.filemgr.structs.RangeQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.TermQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.exceptions.QueryFormulationException;
import org.apache.oodt.cas.filemgr.structs.mime.MimeType;
import org.apache.oodt.cas.filemgr.structs.mime.MimeTypeException;
import org.apache.oodt.cas.metadata.Metadata;

//JDK imports
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

//APACHE imports
import org.apache.commons.lang.StringUtils;

/**
 * 
 * @author bfoster
 *
 */
public class CatalogServiceUtils {

//	public static QueryExpression getCatalogServiceQueryExpression(List<ProductType> productTypes) {
//		List<String> productTypeNames = new Vector<String>();
//		for (ProductType type : productTypes)
//			productTypeNames.add(type.getName());
//		return getCatalogServiceComparisonQueryExpression(CoreMetKeys.PRODUCT_TYPE, productTypeNames, ComparisonQueryExpression.Operator.EQUAL_TO, productTypes);
//	}
	
	public static QueryExpression getMergedQueryExpression(QueryLogicalGroup.Operator operator, Set<String> productTypeNames, QueryExpression expression1, QueryExpression expression2, QueryExpression... otherExpressions) {
		QueryLogicalGroup group = new QueryLogicalGroup();
		group.setBucketNames(productTypeNames);
		group.setOperator(operator);
		group.addExpression(expression1);
		group.addExpression(expression2);
		group.addExpressions(Arrays.asList(otherExpressions));
		return group;
	}
	
	public static ComparisonQueryExpression getCatalogServiceComparisonQueryExpression(String termName, List<String> values, ComparisonQueryExpression.Operator operator, Set<String> productTypeNames) {
		ComparisonQueryExpression comparisonQueryExpression = new ComparisonQueryExpression();
		comparisonQueryExpression.setBucketNames(productTypeNames);
		comparisonQueryExpression.setTerm(new Term(termName, values));
		comparisonQueryExpression.setOperator(operator);
		return comparisonQueryExpression;
	}
	
	public static List<Product> asProducts(List<TransactionalMetadata> transactionalMetadataList, List<ProductType> supportedProductTypes) throws NumberFormatException, MimeTypeException {
		List<Product> products = new Vector<Product>();
		for (TransactionalMetadata transactionalMetadata : transactionalMetadataList)
			products.add(asProduct(transactionalMetadata, supportedProductTypes));
		return products;
	}
	
	public static Product asProduct(TransactionalMetadata transactionalMetadata, List<ProductType> supportedProductTypes) throws NumberFormatException, MimeTypeException {
		HashSet<Term> terms = getProductTerms(transactionalMetadata.getMetadata());
		terms.add(new Term(CatalogService.CATALOG_SERVICE_TRANSACTION_ID_MET_KEY, Collections.singletonList(transactionalMetadata.getTransactionId().toString())));
		TermBucket termBucket = new TermBucket(transactionalMetadata.getMetadata().getMetadata(CoreMetKeys.PRODUCT_TYPE));
		termBucket.addTerms(terms);
		return asProduct(termBucket, supportedProductTypes);
	}
	
	public static Product asProduct(TermBucket termBucket, List<ProductType> supportedProductTypes) throws NumberFormatException, MimeTypeException {
		String productId = safeReadTermValue(termBucket, CatalogService.CATALOG_SERVICE_TRANSACTION_ID_MET_KEY);
		String productName = safeReadTermValue(termBucket, CoreMetKeys.PRODUCT_NAME);
		String status = safeReadTermValue(termBucket, CoreMetKeys.PRODUCT_STATUS);
		String structure = safeReadTermValue(termBucket, CoreMetKeys.PRODUCT_STRUCTURE);
		ProductType productType = null;
		for (ProductType type : supportedProductTypes)
			if (type.getName().equals(termBucket.getName()))
				productType = type;
		List<String> origRefs = safeReadTermValues(termBucket, CoreMetKeys.PRODUCT_REFERENCE_ORIGINAL);
		List<String> dataStoreRefs = safeReadTermValues(termBucket, CoreMetKeys.PRODUCT_REFERENCE_DATA_STORE);
		List<String> refsFileSizes = safeReadTermValues(termBucket, CoreMetKeys.PRODUCT_REFERENCE_FILE_SIZE);
		List<String> refsMimeTypes = safeReadTermValues(termBucket, CoreMetKeys.PRODUCT_REFERENCE_MIME_TYPE);
		Vector<Reference> refs = new Vector<Reference>();
		for (int i = 0; i < origRefs.size(); i++) {
			String dataStoreRef = dataStoreRefs.size() > i ? dataStoreRefs.get(i) : null;
			String refsFileSize = refsFileSizes.size() > i ? refsFileSizes.get(i) : null;
			String refsMimeType = refsMimeTypes.size() > i ? refsMimeTypes.get(i) : null;
			refs.add(new Reference(origRefs.get(i), dataStoreRef, Long.parseLong(refsFileSize), new MimeType(refsMimeType)));
		}
		if (termBucket.getTerms().contains(CoreMetKeys.PRODUCT_ROOT_REF_ORIG) || 
				termBucket.getTerms().contains(CoreMetKeys.PRODUCT_ROOT_REF_DATA_STORE) ||
				termBucket.getTerms().contains(CoreMetKeys.PRODUCT_ROOT_REF_FILE_SIZE) ||
				termBucket.getTerms().contains(CoreMetKeys.PRODUCT_ROOT_REF_MIME_TYPE)) {
			Reference rootRef = new Reference();
			rootRef.setOrigReference(safeReadTermValue(termBucket, CoreMetKeys.PRODUCT_ROOT_REF_ORIG));
			rootRef.setDataStoreReference(safeReadTermValue(termBucket,CoreMetKeys.PRODUCT_ROOT_REF_DATA_STORE));
			if (termBucket.getTermByName(CoreMetKeys.PRODUCT_ROOT_REF_FILE_SIZE) != null)
				rootRef.setFileSize(Long.parseLong(termBucket.getTermByName(CoreMetKeys.PRODUCT_ROOT_REF_FILE_SIZE).getFirstValue()));
			rootRef.setMimeType(safeReadTermValue(termBucket, CoreMetKeys.PRODUCT_ROOT_REF_MIME_TYPE));
		}
		Product product = new Product();
		product.setProductName(productName);
		product.setProductType(productType);
		product.setProductId(productId);
		product.setProductReferences(refs);
		product.setTransferStatus(status);
		if (structure != null)
			product.setProductStructure(structure);
		return product;
	}
	
	protected static String safeReadTermValue(TermBucket termBucket, String termName) {
		if (termName != null) {
			Term term = termBucket.getTermByName(termName);
			if (term != null)
				return term.getFirstValue();
		}
		return null;
	}
	
	protected static List<String> safeReadTermValues(TermBucket termBucket, String termName) {
		if (termName != null) {
			Term term = termBucket.getTermByName(termName);
			if (term != null)
				return term.getValues();
		}
		return Collections.emptyList();
	}
	
//	public static ProductPage getProductPage(QueryPager queryPager, CatalogService catalogService, List<ProductType> supportedProductTypes) throws CatalogServiceException, NumberFormatException, MimeTypeException {
//		List<TransactionalMetadata> metadatas = null;
//		if (queryPager.getPageNumber() >= 0) {
//			metadatas = catalogService.getNextPage(queryPager);
//		}else {
//			metadatas = catalogService.getAllPages(queryPager);
//		}
//		
//		if (metadatas.size() > 0)
//			return new ProductPage(queryPager.getPageNumber(), queryPager.getTotalNumPages(), queryPager.getPageSize(), CatalogServiceUtils.asProducts(metadatas, supportedProductTypes));
//		else
//			return ProductPage.blankPage();
//	}
	
	public static ProductPage getProductPage(Page page, CatalogService catalogService, List<ProductType> supportedProductTypes) throws CatalogServiceException, NumberFormatException, MimeTypeException {
		List<TransactionalMetadata> metadatas = null;
		if (page.getPageNum() >= 1) {
			metadatas = catalogService.getMetadata(page);
			if (metadatas != null && metadatas.size() > 0)
				return new ProductPage(page.getPageNum(), page.getPageSize(), page.getNumOfHits(), CatalogServiceUtils.asProducts(metadatas, supportedProductTypes));
		}
		return ProductPage.blankPage();
	}
	
	protected static Metadata asCatalogServiceMetadata(Product product) {
		Metadata metadata = new Metadata();
		if (product.getProductId() != null)
			metadata.replaceMetadata(CatalogService.CATALOG_SERVICE_TRANSACTION_ID_MET_KEY, product.getProductId());
		metadata.replaceMetadata(CoreMetKeys.PRODUCT_NAME, product.getProductName());
		if (product.getProductStructure() != null)
			metadata.replaceMetadata(CoreMetKeys.PRODUCT_STRUCTURE, product.getProductStructure());
		if (product.getTransferStatus() != null)
			metadata.replaceMetadata(CoreMetKeys.PRODUCT_STATUS, product.getTransferStatus());
		metadata.replaceMetadata(CoreMetKeys.PRODUCT_TYPE, product.getProductType().getName());
		if (product.getRootRef() != null) {
			metadata.replaceMetadata(CoreMetKeys.PRODUCT_ROOT_REF_ORIG, product.getRootRef().getOrigReference());
			metadata.replaceMetadata(CoreMetKeys.PRODUCT_ROOT_REF_DATA_STORE, product.getRootRef().getDataStoreReference());
			metadata.replaceMetadata(CoreMetKeys.PRODUCT_ROOT_REF_FILE_SIZE, Long.toString(product.getRootRef().getFileSize()));
			metadata.replaceMetadata(CoreMetKeys.PRODUCT_ROOT_REF_MIME_TYPE, product.getRootRef().getMimeType().getName());
		}
		for (Reference ref : product.getProductReferences()) {
			metadata.addMetadata(CoreMetKeys.PRODUCT_REFERENCE_DATA_STORE, ref.getDataStoreReference());
			metadata.addMetadata(CoreMetKeys.PRODUCT_REFERENCE_ORIGINAL, ref.getOrigReference());
			metadata.addMetadata(CoreMetKeys.PRODUCT_REFERENCE_FILE_SIZE, Long.toString(ref.getFileSize()));
			if (ref.getMimeType() != null)
				metadata.addMetadata(CoreMetKeys.PRODUCT_REFERENCE_MIME_TYPE, ref.getMimeType().getName());
		}
		return metadata;
	}
	
	public static Metadata asCatalogServiceMetadata(Product product, Metadata metadata) {
		Metadata catalogServiceMetadata = new Metadata();
		catalogServiceMetadata.addMetadata(metadata.getHashtable());
		catalogServiceMetadata.addMetadata(asCatalogServiceMetadata(product).getHashtable(), true);
		return catalogServiceMetadata;
	}
	
	public static TermBucket asTermBucket(Product product, Metadata metadata, List<Element> productTypeElements) {
		return asTermBucket(asCatalogServiceMetadata(product, metadata), product.getProductType(), productTypeElements);
	}
	
	public static TermBucket asTermBucket(Metadata metadata, ProductType productType, List<Element> productTypeElements) {
		TermBucket termBucket = new TermBucket(productType.getName());
		termBucket.addTerms(getProductTypeTerms(metadata, productType, productTypeElements), false);
		termBucket.addTerms(getProductTerms(metadata), true);
		if (termBucket.getTerms().size() > 0)
			return termBucket;
		else 
			return null;
	}
	
	public static HashSet<Term> getProductTerms(Metadata metadata) {
		HashSet<Term> terms = new HashSet<Term>();
		List<String> mimeTypes = metadata.getAllMetadata(CoreMetKeys.PRODUCT_REFERENCE_MIME_TYPE);
		if (mimeTypes != null)
			terms.add(new Term(CoreMetKeys.PRODUCT_REFERENCE_MIME_TYPE, mimeTypes, Term.Type.xml_string));
		List<String> origRefs = metadata.getAllMetadata(CoreMetKeys.PRODUCT_REFERENCE_ORIGINAL);
		if (origRefs != null)
			terms.add(new Term(CoreMetKeys.PRODUCT_REFERENCE_ORIGINAL, origRefs, Term.Type.xml_string));
		List<String> dataStoreRefs = metadata.getAllMetadata(CoreMetKeys.PRODUCT_REFERENCE_DATA_STORE);
		if (dataStoreRefs != null)
			terms.add(new Term(CoreMetKeys.PRODUCT_REFERENCE_DATA_STORE, dataStoreRefs, Term.Type.xml_string));
		List<String> refsFileSize = metadata.getAllMetadata(CoreMetKeys.PRODUCT_REFERENCE_FILE_SIZE);
		if (refsFileSize != null)
			terms.add(new Term(CoreMetKeys.PRODUCT_REFERENCE_FILE_SIZE, refsFileSize, Term.Type.xml_string));
		List<String> productStructure = metadata.getAllMetadata(CoreMetKeys.PRODUCT_STRUCTURE);
		if (productStructure != null)
			terms.add(new Term(CoreMetKeys.PRODUCT_STRUCTURE, productStructure, Term.Type.xml_string));
		List<String> productStatus = metadata.getAllMetadata(CoreMetKeys.PRODUCT_STATUS);
		if (productStatus != null)
			terms.add(new Term(CoreMetKeys.PRODUCT_STATUS, productStatus, Term.Type.xml_string));
		List<String> productRecievedTime = metadata.getAllMetadata(CoreMetKeys.PRODUCT_RECEVIED_TIME);
		if (productRecievedTime != null)
			terms.add(new Term(CoreMetKeys.PRODUCT_RECEVIED_TIME, productRecievedTime, Term.Type.xml_string));
//		List<String> productId = metadata.getAllMetadata(CatalogService.CATALOG_SERVICE_TRANSACTION_ID_MET_KEY);
//		if (productId != null)
//			terms.add(new Term(CatalogService.CATALOG_SERVICE_TRANSACTION_ID_MET_KEY, productId, Term.Type.xml_string));
		List<String> productName = metadata.getAllMetadata(CoreMetKeys.PRODUCT_NAME);
		if (productName != null)
			terms.add(new Term(CoreMetKeys.PRODUCT_NAME, productName, Term.Type.xml_string));
		List<String> productRootRefOrig = metadata.getAllMetadata(CoreMetKeys.PRODUCT_ROOT_REF_ORIG);
		if (productRootRefOrig != null)
			terms.add(new Term(CoreMetKeys.PRODUCT_ROOT_REF_ORIG, productRootRefOrig, Term.Type.xml_string));
		List<String> productRootRefDataStore = metadata.getAllMetadata(CoreMetKeys.PRODUCT_ROOT_REF_DATA_STORE);
		if (productRootRefDataStore != null)
			terms.add(new Term(CoreMetKeys.PRODUCT_ROOT_REF_DATA_STORE, productRootRefDataStore, Term.Type.xml_string));
		List<String> productRootRefFileSize = metadata.getAllMetadata(CoreMetKeys.PRODUCT_REFERENCE_FILE_SIZE);
		if (productRootRefFileSize != null)
			terms.add(new Term(CoreMetKeys.PRODUCT_REFERENCE_FILE_SIZE, productRootRefFileSize, Term.Type.xml_string));
		List<String> productRootRefMimeType = metadata.getAllMetadata(CoreMetKeys.PRODUCT_REFERENCE_MIME_TYPE);
		if (productRootRefMimeType != null)
			terms.add(new Term(CoreMetKeys.PRODUCT_REFERENCE_MIME_TYPE, productRootRefMimeType, Term.Type.xml_string));
		List<String> productType = metadata.getAllMetadata(CoreMetKeys.PRODUCT_TYPE);
		if (productType != null)
			terms.add(new Term(CoreMetKeys.PRODUCT_TYPE, productType, Term.Type.xml_string));
		List<String> catalogAction = metadata.getAllMetadata(CatalogActions.CATALOG_ACTION_KEY);
		if (catalogAction != null)
			terms.add(new Term(CatalogActions.CATALOG_ACTION_KEY, catalogAction, Term.Type.xml_string));
		return terms;
	}
	
	/**
	 * urn:<product_type_name>:<element_name> is also supported for metadata keys
	 * @param metadata
	 * @param productType
	 * @param validationLayer
	 * @return
	 * @throws ValidationLayerException
	 */
	public static HashSet<Term> getProductTypeTerms(Metadata metadata, ProductType productType, List<Element> productTypeElements) {
		HashSet<Term> terms = new HashSet<Term>();
		//System.out.println("Looking for elements: " + validationLayer.getElements(productType));
		//if (metadata.getAllMetadata(CoreMetKeys.PRODUCT_TYPE).contains(productType.getName())) {
			for (Object objKey : metadata.getHashtable().keySet()) {
				String elementName = (String) objKey;
				String[] namespacedElementName = elementName.split(":");
				if (namespacedElementName.length == 3 && namespacedElementName[0].equals("urn")) {
					if (namespacedElementName[1].equals(productType.getName()))
						elementName = namespacedElementName[2];
					else
						continue;
				}
				boolean found = false;
				for (Element element : productTypeElements) {
					//System.out.println("COMPARING: " + element.getElementName() + " " + elementName);
					if (element.getElementName().equals(elementName)) {
						found = true;
						break;
					}
				}
				if (found)
					terms.add(new Term(elementName, metadata.getAllMetadata(elementName), Term.Type.xml_string));
				else if (elementName.equals(CatalogActions.CATALOG_ACTION_KEY)) 
					terms.add(new Term(CatalogActions.CATALOG_ACTION_KEY, metadata.getAllMetadata(elementName), Term.Type.xml_string));
			}
		//}
		return terms;
	}
	
	public static Metadata asMetadata(TermBucket termBucket) {
		Metadata metadata = new Metadata();
		for (Term term : termBucket.getTerms()) 
			metadata.addMetadata(term.getName(), term.getValues());
		return metadata;
	}
	
	public static QueryExpression asQueryExpression(List<QueryCriteria> queryCriteriaList, Set<String> productTypeNames) throws QueryFormulationException {
		if (queryCriteriaList.size() == 0) {
			return null;
		}else if (queryCriteriaList.size() == 1) {
			return asQueryExpression(queryCriteriaList.get(0), productTypeNames);
		}else {
			BooleanQueryCriteria bqCriteria = new BooleanQueryCriteria();
			bqCriteria.setOperator(BooleanQueryCriteria.AND);
			for (QueryCriteria qc : queryCriteriaList)
				bqCriteria.addTerm(qc);
			return asQueryExpression(bqCriteria, productTypeNames);
		}
	}
	
	public static QueryExpression asQueryExpression(QueryCriteria queryCriteria, Set<String> productTypeNames) throws QueryFormulationException {
		if (queryCriteria instanceof BooleanQueryCriteria) {
			BooleanQueryCriteria boolQC = (BooleanQueryCriteria) queryCriteria;
			if (boolQC.getOperator() == BooleanQueryCriteria.NOT) {
				NotQueryExpression notQE = new NotQueryExpression();
				notQE.setBucketNames(productTypeNames);
				notQE.setQueryExpression(asQueryExpression(boolQC.getTerms().get(0), productTypeNames));
				return notQE;
			}else if (boolQC.getOperator() == BooleanQueryCriteria.AND || boolQC.getOperator() == BooleanQueryCriteria.OR) {
				QueryLogicalGroup queryGroup = new QueryLogicalGroup();
				queryGroup.setBucketNames(productTypeNames);
				queryGroup.setOperator((boolQC.getOperator() == BooleanQueryCriteria.AND) ? QueryLogicalGroup.Operator.AND : QueryLogicalGroup.Operator.OR);
				for (QueryCriteria subQC : ((BooleanQueryCriteria) queryCriteria).getTerms())
					queryGroup.addExpression(asQueryExpression(subQC, productTypeNames));
				return queryGroup;
			}else {
				throw new QueryFormulationException("Badly formed BooleanQueryCriteria (unsupported operator): " + boolQC);
			}
		}else if (queryCriteria instanceof TermQueryCriteria) {
			return CatalogServiceUtils.getCatalogServiceComparisonQueryExpression(queryCriteria.getElementName(), Collections.singletonList(((TermQueryCriteria) queryCriteria).getValue()), ComparisonQueryExpression.Operator.EQUAL_TO, productTypeNames);
		}else if (queryCriteria instanceof RangeQueryCriteria) {
			RangeQueryCriteria rangeQC = (RangeQueryCriteria) queryCriteria;
			if (rangeQC.getStartValue() != null && rangeQC.getEndValue() != null) {
				QueryExpression startRangeQueryExpression = CatalogServiceUtils.getCatalogServiceComparisonQueryExpression(rangeQC.getElementName(), Collections.singletonList(rangeQC.getStartValue()), rangeQC.getInclusive() ? ComparisonQueryExpression.Operator.GREATER_THAN_EQUAL_TO : ComparisonQueryExpression.Operator.GREATER_THAN, productTypeNames);
				QueryExpression endRangeQueryExpression = CatalogServiceUtils.getCatalogServiceComparisonQueryExpression(rangeQC.getElementName(), Collections.singletonList(rangeQC.getEndValue()), rangeQC.getInclusive() ? ComparisonQueryExpression.Operator.LESS_THAN_EQUAL_TO : ComparisonQueryExpression.Operator.LESS_THAN, productTypeNames);
				return getMergedQueryExpression(QueryLogicalGroup.Operator.AND, productTypeNames, startRangeQueryExpression, endRangeQueryExpression);
			}else if (rangeQC.getStartValue() != null) {
				return CatalogServiceUtils.getCatalogServiceComparisonQueryExpression(rangeQC.getElementName(), Collections.singletonList(rangeQC.getStartValue()), rangeQC.getInclusive() ? ComparisonQueryExpression.Operator.GREATER_THAN_EQUAL_TO : ComparisonQueryExpression.Operator.GREATER_THAN, productTypeNames);
			}else if (rangeQC.getEndValue() != null) {
				return CatalogServiceUtils.getCatalogServiceComparisonQueryExpression(rangeQC.getElementName(), Collections.singletonList(rangeQC.getEndValue()), rangeQC.getInclusive() ? ComparisonQueryExpression.Operator.LESS_THAN_EQUAL_TO : ComparisonQueryExpression.Operator.LESS_THAN, productTypeNames);
			}else {
				throw new QueryFormulationException("Badly formed RangeQueryCriteria: " + rangeQC);
			}
		}else {
			throw new QueryFormulationException("Unsupported QueryCriteria: " + queryCriteria);
		}
	}
	
	public static QueryCriteria asQueryCriteria(QueryExpression queryExpression) throws QueryFormulationException {
		if (queryExpression instanceof QueryLogicalGroup) {
			QueryLogicalGroup queryLogicalGroup = (QueryLogicalGroup) queryExpression;			
			BooleanQueryCriteria booleanQC = new BooleanQueryCriteria();
			booleanQC.setOperator(queryLogicalGroup.getOperator().equals(QueryLogicalGroup.Operator.AND) ? BooleanQueryCriteria.AND : BooleanQueryCriteria.OR);
			for (QueryExpression subQE : ((QueryLogicalGroup) queryExpression).getExpressions())
				booleanQC.addTerm(asQueryCriteria(subQE));
			return booleanQC;
		}else if (queryExpression instanceof NotQueryExpression) {
			BooleanQueryCriteria booleanQC = new BooleanQueryCriteria();
			booleanQC.setOperator(BooleanQueryCriteria.NOT);
			booleanQC.addTerm(asQueryCriteria(((NotQueryExpression) queryExpression).getQueryExpression()));
			return booleanQC;
		}else if (queryExpression instanceof ComparisonQueryExpression) {
			ComparisonQueryExpression compQE = (ComparisonQueryExpression) queryExpression;
			if (compQE.getOperator().equals(ComparisonQueryExpression.Operator.EQUAL_TO)) {
				return new TermQueryCriteria(compQE.getTerm().getName(), compQE.getTerm().getFirstValue());
			} else if (compQE.getOperator().equals(ComparisonQueryExpression.Operator.GREATER_THAN)) {
				return new RangeQueryCriteria(compQE.getTerm().getName(), compQE.getTerm().getFirstValue(), null, false);
			} else if (compQE.getOperator().equals(ComparisonQueryExpression.Operator.GREATER_THAN_EQUAL_TO)) {
				return new RangeQueryCriteria(compQE.getTerm().getName(), compQE.getTerm().getFirstValue(), null, true);
			} else if (compQE.getOperator().equals(ComparisonQueryExpression.Operator.LESS_THAN)) {
				return new RangeQueryCriteria(compQE.getTerm().getName(), null, compQE.getTerm().getFirstValue(), false);
			} else if (compQE.getOperator().equals(ComparisonQueryExpression.Operator.LESS_THAN_EQUAL_TO)) {
				return new RangeQueryCriteria(compQE.getTerm().getName(), null, compQE.getTerm().getFirstValue(), true);
			} else {
				throw new QueryFormulationException("Unsupported QueryExpression: " + queryExpression);
			}
		}else {
			return null;
		}
	}
	
}
