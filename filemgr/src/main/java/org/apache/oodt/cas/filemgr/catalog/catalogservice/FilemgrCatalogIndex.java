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

//JDK imports
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports
import org.apache.oodt.cas.catalog.exception.CatalogIndexException;
import org.apache.oodt.cas.catalog.exception.IngestServiceException;
import org.apache.oodt.cas.catalog.exception.QueryServiceException;
import org.apache.oodt.cas.catalog.page.IndexPager;
import org.apache.oodt.cas.catalog.page.IngestReceipt;
import org.apache.oodt.cas.catalog.query.CustomQueryExpression;
import org.apache.oodt.cas.catalog.query.CustomWrapperQueryExpression;
import org.apache.oodt.cas.catalog.query.QueryExpression;
import org.apache.oodt.cas.catalog.struct.Index;
import org.apache.oodt.cas.catalog.struct.IngestService;
import org.apache.oodt.cas.catalog.struct.QueryService;
import org.apache.oodt.cas.catalog.struct.TransactionId;
import org.apache.oodt.cas.catalog.struct.TransactionIdFactory;
import org.apache.oodt.cas.catalog.term.TermBucket;
import org.apache.oodt.cas.filemgr.catalog.Catalog;
import org.apache.oodt.cas.filemgr.catalog.CatalogFactory;
import org.apache.oodt.cas.filemgr.metadata.CoreMetKeys;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.TemporalProduct;
import org.apache.oodt.cas.filemgr.structs.ProductPage;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.Query;
import org.apache.oodt.cas.filemgr.structs.QueryCriteria;
import org.apache.oodt.cas.metadata.Metadata;

/**
 * @author bfoster
 * @version $Revision$
 * 
 * <p></p>
 * 
 */
public class FilemgrCatalogIndex implements Index, QueryService, IngestService {

//	private static final long serialVersionUID = 8331106878686380577L;

	private static Logger LOG = Logger.getLogger(FilemgrCatalogIndex.class.getName());
	
	public static enum PropertyKeys {
		SUPPORTED_PRODUCT_TYPES,
		NUMBER_OF_PRODUCTS;
		
		public static String generateNumOfProductsKeyName(String productTypeName) {
			return productTypeName + ":" + NUMBER_OF_PRODUCTS.toString();
		}
	}
	
	protected Catalog fmCatalog;
	protected CatalogFactory fmCatalogFactory;
	protected TransactionIdFactory transactionIdFactory;
	protected List<ProductType> supportedProductTypes;
	protected int fmCatalogPageSize;
	
	public FilemgrCatalogIndex() {}
	
	public CatalogFactory getFmCatalogFactory() {
		return this.fmCatalogFactory;
	}

	public void setFmCatalogFactory(CatalogFactory fmCatalogFactory) {
		this.fmCatalogFactory = fmCatalogFactory;
		this.fmCatalog = fmCatalogFactory.createCatalog();
		this.fmCatalogPageSize = this.fmCatalog.getPageSize();
	}
	
	public void setSupportedProductTypes(List<ProductType> supportedProductTypes) {
		this.supportedProductTypes = new Vector<ProductType>(supportedProductTypes);
	}
	
	public List<ProductType> getSupportedProductTypes() {
		if (this.supportedProductTypes != null)
			return new Vector<ProductType>(this.supportedProductTypes);
		else 
			return Collections.emptyList();
	}
	
	public TransactionIdFactory getTransactionIdFactory() {
		return this.transactionIdFactory;
	}
	
