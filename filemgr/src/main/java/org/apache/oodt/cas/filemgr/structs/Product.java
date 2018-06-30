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

package org.apache.oodt.cas.filemgr.structs;

import org.apache.oodt.cas.filemgr.exceptions.FileManagerException;
import org.apache.oodt.commons.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * A Product is a set of files, or a heirarchical, directory structure that
 * should be ingested into the {@link org.apache.oodt.cas.filemgr.catalog.Catalog}.
 * </p>
 * 
 */
public class Product {

    /* our product id */
    private String productId = null;

    /* our product name */
    private String productName = null;

    /* our product type */
    private ProductType productType = null;

    /* our product structure: can be Heirarchical, or Flat */
    private String productStructure = null;

    /* a set of {@link References} to the items that make up this product */
    private List<Reference> references = null;

    /*
     * the transfer status of this product: is it TRANSFERING, has it been
     * RECEIVED?
     */
    private String transferStatus = null;

    /*
     * the root reference for this product: if it's a dir, it's the root of the
     * dir tree if the product is flat, it's the first (and possibly) only ref.
     */
    private Reference rootRef;

    /* a couple of static final Strings to represent the TransferStatus */
    public static final String STATUS_TRANSFER = "TRANSFERING";

    public static final String STATUS_RECEIVED = "RECEIVED";

    /* a couple of static final Strings to represent the productStructure */
    public static final String STRUCTURE_FLAT = "Flat";

    public static final String STRUCTURE_HIERARCHICAL = "Hierarchical";

    public static final String STRUCTURE_STREAM = "Stream";

    private static String[] VALID_STRUCTURES = new String[]{STRUCTURE_FLAT,STRUCTURE_HIERARCHICAL,STRUCTURE_STREAM};

    /* our log stream */
    private static final Logger LOG = Logger.getLogger(Product.class.getName());

    private String productReceivedTime;
    /**
     * <p>
     * Default Constructor
     * </p>
     * 
     */
    public Product() {
        references = new Vector<Reference>();

    }

    public Product(InputStream is) throws InstantiationException {
        if (is == null) {
            throw new InstantiationException(
                    "Unable to parse product stream: stream not set!");
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder parser = factory.newDocumentBuilder();
            references = new Vector<Reference>();
            parse(parser.parse(new InputSource(is)));
        } catch (Exception e) {
            throw new InstantiationException(
                    "Unable to parse metadata stream.[" + e.getMessage() + "]");
        }
    }

    /**
     * <p>
     * Constructs a new Product with the specified parameters.
     * </p>
     * 
     * @param name
     *            The Product's name.
     * @param pType
     *            The Product's {@link ProductType}.
     * @param structure
     *            The structure of the product: either Hierarchical, or Flat.
     * @param transferStatus
     *            The status of this product's transfer to the DataStore:
     *            TRANSFERING, or RECEIVED would work
     * @param refs
     *            A {@link List} of {@link Reference}s pointing to the items
     *            that make up this product.
     */
    public Product(String name, ProductType pType, String structure,
            String transferStatus, List<Reference> refs) {
        productName = name;
        productType = pType;
        productStructure = structure;
        references = refs;
        this.transferStatus = transferStatus;
    }

    /**
     * @return Returns the productType.
     */
    public ProductType getProductType() {
        return productType;
    }

    /**
     * @param productType
     *            The productType to set.
     */
    public void setProductType(ProductType productType) {
        this.productType = productType;
    }

    /**
     * @return Returns the productStructure.
     */
    public String getProductStructure() {
        return productStructure;
    }

    /**
     * @param productStructure
     *            The productStructure to set.
     */
    public void setProductStructure(String productStructure) {
        //Guard clause, according to a unit test null is a valid value
        if (!java.util.Arrays.asList(VALID_STRUCTURES).contains(productStructure) && productStructure != null) {
            throw new IllegalArgumentException("Undefined product structure: " + productStructure);
        }
        this.productStructure = productStructure;
    }

    /**
     * @return Returns the references.
     */
    public List<Reference> getProductReferences() {
        return references;
    }

    /**
     * @param references
     *            The references to set.
     */
    public void setProductReferences(List<Reference> references) {
        this.references = references;
    }

    /**
     * @return Returns the productName.
     */
    public String getProductName() {
        return productName;
    }

    /**
     * @param productName
     *            The productName to set.
     */
    public void setProductName(String productName) {
        this.productName = productName;
    }

    /**
     * @return Returns the productId.
     */
    public String getProductId() {
        return productId;
    }

