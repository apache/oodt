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

package org.apache.oodt.cas.filemgr.tools;

//JDK imports
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.oodt.cas.filemgr.metadata.CoreMetKeys;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductPage;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.structs.exceptions.RepositoryManagerException;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;
import org.apache.oodt.cas.filemgr.util.RpcCommunicationFactory;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.SerializableMetadata;
import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Indexes products from the File Manager catalog to a Solr instance. Uses an
 * associated config file, indexer.properties to specify how to perform the
 * indexing. See indexer.properties in the src/main/resources directory for
 * specific documentation.
 */
public class SolrIndexer {

    private static Logger LOG = Logger.getLogger(SolrIndexer.class.getName());

	private final static String SOLR_INDEXER_CONFIG = "SOLR_INDEXER_CONFIG";
	private final static String SOLR_URL = "solr.url";
	private final static String FILEMGR_URL = "filemgr.url";
	private final static String ACCESS_KEY = "access.key";
	private final static String ACCESS_URL = "access.url";
	private final static String PRODUCT_NAME = "CAS.ProductName";
	private IndexerConfig config = null;
	private final SolrServer server;
	private String fmUrl;
	private String solrUrl;
	private final static SimpleDateFormat solrFormat = new SimpleDateFormat(
	    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

	/**
	 * Constructor reads in the configuration and initiates the connection to the
	 * Solr instance.
	 * 
	 * @param solrUrl
	 *          URL for the Solr instance.
	 * @param fmUrl
	 *          URL for the File Manager instance.
	 */
	public SolrIndexer(String solrUrl, String fmUrl)
	    throws InstantiationException {
		InputStream input = null;
		String filename;

		try {
			LOG.info("System property " + SOLR_INDEXER_CONFIG + " set to "
			    + System.getProperty(SOLR_INDEXER_CONFIG));
			filename = System.getProperty(SOLR_INDEXER_CONFIG);
			if (filename != null) {
				LOG.info("Reading config from " + filename);
				input = new FileInputStream(filename);
			} else {
				LOG.info("Config file not found, reading config from classpath");
				input = SolrIndexer.class.getResourceAsStream("/indexer.properties");
			}
			config = new IndexerConfig(input);
		} catch (IOException e) {
			LOG
			    .severe("Could not read in configuration for indexer from classpath or file");
			throw new InstantiationException(e.getMessage());
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					// no op
				}
			}
		}

		this.solrUrl = solrUrl;
		if (this.solrUrl == null) {
			this.solrUrl = config.getProperty(SOLR_URL);
		}

		this.fmUrl = fmUrl;
		if (this.fmUrl == null) {
			this.fmUrl = config.getProperty(FILEMGR_URL);
		}

		LOG.info("Using Solr: " + this.solrUrl + " FileManager: " + this.fmUrl);


