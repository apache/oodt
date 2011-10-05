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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

//COMMONS imports
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

//Solr imports
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.CoreContainer;

//OODT imports
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.structs.exceptions.RepositoryManagerException;
import org.apache.oodt.cas.filemgr.system.XmlRpcFileManagerClient;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.SerializableMetadata;

/**
 * 
 * Indexes the File Manager Catalog to Solr. Uses an associated config file,
 * indexer.properties to specify how to perform the indexing. See
 * indexer.properties in the src/main/resources directory of file manager for
 * specific documentation.
 * 
 */
public class SolrIndexer {
  private final static String SOLR_INDEXER_CONFIG = "SOLR_INDEXER_CONFIG";
  private final static String SOLR_URL = "solr.url";
  private final static String FILEMGR_URL = "filemgr.url";
  private IndexerConfig config = null;
  private final SolrServer server;
  private CoreContainer coreContainer;
  private String fmUrl;
  private String solrUrl;
  private static Logger LOG = Logger.getLogger(SolrIndexer.class.getName());

  public SolrIndexer(String solrUrl, String fmUrl)
      throws InstantiationException {
    InputStream input = null;
    String filename = null;

    try {
      LOG.info("System property " + SOLR_INDEXER_CONFIG + " set to "
          + System.getProperty(SOLR_INDEXER_CONFIG));
      filename = System.getProperty(SOLR_INDEXER_CONFIG);
      if (filename != null) {
        LOG.info("Reading config from " + filename);
        input = new FileInputStream(filename);
      } else {
        LOG.info("Config file not found reading config from classpath");
        input = SolrIndexer.class.getResourceAsStream("indexer.properties");
      }
      config = new IndexerConfig(input);
    } catch (IOException e) {
      LOG.severe("Could not read in configuration for indexer from classpath or file");
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

  public void shutdown() {
    coreContainer.shutdown();
  }

  public void commit() throws SolrServerException, IOException {
    server.commit();
  }

  public void optimize() throws SolrServerException, IOException {
    server.optimize();
  }

  @SuppressWarnings("unchecked")
  private SolrInputDocument getSolrDocument(Metadata metadata) {
    SolrInputDocument doc = new SolrInputDocument();

    for (Object objKey : config.getMapProperties().keySet()) {
      String key = (String) objKey;
      if (metadata.isMultiValued(key)) {
        List<String> values = metadata.getAllMetadata(key);
        for (String value : values) {
          if (value != null && !config.getIgnoreValues().contains(value.trim())) {
            LOG.fine("Adding field: "
                + config.getMapProperties().getProperty(key) + " value: "
                + value);
            doc.addField(config.getMapProperties().getProperty(key), value);
          }
        }
      } else {
        String value = metadata.getMetadata(key);
        if (value != null && !config.getIgnoreValues().contains(value.trim())) {
          LOG.fine("Adding field: "
              + config.getMapProperties().getProperty(key) + " value: " + value);
          doc.addField(config.getMapProperties().getProperty(key), value);
        }
      }
    }

    return doc;
  }

  public void indexMetFile(File file, boolean delete)
      throws InstantiationException, FileNotFoundException, IOException,
      SolrServerException {
    SerializableMetadata metadata = new SerializableMetadata("UTF-8", false);
    metadata.loadMetadataFromXmlStream(new FileInputStream(file));
    if (delete) {
      server.deleteById(metadata.getMetadata("uuid"));
    }
    server.add(this.getSolrDocument(metadata));
  }

  public void indexAll(boolean delete) throws SolrServerException {
    LOG.info("Indexing");
    try {
      XmlRpcFileManagerClient fmClient = new XmlRpcFileManagerClient(new URL(
          this.fmUrl));
      if (delete) {
        server.deleteByQuery("*:*");
      }
      LOG.info("Looking up product types");
      List<ProductType> types = fmClient.getProductTypes();
      for (ProductType type : types) {
        if (!config.getIgnoreTypes().contains(type.getName().trim())) {
          LOG.info("Looking up products for product type: " + type.getName());
          List<Product> products = fmClient.getProductsByProductType(type);
          for (Product product : products) {
            LOG.info("Looking up metadata for ProductId "
                + product.getProductId());
            Metadata metadata = fmClient.getMetadata(product);
            if (metadata != null) {
              LOG.info("Found metadata for product ID "
                  + metadata.getMetadata("CAS.ProductId"));
            } else {
              LOG.info("Could not find metadata for product "
                  + product.getProductId());
            }
            if (metadata.getMetadata("UUID") != null) {
              if (metadata.getMetadata("Deleted") == null
                  || !"true".equals(metadata.getMetadata("Deleted"))) {
                try {
                  server.add(this.getSolrDocument(metadata));
                  server.commit();
                  LOG.info("Indexed " + metadata.getMetadata("UUID"));
                } catch (Exception e) {
                  LOG.severe("Could not index " + metadata.getMetadata("UUID")
                      + " " + e.getMessage());
                }
              } else {
                LOG.info("Skipping Deleted: " + metadata.getMetadata("UUID"));
              }
            }
          }
        } else {
          LOG.info("Ignoring product type " + type.getName());
        }
      }
    } catch (MalformedURLException e) {
      LOG.severe("File Manager URL is malformed: " + e.getMessage());
    } catch (ConnectionException e) {
      LOG.severe("Could not connect to File Manager: " + e.getMessage());
    } catch (IOException e) {
      LOG.severe("Could not delete all: " + e.getMessage());
    } catch (RepositoryManagerException e) {
      LOG.severe("Could not look up product types: " + e.getMessage());
    } catch (CatalogException e) {
      LOG.severe("Query to File Manager failed: " + e.getMessage());
    }
    LOG.info("Finished Indexing");
  }

  public void indexProduct(String productId, boolean delete)
      throws SolrServerException, IOException, ConnectionException,
      CatalogException {
    XmlRpcFileManagerClient fmClient = new XmlRpcFileManagerClient(new URL(
        this.fmUrl));
    if (delete) {
      server.deleteById(productId);
    }

    Product product = fmClient.getProductById(productId);
    Metadata metadata = fmClient.getMetadata(product);
    server.add(this.getSolrDocument(metadata));
  }

  @SuppressWarnings("static-access")
  public static Options buildCommandLine() {
    Options options = new Options();

    options.addOption(new Option("h", "help", false, "Print this message"));
    options.addOption(new Option("o", "optimize", false,
        "Optimize the Solr index when done"));
    options.addOption(new Option("d", "delete", false,
        "Delete items before indexing"));
    options.addOption(OptionBuilder.withArgName("Solr URL").hasArg()
        .withDescription("URL to the Solr server").withLongOpt("solrUrl")
        .create("su"));
    options.addOption(OptionBuilder.withArgName("Filemgr URL").hasArg()
        .withDescription("URL to the CAS FileManager").withLongOpt("fmUrl")
        .create("fmu"));

    OptionGroup group = new OptionGroup();
    Option all = new Option("a", "all", false, "Index all items in catalog");
    Option met = OptionBuilder.withArgName("file").hasArg()
        .withDescription("Index this met file").withLongOpt("metFile")
        .create("mf");
    Option query = OptionBuilder.withArgName("query").hasArg()
        .withDescription("Not yet implemented").withLongOpt("catalogQuery")
        .create("cq");
    Option product = OptionBuilder.withArgName("productId").hasArg()
        .withDescription("Product id to index").withLongOpt("product")
        .create("p");

    group.addOption(all);
    group.addOption(met);
    group.addOption(query);
    group.addOption(product);
    options.addOptionGroup(group);

    return options;
  }

  public static void main(String[] args) throws Exception {
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
        || line.hasOption("metFile") || line.hasOption("catalogQuery")) {
      SolrIndexer indexer = null;
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
          indexer.indexProduct(line.getOptionValue("product"),
              line.hasOption("delete"));
        } else if (line.hasOption("metFile")) {
          indexer.indexMetFile(new File(line.getOptionValue("metFile")),
              line.hasOption("delete"));
        } else {
          LOG.info("Catalog query not yet implemented.");
        }
        indexer.commit();
        if (line.hasOption("optimize")) {
          indexer.optimize();
        }
      } catch (Exception ex) {
        LOG.severe("Did not complete indexing: " + ex.getMessage());
        ex.printStackTrace();
      }
    }

  }

  public class IndexerConfig {
    private final static String PREFIX_CONFIG = "config.";
    private final static String PREFIX_MET = "map.";
    private Properties properties = new Properties();
    private Properties mapProperties = new Properties();
    private HashMap<String, Properties> xmlMapProperties = new HashMap<String, Properties>();
    private List<String> xmlKeys = new ArrayList<String>();
    private List<String> xmlMultiKeys = new ArrayList<String>();
    private List<String> ignoreTypes = new ArrayList<String>();
    private List<String> ignoreValues = new ArrayList<String>();

    public IndexerConfig(InputStream inputStream) throws IOException {
      Properties props = new Properties();
      props.load(inputStream);
      for (Object objKey : props.keySet()) {
        String key = (String) objKey;
        if (key.startsWith(PREFIX_CONFIG)) {
          properties.put(key.substring(PREFIX_CONFIG.length()),
              props.getProperty(key));
        } else if (key.startsWith(PREFIX_MET)) {
          mapProperties.put(key.substring(PREFIX_MET.length()),
              props.getProperty(key));
        }
      }

      if (properties.getProperty("ignore.types") != null) {
        String[] values = properties.getProperty("ignore.types").trim()
            .split(",");
        for (String value : values) {
          ignoreTypes.add(value);
        }
      }
      if (properties.getProperty("ignore.values") != null) {
        String[] values = properties.getProperty("ignore.values").trim()
            .split(",");
        for (String value : values) {
          ignoreValues.add(value);
        }
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

    public Properties getXmlMapProperties(String name) {
      return xmlMapProperties.get(name);
    }

    public List<String> getXmlKeys() {
      return this.xmlKeys;
    }

    public List<String> getXmlMultiKeys() {
      return this.xmlMultiKeys;
    }

    public List<String> getIgnoreTypes() {
      return this.ignoreTypes;
    }

    public List<String> getIgnoreValues() {
      return this.ignoreValues;
    }

  }

}