    /**
     * @param productId
     *            The productId to set.
     */
    public void setProductId(String productId) {
        this.productId = productId;
    }

    /**
     * @return Returns the transferStatus.
     */
    public String getTransferStatus() {
        return transferStatus;
    }

    /**
     * @param transferStatus
     *            The transferStatus to set.
     */
    public void setTransferStatus(String transferStatus) {
        this.transferStatus = transferStatus;
    }

    /**
     * @return the rootRef
     */
    public Reference getRootRef() {
        return rootRef;
    }

    /**
     * @param rootRef
     *            the rootRef to set
     */
    public void setRootRef(Reference rootRef) {
        this.rootRef = rootRef;
    }

    public static Product getDefaultFlatProduct(String name,
            String defaultProductTypeId) {
        Product defaultProduct = new Product();
        defaultProduct.setProductName(name);
        defaultProduct.setProductReferences(new Vector<Reference>());
        defaultProduct.setProductStructure(Product.STRUCTURE_FLAT);
        ProductType pType = new ProductType();
        pType.setProductTypeId(defaultProductTypeId);
        defaultProduct.setProductType(pType);
        defaultProduct.setTransferStatus(Product.STATUS_TRANSFER);
        return defaultProduct;
    }

    public Document toXML() throws UnsupportedEncodingException, FileManagerException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        Document doc;

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.newDocument();

            Element root = (Element) doc.createElement("cas:product");
            XMLUtils.addAttribute(doc, root, "xmlns:cas",
                    "http://oodt.jpl.nasa.gov/1.0/cas");
            XMLUtils.addAttribute(doc, root, "id", this.productId);
            XMLUtils.addAttribute(doc, root, "name", URLEncoder.encode(
                    this.productName, "UTF-8"));
            doc.appendChild(root);

            XMLUtils.addNode(doc, root, "structure", this.productStructure);
            XMLUtils.addNode(doc, root, "transferStatus", this.transferStatus);
            XMLUtils.addNode(doc, root, "type",
                    this.productType != null ? this.productType.getName() : "");

            if (this.getProductReferences() != null
                    && this.getProductReferences().size() > 0) {
                Element refsElem = XMLUtils.addNode(doc, root, "references");
                for (Reference r : this.getProductReferences()) {
                    Element refElem = XMLUtils.addNode(doc, refsElem,
                        "reference");
                    XMLUtils.addAttribute(doc, refElem, "orig", r
                        .getOrigReference());
                    XMLUtils.addAttribute(doc, refElem, "dataStore", r
                        .getDataStoreReference());
                    XMLUtils.addAttribute(doc, refElem, "size", String
                        .valueOf(r.getFileSize()));

                }
            }

        } catch (ParserConfigurationException pce) {
            LOG.log(Level.WARNING, "Error generating product xml file!: "
                    + pce.getMessage());
            throw new FileManagerException("Error generating product xml file!: "
                    + pce.getMessage());
        }

        return doc;
    }

    private void parse(Document doc) {
        Element root = doc.getDocumentElement();

        this.productId = root.getAttribute("id");
        try {
            this.productName = URLDecoder.decode(root.getAttribute("name"),
                    "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOG.log(Level.WARNING,
                    "Unable to set product name: error decoding encoded string: Message: "
                            + e.getMessage());

        }

        this.productStructure = XMLUtils.getElementText("structure", root);
        this.productType = new ProductType();
        this.productType.setName(XMLUtils.getElementText("type", root));
        this.transferStatus = XMLUtils.getElementText("transferStatus", root);

        Element refsElem = XMLUtils.getFirstElement("references", root);

        if (refsElem != null) {
            NodeList refNodeList = refsElem.getElementsByTagName("reference");

            if (refNodeList != null && refNodeList.getLength() > 0) {
                for (int i = 0; i < refNodeList.getLength(); i++) {
                    Element refElem = (Element) refNodeList.item(i);
                    Reference r = new Reference();
                    r.setOrigReference(refElem.getAttribute("orig"));
                    r.setDataStoreReference(refElem.getAttribute("dataStore"));
                    r.setFileSize(Long.valueOf(refElem.getAttribute("size")));
                    this.references.add(r);
                }
            }
        }

    }
    /**
     * Set time product was ingested.
     * @param productReceivedTime
     */
    public void setProductRecievedTime(String productReceivedTime) {
        this.productReceivedTime = productReceivedTime;
    }

    public String getProductReceivedTime() {
        return productReceivedTime;
    }

    @Override
    public String toString() {
        return String.format("%s[%s-%s]", this.getProductId(), this.getProductName(), this.getProductType());
    }
}
