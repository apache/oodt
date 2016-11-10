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
package org.apache.oodt.cas.filemgr.catalog.solr;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.oodt.cas.filemgr.catalog.Catalog;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductPage;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.Query;
import org.apache.oodt.cas.filemgr.structs.QueryCriteria;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.filemgr.validation.ValidationLayer;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.commons.pagination.PaginationUtils;
import org.springframework.util.StringUtils;

/**
 * Implementation of the CAS {@link Catalog} interface
 * that uses a Solr back-end metadata store.
 *
 * @author Luca Cinquini
 *
 */
public class SolrCatalog implements Catalog {

	// Class responsible for serializing/deserializing CAS products into Solr documents
	ProductSerializer productSerializer;

	// Class responsible for generating unique identifiers for incoming Products
	ProductIdGenerator productIdGenerator;

	// Class responsible for interacting with the Solr server
	SolrClient solrClient;

	private static final Logger LOG = Logger.getLogger(SolrCatalog.class.getName());

	public SolrCatalog(String solrUrl, ProductIdGenerator productIdGenerator, ProductSerializer productSerializer) {
		this.productIdGenerator = productIdGenerator;
		this.productSerializer = productSerializer;
		this.solrClient = new SolrClient(solrUrl);
	}

	@Override
	public void addMetadata(Metadata metadata, Product product) throws CatalogException {

		LOG.info("Adding metadata for product:"+product.getProductName());
		if(metadata.containsKey("_version_")){
			metadata.removeMetadata("_version_");
		}
		// serialize metadadta to Solr document(s)
		// replace=false i.e. add metadata to existing values
		List<String> docs = productSerializer.serialize(product.getProductId(), metadata, false);

		// send documents to Solr server
		solrClient.index(docs, true, productSerializer.getMimeType());

	}

	/**
	 * This method implementation will remove the specified keys and values from the product metadata,
	 * leaving all other metadata keys and values unchanged.
	 * Note: ALL occurrences of each specified (key, value) pair will be removed, not just the first.
	 */
	@Override
	public void removeMetadata(Metadata metadata, Product product) throws CatalogException {

		// retrieve full existing metadata
		Metadata currentMetadata = getMetadata(product);

		// metadata to be updated
		Metadata updateMetadata = new Metadata();

		// loop over keys of metadata be updated
		for (String key : metadata.getKeys()) {

			// list of values remaining after removal
			List<String> values = new ArrayList<String>();
			if (currentMetadata.containsKey(key)) {
				for (String value : currentMetadata.getAllMetadata(key)) {
					if (!metadata.getAllMetadata(key).contains(value)) {
						values.add(value);
					}
				}

				// add remaining values to updated metadata
				if (values.isEmpty()) {
					// special value because Metadata will NOT store an empty list
					values.add(Parameters.NULL);
				}
				updateMetadata.addMetadata(key, values);

			}
		}

		if(updateMetadata.containsKey("_version_")){
			updateMetadata.removeMetadata("_version_");
		}
		// generate Solr update documents
		// replace=true to override existing values
		List<String> docs = productSerializer.serialize(product.getProductId(), updateMetadata, true);

		// send documents to Solr server
		solrClient.index(docs, true, productSerializer.getMimeType());

	}

	/**
	 * Method that adds a Product to the Catalog,
	 * persisting its fundamental CAS attributes (id, name, type, ingestion time, etc.).
	 * This method assigns the product a unique identifier, if not existing already.
	 */
	@Override
	public void addProduct(Product product) throws CatalogException {

		if(product.getProductId()!=null && this.getCompleteProductById(product.getProductId()) !=null) {
			throw new CatalogException(
					"Attempt to add a product that already existed: product: ["
							+ product.getProductName() + "]");





		} else {
			LOG.info("Adding product:" + product.getProductName());

			// generate product identifier if not existing already
			if (!StringUtils.hasText(product.getProductId())) {
				String productId = this.productIdGenerator.generateId(product);
				product.setProductId(productId);
			}

			// serialize product for ingestion into Solr
			List<String> docs = productSerializer.serialize(product, true); // create=true

			// send records to Solr
			solrClient.index(docs, true, productSerializer.getMimeType());
		}

	}