	public void setTransactionIdFactory(String transactionIdFactory) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		this.transactionIdFactory = (TransactionIdFactory) Class.forName(transactionIdFactory).newInstance();
	}
	
	public String getProperty(String key) throws CatalogIndexException {
		try {
			String[] splitKey = key.split(":");
			if (key.equals(PropertyKeys.SUPPORTED_PRODUCT_TYPES.toString())) {
				String productTypeStringList = "";
				for (ProductType productType : this.supportedProductTypes)
					productTypeStringList += productType.getName() + ",";
				return productTypeStringList.replaceAll(",$", "");
			}else if (splitKey.length > 1 && splitKey[1].equals(PropertyKeys.NUMBER_OF_PRODUCTS.toString())) {
				for (ProductType productType : this.supportedProductTypes) {
					if (productType.getName().equals(splitKey[0])) 
						return Integer.toString(this.fmCatalog.getNumProducts(productType));
				}
				return null;
			}else {
				return this.getProperties().getProperty(key);
			}
		}catch (Exception e) {
			throw new CatalogIndexException("Failed to get Property '" + key + "' from index : " + e.getMessage(), e);
		}
	}
	
	public Properties getProperties() throws CatalogIndexException {
		try {
			Properties fmCatalogProps = new Properties();
			String productTypeStringList = "";
			int totalNumOfProducts = 0;
			for (ProductType productType : this.supportedProductTypes) {
				productTypeStringList += productType.getName() + ",";
				int numOfProducts = this.fmCatalog.getNumProducts(productType);
				fmCatalogProps.setProperty(PropertyKeys.generateNumOfProductsKeyName(productType.getName()), Integer.toString(numOfProducts));
				totalNumOfProducts += numOfProducts;
			}
			fmCatalogProps.setProperty(PropertyKeys.NUMBER_OF_PRODUCTS.toString(), Integer.toString(totalNumOfProducts));
			fmCatalogProps.setProperty(PropertyKeys.SUPPORTED_PRODUCT_TYPES.toString(), productTypeStringList.replaceAll(",$", ""));
			return fmCatalogProps;
		}catch (Exception e) {
			throw new CatalogIndexException("Failed to get Properties from index : " + e.getMessage(), e);
		}
	}
