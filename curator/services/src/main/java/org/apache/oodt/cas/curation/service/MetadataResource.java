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


package org.apache.oodt.cas.curation.service;

//JDK imports

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.apache.oodt.cas.curation.structs.ExtractorConfig;
import org.apache.oodt.cas.curation.util.CurationXmlStructFactory;
import org.apache.oodt.cas.curation.util.ExtractorConfigReader;
import org.apache.oodt.cas.curation.util.exceptions.CurationException;
import org.apache.oodt.cas.filemgr.catalog.Catalog;
import org.apache.oodt.cas.filemgr.repository.XMLRepositoryManager;
import org.apache.oodt.cas.filemgr.structs.Element;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.filemgr.structs.exceptions.RepositoryManagerException;
import org.apache.oodt.cas.filemgr.structs.exceptions.ValidationLayerException;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;
import org.apache.oodt.cas.filemgr.util.GenericFileManagerObjectFactory;
import org.apache.oodt.cas.filemgr.validation.XMLValidationLayer;
import org.apache.oodt.cas.metadata.MetExtractor;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.SerializableMetadata;
import org.apache.oodt.cas.metadata.exceptions.MetExtractionException;
import org.apache.oodt.cas.metadata.util.GenericMetadataObjectFactory;
import org.springframework.util.StringUtils;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("metadata")
/**
 *
 * A web-service endpoint for dealing with CAS {@link Metadata} object.
 *
 * @author pramirez
 * @version $Id$
 */
public class MetadataResource extends CurationService {

  @Context
  UriInfo uriInfo;

  @Context
  private ServletContext context;

  private static final long serialVersionUID = 1930946924218765724L;

  public static final String STAGING = "staging";

  public static final String CATALOG = "catalog";

  public static final String PRODUCT_TYPE = "productType";

  public static final String UPDATE = "update";

  public static final String DELETE = "delete";
  private static Logger LOG = Logger.getLogger(MetadataResource.class.getName());
  // single instance of CAS catalog shared among all requests
  private Catalog catalog = null;

  public MetadataResource(){

  }

  public MetadataResource(@Context ServletContext context) {

  }

  @GET
  @Path(STAGING)
  @Produces("text/plain")
  public String getStagingMetadata(@QueryParam("id") String id,
      @DefaultValue(FORMAT_HTML) @QueryParam("format") String format,
      @QueryParam("configId") String configId,
      @DefaultValue("false") @QueryParam("overwrite") Boolean overwrite,
      @Context HttpServletRequest req, @Context HttpServletResponse res) {

      Metadata metadata;

      try {
        metadata = this.getStagingMetadata(id, configId, overwrite);
      } catch (Exception e) {
        return "<div class=\"error\">" + e.getMessage() + "</div>";
      }

      if (FORMAT_HTML.equals(format)) {
        return this.getMetadataAsHTML(metadata);
      }
      return this.getMetadataAsJSON(metadata).toString();
  }

  @GET
  @Path("extractor/config")
  @Produces("text/plain")
  public String getMetExtractorConfigList(
      @DefaultValue("") @QueryParam("current") String current,
      @DefaultValue(FORMAT_HTML) @QueryParam("format") String format) {
    String[] configIds = this.getFilesInDirectory(config
        .getMetExtrConfUploadPath(), false);

    if (FORMAT_HTML.equals(format)) {
      return this.getExtractorConfigIdsAsHTML(configIds, current);
    }
    return this.getExtractorConfigIdsAsJSON(configIds);
  }

  protected String getExtractorConfigIdsAsHTML(String[] configIds,
      String current) {
    StringBuilder html = new StringBuilder();
    for (String configId : configIds) {
      html.append("<option ");
      if (configId.equals(current)) {
        html.append("selected ");
      }
      html.append("value=\"");
      html.append(configId);
      html.append("\">");
      html.append(configId);
      html.append("</option>\r\n");
    }
    return html.toString();
  }

  protected String getExtractorConfigIdsAsJSON(String[] configIds) {
    // TODO: Support JSON
    return "Not Implemented...";
  }

