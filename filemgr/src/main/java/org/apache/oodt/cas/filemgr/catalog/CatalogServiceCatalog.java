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
package org.apache.oodt.cas.filemgr.catalog;

//OODT imports
import org.apache.oodt.cas.catalog.metadata.TransactionalMetadata;
import org.apache.oodt.cas.catalog.page.Page;
import org.apache.oodt.cas.catalog.page.PageInfo;
import org.apache.oodt.cas.catalog.page.QueryPager;
import org.apache.oodt.cas.catalog.page.TransactionReceipt;
import org.apache.oodt.cas.catalog.query.CustomQueryExpression;
import org.apache.oodt.cas.catalog.query.CustomWrapperQueryExpression;
import org.apache.oodt.cas.catalog.query.StdQueryExpression;
import org.apache.oodt.cas.catalog.system.CatalogService;
import org.apache.oodt.cas.filemgr.catalog.catalogservice.CatalogActions;
import org.apache.oodt.cas.filemgr.catalog.catalogservice.CatalogServiceMetKeys;
import org.apache.oodt.cas.filemgr.catalog.catalogservice.CatalogServiceUtils;
import org.apache.oodt.cas.filemgr.catalog.catalogservice.FilemgrCatalogIndex;
import org.apache.oodt.cas.filemgr.metadata.CoreMetKeys;
import org.apache.oodt.cas.filemgr.repository.RepositoryManager;
import org.apache.oodt.cas.filemgr.structs.Element;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductPage;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.Query;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.filemgr.structs.exceptions.RepositoryManagerException;
import org.apache.oodt.cas.filemgr.structs.query.QueryResult;
import org.apache.oodt.cas.filemgr.validation.ValidationLayer;
import org.apache.oodt.cas.metadata.Metadata;

//JDK imports
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

//Apache imports
import org.apache.commons.lang.StringUtils;

/**
 * @author bfoster
 * @version $Revision$
 */
public class CatalogServiceCatalog extends AbstractCatalog {

	private static Logger LOG = Logger.getLogger(CatalogServiceCatalog.class.getName());
	
	protected ValidationLayer validationLayer;
	protected RepositoryManager repositoryManager;
	protected CatalogService catalogService;
	protected int pageSize;
	
	public CatalogServiceCatalog(CatalogService catalogService, RepositoryManager repositoryManager, ValidationLayer validationLayer, int pageSize) {
		this.catalogService = catalogService;
		this.repositoryManager = repositoryManager;
		this.validationLayer = validationLayer;
		this.pageSize = pageSize;
	}
	
    public int getPageSize() {
    	return this.pageSize;
    }
	