		try {
			server = new CommonsHttpSolrServer(this.solrUrl);
		} catch (MalformedURLException e) {
			LOG.severe("Could not connect to Solr server " + this.solrUrl);
			throw new InstantiationException(e.getMessage());
		}

	}

	/**
	 * This method deletes all entries from the Solr index.
	 */
	public void delete() throws SolrServerException, IOException {
		server.deleteByQuery("*:*");
	}

	/**
	 * This method commits all of the modifications to the Solr index.
	 */
	public void commit() throws SolrServerException, IOException {
		server.commit();
	}

	/**
	 * This method optimizes the Solr index.
	 */
	public void optimize() throws SolrServerException, IOException {
		server.optimize();
	}

	/**
	 * This method transforms the product metadata into a Solr document.
	 * 
	 * @param metadata
	 *          The metadata object for the product to index.
	 * @return Returns the SolrInputDocument containing product metadata.
	 */
	private SolrInputDocument getSolrDocument(Metadata metadata) {
		SolrInputDocument doc = new SolrInputDocument();
		// Only grab metadata which have a mapping in the indexer.properties
		for (Object objKey : config.getMapProperties().keySet()) {
			// The key in the metadata object
			String key = (String) objKey;
			// The solr field name this metadata key will be mapped to
			String fieldName = config.getMapProperties().getProperty(key);
			List<String> values = metadata.getAllMetadata(key);
			if (values != null) {
				for (String value : values) {
					// Add each metadata value into the
					if (value != null && !config.getIgnoreValues().contains(value.trim())) {
						LOG.fine("Adding field: " + fieldName + " value: " + value);
						doc.addField(fieldName, value);
					}
				}
			}
		}
		return doc;
	}

	/**
	 * This method adds a single product extracted from a metadata file to the
	 * Solr index.
	 * 
	 * @param file
	 *          The file containing product metadata.
	 * @param delete
	 *          Flag indicating whether the entry should be deleted from the
	 *          index.
	 * @throws SolrServerException
	 *           When an error occurs communicating with the Solr server instance.
	 */
	public void indexMetFile(File file, boolean delete)
	    throws
		SolrServerException {
		LOG.info("Attempting to index product from metadata file.");
		try {
			SerializableMetadata metadata = new SerializableMetadata("UTF-8", false);
			metadata.loadMetadataFromXmlStream(new FileInputStream(file));
			metadata.addMetadata("id", metadata.getMetadata("CAS."
			    + CoreMetKeys.PRODUCT_ID));
			metadata.addMetadata(config.getProperty(ACCESS_KEY), config
			    .getProperty(ACCESS_URL)
			    + metadata.getMetadata("CAS." + CoreMetKeys.PRODUCT_ID));
			if (delete) {
				server
				    .deleteById(metadata.getMetadata("CAS." + CoreMetKeys.PRODUCT_ID));
			}
			server.add(this.getSolrDocument(metadata));
			LOG.info("Indexed product: "
			    + metadata.getMetadata("CAS." + CoreMetKeys.PRODUCT_ID));
		} catch (InstantiationException e) {
			LOG.severe("Could not instantiate metadata object: " + e.getMessage());
		} catch (FileNotFoundException e) {
			LOG.severe("Could not find metadata file: " + e.getMessage());
		} catch (IOException e) {
			LOG.severe("Could not delete product from index: " + e.getMessage());
		}
	}

	/**
	 * This method indexes all product types retrieved from the File Manager to
	 * the Solr index.
	 * 
	 * @param delete
	 *          Flag indicating whether each product type retrieved from the File
	 *          Manager should be deleted from the index.
	 * @throws SolrServerException
	 *           When an error occurs communicating with the Solr server instance.
	 */
	public void indexProductTypes(boolean delete) {
		LOG.info("Indexing product types...");
		try (FileManagerClient fmClient = RpcCommunicationFactory.createClient(new URL(this.fmUrl))) {
			LOG.info("Retrieving list of product types.");
			List<ProductType> types = fmClient.getProductTypes();
			for (ProductType type : types) {
				if (!config.getIgnoreTypes().contains(type.getName().trim())) {
					Metadata metadata = new Metadata();
					metadata.addMetadata("id", type.getProductTypeId());
					metadata.addMetadata("CAS.ProductTypeId", type.getProductTypeId());
					metadata.addMetadata("CAS.ProductTypeDescription", type
					    .getDescription());
					metadata.addMetadata("CAS.ProductTypeRepositoryPath", type
					    .getProductRepositoryPath());
					metadata.addMetadata("CAS.ProductTypeVersioner", type.getVersioner());
					metadata.addMetadata("CAS.ProductTypeName", type.getName());
					metadata.addMetadata("ProductType", "ProductType");
					metadata.replaceMetadata(type.getTypeMetadata());
					if (delete) {
						try {
							server.deleteById(type.getProductTypeId());
						} catch (Exception e) {
							LOG.severe("Could not delete product type " + type.getName()
							    + " from index: " + e.getMessage());
						}
					}
					try {
						performSubstitution(metadata);
						server.add(this.getSolrDocument(metadata));
						LOG.info("Indexed product type: " + type.getName());
					} catch (Exception e) {
						LOG.severe("Could not index " + type.getName() + ": "
						    + e.getMessage());
					}
				} else {
					LOG.info("Ignoring product type: " + type.getName());
				}
			}
		} catch (MalformedURLException e) {
			LOG.severe("File Manager URL is malformed: " + e.getMessage());
		} catch (ConnectionException e) {
			LOG.severe("Could not connect to File Manager: " + e.getMessage());
		} catch (RepositoryManagerException e) {
			LOG.severe("Could not retrieve product types from File Manager: "
			    + e.getMessage());
		} catch (IOException e) {
			LOG.severe(String.format("Error occurred when indexing product types: %s", e.getMessage()));
		}
		LOG.info("Finished indexing product types.");
	}
	
	/**
	 * Suppresses exception that occurred with older file managers. 
	 */
	private ProductPage safeFirstPage(FileManagerClient fmClient, ProductType type) {
		ProductPage page = null;
		try {
			page = fmClient.getFirstPage(type);
		} catch (Exception e) {
			LOG.info("No products found for: " + type.getName());
		}
		return page;
	}

	/**
	 * This method indexes all products retrieved from the File Manager to the
	 * Solr index. Metadata from the product's associated ProductType is also
	 * included.
	 * 
	 * @param delete
	 *          Flag indicating whether each product retrieved from the File
	 *          Manager should be deleted from the index.
	 * @throws SolrServerException
	 *           When an error occurs communicating with the Solr server instance.
	 */
	public void indexAll(boolean delete) {
		LOG.info("Indexing products...");
		try (FileManagerClient fmClient = RpcCommunicationFactory.createClient(new URL(this.fmUrl))) {
			LOG.info("Retrieving list of product types.");
			List<ProductType> types = fmClient.getProductTypes();
			for (ProductType type : types) {
				if (!config.getIgnoreTypes().contains(type.getName().trim())) {
					LOG.info("Paging through products for product type: "
					    + type.getName());
					ProductPage page = safeFirstPage(fmClient, type); 
					while (page != null) {
					    for (Product product : page.getPageProducts()) {
							try {
								this.indexProduct(product.getProductId(), fmClient
								    .getMetadata(product), type.getTypeMetadata());
							} catch (Exception e) {
								LOG.severe("Could not index " + product.getProductId() + ": "
								    + e.getMessage());
							}
						}
					    if (page.isLastPage()) {
					        break;
					    }
					    page = fmClient.getNextPage(type, page);
					}
				}
			}
			LOG.info("Finished indexing products.");
		} catch (MalformedURLException e) {
			LOG.severe("File Manager URL is malformed: " + e.getMessage());
		} catch (ConnectionException e) {
			LOG.severe("Could not connect to File Manager: " + e.getMessage());
		} catch (CatalogException e) {
			LOG.severe("Could not retrieve products from File Manager: "
			    + e.getMessage());
		} catch (RepositoryManagerException e) {
			LOG.severe("Could not retrieve product types from File Manager: "
			    + e.getMessage());
		} catch (IOException e) {
			LOG.severe(String.format("Error occurred when indexing: %s", e.getMessage()));
		}
	}

	/**
	 * This method adds a single product retrieved from the File Manager by its
	 * product identifier to the Solr index. Metadata from the ProductType is also
	 * included.
	 * 
	 * @param productId
	 *          The identifier of the product (CAS.ProductId).
	 * @throws SolrServerException
	 *           When an error occurs communicating with the Solr server instance.
	 */
	public void indexProduct(String productId)
	    throws SolrServerException {
		LOG.info("Attempting to index product: " + productId);
		try (FileManagerClient fmClient = RpcCommunicationFactory.createClient(new URL(this.fmUrl))) {
			Product product = fmClient.getProductById(productId);
			Metadata productMetadata = fmClient.getMetadata(product);
			indexProduct(product.getProductId(), productMetadata, product
			    .getProductType().getTypeMetadata());
		} catch (MalformedURLException e) {
			LOG.severe("File Manager URL is malformed: " + e.getMessage());
		} catch (ConnectionException e) {
			LOG.severe("Could not connect to File Manager: " + e.getMessage());
		} catch (CatalogException e) {
			LOG.severe("Could not retrieve product from File Manager: "
			    + e.getMessage());
		} catch (java.text.ParseException e) {
			LOG.severe("Could not format date: " + e.getMessage());
		} catch (IOException e) {
			LOG.severe(String.format("Error occurred when indexing product types: %s", e.getMessage()));
		}
	}
	
	/**
	 * This method adds a single product retrieved from the File Manager by its
	 * product name to the Solr index. Metadata from the ProductType is also
	 * included.
	 * 
	 * @param productName
	 *          The identifier of the product (CAS.ProductId).
	 * @param delete
	 *          Flag indicating whether the entry should be deleted from the
	 *          index.
	 * @throws SolrServerException
	 *           When an error occurs communicating with the Solr server instance.
	 */
	public void indexProductByName(String productName, boolean delete) throws SolrServerException {
		
		LOG.info("Attempting to index product: " + productName);
		try (FileManagerClient fmClient = RpcCommunicationFactory.createClient(new URL(this.fmUrl))) {
			// Try to delete product by name
			// Note: the standard field "CAS.ProductName" must be mapped to some Solr field in file indexer.properties
			if (delete) {
				try {
					String productNameField = config.mapProperties.getProperty(PRODUCT_NAME);
					if (StringUtils.hasText(productNameField)) {
						server.deleteByQuery(productNameField+":"+productName);
					} else {
						LOG.warning("Metadata field "+PRODUCT_NAME+" is not mapped to any Solr field, cannot delete product by name");
					}
				} catch(Exception e) {
					LOG.warning("Could not delete product: "+productName+" from Solr index");
				}
			}
			Product product = fmClient.getProductByName(productName);
			Metadata productMetadata = fmClient.getMetadata(product);
			// NOTE: delete (by id) is now false
			indexProduct(product.getProductId(), productMetadata, product.getProductType().getTypeMetadata());
			
		} catch (MalformedURLException e) {
			LOG.severe("File Manager URL is malformed: " + e.getMessage());
		} catch (ConnectionException e) {
			LOG.severe("Could not connect to File Manager: " + e.getMessage());
		} catch (CatalogException e) {
			LOG.severe("Could not retrieve product from File Manager: "
			    + e.getMessage());
		} catch (java.text.ParseException e) {
			LOG.severe("Could not format date: " + e.getMessage());
		} catch (IOException e) {
			LOG.severe(String.format("Error occurred when indexing product types: %s", e.getMessage()));
		}
	}

	private void indexProduct(String productId, Metadata productMetadata,
	    Metadata typeMetadata) throws SolrServerException,
	    java.text.ParseException {
		Metadata metadata = new Metadata();
		metadata.addMetadata("id", productId);
		// Add in product type metadata
		if (typeMetadata != null) {
			metadata.addMetadata(typeMetadata);
		}
		if (productMetadata != null) {
			String accessKey = config.getProperty(ACCESS_KEY);
			// Product metadata takes precedence
			metadata.replaceMetadata(productMetadata);
			// If there is an access url configured add it to the metadata
			if (config.getProperty(ACCESS_URL) != null) {
				metadata.addMetadata(accessKey, config.getProperty(ACCESS_URL));
			}
			// Replace values for metadata keys specified in config. This allows
			// for metadata substitution. For instance, if a key named "product_url"
			// has a value of
			// http://localhost:8080/cas-product/data?productID=[CAS.ProductId]
			// the value in brakets will be updated with the value from the
			// CAS.ProductId.
			performSubstitution(metadata);
			try {
				server.add(this.getSolrDocument(metadata));
				LOG.info("Indexed product: " + productId);
			} catch (IOException e) {
				LOG.severe("Could not index product: " + productId);
			}
		} else {
			LOG.info("Could not find metadata for product: " + productId);
		}
	}

	/**
	 * This method deletes a single product identified by a productID from the Solr index
	 * 
	 * @param productId
	 * @throws IOException
	 * @throws SolrServerException
	 */
	public void deleteProduct(String productId) throws IOException, SolrServerException {
		LOG.info("Attempting to delete product: " + productId);
		this.deleteProductFromIndex(productId);
	}

	/**
	 * This method deletes a product(s) from the Solr index identified by a given name
	 * 
	 * @param productName
	 * @throws IOException
	 * @throws SolrServerException
	 */
	public void deleteProductByName(String productName) {
		LOG.info("Attempting to delete product: " + productName);

		try {
			String productNameField = config.mapProperties.getProperty(PRODUCT_NAME);
			if (StringUtils.hasText(productNameField)) {
				server.deleteByQuery(productNameField+":"+productName);
			} else {
				LOG.warning("Metadata field "+PRODUCT_NAME+" is not mapped to any Solr field, cannot delete product by name");
			}
		} catch(Exception e) {
			LOG.warning("Could not delete product: "+productName+" from Solr index");
		}

	}

	private void deleteProductFromIndex(String productId) throws IOException, SolrServerException {
		server.deleteById(productId);
	}
	
	/**
	 * Quick helper method to do substitution on the keys specified in the config
	 * 
	 * @param metadata
	 *          to substitute on
	 * @throws java.text.ParseException
	 */
	private void performSubstitution(Metadata metadata)
	    throws java.text.ParseException {
		// Do metadata replacement
		for (String key : config.getReplacementKeys()) {
			List<String> values = metadata.getAllMetadata(key);
			if (values != null) {
				List<String> newValues = new ArrayList<String>();
				for (String value : values) {
					newValues.add(PathUtils.replaceEnvVariables(value, metadata));
				}
				metadata.removeMetadata(key);
				metadata.addMetadata(key, newValues);
			}
		}
		// Format dates
		for (Object key : config.getFormatProperties().keySet()) {
			String keyString = (String) key;
			if (metadata.containsKey(keyString)) {
				List<String> values = metadata.getAllMetadata(keyString);
				if (values != null) {
					List<String> newValues = new ArrayList<String>();
					SimpleDateFormat format = new SimpleDateFormat(config
					    .getFormatProperties().getProperty(keyString).trim());
					for (String value : values) {
						newValues.add(formatDate(format, value));
					}
					metadata.removeMetadata(keyString);
					metadata.addMetadata(keyString, newValues);
				}
			}
		}
	}

	private String formatDate(SimpleDateFormat format, String value)
	    throws java.text.ParseException {
		// Ignore formating if its an ignore value
		if (config.getIgnoreValues().contains(value.trim())) {
		  return value;
		}
		return solrFormat.format(format.parse(value));
	}

	/**
	 * This method builds the command-line options.
	 * 
	 * @return Returns the supported Options.
	 */
	@SuppressWarnings("static-access")
	public static Options buildCommandLine() {
		Options options = new Options();

		options.addOption(new Option("h", "help", false, "Print this message"));
		options.addOption(new Option("o", "optimize", false,
		    "Optimize the Solr index"));
		options.addOption(new Option("d", "delete", false,
		    "Delete item before indexing"));
		options.addOption(OptionBuilder.withArgName("Solr URL").hasArg()
		    .withDescription("URL to the Solr instance").withLongOpt("solrUrl")
		    .create("su"));
		options.addOption(OptionBuilder.withArgName("Filemgr URL").hasArg()
		    .withDescription("URL to the File Manager").withLongOpt("fmUrl")
		    .create("fmu"));

		OptionGroup group = new OptionGroup();
		Option all = new Option("a", "all", false,
		    "Index all products from the File Manager");
		Option product = OptionBuilder.withArgName("productId").hasArg()
		    .withDescription("Index the product from the File Manager")
		    .withLongOpt("product").create("p");
		Option met = OptionBuilder.withArgName("file").hasArg().withDescription(
		    "Index the product from a metadata file").withLongOpt("metFile")
		    .create("mf");
		Option read = new Option("r", "read", false,
		    "Index all products based on a list of product identifiers passed in");
		Option types = new Option("t", "types", false,
		    "Index all product types from the File Manager");
		Option deleteAll = new Option("da", "deleteAll", false,
		    "Delete all products/types from the Solr index");

		group.addOption(all);
		group.addOption(product);
		group.addOption(met);
		group.addOption(read);
		group.addOption(types);
		group.addOption(deleteAll);
		options.addOptionGroup(group);

		return options;
	}

	/**
	 * The main method. Execution without argument displays help message.
	 * 
	 * @param args
	 *          Command-line arguments.
	 */
	public static void main(String[] args)  {
		Options options = SolrIndexer.buildCommandLine();
		CommandLineParser parser = new GnuParser();
		CommandLine line = null;

		try {
			line = parser.parse(options, args);
		} catch (ParseException e) {
			LOG.severe("Could not parse command line: " + e.getMessage());
		}

		if (line == null || line.hasOption("help") || line.getOptions().length == 0) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("java " + SolrIndexer.class.getName(), options);
		} else if (line.hasOption("all") || line.hasOption("product")
		    || line.hasOption("metFile") || line.hasOption("read")
		    || line.hasOption("types") || line.hasOption("deleteAll")) {
			SolrIndexer indexer;
			String solrUrl = null;
			String fmUrl = null;
			if (line.hasOption("solrUrl")) {
				solrUrl = line.getOptionValue("solrUrl");
			}
			if (line.hasOption("fmUrl")) {
				fmUrl = line.getOptionValue("fmUrl");
			}
			try {
				indexer = new SolrIndexer(solrUrl, fmUrl);
				if (line.hasOption("all")) {
					indexer.indexAll(line.hasOption("delete"));
				} else if (line.hasOption("product")) {
					indexer.indexProduct(line.getOptionValue("product"));
				} else if (line.hasOption("metFile")) {
					indexer.indexMetFile(new File(line.getOptionValue("metFile")), line
					    .hasOption("delete"));
				} else if (line.hasOption("read")) {
					for (String productId : readProductIdsFromStdin()) {
						indexer.indexProduct(productId);
					}
				} else if (line.hasOption("types")) {
					indexer.indexProductTypes(line.hasOption("delete"));
				} else if (line.hasOption("deleteAll")) {
					indexer.delete();
				} else {
					LOG.severe("Option not supported.");
				}
				indexer.commit();
				if (line.hasOption("optimize")) {
					indexer.optimize();
				}
			} catch (Exception e) {
				LOG.severe("An error occurred indexing: " + e.getMessage());
				LOG
				    .severe("If the above message is related to accessing the Solr instance, see the Application Server's log for additional information.");
			}
		}
	}

	/**
	 * This method reads product identifiers from the standard input.
	 * 
	 * @return Returns a List of product identifiers.
	 */
	private static List<String> readProductIdsFromStdin() {
		List<String> productIds = new ArrayList<String>();
		BufferedReader br;

		br = new BufferedReader(new InputStreamReader(System.in));
		String line = null;

		try {
			while ((line = br.readLine()) != null) {
				productIds.add(line);
			}
		} catch (IOException e) {
			LOG.severe("Error reading product id: line: [" + line + "]: Message: "
			    + e.getMessage());
		} finally {
		  try {
              br.close();
          } catch (Exception ignore) {
          }
		}
		return productIds;
	}

	/**
	 * This class manages the Indexer configuration.
	 */
	public class IndexerConfig {
		private final static String PREFIX_CONFIG = "config.";
		private final static String PREFIX_MET = "map.";
		private final static String PREFIX_FORMAT = "format.";
		private final static String IGNORE_TYPES = "ignore.types";
		private final static String IGNORE_VALUES = "ignore.values";
		private final static String REPLACEMENT_KEYS = "replacement.keys";
		// Used to hold general properties for indexer configuration
		private Properties properties = new Properties();
		// Used to hold mappings for filemanager -> solr for keys
		private Properties mapProperties = new Properties();
		// Used to define the date format for a field
		private Properties formatProperties = new Properties();
		private List<String> ignoreTypes = new ArrayList<String>();
		private List<String> ignoreValues = new ArrayList<String>();
		private List<String> replacementKeys = new ArrayList<String>();

		public IndexerConfig(InputStream inputStream) throws IOException {
			Properties props = new Properties();
			props.load(inputStream);
			for (Object objKey : props.keySet()) {
				String key = (String) objKey;
				if (key.startsWith(PREFIX_CONFIG)) {
					properties.put(key.substring(PREFIX_CONFIG.length()), props
					    .getProperty(key));
				} else if (key.startsWith(PREFIX_MET)) {
					mapProperties.put(key.substring(PREFIX_MET.length()), props
					    .getProperty(key));
				} else if (key.startsWith(PREFIX_FORMAT)) {
					formatProperties.put(key.substring(PREFIX_FORMAT.length()), props
					    .getProperty(key));
				}
			}

			if (properties.getProperty(IGNORE_TYPES) != null) {
				String[] values = properties.getProperty(IGNORE_TYPES).trim()
				    .split(",");
			  Collections.addAll(ignoreTypes, values);
			}

			if (properties.getProperty(IGNORE_VALUES) != null) {
				String[] values = properties.getProperty(IGNORE_VALUES).trim().split(
				    ",");
			  Collections.addAll(ignoreValues, values);
			}

			if (properties.getProperty(REPLACEMENT_KEYS) != null) {
				String[] values = properties.getProperty(REPLACEMENT_KEYS).trim()
				    .split(",");
			  Collections.addAll(replacementKeys, values);
			}
		}

		public String getProperty(String key) {
			return properties.getProperty(key);
		}

		public String getProperty(String key, String defaultValue) {
			return properties.getProperty(key, defaultValue);
		}

		public Properties getMapProperties() {
			return mapProperties;
		}

		public Properties getFormatProperties() {
			return formatProperties;
		}

		public List<String> getIgnoreTypes() {
			return this.ignoreTypes;
		}

		public List<String> getIgnoreValues() {
			return this.ignoreValues;
		}

		public List<String> getReplacementKeys() {
			return this.replacementKeys;
		}
	}

}