	@Override
	public void modifyProduct(Product product) throws CatalogException {

		LOG.info("Modifying product:"+product.getProductName());

		// serialize the update product information to Solr document(s)
		List<String> docs = productSerializer.serialize(product, false); // create=false

		// send records to Solr
		solrClient.index(docs, true, productSerializer.getMimeType());


	}

	@Override
	public void removeProduct(Product product) throws CatalogException {

		// send message to Solr server
		solrClient.delete(product.getProductId(), true);

	}

	@Override
	public void setProductTransferStatus(Product product) throws CatalogException {

		this.modifyProduct(product);

	}

	@Override
	public void addProductReferences(Product product) throws CatalogException {

		// generate update documents (with replace=true)
		List<String> docs = productSerializer.serialize(product.getProductId(), product.getRootRef(), product.getProductReferences(), true);

		// send documents to Solr server
		solrClient.index(docs, true, productSerializer.getMimeType());

	}

	@Override
	public Product getProductById(String productId) throws CatalogException {

		CompleteProduct cp = getCompleteProductById(productId);
		return cp.getProduct();

	}

	@Override
	public Product getProductByName(String productName) throws CatalogException {

		CompleteProduct cp = getCompleteProductByName(productName);
		if (cp!=null) {
			LOG.info("Found product name="+productName+" id="+cp.getProduct().getProductId());
			return cp.getProduct();
		} else {
			LOG.info("Product with name="+productName+" not found");
			return null;
		}

	}

	@Override
	public List<Reference> getProductReferences(Product product) throws CatalogException {

		CompleteProduct cp = getCompleteProductById(product.getProductId());
		return cp.getProduct().getProductReferences();

	}

	@Override
	public List<Product> getProducts() throws CatalogException {

		// build query parameters
		Map<String, String[]> params = new ConcurrentHashMap<String, String[]>();
		params.put("q", new String[] { "*:*" } );
		//params.put("rows", new String[] { "20" } ); // control pagination ?

		// query Solr for all matching products
		return getProducts(params, 0, -1).getProducts(); // get ALL products

	}

	/**
	 * Common utility to retrieve a range of products matching the specified {@link Query} and {@link ProductType}.
	 * This method transforms the given constraints in a map of HTTP (name, value) pairs and delegates to the following method.
	 *
	 * @param query
	 * @param type
	 * @param offset
	 * @param limit
	 * @return
	 * @throws CatalogException
	 */
	private QueryResponse getProducts(Query query, ProductType type, int offset, int limit) throws CatalogException {

		// build HTTP request
		ConcurrentHashMap<String, String[]> params = new ConcurrentHashMap<String, String[]>();
		// product type constraint
		params.put("q", new String[]{Parameters.PRODUCT_TYPE_NAME+":"+type.getName()} );
		// convert filemgr query into a Solr query
		List<String> qc = new ArrayList<String>();
		for (QueryCriteria queryCriteria : query.getCriteria()) {
			LOG.info("Query criteria="+queryCriteria.toString());
			qc.add(queryCriteria.toString());
		}
		params.put("fq", qc.toArray( new String[ qc.size() ] ));
		// sort
		params.put("sort", new String[]{ Parameters.PRODUCT_RECEIVED_TIME+" desc"} );

		return this.getProducts(params, offset, limit);

	}