	/*
	 * (non-Javadoc)
	 * @see org.apache.oodt.cas.filemgr.catalog.Catalog#addMetadata(org.apache.oodt.cas.metadata.Metadata, org.apache.oodt.cas.filemgr.structs.Product)
	 */
	public void addMetadata(Metadata metadata, Product product) throws CatalogException {
		try {
			metadata.replaceMetadata(CatalogActions.CATALOG_ACTION_KEY, CatalogActions.INGEST_METADATA);
			metadata.replaceMetadata(CatalogService.ENABLE_UPDATE_MET_KEY, "true");
			this.catalogService.ingest(CatalogServiceUtils.asCatalogServiceMetadata(product, metadata));
		}catch (Exception e) {
			throw new CatalogException("", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.oodt.cas.filemgr.catalog.Catalog#addProduct(org.apache.oodt.cas.filemgr.structs.Product)
	 */
	public void addProduct(Product product) throws CatalogException {
		try {
			Metadata actionMetadata = new Metadata();
			actionMetadata.replaceMetadata(CatalogActions.CATALOG_ACTION_KEY, CatalogActions.INGEST_PRODUCT);
			actionMetadata.replaceMetadata(CatalogService.ENABLE_UPDATE_MET_KEY, "true");
			product.setProductId(this.catalogService.ingest(CatalogServiceUtils.asCatalogServiceMetadata(product, actionMetadata)).getTransactionId().toString());
		}catch (Exception e) {
			throw new CatalogException("", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.oodt.cas.filemgr.catalog.Catalog#addProductReferences(org.apache.oodt.cas.filemgr.structs.Product)
	 */
	public void addProductReferences(Product product) throws CatalogException {
		try {
			Metadata actionMetadata = new Metadata();
			actionMetadata.replaceMetadata(CatalogActions.CATALOG_ACTION_KEY, CatalogActions.INGEST_REFERENCE);
			actionMetadata.replaceMetadata(CatalogService.ENABLE_UPDATE_MET_KEY, "true");
			this.catalogService.ingest(CatalogServiceUtils.asCatalogServiceMetadata(product, actionMetadata));
		}catch (Exception e) {
			throw new CatalogException("", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.oodt.cas.filemgr.catalog.Catalog#getMetadata(org.apache.oodt.cas.filemgr.structs.Product)
	 */
	public Metadata getMetadata(Product product) throws CatalogException {
		try {
			List<TransactionalMetadata> metadata = this.catalogService.getMetadataFromTransactionIdStrings(Collections.singletonList(product.getProductId()));
			if (!metadata.isEmpty()) {
				TransactionalMetadata tranactionalMet = metadata.iterator().next();
				Metadata m = tranactionalMet.getMetadata();
				Metadata returnMet = new Metadata();
				List<Element> elements = this.validationLayer.getElements(product.getProductType());
				elements.add(new Element(null, CoreMetKeys.PRODUCT_RECEVIED_TIME, null, null, null));
//				for (Object key : m.getHashtable().keySet())  {
					for (Element element : elements) {
						if (m.containsKey(element.getElementName()))
//						if (element.getElementName().equals((String) key)) {
							returnMet.replaceMetadata((String) element.getElementName(), m.getAllMetadata((String) element.getElementName()));
//							break;
//						}
					}
//				}
				returnMet.replaceMetadata(CatalogService.CATALOG_SERVICE_TRANSACTION_ID_MET_KEY, tranactionalMet.getTransactionId().toString());
				returnMet.replaceMetadata(CatalogService.CATALOG_IDS_MET_KEY, StringUtils.join(tranactionalMet.getCatalogIds().iterator(), ','));
				return returnMet;
			}else {
				return new Metadata();
			}
		}catch (Exception e) {
			throw new CatalogException("", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.oodt.cas.filemgr.catalog.Catalog#getNumProducts(org.apache.oodt.cas.filemgr.structs.ProductType)
	 */
	public int getNumProducts(ProductType type) throws CatalogException {
		try {
			List<String> values = this.catalogService.getProperty(FilemgrCatalogIndex.PropertyKeys.generateNumOfProductsKeyName(type.getName()));
			int numOfProducts = 0;
			for (String value : values)
				numOfProducts += Integer.parseInt(value);
			return numOfProducts;
		}catch (Exception e) {
			throw new CatalogException("", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.oodt.cas.filemgr.catalog.Catalog#getProductById(java.lang.String)
	 */
	public Product getProductById(String productId) throws CatalogException {
		try {
			List<TransactionalMetadata> metadata = this.catalogService.getMetadataFromTransactionIdStrings(Collections.singletonList(productId));
			if (!metadata.isEmpty()) {
				TransactionalMetadata m = metadata.iterator().next();
				//XMLUtils.writeXmlToStream(m.getMetadata().toXML(), System.out);
				return CatalogServiceUtils.asProduct(m, this.repositoryManager.getProductTypes());
			}else {
				return null;
			}
		}catch (Exception e) {
			throw new CatalogException("", e);
		}
	}
	
    public List<Product> getProductsByIds(List<String> productIds) throws CatalogException {
		try {
			Vector<Product> products = new Vector<Product>();
			List<TransactionalMetadata> transactionalMetadatas = this.catalogService.getMetadataFromTransactionIdStrings(productIds);
			for (TransactionalMetadata transactionalMetadata : transactionalMetadatas)
				products.add(CatalogServiceUtils.asProduct(transactionalMetadata, this.repositoryManager.getProductTypes()));
			return products;
		}catch (Exception e) {
			throw new CatalogException("", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.oodt.cas.filemgr.catalog.Catalog#getProductByName(java.lang.String)
	 */
	public Product getProductByName(String productName) throws CatalogException {
		try {
			CustomQueryExpression productNameQE = new CustomQueryExpression(CatalogServiceMetKeys.PRODUCT_NAME_QUERY_EXPRESSION);
			productNameQE.setProperty(CatalogServiceMetKeys.PRODUCT_NAME_CUSTOM_KEY, productName);
			QueryPager queryPager = this.catalogService.query(new CustomWrapperQueryExpression(CatalogServiceMetKeys.FILEMGR_CATALOGS_ONLY, productNameQE));
			List<TransactionalMetadata> metadata = this.catalogService.getAllPages(queryPager);
			if (!metadata.isEmpty()) {
				TransactionalMetadata m = metadata.iterator().next();
//				XMLUtils.writeXmlToStream(m.getMetadata().toXML(), System.out);
				return CatalogServiceUtils.asProduct(m, this.repositoryManager.getProductTypes());
			}else 
				return null;
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to get Product by name : " + e.getMessage(), e);
			throw new CatalogException("", e);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.apache.oodt.cas.filemgr.catalog.Catalog#getProductReferences(org.apache.oodt.cas.filemgr.structs.Product)
	 */
	public List<Reference> getProductReferences(Product product)
			throws CatalogException {
		product.setProductReferences(this.getProductById(product.getProductId()).getProductReferences());
		return product.getProductReferences();
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.oodt.cas.filemgr.catalog.Catalog#getProducts()
	 */
	public List<Product> getProducts() throws CatalogException {
		try {
			HashMap<String, TransactionalMetadata> transactionalMetadatas = new HashMap<String, TransactionalMetadata>();
//			for (ProductType productType : this.repositoryManager.getProductTypes()) {
//				QueryPager queryPager = this.catalogService.query(new ProductTypeQueryExpression(Collections.singletonList(productType)));
//				for (TransactionalMetadata transactionalMetadata : catalogService.getAllPages(queryPager)) {
//					TransactionalMetadata existingMet = transactionalMetadatas.get(transactionalMetadata.getTransactionId().toString());
//					if (existingMet != null) {
//						existingMet.getMetadata().addMetadata(transactionalMetadata.getMetadata().getHashtable(), false);
//					}else {
//						transactionalMetadatas.put(transactionalMetadata.getTransactionId().toString(), transactionalMetadata);
//					}
//				}
//			}			
			QueryPager queryPager = this.catalogService.query(new CustomWrapperQueryExpression(CatalogServiceMetKeys.FILEMGR_CATALOGS_ONLY, new StdQueryExpression(this.getProductTypeNames(this.repositoryManager.getProductTypes()))));
			for (TransactionalMetadata transactionalMetadata : catalogService.getAllPages(queryPager)) {
				TransactionalMetadata existingMet = transactionalMetadatas.get(transactionalMetadata.getTransactionId().toString());
				if (existingMet != null) {
					existingMet.getMetadata().addMetadata(transactionalMetadata.getMetadata().getHashtable(), false);
				}else {
					transactionalMetadatas.put(transactionalMetadata.getTransactionId().toString(), transactionalMetadata);
				}
			}
			List<Product> products = new Vector<Product>();
			for (String currentKey : transactionalMetadatas.keySet())
				products.add(CatalogServiceUtils.asProduct(transactionalMetadatas.get(currentKey), this.repositoryManager.getProductTypes()));
			return products;		
		}catch (Exception e) {
			throw new CatalogException("", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.oodt.cas.filemgr.catalog.Catalog#getProductsByProductType(org.apache.oodt.cas.filemgr.structs.ProductType)
	 */
	public List<Product> getProductsByProductType(ProductType type)
			throws CatalogException {
		try {
			QueryPager queryPager = this.catalogService.query(new CustomWrapperQueryExpression(CatalogServiceMetKeys.FILEMGR_CATALOGS_ONLY, new StdQueryExpression(this.getProductTypeNames(Collections.singletonList(type)))));
			List<TransactionalMetadata> metadata = this.catalogService.getAllPages(queryPager);
			List<Product> products = new Vector<Product>();
			for (TransactionalMetadata currentMet : metadata)
				products.add(CatalogServiceUtils.asProduct(currentMet, this.repositoryManager.getProductTypes()));
			return products;
		}catch (Exception e) {
			throw new CatalogException("", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.oodt.cas.filemgr.catalog.Catalog#getReducedMetadata(org.apache.oodt.cas.filemgr.structs.Product, java.util.List)
	 */
	public Metadata getReducedMetadata(Product product, List<String> elements)
			throws CatalogException {
		HashSet<String> elementsHash = new HashSet<String>(elements);
		Metadata metadata = this.getMetadata(product);
		Metadata reducedMetadata = new Metadata();
		for (String elementName : elementsHash) 
			if (metadata.containsKey(elementName))
				reducedMetadata.addMetadata(elementName, metadata.getAllMetadata(elementName));
		if (elementsHash.contains(CatalogService.CATALOG_SERVICE_TRANSACTION_ID_MET_KEY))
			reducedMetadata.replaceMetadata(CatalogService.CATALOG_SERVICE_TRANSACTION_ID_MET_KEY, product.getProductId());
		if (elementsHash.contains(CatalogService.CATALOG_IDS_MET_KEY))
			reducedMetadata.replaceMetadata(CatalogService.CATALOG_IDS_MET_KEY, metadata.getAllMetadata(CatalogService.CATALOG_IDS_MET_KEY));
		return reducedMetadata;
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.oodt.cas.filemgr.catalog.Catalog#getTopNProducts(int)
	 */
	public List<Product> getTopNProducts(int n) throws CatalogException {
		try {
			CustomQueryExpression topN_QE = new CustomQueryExpression(CatalogServiceMetKeys.TOP_N_QUERY_EXPRESSION);
			topN_QE.setProperty(CatalogServiceMetKeys.N_CUSTOM_KEY, n+"");
			Page page = this.catalogService.getPage(new PageInfo(n, PageInfo.FIRST_PAGE), new CustomWrapperQueryExpression(CatalogServiceMetKeys.FILEMGR_CATALOGS_ONLY, topN_QE));
			List<TransactionalMetadata> metadata = this.catalogService.getMetadata(page);
//			QueryPager queryPager = this.catalogService.query(topN_QE);
//			queryPager.setPageInfo(new PageInfo(n, 1));
//			List<TransactionalMetadata> metadata = this.catalogService.getNextPage(queryPager);
			List<Product> products = new Vector<Product>();
			for (TransactionalMetadata currentMet : metadata)
				products.add(CatalogServiceUtils.asProduct(currentMet, this.repositoryManager.getProductTypes()));
			return products;
		}catch (Exception e) {
			throw new CatalogException("", e);
		}	
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.oodt.cas.filemgr.catalog.Catalog#getTopNProducts(int, org.apache.oodt.cas.filemgr.structs.ProductType)
	 */
	public List<Product> getTopNProducts(int n, ProductType type)
			throws CatalogException {
		try {
//			QueryPager queryPager = this.catalogService.query(new StdQueryExpression(this.getProductTypeNames(Collections.singletonList(type))));
			Page page = this.catalogService.getPage(new PageInfo(n, PageInfo.FIRST_PAGE), new CustomWrapperQueryExpression(CatalogServiceMetKeys.FILEMGR_CATALOGS_ONLY, new StdQueryExpression(this.getProductTypeNames(Collections.singletonList(type)))));
			List<TransactionalMetadata> metadata = this.catalogService.getMetadata(page);
//			queryPager.setPageInfo(new PageInfo(n, 1));
//			List<TransactionalMetadata> metadata = this.catalogService.getNextPage(queryPager);
			List<Product> products = new Vector<Product>();
			for (TransactionalMetadata currentMet : metadata)
				products.add(CatalogServiceUtils.asProduct(currentMet, this.repositoryManager.getProductTypes()));
			return products;
		}catch (Exception e) {
			throw new CatalogException("", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.oodt.cas.filemgr.catalog.Catalog#getValidationLayer()
	 */
	public ValidationLayer getValidationLayer() throws CatalogException {
		return this.validationLayer;
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.oodt.cas.filemgr.catalog.Catalog#modifyProduct(org.apache.oodt.cas.filemgr.structs.Product)
	 */
	public void modifyProduct(Product product) throws CatalogException {
		this.addProduct(product);
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.oodt.cas.filemgr.catalog.Catalog#pagedQuery(org.apache.oodt.cas.filemgr.structs.Query, org.apache.oodt.cas.filemgr.structs.ProductType, int)
	 */
	public ProductPage pagedQuery(Query query, ProductType type, int pageNum)
			throws CatalogException {
		try {
//			QueryPager queryPager = this.catalogService.query((query.getCriteria() == null || query.getCriteria().size() <= 0) ? new StdQueryExpression(this.getProductTypeNames(Collections.singletonList(type))) : CatalogServiceUtils.asQueryExpression(query.getCriteria(), Collections.singleton(this.getCompleteProductType(type).getName())));
			Page page = this.catalogService.getPage(new PageInfo(this.pageSize, pageNum), new CustomWrapperQueryExpression(CatalogServiceMetKeys.FILEMGR_CATALOGS_ONLY, (query.getCriteria() == null || query.getCriteria().size() <= 0) ? new StdQueryExpression(this.getProductTypeNames(Collections.singletonList(type))) : CatalogServiceUtils.asQueryExpression(query.getCriteria(), Collections.singleton(this.getCompleteProductType(type).getName()))));
//			queryPager.setPageSize(this.pageSize);
//			queryPager.setPageNumber(pageNum - 1);
			return CatalogServiceUtils.getProductPage(page, this.catalogService, this.repositoryManager.getProductTypes());
		}catch (Exception e) {
			throw new CatalogException("", e);
		}
	}
	
	public List<QueryResult> getFullResults(Query query, List<ProductType> types) throws CatalogException {
		try {
			List<QueryResult> queryResults = new Vector<QueryResult>();
			//ProductTypeQueryExpression ptQuery = new ProductTypeQueryExpression();
			//ptQuery.setProductType(type);
			//ptQuery.setQueryExpression(CatalogServiceUtils.asQueryExpression(query.getCriteria()));
			QueryPager queryPager = this.catalogService.query(new CustomWrapperQueryExpression(CatalogServiceMetKeys.FILEMGR_CATALOGS_ONLY, (query.getCriteria() == null || query.getCriteria().size() <= 0) ? new StdQueryExpression(this.getProductTypeNames(types)) : CatalogServiceUtils.asQueryExpression(query.getCriteria(), this.getProductTypeNames(types))));
			//QueryPager queryPager = this.catalogService.query(ptQuery);
			List<TransactionalMetadata> transactionalMetadatas = this.catalogService.getAllPages(queryPager);
			List<Metadata> retMet = new Vector<Metadata>();
			List<String> productIds = new Vector<String>();
			for (TransactionalMetadata transactionalMetadata : transactionalMetadatas) {
				HashSet<String> elementsHash = new HashSet<String>();
				for (ProductType type : types)
					for (Element element : this.validationLayer.getElements(type))
						elementsHash.add(element.getElementName());
				elementsHash.add(CatalogService.CATALOG_SERVICE_TRANSACTION_ID_MET_KEY);
				elementsHash.add(CatalogService.CATALOG_IDS_MET_KEY);
				Metadata m = new Metadata();
				for (String element : elementsHash) {
					List<String> val = transactionalMetadata.getMetadata().getAllMetadata(element);
					if (val != null)
						m.addMetadata(element, val);
				}
				if (elementsHash.contains(CatalogService.CATALOG_SERVICE_TRANSACTION_ID_MET_KEY))
					m.replaceMetadata(CatalogService.CATALOG_SERVICE_TRANSACTION_ID_MET_KEY, transactionalMetadata.getTransactionId().toString());
				if (elementsHash.contains(CatalogService.CATALOG_IDS_MET_KEY))
					m.replaceMetadata(CatalogService.CATALOG_IDS_MET_KEY, StringUtils.join(transactionalMetadata.getCatalogIds().iterator(), ','));
				retMet.add(m);
				productIds.add(transactionalMetadata.getTransactionId().toString());
			}
			List<Product> products = this.getProductsByIds(productIds);
			for (int i = 0; i < products.size(); i++)
				queryResults.add(new QueryResult(products.get(i), retMet.get(i)));
			return queryResults;
		}catch (Exception e) {
			throw new CatalogException("", e);
		}   
	}

	public List<Metadata> getReducedMetadata(Query query, List<ProductType> types, List<String> elements) throws CatalogException {
		try {
			List<Metadata> metadatas = new Vector<Metadata>();
			//ProductTypeQueryExpression ptQuery = new ProductTypeQueryExpression();
			//ptQuery.setProductType(type);
			//ptQuery.setQueryExpression(CatalogServiceUtils.asQueryExpression(query.getCriteria()));
			QueryPager queryPager = this.catalogService.query(new CustomWrapperQueryExpression(CatalogServiceMetKeys.FILEMGR_CATALOGS_ONLY, (query.getCriteria() == null || query.getCriteria().size() <= 0) ? new StdQueryExpression(this.getProductTypeNames(types)) : CatalogServiceUtils.asQueryExpression(query.getCriteria(), this.getProductTypeNames(types))));
			//QueryPager queryPager = this.catalogService.query(ptQuery);
			List<TransactionalMetadata> transactionalMetadatas = this.catalogService.getAllPages(queryPager);
			for (TransactionalMetadata transactionalMetadata : transactionalMetadatas) {
				if (elements == null) {
					elements = new Vector<String>();
					for (ProductType type : types)
						for (Element element : this.validationLayer.getElements(type))
							elements.add(element.getElementName());
					elements.add(CatalogService.CATALOG_SERVICE_TRANSACTION_ID_MET_KEY);
					elements.add(CatalogService.CATALOG_IDS_MET_KEY);
				}
				Metadata m = new Metadata();
				HashSet<String> elementsHash = new HashSet<String>(elements);
				for (String element : elementsHash) {
					List<String> val = transactionalMetadata.getMetadata().getAllMetadata(element);
					if (val != null)
						m.addMetadata(element, val);
				}
				if (elementsHash.contains(CatalogService.CATALOG_SERVICE_TRANSACTION_ID_MET_KEY))
					m.replaceMetadata(CatalogService.CATALOG_SERVICE_TRANSACTION_ID_MET_KEY, transactionalMetadata.getTransactionId().toString());
				if (elementsHash.contains(CatalogService.CATALOG_IDS_MET_KEY))
					m.replaceMetadata(CatalogService.CATALOG_IDS_MET_KEY, StringUtils.join(transactionalMetadata.getCatalogIds().iterator(), ','));
				metadatas.add(m);
			}
			return metadatas;
		}catch (Exception e) {
			throw new CatalogException("", e);
		}    
	}

    public List<Metadata> getReducedMetadata(Query query, ProductType type, List<String> elements)
			throws CatalogException {
    	return this.getReducedMetadata(query, Collections.singletonList(type), elements);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.apache.oodt.cas.filemgr.catalog.Catalog#query(org.apache.oodt.cas.filemgr.structs.Query, org.apache.oodt.cas.filemgr.structs.ProductType)
	 */
	public List<String> query(Query query, ProductType type)
			throws CatalogException {
		try {
			List<String> productIds = new Vector<String>();
			//ProductTypeQueryExpression ptQuery = new ProductTypeQueryExpression();
			//ptQuery.setProductType(type);
			//ptQuery.setQueryExpression(CatalogServiceUtils.asQueryExpression(query.getCriteria()));
			QueryPager queryPager = this.catalogService.query(new CustomWrapperQueryExpression(CatalogServiceMetKeys.FILEMGR_CATALOGS_ONLY, (query.getCriteria() == null || query.getCriteria().size() <= 0) ? new StdQueryExpression(Collections.singleton(this.getCompleteProductType(type).getName())) : CatalogServiceUtils.asQueryExpression(query.getCriteria(), Collections.singleton(this.getCompleteProductType(type).getName()))));
			//QueryPager queryPager = this.catalogService.query(ptQuery);
//			List<TransactionId<?>> catalogServiceTransactionIds = this.catalogService.getTransactionIdsForAllPages(queryPager);
//			System.out.println("PRODUCT_IDS : " + catalogServiceTransactionIds);
//			for (TransactionId<?> transactionId : catalogServiceTransactionIds)
//				productIds.add(transactionId.toString());
			return this.getProductIds(queryPager.getTransactionReceipts());
		}catch (Exception e) {
			throw new CatalogException("", e);
		}
	}
	
	public List<String> query(Query query, List<ProductType> types) throws CatalogException {
		try {
			List<String> productIds = new Vector<String>();
			//ProductTypeQueryExpression ptQuery = new ProductTypeQueryExpression();
			//ptQuery.setProductType(type);
			//ptQuery.setQueryExpression(CatalogServiceUtils.asQueryExpression(query.getCriteria()));
			Set<String> productTypeNames = this.getProductTypeNames(types);
			QueryPager queryPager = this.catalogService.query(new CustomWrapperQueryExpression(CatalogServiceMetKeys.FILEMGR_CATALOGS_ONLY, (query.getCriteria() == null || query.getCriteria().size() <= 0) ? new StdQueryExpression(productTypeNames) : CatalogServiceUtils.asQueryExpression(query.getCriteria(), productTypeNames)));
			//QueryPager queryPager = this.catalogService.query(ptQuery);
//			List<TransactionId<?>> catalogServiceTransactionIds = this.catalogService.getTransactionIdsForAllPages(queryPager);
//			System.out.println("PRODUCT_IDS : " + catalogServiceTransactionIds);
//			for (TransactionId<?> transactionId : catalogServiceTransactionIds)
//				productIds.add(transactionId.toString());
			return this.getProductIds(queryPager.getTransactionReceipts());
		}catch (Exception e) {
			throw new CatalogException("", e);
		}
	}

	protected List<String> getProductIds(List<TransactionReceipt> transactionReceipts) {
		Vector<String> productIds = new Vector<String>();
		for (TransactionReceipt transactionReceipt : transactionReceipts)
			productIds.add(transactionReceipt.getTransactionId().toString());
		return productIds;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.apache.oodt.cas.filemgr.catalog.Catalog#removeMetadata(org.apache.oodt.cas.metadata.Metadata, org.apache.oodt.cas.filemgr.structs.Product)
	 */
	public void removeMetadata(Metadata metadata, Product product)
			throws CatalogException {
		try {
			metadata.replaceMetadata(CatalogActions.CATALOG_ACTION_KEY, CatalogActions.REMOVE_METADATA);
			this.catalogService.delete(CatalogServiceUtils.asCatalogServiceMetadata(product, metadata));
		}catch (Exception e) {
			throw new CatalogException("", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.oodt.cas.filemgr.catalog.Catalog#removeProduct(org.apache.oodt.cas.filemgr.structs.Product)
	 */
	public void removeProduct(Product product) throws CatalogException {
		try {
			Metadata actionMetadata = new Metadata();
			actionMetadata.replaceMetadata(CatalogActions.CATALOG_ACTION_KEY, CatalogActions.REMOVE_PRODUCT);
			this.catalogService.delete(CatalogServiceUtils.asCatalogServiceMetadata(product, actionMetadata));
		}catch (Exception e) {
			throw new CatalogException("", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.oodt.cas.filemgr.catalog.Catalog#setProductTransferStatus(org.apache.oodt.cas.filemgr.structs.Product)
	 */
	public void setProductTransferStatus(Product product)
			throws CatalogException {
		try {
			Metadata actionMetadata = new Metadata();
			actionMetadata.replaceMetadata(CatalogActions.CATALOG_ACTION_KEY, CatalogActions.STATUS_UPDATE);
			actionMetadata.replaceMetadata(CatalogService.ENABLE_UPDATE_MET_KEY, "true");
			this.catalogService.ingest(CatalogServiceUtils.asCatalogServiceMetadata(product, actionMetadata));
		}catch (Exception e) {
			throw new CatalogException("", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.oodt.cas.filemgr.util.Pagination#getFirstPage(org.apache.oodt.cas.filemgr.structs.ProductType)
	 */
	public ProductPage getFirstPage(ProductType type) {
		try {
			Page page = this.catalogService.getPage(new PageInfo(this.pageSize, PageInfo.FIRST_PAGE), new CustomWrapperQueryExpression(CatalogServiceMetKeys.FILEMGR_CATALOGS_ONLY, new StdQueryExpression(this.getProductTypeNames(Collections.singletonList(type)))));
//			QueryPager queryPager = this.catalogService.query(new StdQueryExpression(this.getProductTypeNames(Collections.singletonList(type))));
//			queryPager.setPageNumber(0);
//			queryPager.setPageSize(this.pageSize);
			return CatalogServiceUtils.getProductPage(page, this.catalogService, this.repositoryManager.getProductTypes());
		}catch (Exception e) {
			e.printStackTrace();
			return ProductPage.blankPage();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.oodt.cas.filemgr.util.Pagination#getLastProductPage(org.apache.oodt.cas.filemgr.structs.ProductType)
	 */
	public ProductPage getLastProductPage(ProductType type) {
		try {
			Page page = this.catalogService.getPage(new PageInfo(this.pageSize, PageInfo.LAST_PAGE), new CustomWrapperQueryExpression(CatalogServiceMetKeys.FILEMGR_CATALOGS_ONLY, new StdQueryExpression(this.getProductTypeNames(Collections.singletonList(type)))));
//			Page page = this.catalogService.getPage(new PageInfo(this.pageSize, 1), );
//			page = this.catalogService.getPage(new PageInfo(this.pageSize, page.getTotalPages() - 1), new StdQueryExpression(this.getProductTypeNames(Collections.singletonList(type))));
//			QueryPager queryPager = this.catalogService.query(new StdQueryExpression(this.getProductTypeNames(Collections.singletonList(type))));
//			queryPager.setPageSize(this.pageSize);
//			queryPager.setPageNumber(queryPager.getTotalNumPages() - 1);
			return CatalogServiceUtils.getProductPage(page, this.catalogService, this.repositoryManager.getProductTypes());
		}catch (Exception e) {
			e.printStackTrace();
			return ProductPage.blankPage();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.oodt.cas.filemgr.util.Pagination#getNextPage(org.apache.oodt.cas.filemgr.structs.ProductType, org.apache.oodt.cas.filemgr.structs.ProductPage)
	 */
	public ProductPage getNextPage(ProductType type, ProductPage currentPage) {
		try {
			Page page = this.catalogService.getPage(new PageInfo(this.pageSize, currentPage.getPageNum() + 1), new CustomWrapperQueryExpression(CatalogServiceMetKeys.FILEMGR_CATALOGS_ONLY, new StdQueryExpression(this.getProductTypeNames(Collections.singletonList(type)))));
//			QueryPager queryPager = this.catalogService.query(new StdQueryExpression(this.getProductTypeNames(Collections.singletonList(type))));
//			queryPager.setPageSize(this.pageSize);
//			queryPager.setPageNumber(currentPage.getPageNum() + 1);
			return CatalogServiceUtils.getProductPage(page, this.catalogService, this.repositoryManager.getProductTypes());
		}catch (Exception e) {
			e.printStackTrace();
			return ProductPage.blankPage();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.oodt.cas.filemgr.util.Pagination#getPrevPage(org.apache.oodt.cas.filemgr.structs.ProductType, org.apache.oodt.cas.filemgr.structs.ProductPage)
	 */
	public ProductPage getPrevPage(ProductType type, ProductPage currentPage) {
		try {
			Page page = this.catalogService.getPage(new PageInfo(this.pageSize, currentPage.getPageNum() - 1), new CustomWrapperQueryExpression(CatalogServiceMetKeys.FILEMGR_CATALOGS_ONLY, new StdQueryExpression(this.getProductTypeNames(Collections.singletonList(type)))));
//			QueryPager queryPager = this.catalogService.query(new StdQueryExpression(this.getProductTypeNames(Collections.singletonList(type))));
//			queryPager.setPageSize(this.pageSize);
//			queryPager.setPageNumber(currentPage.getPageNum() - 1);
			return CatalogServiceUtils.getProductPage(page, this.catalogService, this.repositoryManager.getProductTypes());
		}catch (Exception e) {
			e.printStackTrace();
			return ProductPage.blankPage();
		}
	}
	
	protected Set<String> getProductTypeNames(List<ProductType> productTypes) throws RepositoryManagerException {
		HashSet<String> productTypeNames = new HashSet<String>();
		for (ProductType productType : productTypes)
			productTypeNames.add(this.getCompleteProductType(productType).getName());
		return productTypeNames;
	}
	
	protected List<ProductType> getCompleteProductTypes(List<ProductType> productTypes) throws RepositoryManagerException {
		List<ProductType> completePTs = new Vector<ProductType>();
		for (ProductType type : productTypes)
			completePTs.add(this.getCompleteProductType(type));
		return completePTs;
	}
	
	protected ProductType getCompleteProductType(ProductType productType) throws RepositoryManagerException {
		ProductType completeProductType = null;
		if (productType.getProductTypeId() != null) 
			completeProductType = this.repositoryManager.getProductTypeById(productType.getProductTypeId());
		if (completeProductType == null && productType.getName() != null)
			completeProductType = this.repositoryManager.getProductTypeByName(productType.getName());
		return completeProductType;
	}

}