  /**
   *
   * @param id
   *          Relative path from staging root to product. The met extension will
   *          be added to this id to look up and see if a met file exists with
   *          in the met area.
   * @param configId
   *          Reference to the extractor config. {@link ExtractorConfigReader}
   *          will load the configuration
   * @param overwrite
   *          Flag to indicate whether or not to overwrite a met file if present
   *          in the staging area.
   * @return The {@link Metadata} retrieved from the met area path if present or
   *         extracted using the met extractor config.
   * @throws FileNotFoundException
   * @throws InstantiationException
   * @throws IOException
   * @throws MetExtractionException
   */
  protected Metadata getStagingMetadata(String id, String configId,
      Boolean overwrite) throws InstantiationException,
      IOException, MetExtractionException {
    if (configId == null || configId.trim().length() == 0) {
      return this.readMetFile(id + CurationService.config.getMetExtension());
    } else {
      String relMetPath = id.startsWith("/") ? id : "/" + id;
      String pathToMetFile = CurationService.config.getMetAreaPath()
          + relMetPath + CurationService.config.getMetExtension();
      if (!overwrite && new File(pathToMetFile).exists()) {
        return this.readMetFile(id + CurationService.config.getMetExtension());
      } else {
        // Make sure the parent directory exists
        new File(pathToMetFile).getParentFile().mkdirs();
        Metadata metadata = this.runMetExtractor(id, ExtractorConfigReader
            .readFromDirectory(
                new File(CurationService.config
                .getMetExtrConfUploadPath()), configId));
        this.writeMetFile(id, metadata);
        return metadata;
      }
    }
  }

  /**
   *
   * @param id
   *          Relative path from staging root to the product
   * @param config
   *          Configuration to run this met extractor
   * @return
   * @throws MetExtractionException
   */
  protected Metadata runMetExtractor(String id, ExtractorConfig config)
      throws MetExtractionException {
    MetExtractor metExtractor = GenericMetadataObjectFactory
        .getMetExtractorFromClassName(config.getClassName());
    metExtractor.setConfigFile(config.getConfigFiles().get(0));
    return metExtractor.extractMetadata(CurationService.config
        .getStagingAreaPath()
        + "/" + id);
  }

  @GET
  @Path(CATALOG)
  @Produces("text/plain")
  public String getCatalogMetadata(@QueryParam("id") String id,
      @DefaultValue(FORMAT_HTML) @QueryParam("format") String format,
      @Context HttpServletRequest req, @Context HttpServletResponse res) {

      // Call file manager to get metadata
      Product prod;
      Metadata metadata;
      String productId = id.substring(id.lastIndexOf("/") + 1);

      try (FileManagerClient fmClient = CurationService.config.getFileManagerClient()) {
        prod = fmClient.getProductById(productId);
        metadata = this.getCatalogMetadata(prod);
      } catch (Exception e) {
        return "<div class=\"error\">" + e.getMessage() + "</div>";
      }

      if (FORMAT_HTML.equals(format)) {
        return this.getMetadataAsHTML(metadata);
      }
      return this.getMetadataAsJSON(metadata).toString();
  }

  @POST
  @Path(CATALOG)
  @Consumes("application/x-www-form-urlencoded")
  @Produces("text/plain")
  public String setCatalogMetadata(MultivaluedMap<String, String> formParams,
      @FormParam("id") String id) {

    Product prod;
    Metadata metadata = this.getMetadataFromMap(formParams);

    String productId = id.substring(id.lastIndexOf("/") + 1);

    try (FileManagerClient fmClient = CurationService.config.getFileManagerClient()) {
      prod = fmClient.getProductById(productId);
      this.updateCatalogMetadata(prod, metadata);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      return "<div class=\"error\">" + e.getMessage() + "</div>";
    }

    return this.getMetadataAsHTML(metadata);
  }

  @GET
  @Path(PRODUCT_TYPE)
  @Produces("text/plain")
  public String getProductTypeMetadata(@QueryParam("id") String id,
      @DefaultValue(FORMAT_HTML) @QueryParam("format") String format,
      @Context HttpServletRequest req, @Context HttpServletResponse res) {


    Metadata metadata;
    String[] idParts = id.split("/", 3);
    String policy = idParts[1];
    String productType = idParts[2];
    productType = productType.substring(0, productType.lastIndexOf("/"));
    try {
      metadata = getProductTypeMetadataForPolicy(policy, productType);
    } catch (Exception e) {
      return "<div class=\"error\">" + e.getMessage() + "</div>";
    }

    if (FORMAT_HTML.equals(format)) {
      return this.getMetadataAsHTML(metadata);
    }
    return this.getMetadataAsJSON(metadata).toString();
  }