//	
//	public List<TransactionReceipt> getPage(IndexPager indexPager) throws CatalogIndexException {
//		try {
//			List<String> elements = new Vector<String>(); 
//			Collections.addAll(elements, CoreMetKeys.PRODUCT_ID,CoreMetKeys.PRODUCT_RECEVIED_TIME);
//			HashMap<String, List<TransactionReceipt>> productTypeToReceiptMap = new  HashMap<String, List<TransactionReceipt>>();
//			
//			for (ProductType productType : this.supportedProductTypes.values()) {
//				List<Metadata> metadatas = this.fmCatalog.getReducedMetadata(new Query(), productType, elements);
//				for (int i = (indexPager.getPageNumber() * indexPager.getPageSize()); i < metadatas.size() && i < ((indexPager.getPageNumber() + 1) * indexPager.getPageSize()); i++) {
//					Metadata metadata = metadatas.get(i);
//					List<TransactionReceipt> transactionReceipts = productTypeToReceiptMap.get(productType.getProductTypeId());
//					if (transactionReceipts == null)
//						transactionReceipts = new Vector<TransactionReceipt>();
//					transactionReceipts.add(this.generateReceipt(metadata));
//					productTypeToReceiptMap.put(productType.getProductTypeId(), transactionReceipts);
//				}
//			}
//
//			
//			List<Metadata> metadatas = null;
//			if (queryExpression instanceof ProductTypeQueryExpression) {
//				Query fmQuery = new Query();
//				if (((ProductTypeQueryExpression) queryExpression).getQueryExpression() != null)
//					fmQuery.addCriterion(CatalogServiceUtils.asQueryCriteria(((ProductTypeQueryExpression) queryExpression).getQueryExpression()));
//				LOG.log(Level.INFO, "Performing Filemgr Query '" + fmQuery + "'");
//				metadatas = this.fmCatalog.getReducedMetadata(fmQuery, ((ProductTypeQueryExpression) queryExpression).getProductType(), elements);
//			}else if (queryExpression instanceof ProductNameQueryExpression) {
//				Product product = this.fmCatalog.getProductByName(((ProductNameQueryExpression) queryExpression).getProductName());
//				if (product != null)
//					metadatas = Collections.singletonList(this.fmCatalog.getReducedMetadata(product, elements));
//				else
//					metadatas = Collections.emptyList();
//			}else {
//				throw new QueryServiceException("Unknown Query type");
//			}
//			List<TransactionReceipt> transactionIds = new Vector<TransactionReceipt>();
//			for (Metadata metadata : metadatas) 
//				transactionIds.add(generateReceipt(metadata));
//			return transactionIds;
//		}catch (Exception e) {
//			throw new QueryServiceException("Failed to perform CatalogQuery '" + queryExpression + "' : " + e.getMessage(), e);
//		}
//	}

	
	public List<TransactionId<?>> getPage(IndexPager indexPager) throws CatalogIndexException {
		try {

			List<Product> productsPage = new Vector<Product>();
			int startProductLoc = indexPager.getPageSize() * indexPager.getPageNum();
			int curNumOfProds = 0;
			
			for (ProductType productType : this.supportedProductTypes) {
				int curProdTypeNumOfProds = this.fmCatalog.getNumProducts(productType);
				//System.out.println("curProdTypeNumOfProds: " + curProdTypeNumOfProds);
				int prevNumOfProds = curNumOfProds;
				curNumOfProds += curProdTypeNumOfProds;
				
				if (curNumOfProds >= startProductLoc) {
					int locInCurProdType = startProductLoc - prevNumOfProds;
					//System.out.println("locInCurProdType: " + locInCurProdType + " startProductLoc:" + startProductLoc + " prevNumOfProds: " + prevNumOfProds);
					int curPtPage = locInCurProdType / this.fmCatalogPageSize;
					int curPageShift = locInCurProdType % this.fmCatalogPageSize;
					//System.out.println("CurPage: " + curPtPage + " shift: " + curPageShift);
					ProductPage pp = this.fmCatalog.pagedQuery(new Query(), productType, curPtPage + 1);
					for (int i = curPageShift; i < pp.getPageProducts().size(); i++)
						productsPage.add(pp.getPageProducts().get(i));
					
					while (productsPage.size() < indexPager.getPageSize()) {
						int lastPage = pp.getPageNum();
						pp = this.fmCatalog.getNextPage(productType, pp);
						if (pp.getPageNum() > lastPage && pp.getPageProducts() != null && pp.getPageProducts().size() > 0) {
							productsPage.addAll(pp.getPageProducts());
						}else {
							break;
						}
					}
					
					startProductLoc += curProdTypeNumOfProds;
					
					if (productsPage.size() == indexPager.getPageSize()) {
						break;
					}else if (productsPage.size() > indexPager.getPageSize()) {
						List<Product> productsPageSubList = new Vector<Product>();
						for (int i = 0; i < indexPager.getPageSize(); i++)
							productsPageSubList.add(productsPage.get(i));
						productsPage = productsPageSubList;
						break;
					}
				}
			}
			// convert product ids to transaction ids
			LinkedHashSet<TransactionId<?>> transactionIds = new LinkedHashSet<TransactionId<?>>();
			for (Product product : productsPage)
				transactionIds.add(this.generateTransactionId(product.getProductId()));
			return new Vector<TransactionId<?>>(transactionIds);
		}catch(Exception e) {
			throw new CatalogIndexException("", e);
		}
	}

	public boolean hasTransactionId(TransactionId<?> catalogTransactionId) throws CatalogIndexException {
		try {
			Product product = this.fmCatalog.getProductById(catalogTransactionId.toString());
			return product != null && this.isSupportedProductType(product.getProductType());
		}catch(Exception e) {
			throw new CatalogIndexException("", e);
		}
	}

	public boolean delete(TransactionId<?> transactionId) throws IngestServiceException {
		try {
			Product product = this.fmCatalog.getProductById(transactionId.toString());
			product.setProductType(this.getCompleteProductType(product.getProductType()));
			if (product != null) {
				LOG.log(Level.INFO, "Deleting Product for TransactionId: " + transactionId);
				this.fmCatalog.removeProduct(product);
				return true;
			}else {
				LOG.log(Level.INFO, "Failed to find existing Product for TransactionId: " + transactionId);
				return false;
			}
		}catch(Exception e) {
			throw new IngestServiceException("Failed to delete product for TransactionId '" + transactionId + "' : " + e.getMessage(), e);
		}
	}

	public IngestReceipt ingest(List<TermBucket> termBuckets) throws IngestServiceException {
		TransactionId<?> transactionId = null;
		try {
			transactionId = this.getTransactionIdFactory().createNewTransactionId();
			Product product = CatalogServiceUtils.asProduct(termBuckets.get(0), new Vector<ProductType>(this.supportedProductTypes));
			product.setProductType(this.getCompleteProductType(product.getProductType()));
			Metadata metadata = CatalogServiceUtils.asMetadata(termBuckets.get(0));
			String catalogAction = metadata.getMetadata(CatalogActions.CATALOG_ACTION_KEY);
			if (catalogAction.equals(CatalogActions.INGEST_PRODUCT)) {
				LOG.log(Level.INFO, "Ingesting Product metadata for TransactionId: " + transactionId);
				this.fmCatalog.addProduct(product);
			}else {
				throw new IngestServiceException("Unsupport CatalogAction: '" + catalogAction + "' for TransactionId '" + transactionId + "'");
			}
//			String productReceivedTime = metadata.getMetadata(CoreMetKeys.PRODUCT_RECEVIED_TIME);
//			if (productReceivedTime == null)
//				metadata.replaceMetadata(CoreMetKeys.PRODUCT_RECEVIED_TIME, DateConvert.isoFormat(new Date()));
//			metadata.replaceMetadata(CoreMetKeys.PRODUCT_ID, product.getProductId());
			return new IngestReceipt(this.generateTransactionId(product.getProductId()), (product instanceof TemporalProduct && ((TemporalProduct) product).getProductReceivedTime() != null ? ((TemporalProduct) product).getProductReceivedTime() : new Date()));
		}catch (Exception e) {
			throw new IngestServiceException("Failed to Ingest Product for TransactionId: " + transactionId, e);
		}
	}

	public boolean reduce(TransactionId<?> transactionId,
			List<TermBucket> termBuckets) throws IngestServiceException {
		try {
			Product product = this.fmCatalog.getProductById(transactionId.toString());
			product.setProductType(this.getCompleteProductType(product.getProductType()));
			Metadata metadata = CatalogServiceUtils.asMetadata(termBuckets.get(0));
			String catalogAction = metadata.getMetadata(CatalogActions.CATALOG_ACTION_KEY);
			if (catalogAction.equals(CatalogActions.REMOVE_PRODUCT)) {
				LOG.log(Level.INFO, "Removing Product for TransactionId: " + transactionId);
				this.fmCatalog.removeProduct(product);
			}else if (catalogAction.equals(CatalogActions.REMOVE_METADATA)) {
				LOG.log(Level.INFO, "Removing Product Metadata for TransactionId: " + transactionId);
				this.fmCatalog.removeMetadata(metadata, product);
			}
			return true;
		}catch (Exception e) {
			throw new IngestServiceException("", e);
		}
	}

	public IngestReceipt update(TransactionId<?> transactionId,
			List<TermBucket> termBuckets) throws IngestServiceException {
		try {
			Product product = CatalogServiceUtils.asProduct(termBuckets.get(0), new Vector<ProductType>(this.supportedProductTypes));
			product.setProductId(transactionId.toString());
			product.setProductType(this.getCompleteProductType(product.getProductType()));
			Metadata metadata = CatalogServiceUtils.asMetadata(termBuckets.get(0));
			metadata.replaceMetadata(CoreMetKeys.PRODUCT_ID, product.getProductId());
			String catalogAction = metadata.getMetadata(CatalogActions.CATALOG_ACTION_KEY);
			if (catalogAction.equals(CatalogActions.INGEST_PRODUCT)) {
				LOG.log(Level.INFO, "Modifying Product for TransactionId: " + transactionId);
				this.fmCatalog.modifyProduct(product);
			}else if (catalogAction.equals(CatalogActions.STATUS_UPDATE)) {
				LOG.log(Level.INFO, "Updating Product transfer status for TransactionId: " + transactionId);
				this.fmCatalog.setProductTransferStatus(product);
			}else if (catalogAction.equals(CatalogActions.INGEST_METADATA)) {
				LOG.log(Level.INFO, "Adding Product Metadata for TransactionId: " + transactionId);
				this.fmCatalog.addMetadata(metadata, product);
			}else if (catalogAction.equals(CatalogActions.INGEST_REFERENCE)) {
				LOG.log(Level.INFO, "Adding Product References for TransactionId: " + transactionId);
				this.fmCatalog.addProductReferences(product);
			}
			return new IngestReceipt(transactionId, new Date());
		}catch (Exception e) {
			throw new IngestServiceException("", e);
		}
	}

	public List<TermBucket> getBuckets(TransactionId<?> transactionId) throws QueryServiceException {
		try {
			Product product = this.fmCatalog.getProductById(transactionId.toString());
			if (product != null) {
				product.setProductType(this.getCompleteProductType(product.getProductType()));
				product.setProductReferences(this.fmCatalog.getProductReferences(product));
				return Collections.singletonList(CatalogServiceUtils.asTermBucket(product, this.fmCatalog.getMetadata(product), this.fmCatalog.getValidationLayer().getElements(product.getProductType())));
			}else {
				return Collections.emptyList();
			}
		}catch (Exception e) {
			throw new QueryServiceException("", e);
		}
	}

	public Map<TransactionId<?>, List<TermBucket>> getBuckets(
			List<TransactionId<?>> transactionIds) throws QueryServiceException {
		HashMap<TransactionId<?>, List<TermBucket>> termBucketMap = new HashMap<TransactionId<?>, List<TermBucket>>();
		for (TransactionId<?> transactionId : transactionIds)
			termBucketMap.put(transactionId, this.getBuckets(transactionId));
		return termBucketMap;
	}

	public List<IngestReceipt> query(QueryExpression queryExpression)
			throws QueryServiceException {
		try {
//			List<String> elements = new Vector<String>(); 
//			Collections.addAll(elements, CoreMetKeys.PRODUCT_ID,CoreMetKeys.PRODUCT_RECEVIED_TIME);
			
			List<Product> products = new Vector<Product>();

			if (queryExpression instanceof CustomWrapperQueryExpression) {
				CustomWrapperQueryExpression cwqe = (CustomWrapperQueryExpression) queryExpression;
				if (cwqe.getMeaning().equals(CatalogServiceMetKeys.FILEMGR_CATALOGS_ONLY))
					queryExpression = ((CustomWrapperQueryExpression) queryExpression).getQueryExpression();
				else
					return Collections.emptyList();
			}
			
			if (queryExpression instanceof CustomQueryExpression) {
				CustomQueryExpression customQE = (CustomQueryExpression) queryExpression;
				if (customQE.getName().equals(CatalogServiceMetKeys.PRODUCT_NAME_QUERY_EXPRESSION)) {
					Product product = this.fmCatalog.getProductByName(customQE.getProperty(CatalogServiceMetKeys.PRODUCT_NAME_CUSTOM_KEY));
					if (product != null && product.getProductType() != null && this.isSupportedProductType(product.getProductType())) 
						products.add(product);
//					if (product != null && product.getProductType() != null && this.isSupportedProductType(product.getProductType())) {
//						product.setProductType(this.getCompleteProductType(product.getProductType()));
//						metadatas = Collections.singletonList(this.fmCatalog.getReducedMetadata(product, elements));
//					}else {
//						metadatas = Collections.emptyList();
//					}
				}else if (customQE.getName().equals(CatalogServiceMetKeys.TOP_N_QUERY_EXPRESSION)) {
					List<Product> topNProducts = this.fmCatalog.getTopNProducts(Integer.parseInt(customQE.getProperty(CatalogServiceMetKeys.N_CUSTOM_KEY)));
					if (topNProducts != null) {
						for (Product product : topNProducts) {
							if (product != null && product.getProductType() != null && this.isSupportedProductType(product.getProductType())) { 
								product.setProductType(this.getCompleteProductType(product.getProductType()));
								products.add(product);
//								Metadata m = this.fmCatalog.getMetadata(product);
//								m.replaceMetadata(CoreMetKeys.PRODUCT_ID, product.getProductId());
//								metadatas.add(m);
							}
						}
					}
				}else {
					throw new QueryServiceException("Unknown Custom Query type '" + (queryExpression != null ? queryExpression.getClass().getCanonicalName() : queryExpression) + "'");
				}
			}else {
				Query fmQuery = new Query();
				QueryCriteria qc = CatalogServiceUtils.asQueryCriteria(queryExpression);
				if (qc != null)
					fmQuery.addCriterion(qc);
				LOG.log(Level.INFO, "Performing Filemgr Query '" + fmQuery + "'");
				List<ProductType> supportedSubsetOfTypes = new Vector<ProductType>();
				Set<String> productTypeNames = queryExpression.getBucketNames();
				if (productTypeNames == null)
					productTypeNames = this.getSupportedProductTypeNames();
				for (String productTypeName : productTypeNames) {
					ProductType type = this.getProductTypeByName(productTypeName);
					if (type != null)
						supportedSubsetOfTypes.add(type);
				}
				List<String> productIds = this.fmCatalog.query(fmQuery, supportedSubsetOfTypes);
				for (String productId : productIds)
					products.add(this.fmCatalog.getProductById(productId));
//				metadatas = this.fmCatalog.getReducedMetadata(fmQuery, supportedSubsetOfTypes, elements);
			}

			List<IngestReceipt> ingestReceipts = new Vector<IngestReceipt>();
//			for (Metadata metadata : metadatas) 
//				ingestReceipts.add(generateReceipt(metadata));
			for (Product product : products)
				ingestReceipts.add(new IngestReceipt(this.generateTransactionId(product.getProductId()), (product instanceof TemporalProduct && ((TemporalProduct) product).getProductReceivedTime() != null ? ((TemporalProduct) product).getProductReceivedTime() : new Date())));
			return ingestReceipts;
		}catch (Exception e) {
			throw new QueryServiceException("Failed to perform CatalogQuery '" + queryExpression + "' : " + e.getMessage(), e);
		}
	} 

	public List<IngestReceipt> query(QueryExpression queryExpression, int startIndex, int endIndex)
			throws QueryServiceException {
		try {
			List<Product> products = new Vector<Product>();

			if (queryExpression instanceof CustomWrapperQueryExpression) {
				CustomWrapperQueryExpression cwqe = (CustomWrapperQueryExpression) queryExpression;
				if (cwqe.getMeaning().equals(CatalogServiceMetKeys.FILEMGR_CATALOGS_ONLY))
					queryExpression = ((CustomWrapperQueryExpression) queryExpression).getQueryExpression();
				else
					return Collections.emptyList();
			}
			
			if (queryExpression instanceof CustomQueryExpression) {
				CustomQueryExpression customQE = (CustomQueryExpression) queryExpression;
				if (customQE.getName().equals(CatalogServiceMetKeys.PRODUCT_NAME_QUERY_EXPRESSION)) {
					Product product = this.fmCatalog.getProductByName(customQE.getProperty(CatalogServiceMetKeys.PRODUCT_NAME_CUSTOM_KEY));
					if (product != null && product.getProductType() != null && this.isSupportedProductType(product.getProductType())) 
						products.add(product);
				}else if (customQE.getName().equals(CatalogServiceMetKeys.TOP_N_QUERY_EXPRESSION)) {
					List<Product> topNProducts = this.fmCatalog.getTopNProducts(Integer.parseInt(customQE.getProperty(CatalogServiceMetKeys.N_CUSTOM_KEY)));
					if (topNProducts != null) {
						for (Product product : topNProducts) 
							if (product != null && product.getProductType() != null && this.isSupportedProductType(product.getProductType()))
								products.add(product);
					}
				}else {
					throw new QueryServiceException("Unknown Custom Query type '" + (queryExpression != null ? queryExpression.getClass().getCanonicalName() : queryExpression) + "'");
				}
			}else {
				Query fmQuery = new Query();
				QueryCriteria qc = CatalogServiceUtils.asQueryCriteria(queryExpression);
				if (qc != null)
					fmQuery.addCriterion(qc);
				LOG.log(Level.INFO, "Performing Filemgr Query '" + fmQuery + "'");
				List<ProductType> supportedSubsetOfTypes = new Vector<ProductType>();
				Set<String> productTypeNames = queryExpression.getBucketNames();
				if (productTypeNames == null)
					productTypeNames = this.getSupportedProductTypeNames();
				for (String productTypeName : productTypeNames) {
					ProductType type = this.getProductTypeByName(productTypeName);
					if (type != null)
						supportedSubsetOfTypes.add(type);
				}

				int localIndex = startIndex;
				int currentIndex = startIndex;
				TOP: for (ProductType type : supportedSubsetOfTypes) {
					if (endIndex <= currentIndex)
						break;
					int pageNum = (int) Math.ceil((double) localIndex / (double) this.fmCatalog.getPageSize());
					ProductPage page = this.fmCatalog.pagedQuery(fmQuery, type, pageNum);
					while (page.getPageProducts().size() > 0 && pageNum <= (int) Math.ceil((double) page.getNumOfHits() / (double) this.fmCatalog.getPageSize())) {
						for (int i = 0; i < page.getPageProducts().size(); i++) {
							Product product = page.getPageProducts().get(i);
							product.setProductType(type);
							products.add(product);
							if (i + currentIndex + 1 >= endIndex)
								break TOP;
						}
						page = this.fmCatalog.pagedQuery(fmQuery, type, ++pageNum);
					}
					currentIndex = startIndex + products.size();
					if (products.size() > 0)
						localIndex = 0;
				}
			}

			List<IngestReceipt> ingestReceipts = new Vector<IngestReceipt>();
			for (Product product : products)
				ingestReceipts.add(new IngestReceipt(this.generateTransactionId(product.getProductId()), (product instanceof TemporalProduct && ((TemporalProduct) product).getProductReceivedTime() != null ? ((TemporalProduct) product).getProductReceivedTime() : new Date())));
			return ingestReceipts;
		}catch (Exception e) {
			throw new QueryServiceException("Failed to perform CatalogQuery '" + queryExpression + "' : " + e.getMessage(), e);
		}
	}

	public int sizeOf(QueryExpression queryExpression) throws QueryServiceException {
		try {
			if (queryExpression instanceof CustomWrapperQueryExpression) {
				CustomWrapperQueryExpression cwqe = (CustomWrapperQueryExpression) queryExpression;
				if (cwqe.getMeaning().equals(CatalogServiceMetKeys.FILEMGR_CATALOGS_ONLY))
					queryExpression = ((CustomWrapperQueryExpression) queryExpression).getQueryExpression();
				else
					return 0;
			}
			
			if (queryExpression instanceof CustomQueryExpression) {
				CustomQueryExpression customQE = (CustomQueryExpression) queryExpression;
				if (customQE.getName().equals(CatalogServiceMetKeys.PRODUCT_NAME_QUERY_EXPRESSION)) {
					Product product = this.fmCatalog.getProductByName(customQE.getProperty(CatalogServiceMetKeys.PRODUCT_NAME_CUSTOM_KEY));
					return (product != null && product.getProductType() != null && this.isSupportedProductType(product.getProductType())) ? 1 : 0;
				}else if (customQE.getName().equals(CatalogServiceMetKeys.TOP_N_QUERY_EXPRESSION)) {
					List<Product> topNProducts = this.fmCatalog.getTopNProducts(Integer.parseInt(customQE.getProperty(CatalogServiceMetKeys.N_CUSTOM_KEY)));
					return (topNProducts != null) ? topNProducts.size() : 0;
				}else {
					throw new QueryServiceException("Unknown Custom Query type '" + (queryExpression != null ? queryExpression.getClass().getCanonicalName() : queryExpression) + "'");
				}
			}else {
				Query fmQuery = new Query();
				QueryCriteria qc = CatalogServiceUtils.asQueryCriteria(queryExpression);
				if (qc != null)
					fmQuery.addCriterion(qc);
				LOG.log(Level.INFO, "Performing Filemgr Query '" + fmQuery + "'");
				List<ProductType> supportedSubsetOfTypes = new Vector<ProductType>();
				Set<String> productTypeNames = queryExpression.getBucketNames();
				if (productTypeNames == null)
					productTypeNames = this.getSupportedProductTypeNames();
				for (String productTypeName : productTypeNames) {
					ProductType type = this.getProductTypeByName(productTypeName);
					if (type != null)
						supportedSubsetOfTypes.add(type);
				}
				int totalResults = 0;
				for (ProductType type : supportedSubsetOfTypes)
					totalResults += this.fmCatalog.pagedQuery(fmQuery, type, 1).getNumOfHits();
				return totalResults;
			}
		}catch (Exception e) {
			throw new QueryServiceException("Failed to perform CatalogQuery '" + queryExpression + "' : " + e.getMessage(), e);
		}
	}
	
	protected TransactionId<?> generateTransactionId() {
		return this.generateTransactionId(null);
	}
		
	protected TransactionId<?> generateTransactionId(String transactionId) {
		try {
			if (transactionId != null) {
				return this.getTransactionIdFactory().createTransactionId(transactionId);
			}else {
				return this.getTransactionIdFactory().createNewTransactionId();
			}
		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
//	protected IngestReceipt generateReceipt(Metadata metadata) throws ParseException {
//		String reveivedTimeString = metadata.getMetadata(CoreMetKeys.PRODUCT_RECEVIED_TIME);
//		Date receivedDate = new GregorianCalendar(2000, 01, 01).getTime();
//		if (reveivedTimeString != null)
//			receivedDate = DateConvert.isoParse(reveivedTimeString);
////		System.out.println("generating receipt for product_id : " + metadata.getMetadata(CoreMetKeys.PRODUCT_ID));
//		return new IngestReceipt(this.generateTransactionId(metadata.getMetadata(CoreMetKeys.PRODUCT_ID)), receivedDate);
//	}
	
//	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
//		try {
//			XStream xStream = new XStream();
//			xStream.toXML(this.fmCatalogFactory, out);
//			out.writeObject(this.transactionIdClass.getName());
//			Vector<Hashtable<String, Object>> productTypeHash = XmlRpcStructFactory.getXmlRpcProductTypeList(new Vector<ProductType>(this.supportedProductTypes.values()));
//			out.writeObject(productTypeHash);
//		}catch (Exception e) {
//			LOG.log(Level.SEVERE, "Failed to serialized FilemgrCatalogIndex : " + e.getMessage(), e);
//			throw new IOException("");
//		}
//	}
//
//	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
//		try {
//			XStream xStream = new XStream();
//			this.setFmCatalogFactory((CatalogFactory) xStream.fromXML(in));
//			this.setTransactionIdClass((String) in.readObject()); 
//			this.setSupportedProductTypes(XmlRpcStructFactory.getProductTypeListFromXmlRpc((Vector<Hashtable<String, Object>>) in.readObject()));
//		}catch (Exception e) {
//			LOG.log(Level.SEVERE, "Failed to deserialized FilemgrCatalogIndex : " + e.getMessage(), e);
//			throw new IOException(e.getMessage());
//		}
//	}

//	protected boolean isSupportedProductTypeName(String name) {
//		return this.getProductTypeByName(name) != null;
//	}
//	
//	protected boolean isSupportedProductTypeId(String id) {
//		return this.getProductTypeById(id) != null;
//	}
	
	protected ProductType getProductTypeByName(String name) {
		for (ProductType type : this.supportedProductTypes)
			if (type.getName().equals(name))
				return type;
		return null;
	}
//	
//	protected ProductType getProductTypeById(String id) {
//		for (ProductType type : this.supportedProductTypes)
//			if (type.getProductTypeId().equals(id))
//				return type;
//		return null;
//	}
	
	protected boolean isSupportedProductType(ProductType productType) {
		for (ProductType type : this.supportedProductTypes)
			if (type.equals(productType))
				return true;
		return false;
	}
	
    protected ProductType getCompleteProductType(ProductType productType) throws Exception {
		for (ProductType type : this.supportedProductTypes) 
			if (type.equals(productType)) 
				return type;
		return null;
    }
	
	protected Set<String> getSupportedProductTypeNames() {
		Set<String> productTypeNames = new HashSet<String>();
		for (ProductType type : this.supportedProductTypes)
			productTypeNames.add(type.getName());
		return productTypeNames;
	}
	
}
