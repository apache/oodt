package org.apache.oodt.cas.product.jaxrs.services;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.oodt.cas.filemgr.exceptions.FileManagerException;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.Query;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.TermQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.structs.exceptions.DataTransferException;
import org.apache.oodt.cas.filemgr.structs.exceptions.RepositoryManagerException;
import org.apache.oodt.cas.filemgr.structs.exceptions.ValidationLayerException;
import org.apache.oodt.cas.filemgr.structs.exceptions.VersioningException;
import org.apache.oodt.cas.filemgr.system.XmlRpcFileManagerClient;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.xmlrpc.XmlRpcException;

public class CelgeneDataAssetMetadataUpdater {

  private static final String DA_NUMBER_ELEMENT = "DataAssetNumber";
  private static final Logger LOG =
      Logger.getLogger(CelgeneDataAssetMetadataUpdater.class.getName());
  private static String SOLR_URL;

  private XmlRpcFileManagerClient client = null;

  /**
   * Setup the xml-rpc file manager client.
   *
   * @param url - file manager's url
   * @throws ConnectionException - exception when the connection is not established
   * @throws MalformedURLException - exception from a bad url
   */
  public void setup(String url) throws ConnectionException, MalformedURLException {
    URL fmUrl = new URL(url);
    LOG.log(Level.INFO, "Connecting to File Manager at:" + fmUrl.toString());
    this.client = new XmlRpcFileManagerClient(fmUrl);
  }

  /**
   * Gets list of product ids in catalog belonging to given daNumber.
   *
   * @param daNumber - daNumber to check
   * @param field - field being updated
   * @return list of product ids
   * @throws CatalogException - exception in the catalog layer
   */
  public List<String> getProductIds(String daNumber, String field)
      throws CatalogException, RepositoryManagerException {
    List<String> ids = new LinkedList<String>();
    //Build a query
    Query query = new Query();
    TermQueryCriteria qc = new TermQueryCriteria();
    qc.setElementName(DA_NUMBER_ELEMENT);
    qc.setValue(daNumber);
    query.addCriterion(qc);
    for (ProductType type : this.client.getProductTypes()) {
      for (Product prod : this.client.query(query, type)) {
        ids.add(prod.getProductId());
      }
    }
    return ids;
  }

  private void update(Product product, Metadata metadata, String field, String value, boolean force)
      throws RepositoryManagerException, CatalogException, DataTransferException {
    if (field.equals("ProductType")) {
      boolean isValidProductType = false;
      for (ProductType type : this.client.getProductTypes()) {
        if (type.getName().equals(value)) {
          product.setProductType(type);
          isValidProductType = true;
          break;
        }
      }
      if (!isValidProductType) {
        throw new CatalogException("Error: " + value + " is not a valid Product Type");
      }
    }

    String fn = metadata.getMetadata("Filename");
    String currFilelocation = metadata.getMetadata("FileLocation");

    String oldDaNumber = metadata.getMetadata("DataAssetNumber");
    String archiveLoc = currFilelocation.substring(0, currFilelocation.indexOf(oldDaNumber));
    String[] params = currFilelocation.substring(currFilelocation.indexOf(oldDaNumber)).split("/");
    String newDaNumber =
        (field.equals("DataAssetNumber")) ? value : metadata.getMetadata("DataAssetNumber");
    String newProductType =
        (field.equals("ProductType")) ? value : metadata.getMetadata("ProductType");
    String newExperimentType =
        (field.equals("ExperimentType")) ? value : metadata.getMetadata("ExperimentType");

    StringBuilder builder = new StringBuilder();
    builder = builder.append(newDaNumber).append("/")
        .append(newExperimentType).append("/")
        .append(newProductType).append("/");
    for (int i = 3; i < params.length; i++) {
      builder.append(params[i]).append("/");
    }

    String newFileLocation = archiveLoc + builder.toString();
    String newProductName = builder.toString().replaceAll("/", "-") + fn;

    try {
      this.client.moveProduct(product, newFileLocation + "/" + fn);
    } catch (Exception moveException) {
      System.out.println("Failed to move product " + newProductName + " to " + newFileLocation);
      if (force) {
        System.out.println("Forcing product update");
      } else {
        throw new DataTransferException("File does not exist");
      }
    }
    metadata.replaceMetadata("FileLocation", newFileLocation);
    product.setProductName(newProductName);
    metadata.replaceMetadata("ProductName", newProductName);
    this.client.updateMetadata(product, metadata);
    List<Reference> refs = this.client.getProductReferences(product);
    product.setProductReferences(refs);
    this.client.removeProduct(product);

    try {
      this.client.ingestProduct(product, metadata, false);
    } catch (VersioningException versioningException) {
      // TODO Auto-generated catch block
      versioningException.printStackTrace();
    } catch (XmlRpcException xmlException) {
      // TODO Auto-generated catch block
      xmlException.printStackTrace();
    } catch (FileManagerException fileManagerException) {
      // TODO Auto-generated catch block
      fileManagerException.printStackTrace();
    }

  }