  @POST
  @Path(PRODUCT_TYPE)
  @Consumes("application/x-www-form-urlencoded")
  @Produces("text/plain")
  public String setProductTypeMetadata(MultivaluedMap<String, String> formParams) {
    String[] idParts = formParams.getFirst("id").split("/");
    String policy = idParts[1];
    String productType = idParts[2];
    try {
    this.writeProductTypeMetadata(policy, productType, this
        .getMetadataFromMap(formParams));
    } catch (Exception e) {
      return "<div class=\"error\">" + e.getMessage() + "</div>";
    }
    return "";
  }

  @POST
  @Path(STAGING)
  @Consumes("application/x-www-form-urlencoded")
  @Produces("text/plain")
  public String setStagingMetadata(MultivaluedMap<String, String> formParams) {
    try {
      this.writeMetFile(formParams.getFirst("id"), this
          .getMetadataFromMap(formParams));
    } catch (Exception e) {
      return "<div class=\"error\">" + e.getMessage() + "</div>";
    }
    return "";
  }

  @GET
  @Path("staging/info")
  @Produces("text/plain")
  public String getMetadataInfo(@QueryParam("id") String id) {

      return "Staging met info";
  }


  private JSONObject getMetadataAsJSON(Metadata metadata) {
    return JSONObject.fromObject(metadata.getMap());
  }

  private Metadata getMetadataFromJSON(String metadataJSON) {
    JSONObject json = (JSONObject) JSONSerializer.toJSON(metadataJSON);
    Metadata metadata = new Metadata();

    Set<String> keys = json.keySet();
    for (String key : keys) {
      List values = (List) JSONSerializer.toJava((JSONArray) json.get(key));
      metadata.addMetadata(key, values);
    }

    return metadata;
  }

  private Metadata getMetadataFromMap(MultivaluedMap<String, String> formParams) {
    Metadata metadata = new Metadata();

    for (Map.Entry<String, List<String>> entry : formParams.entrySet()) {
      if (entry.getKey().startsWith("metadata.")) {
        String newKey = entry.getKey().substring(entry.getKey().indexOf('.') + 1);
        for (String value : entry.getValue()) {
          metadata.addMetadata(newKey, value);
        }
      }
    }

    return metadata;
  }

  protected String getMetadataAsHTML(Metadata metadata) {
    if (metadata == null) {
      return "<table></table>";
    }

    StringBuilder html = new StringBuilder();

    html.append("<table>\r\n");
    for (String key : (Set<String>) metadata.getMap().keySet()) {
      html.append(" <tr>\r\n");
      html.append("  <th>").append(key).append("</th>\r\n");
      html.append("  <td class=\"").append(key).append("\">");
      List<String> values = metadata.getAllMetadata(key);
      for (Iterator<String> i = values.iterator(); i.hasNext();) {
        html.append("<span>").append(i.next()).append("</span>");
        if (i.hasNext()) {
          html.append(", ");
        }
      }
      for (String value : (List<String>) metadata.getAllMetadata(key)) {
      }
      html.append("</td>\r\n");
      html.append(" </tr>\r\n");
    }
    html.append("</table>\r\n");

    return html.toString();
  }


  /**
   * Reads a {@link Metadata} object from a String representation of a .met
   * {@link File}.
   *
   * @param file
   *          The full path to the .met {@link File}.
   * @return The read-in CAS {@link Metadata} object.
   * @throws InstantiationException
   * @throws InstantiationException
   *           If there is an error instantiating the {@link Metadata} class.
   * @throws IOException
   * @throws FileNotFoundException
   * @throws FileNotFoundException
   *           If the .met {@link File} is not found.
   * @throws IOException
   *           If there is an IO problem opening the met file.
   */
  public Metadata readMetFile(String file) throws InstantiationException,
      IOException {
    SerializableMetadata metadata = new SerializableMetadata("UTF-8", false);
    metadata.loadMetadataFromXmlStream(new FileInputStream(config
        .getMetAreaPath()
        + "/" + file));

    return metadata;
  }

  /**
   * Retrieves the cataloged {@link Metadata} associated with a {@link Product}.
   *
   * @param product
   *          The {@link Product} to obtain cataloged {@link Metadata} for.
   * @return The cataloged {@link Metadata} for {@link Product}.
   * @throws CatalogException
   *           If there is an error talking to the CAS File Manager
   *           {@link Catalog}.
   */
  public Metadata getCatalogMetadata(Product product) throws CatalogException {
    try(    FileManagerClient fmClient = CurationService.config.getFileManagerClient()){
      return fmClient.getMetadata(fmClient.getProductById(product.getProductId()));
    } catch (IOException e){
      LOG.severe("Error occurred when fetching catalog metadata for product: " +
              product.getProductName() + " :" + e.getMessage());
      return null;
    }
  }

