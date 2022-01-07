package org.apache.oodt.cas.product.jaxrs.resources;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductPage;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * Implements tests for methods in the {@link ProductPageResource} class.
 *
 * @author ngimhana (Nadeeshan Gimhana)
 */
public class ProductPageResourceTest {

  @Test
  public void testXmlMarshalling()
      throws JAXBException, IOException, SAXException, MimeTypeException {

    // Create a ProductResource using ProductType, Reference, Metadata and
    // Product instances.
    HashMap<String, Object> metadataEntries = new HashMap<>();
    metadataEntries.put("CAS.Test", "test value");
    Metadata metadata = new Metadata();
    metadata.addMetadata(metadataEntries);

    List<Metadata> metadataList = new Vector<>();
    metadataList.add(metadata);

    Reference reference =
        new Reference("original", "dataStore", 1000, new MimeTypes().forName("text/plain"));
    List<Reference> references = new ArrayList<>();
    references.add(reference);

    List<List<Reference>> productReferencesList = new Vector<>();
    productReferencesList.add(references);

    ProductType productType =
        new ProductType("1", "GenericFile", "test type", "repository", "versioner");

    Product product = new Product();
    product.setProductId("123");
    product.setProductName("test.txt");
    product.setProductStructure(Product.STRUCTURE_FLAT);
    product.setProductType(productType);
    product.setTransferStatus("RECEIVED");

    List<Product> pageProducts = new Vector<>();
    pageProducts.add(product);

    ProductPage productPage = new ProductPage(1, 1, 20, pageProducts);

    //      Create a ProductPageResource using Reference, Metadata and Product instances.
    ProductPageResource resource =
        new ProductPageResource(productPage, metadataList, productReferencesList, new File("/tmp"));

    //      Generate the expected output.
    String expectedXml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
            + "<productPage>"
            + "<pageSize>"
            + resource.getPageSize()
            + "</pageSize>"
            + "<pageNum>"
            + resource.getPageNum()
            + "</pageNum>"
            + "<totalPages>"
            + resource.getTotalPages()
            + "</totalPages>"
            + "<totalProducts>"
            + resource.getTotalProducts()
            + "</totalProducts>"
            + "<numOfHits>"
            + resource.getNumOfHits()
            + "</numOfHits>"
            + "<products>"
            + "<product>"
            + "<id>"
            + product.getProductId()
            + "</id>"
            + "<name>"
            + product.getProductName()
            + "</name>"
            + "<structure>"
            + product.getProductStructure()
            + "</structure>"
            + "<type>"
            + productType.getName()
            + "</type>"
            + "<transferStatus>"
            + product.getTransferStatus()
            + "</transferStatus>"
            + "<metadata>"
            + "<keyval>"
            + "<key>"
            + metadataList.get(0).getAllKeys().get(0)
            + "</key>"
            + "<val>"
            + metadataList.get(0).getAllValues().get(0)
            + "</val>"
            + "</keyval>"
            + "</metadata>"
            + "<references>"
            + "<reference>"
            + "<productId>"
            + product.getProductId()
            + "</productId>"
            + "<refIndex>0</refIndex>"
            + "<dataStoreReference>"
            + productReferencesList.get(0).get(0).getDataStoreReference()
            + "</dataStoreReference>"
            + "<originalReference>"
            + productReferencesList.get(0).get(0).getOrigReference()
            + "</originalReference>"
            + "<mimeType>"
            + productReferencesList.get(0).get(0).getMimeType().getName()
            + "</mimeType>"
            + "<fileSize>"
            + productReferencesList.get(0).get(0).getFileSize()
            + "</fileSize>"
            + "</reference>"
            + "</references>"
            + "</product>"
            + "</products>"
            + "</productPage>";

    // Set up a JAXB context and marshall the ProductResource to XML.
    JAXBContext context = JAXBContext.newInstance(resource.getClass());
    Marshaller marshaller = context.createMarshaller();
    StringWriter writer = new StringWriter();
    marshaller.marshal(resource, writer);

    // Compare the expected and actual outputs.
    XMLUnit.setIgnoreWhitespace(true);
    XMLUnit.setIgnoreComments(true);
    XMLUnit.setIgnoreAttributeOrder(true);
    Diff diff = new Diff(expectedXml, writer.toString());
    assertTrue(
        "The output XML was different to the expected XML: " + diff.toString(), diff.identical());
  }
}