  /**
   * Update an individual project.
   *
   * @param id - id of product
   * @param force Force update product and ignore file move errors
   * @return true if successful
   * @throws CatalogException - thrown on catalog failure
   */
  public boolean update(String id, String field, String value, boolean force)
      throws RepositoryManagerException {
    Product product = null;
    try {
      product = this.client.getProductById(id);
      Metadata metadata = this.client.getMetadata(product);

      if (!metadata.getMetadata("ProductType").equals("DataAsset")) {
        if (field.equals("ProductType") || field.equals("ExperimentType") || field
            .equals("DataAssetNumber")) {
          update(product, metadata, field, value, force);
        }
      }
      metadata.replaceMetadata(field, value);
      this.client.updateMetadata(product, metadata);
      if (metadata.getMetadata("ProductType").equals("DataAsset") && field
          .equalsIgnoreCase("DataAssetNumber")) {
        product.setProductName(value);
        this.client.removeProduct(product);
        try {
          this.client.ingestProduct(product, metadata, false);
        } catch (VersioningException versioningException) {
          // TODO Auto-generated catch block
          versioningException.printStackTrace();
        } catch (XmlRpcException xmlException) {
          // TODO Auto-generated catch block
          xmlException.printStackTrace();
        } catch (FileManagerException fileManagerException) {
          // TODO Auto-generated catch block
          fileManagerException.printStackTrace();
        }
      }

      if (metadata.getValues().isEmpty() || !metadata.getMetadata(field).equals(value)) {
        throw new CatalogException(
            "Error: Metadata update failed to save. Deteceted value: " + metadata.getValues()
                .get(0));
      }
      System.out.println("Sucessfully update product: " + id);
    } catch (CatalogException catalogException) {
      System.err.println("Error: Failed update product: " + (product == null ? "unavailable"
          : product.getProductName()) + ". " + catalogException.getMessage());
      return false;
    } catch (DataTransferException dataTransferException) {
      dataTransferException.printStackTrace();
      return false;
    }
    return true;
  }

  /**
   * DA Number updating function.
   *
   * @param daNumber - da number to update
   * @param field - field to update
   * @param value - value to update with
   */
  public void updateAll(String type, String daNumber, String field, String value, boolean force)
      throws CatalogException, UpdateException, RepositoryManagerException,
      ValidationLayerException {
    System.out.println("Updating: " + daNumber);
    if (type == null || type.equalsIgnoreCase("DA")) {
      List<String> ids = this.getProductIds(daNumber, field);
      int count = 0;
      if (ids.isEmpty()) {
        throw new UpdateException("No files found in " + daNumber);
      }
      for (String id : ids) {
        if (this.update(id, field, value, force)) {
          count++;
        }
      }
      System.out.println("Successfully updated " + count + "/" + ids.size() + " products.");

    } else if (type.equalsIgnoreCase("PROD")) {
      if (this.update(daNumber, field, value, force)) {
        System.out.println("Successfully updated product.");
      } else {
        return;
      }
    } else {
      throw new UpdateException("Unknown Type Parameter");
    }
    System.out.println("Field: " + field);
    System.out.println("New Value: " + value);
  }

  /**
   * Main Program.
   *
   * @param args - arguments to parse
   */
  public static void main(String[] args) {
    org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
    //Check if enough arguments
    if (args.length <= 3) {
      System.err.println(
          "Usage:\n\tmetadata-update <file-manager-url> <product id> <field> <value> <force> \n");
      System.exit(1);
    }
    //Variable assignments, for clarity
    String url = args[0];
    String da = args[1];
    String field = args[2];
    String value = args[3];
    boolean force = false;
    if (args.length == 5) {
      if (args[4].equalsIgnoreCase("true")) {
        force = true;
      }
    }

    CelgeneDataAssetMetadataUpdater updater = new CelgeneDataAssetMetadataUpdater();
    try {
      updater.setup(url);
      updater.update(da, field, value, force);
    } catch (ConnectionException connectionException) {
      System.err.println("Error: Failed to connect to file-manager at: " + url);
      System.exit(2);
    } catch (MalformedURLException urlException) {
      System.err.println(
          "Error: Incorrect filemanager url: " + url + ". Please use form: http://<host>:<port>");
      System.exit(3);
    } catch (RepositoryManagerException repoException) {
      System.err.println("Error: Failed to retrieve product types: " + repoException.getMessage());
      System.exit(6);
    }
  }

  /**
   * Exception during update.
   *
   * @author mstarch
   */
  class UpdateException extends Exception {

    /**
     * Constructor.
     *
     * @param message - message
     */
    public UpdateException(String message) {
      super(message);
    }
  }

}