	/**
	 * Common utility to retrieve a range of products matching the specified query parameters
	 * @param params : HTTP query parameters used for query to Solr
	 * @param offset : the index of the first result, starting at 0
	 * @param limit : the maximum number of returned results (use -1 to return ALL results)
	 * @return
	 */
	private QueryResponse getProducts(Map<String, String[]> params, int offset, int limit) throws CatalogException {

		// combined results from pagination
		QueryResponse queryResponse = new QueryResponse();
		queryResponse.setStart(offset);
		int start = offset;
		while (queryResponse.getCompleteProducts().size()<limit || limit<0) {

			params.put("start", new String[] { ""+start } );
			String response = solrClient.query(params, productSerializer.getMimeType());
			QueryResponse qr = productSerializer.deserialize(response);

			for (CompleteProduct cp : qr.getCompleteProducts()) {
				if (queryResponse.getCompleteProducts().size()<limit) {
					queryResponse.getCompleteProducts().add( cp );
				}
			}

			queryResponse.setNumFound( qr.getNumFound() );
			start = offset+queryResponse.getCompleteProducts().size();
			if (limit<0) {
				limit = queryResponse.getNumFound(); // retrieve ALL results
			}
			if (start>=queryResponse.getNumFound()) {
				break; // don't query any longer
			}

		}

		LOG.info("Total number of products found="+queryResponse.getNumFound());
		LOG.info("Total number of products returned="+queryResponse.getCompleteProducts().size());
		return queryResponse;

	}

	@Override
	public List<Product> getProductsByProductType(ProductType type) throws CatalogException {

		// build query parameters
		Map<String, String[]> params = new ConcurrentHashMap<String, String[]>();
		params.put("q", new String[] { "*:*" } );
		// use the product type name as query parameter
		params.put("fq", new String[] { Parameters.PRODUCT_TYPE_NAME+":"+type.getName() } );

		// query Solr for all matching products
		return getProducts(params, 0, -1).getProducts(); // get ALL products

	}

	@Override
	public Metadata getMetadata(Product product) throws CatalogException {

		CompleteProduct cp = getCompleteProductById(product.getProductId());
		return cp.getMetadata();

	}

	@Override
	public Metadata getReducedMetadata(Product product, List<String> elements) throws CatalogException {

		// build HTTP request
		ConcurrentHashMap<String, String[]> params = new ConcurrentHashMap<String, String[]>();
		params.put("q", new String[]{Parameters.PRODUCT_ID+":"+product.getProductId()} );
		// request metadata elements explicitly
		params.put("fl", elements.toArray(new String[elements.size()]) );

		// execute request
		String doc = solrClient.query(params, productSerializer.getMimeType());

		// parse response
		CompleteProduct cp = extractCompleteProduct(doc);
		return cp.getMetadata();

	}

	/**
	 * {@inheritDoc}
	 * Note that this method implementation will return ALL products in the catalog matching the query criteria.
	 */
	@Override
	public List<String> query(Query query, ProductType type) throws CatalogException {

		// execute request for ALL results
		QueryResponse queryResponse =  this.getProducts(query, type, 0, -1); // get ALL products

		// extract ids from products
		List<String> ids = new ArrayList<String>();
		for (CompleteProduct cp : queryResponse.getCompleteProducts()) {
			ids.add(cp.getProduct().getProductId());
		}

		return ids;

	}

	@Override
	public List<Product> getTopNProducts(int n) throws CatalogException {

		// retrieve most recent n products from Solr
		String doc = solrClient.queryProductsByDate(n, productSerializer.getMimeType());

		// parse Solr response into Product objects
		return this.getProductsFromDocument(doc);

	}

	@Override
	public ProductPage pagedQuery(Query query, ProductType type, int pageNum) throws CatalogException {


		// execute request for one page of results
		int offset = (pageNum-1)*Parameters.PAGE_SIZE;
		int limit = Parameters.PAGE_SIZE;
		QueryResponse queryResponse = this.getProducts(query, type, offset, limit);

		// build product page from query response
		return newProductPage(pageNum, queryResponse);

	}