  /**
   * Writes a CAS {@link Metadata} {@link File} using the given identifier.
   *
   * @param id
   *          The identifier of the .met file to write.
   * @param metadata
   *          The {@link Metadata} object to persist and write to a {@link File}
   *          .
   * @throws FileNotFoundException
   *           If the .met {@link File} cannot be written.
   * @throws IOException
   *           If there is an IO exception writing the {@link File}.
   */
  public void writeMetFile(String id, Metadata metadata)
      throws IOException {
    SerializableMetadata serMet = new SerializableMetadata(metadata, "UTF-8",
        false);
    serMet.writeMetadataToXmlStream(new FileOutputStream(new File(config
        .getMetAreaPath(), id + config.getMetExtension())));
  }

  /**
   * Method to update the catalog metadata for a given product.
   * All current metadata fields will be preserved,
   * except those specified in the HTTP POST request as 'metadata.<field_name>=<field_value>'.
   *
   * @param id
   * 	identifier of CAS product - either 'id' or 'name' must be specified
   * @param name
   * 	name of CAS product - either 'id' or 'name' must be specified
   * @param formParams
   * 	HTTP (name, value) form parameters. The parameter names MUST start with "metadata."
   * @param replace
   * 	optional flag set to false to add the new metadata values to the existing values, for the given flags
   */
  @POST
  @Path(UPDATE)
  @Consumes("application/x-www-form-urlencoded")
  @Produces("text/plain")
  public String updateMetadata(MultivaluedMap<String, String> formParams,
		  @FormParam("id") String id,
		  @FormParam("name") String name,
		  @DefaultValue("true") @FormParam("replace") boolean replace,
		  @DefaultValue("false") @FormParam("remove") boolean remove) {

  	// new metadata from HTTP POST request
    Metadata newMetadata = this.getMetadataFromMap(formParams);
    // empty metadata
    Metadata metadata;
    try (FileManagerClient fmClient = CurationService.config.getFileManagerClient()) {
      // retrieve product from catalog
      Product product;
      if (StringUtils.hasText(id)) {
    	  id = id.substring(id.lastIndexOf("/") + 1);
    	  product = fmClient.getProductById(id);
      } else if (StringUtils.hasText(name)) {
    	  product = fmClient.getProductByName(name);
      } else {
    	  throw new Exception("Either the HTTP parameter 'id' or the HTTP parameter 'name' must be specified");
      }

      // retrieve existing metadata
      metadata = fmClient.getMetadata(product);

      // remove product references (as they will be added later)
      metadata.removeMetadata("reference_orig");
      metadata.removeMetadata("reference_data_store");
      metadata.removeMetadata("reference_fileSize");
      metadata.removeMetadata("reference_mimeType");

      // merge new and existing metadata
      metadata.addMetadata(newMetadata);

      // replace metadata values for keys specified in HTTP request (not others)
      if (replace) {
    	  for (String key : newMetadata.getAllKeys()) {
    		  metadata.replaceMetadata(key, newMetadata.getAllMetadata(key));
    	  }
      }

      // remove metadata tags
      if (remove) {
	      for (String key : newMetadata.getAllKeys()) {
	      	metadata.removeMetadata(key);
	      }
      }

      // insert old and new metadata
      fmClient.updateMetadata(product, metadata);

      // return product id to downstream processors
      return "id="+product.getProductId();

    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      // return error message
      throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Updates the cataloged {@link Metadata} for a {@link Product} in the CAS
   * File Manager.
   *
   * @param product
   *          The {@link Product} to update {@link Metadata} for.
   * @throws CatalogException
   *           If any error occurs during the update.
   * @throws IOException
   * @throws FileNotFoundException
   */
  public void updateCatalogMetadata(Product product, Metadata newMetadata)
      throws CatalogException, IOException {
    InputStream is = new FileInputStream(CurationService.config.getFileMgrProps());
    try {
      System.getProperties().load(is);
    }
    finally{
      is.close();
    }
    Catalog catalog = this.getCatalog();

    Metadata oldMetadata = catalog.getMetadata(product);
    List<Reference> references = catalog.getProductReferences(product);
    Product newProduct = new Product(product.getProductName(), product
        .getProductType(), product.getProductStructure(), product
        .getTransferStatus(), product.getProductReferences());
    // Constructor is bugged and doesn't set transfer status
    newProduct.setTransferStatus(product.getTransferStatus());
    catalog.removeMetadata(oldMetadata, product);
    catalog.removeProduct(product);
    newProduct.setProductId(product.getProductId());
    catalog.addProduct(newProduct);
    newProduct.setProductReferences(references);
    catalog.addProductReferences(newProduct);
    catalog.addMetadata(newMetadata, newProduct);
  }

  /**
   * Method to delete a specific product from the catalog
   *
   * @param id
   * 	identifier of CAS product - either 'id' or 'name' must be specified
   * @param name
   * 	name of CAS product - either 'id' or 'name' must be specified
   * @return the product ID of the deleted product if deletion successful
   */
  @POST
  @Path(DELETE)
  @Consumes("application/x-www-form-urlencoded")
  @Produces("text/plain")
  public String deleteCatalogMetadata(
		  @FormParam("id") String id,
		  @FormParam("name") String name) {

	  try (FileManagerClient fmClient = CurationService.config.getFileManagerClient()) {
		  // retrieve product from catalog
		  Product product;
          if (StringUtils.hasText(id)) {
              id = id.substring(id.lastIndexOf("/") + 1);
              product = fmClient.getProductById(id);
          } else if (StringUtils.hasText(name)) {
              product = fmClient.getProductByName(name);
          } else {
              throw new Exception("Either the HTTP parameter 'id' or the HTTP parameter 'name' must be specified");
          }

		  // remove product from catalog
		  this.deleteCatalogProduct(product);

		  // return product id to downstream processors
		  return "id="+product.getProductId();

	  } catch (Exception e) {
		  LOG.log(Level.SEVERE, e.getMessage());
		  // return error message
		  throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);

	  }
  }

  /**
   * Deletes a given product from the catalog
   *
   * @param product
   *          The {@link Product} to delete
   * @throws FileNotFoundException
   * @throws IOException
   * @throws CatalogException
   *           If any error occurs during this delete operation.
   */
  public void deleteCatalogProduct(Product product) throws CatalogException {
	try (FileManagerClient fmClient = CurationService.config.getFileManagerClient()){
	  fmClient.removeProduct(product);
    } catch (IOException e) {
      LOG.severe(String.format("Couldn't detele product - %s : %s", product.getProductName(), e.getMessage()));
      throw new CatalogException("Unable to delete product", e);
    }
  }

  private Metadata getProductTypeMetadataForPolicy(String policy,
      String productTypeName) throws MalformedURLException,
      InstantiationException, RepositoryManagerException {
    String rootPolicyPath = this.cleanse(CurationService.config
        .getPolicyUploadPath());
    String policyPath = new File(rootPolicyPath + policy).toURI().toURL()
        .toExternalForm();
    String[] policies = { policyPath };
    XMLRepositoryManager repMgr = new XMLRepositoryManager(Arrays
        .asList(policies));
    ProductType productType = repMgr.getProductTypeByName(productTypeName);

    return productType.getTypeMetadata();
  }

    private Metadata writeProductTypeMetadata(String policy,
            String productTypeName, Metadata metadata)
            throws MalformedURLException, InstantiationException, RepositoryManagerException, UnsupportedEncodingException,
            CurationException {
        String rootPolicyPath = this.cleanse(CurationService.config
                .getPolicyUploadPath());
        String policyPath = new File(rootPolicyPath + policy).toURI().toURL()
                .toExternalForm();
        String[] policies = {policyPath};
        XMLRepositoryManager repMgr = new XMLRepositoryManager(Arrays
                .asList(policies));

        ProductType productType = repMgr.getProductTypeByName(productTypeName);
        productType.setTypeMetadata(metadata);

        CurationXmlStructFactory.writeProductTypeXmlDocument(repMgr
                .getProductTypes(), rootPolicyPath + policy + "/product-types.xml");

        // refresh the config on the fm end
        try (FileManagerClient fmClient = CurationService.config.getFileManagerClient()) {
            fmClient.refreshConfigAndPolicy();
        } catch (IOException e) {
            LOG.severe(String.format("Unable to refresh config and policy: %s", e.getMessage()));
        }

        return productType.getTypeMetadata();
    }

  private String cleanse(String origPath) {
    String retStr = origPath;
    if (!retStr.endsWith("/")) {
      retStr += "/";
    }
    return retStr;
  }

  // Method to instantiate the CAS catalog, if not done already.
  private synchronized Catalog getCatalog() {

  	if (catalog==null) {
  		String catalogFactoryClass = this.context.getInitParameter(CATALOG_FACTORY_CLASS);
  		// preserve backward compatibility
  		if (!StringUtils.hasText(catalogFactoryClass)) {
          catalogFactoryClass = "org.apache.oodt.cas.filemgr.catalog.LuceneCatalogFactory";
        }
  		catalog = GenericFileManagerObjectFactory.getCatalogServiceFromFactory(catalogFactoryClass);
  	}

  	return catalog;
  }

  @DELETE
  @Path(PRODUCT_TYPE+"/remove")
  @Produces("text/plain")
  public boolean removeProductType(
		  @FormParam("policy") String policy,
		  @FormParam("id") String id) {
    XMLRepositoryManager xmlRepo = getRepo(policy);
    try {
      ProductType type = xmlRepo.getProductTypeById(id);
      xmlRepo.removeProductType(type);
      return true;
    } catch (RepositoryManagerException e) {
      LOG.log(Level.SEVERE, e.getMessage());
      return false;
    }
  }

  @GET
  @Path(PRODUCT_TYPE+"/parentmap")
  @Produces("text/plain")
  public String getParentTypeMap(
      @FormParam("policy") String policy) {
    XMLValidationLayer vLayer = getValidationLayer(policy);
    return JSONSerializer.toJSON(vLayer.getSubToSuperMap()).toString();
  }

  @POST
  @Path(PRODUCT_TYPE+"/parent/add")
  @Produces("text/plain")
  public boolean addParentForProductType(
      @FormParam("policy") String policy,
      @FormParam("id") String id,
      @FormParam("parentId") String parentId) {
    XMLValidationLayer vLayer = getValidationLayer(policy);
    XMLRepositoryManager xmlRepo = getRepo(policy);
    try {
      ProductType type = xmlRepo.getProductTypeById(id);
      vLayer.addParentForProductType(type, parentId);
      return true;
    } catch (RepositoryManagerException e) {
      LOG.log(Level.SEVERE, e.getMessage());
    }
    return false;
  }

  @DELETE
  @Path(PRODUCT_TYPE+"/parent/remove")
  @Produces("text/plain")
  public boolean removeParentForProductType(
      @FormParam("policy") String policy,
      @FormParam("id") String id)
          throws ValidationLayerException {
    XMLValidationLayer vLayer = getValidationLayer(policy);
    XMLRepositoryManager xmlRepo = getRepo(policy);
    try {
      ProductType type = xmlRepo.getProductTypeById(id);
      vLayer.removeParentForProductType(type);
      return true;
    } catch (RepositoryManagerException e) {
      LOG.log(Level.SEVERE, e.getMessage());
    }
    return false;
  }

  @POST
  @Path(PRODUCT_TYPE+"/elements/add")
  @Produces("text/plain")
  public boolean addElementsForProductType(
		  @FormParam("policy") String policy,
		  @FormParam("id") String id,
		  @FormParam("elementIds") String elementIds) {
    XMLValidationLayer vLayer = getValidationLayer(policy);
    XMLRepositoryManager xmlRepo = getRepo(policy);
    try {
      ProductType type = xmlRepo.getProductTypeById(id);
      for(String elementid: elementIds.split(",")) {
        Element element = vLayer.getElementById(elementid);
        if(element == null) {
          element = new Element(elementid, elementid, "", "", "Automatically added", "");
          vLayer.addElement(element);
        }
        vLayer.addElementToProductType(type, element);
      }
      return true;
    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
    }
    return false;
  }

  @GET
  @Path(PRODUCT_TYPE+"/elements")
  @Produces("text/plain")
  public String getElementsForProductType(
		  @FormParam("policy") String policy,
		  @FormParam("id") String id,
		  @FormParam("direct") boolean direct) {
    XMLValidationLayer vLayer = getValidationLayer(policy);
    XMLRepositoryManager xmlRepo = getRepo(policy);
    try {
      ProductType type = xmlRepo.getProductTypeById(id);
      ArrayList<String> elementIds = new ArrayList<String>();
      for(Element el : vLayer.getElements(type, direct)) {
        elementIds.add(el.getElementId());
      }
      return JSONSerializer.toJSON(elementIds).toString();
    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
    }
    return null;
  }

  @DELETE
  @Path(PRODUCT_TYPE+"/elements/remove/all")
  @Produces("text/plain")
  public boolean removeAllElementsForProductType(
		  @FormParam("policy") String policy,
		  @FormParam("id") String id) {
    XMLValidationLayer vLayer = getValidationLayer(policy);
    XMLRepositoryManager xmlRepo = getRepo(policy);
    try {
      ProductType type = xmlRepo.getProductTypeById(id);
      List<Element> elementList = vLayer.getElements(type);
      for(Element element: elementList) {
        vLayer.removeElementFromProductType(type, element);
      }
      this.removeUnusedElements(elementList, xmlRepo, vLayer);
      return true;
    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
    }
    return false;
  }

  @DELETE
  @Path(PRODUCT_TYPE+"/elements/remove")
  @Produces("text/plain")
  public boolean removeElementsForProductType(
		  @FormParam("policy") String policy,
		  @FormParam("id") String id,
		  @FormParam("elementIds") String elementIds) {
    XMLValidationLayer vLayer = getValidationLayer(policy);
    XMLRepositoryManager xmlRepo = getRepo(policy);
    try {
      ProductType type = xmlRepo.getProductTypeById(id);
      ArrayList<Element> elements = new ArrayList<Element>();
      for(String elementId: elementIds.split(",")) {
        Element element = vLayer.getElementById(elementId);
        if(element != null) {
          vLayer.removeElementFromProductType(type, element);
          elements.add(element);
        }
      }
      this.removeUnusedElements(elements, xmlRepo, vLayer);
      return true;
    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
    }
    return false;
  }

  @GET
  @Path(PRODUCT_TYPE+"/typeswithelement/{elementId}")
  @Produces("text/plain")
  public String getProductTypeIdsHavingElement(
		  @FormParam("policy") String policy,
		  @PathParam("elementId") String elementId) {
	  XMLValidationLayer vLayer = getValidationLayer(policy);
	  XMLRepositoryManager xmlRepo = getRepo(policy);
	  ArrayList<String> typeids = new ArrayList<String>();
      try {
    	  for(ProductType type : xmlRepo.getProductTypes()) {
    		  for(Element el : vLayer.getElements(type)) {
    			  if(el.getElementId().equals(elementId)) {
                    typeids.add(type.getProductTypeId());
                  }
    		  }
    	  }
      } catch (Exception e) {
          LOG.log(Level.SEVERE, e.getMessage());
      }
      return JSONSerializer.toJSON(typeids).toString();
  }


  /*
   * Private helper functions
   */
  private void removeUnusedElements(List<Element> elements,
		  XMLRepositoryManager xmlRepo, XMLValidationLayer vLayer)
				  throws ValidationLayerException, RepositoryManagerException {
      // Remove Elements that aren't used in any product type
      List<ProductType> ptypelist = xmlRepo.getProductTypes();
      ConcurrentHashMap<String, Boolean> usedElementIds = new ConcurrentHashMap<String, Boolean>();
      for(ProductType ptype: ptypelist) {
          List<Element> ptypeElements =
              vLayer.getElements(ptype);
          for(Element el: ptypeElements) {
              usedElementIds.put(el.getElementId(), true);
          }
      }
      for(Element el: elements) {
          if(!usedElementIds.containsKey(el.getElementId())) {
            vLayer.removeElement(el);
          }
      }
  }

	private XMLRepositoryManager getRepo(String policy) {
		XMLRepositoryManager xmlRepo = null;
		String url = "file://" + CurationService.config.getPolicyUploadPath() + "/" + policy;

		try {
			xmlRepo = new XMLRepositoryManager(Collections.singletonList(url));
		} catch (Exception e) {
			LOG.log(Level.SEVERE, e.getMessage());
		}

		return xmlRepo;
	}

	private XMLValidationLayer getValidationLayer(String policy) {
		XMLValidationLayer vLayer = null;
		String url = "file://" + CurationService.config.getPolicyUploadPath() + "/" + policy;

		try {
			vLayer = new XMLValidationLayer(Collections.singletonList(url));
		} catch (Exception e) {
			LOG.log(Level.SEVERE, e.getMessage());
		}
		return vLayer;
	}
}