	@Override
	public ProductPage getFirstPage(ProductType type) {
		try {
			return this.pagedQuery(new Query(), type, 1);

		} catch(CatalogException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	@Override
	public ProductPage getLastProductPage(ProductType type) {

		try {

			// query for total number of products of this type
			int numTotalResults = this.getNumProducts(type);

			// compute last page number
			int numOfPages = PaginationUtils.getTotalPage(numTotalResults, Parameters.PAGE_SIZE);

			// request last page
			return pagedQuery(new Query(), type, numOfPages);

		} catch(CatalogException e) {
			throw new RuntimeException(e.getMessage(), e);
		}

	}

	@Override
	public ProductPage getNextPage(ProductType type, ProductPage currentPage) {

		int nextPageNumber = currentPage.getPageNum()+1;
		if (nextPageNumber>currentPage.getTotalPages()) {
			throw new RuntimeException("Invalid next page number: " + nextPageNumber);
		}

		try {
			return this.pagedQuery(new Query(), type, currentPage.getPageNum()+1);

		} catch(CatalogException e) {
			throw new RuntimeException(e.getMessage(), e);
		}

	}

	@Override
	public ProductPage getPrevPage(ProductType type, ProductPage currentPage) {

		int prevPageNumber = currentPage.getPageNum()-1;
		if (prevPageNumber<=0) {
			throw new RuntimeException("Invalid previous page number: " + prevPageNumber);
		}

		try {
			return this.pagedQuery(new Query(), type, prevPageNumber);

		} catch(CatalogException e) {
			throw new RuntimeException(e.getMessage(), e);
		}

	}

	/**
	 * Common functionality for extracting products from a Solr response document.
	 * @param doc
	 * @return
	 */
	private List<Product> getProductsFromDocument(String doc) throws CatalogException {

		// extract full product information from Solr response
		QueryResponse queryResponse = productSerializer.deserialize(doc);

		// return products only
		return queryResponse.getProducts();

	}

	@Override
	public List<Product> getTopNProducts(int n, ProductType type) throws CatalogException {

		// retrieve most recent n products from Solr
		String doc = solrClient.queryProductsByDateAndType(n, type, productSerializer.getMimeType());

		// parse Solr response into Product objects
		return this.getProductsFromDocument(doc);

	}

	@Override
	public ValidationLayer getValidationLayer() {
		// FIXME: must parse Solr schema.xmnl from:
		// http://localhost:8080/solr/admin/file/?contentType=text/xml;charset=utf-8&file=schema.xml
		throw new RuntimeException("Method 'getValidationLayer' not yet implemented");
	}

	@Override
	public int getNumProducts(ProductType type) throws CatalogException {

		// build query parameters
		Map<String, String[]> params = new ConcurrentHashMap<String, String[]>();
		params.put("q", new String[] { "CAS.ProductTypeName:"+type.getName() } );
		params.put("rows", new String[] { "0" } ); // don't return any results

		// execute query
		String response = solrClient.query(params, productSerializer.getMimeType());

		// parse response
		QueryResponse queryResponse = productSerializer.deserialize(response);
		return queryResponse.getNumFound();

	}

	private CompleteProduct getCompleteProductById(String productId) throws CatalogException {

		// request document with given id
		String doc = solrClient.queryProductById(productId, productSerializer.getMimeType());

		// parse document into complete product
		return extractCompleteProduct(doc);

	}

	private CompleteProduct getCompleteProductByName(String productName) throws CatalogException {

		// request document with given id
		String doc = solrClient.queryProductByName(productName, productSerializer.getMimeType());

		// parse document into complete product
		return extractCompleteProduct(doc);

	}

	private CompleteProduct extractCompleteProduct(String doc) throws CatalogException {

		// deserialize document into Product
		LOG.info("Parsing Solr document: "+doc);
		QueryResponse queryResponse = productSerializer.deserialize(doc);
		int numFound = queryResponse.getNumFound();
		if (numFound>1) {
			throw new CatalogException("Product query returned "+numFound+" results instead of 1!");
		} else if (numFound==0) {
			return null; // no product found
		} else {
			return queryResponse.getCompleteProducts().get(0);
		}

	}

	/**
	 * Factory method to create a {@link ProductPage} from a {@link QueryResponse}.
	 * @param queryResponse
	 * @return
	 */
	private ProductPage newProductPage(int pageNum, QueryResponse queryResponse) {

		ProductPage page = new ProductPage();
		page.setPageNum(pageNum);
		page.setPageSize(queryResponse.getProducts().size());
		page.setNumOfHits(queryResponse.getNumFound());
		page.setPageProducts(queryResponse.getProducts());
		page.setTotalPages(PaginationUtils.getTotalPage(queryResponse.getNumFound(), Parameters.PAGE_SIZE));
		return page;

	}

}
