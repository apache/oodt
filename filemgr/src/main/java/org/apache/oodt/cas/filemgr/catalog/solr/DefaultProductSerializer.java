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

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.metadata.Metadata;

import org.apache.solr.client.solrj.util.ClientUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Default implementation of {@link ProductSerializer}
 * that transforms a CAS product into a single Solr document based on the following rules:
 * o) the core product attributes are used to generate Solr fields starting with "CAS...."
 * o) the product references are converted to Solr fields starting with "CAS.Reference..." or "CAS.RootReference..."
 * o) all other metadata fields are converted into Solr fields with the same name and number of values.
 *    Note that the field multiplicity must be consistent with its definition in the Solr schema.xml.
 *
 * This class generates all Solr documents in XML format.
 *
 * @author Luca Cinquini
 *
 */
public class DefaultProductSerializer implements ProductSerializer {

	private static Logger LOG = Logger.getLogger(DefaultProductSerializer.class.getName());
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getMimeType() {
		return Parameters.MIME_TYPE_XML;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> serialize(Product product, boolean create) {

		Map<String, List<String>> fields = new ConcurrentHashMap<String, List<String>>();
		List<String> docs = new ArrayList<String>();

		// add core product attributes to map
		this.addKeyValueToMap(fields, Parameters.PRODUCT_ID, product.getProductId());
		this.addKeyValueToMap(fields, Parameters.PRODUCT_NAME, product.getProductName());
		this.addKeyValueToMap(fields, Parameters.PRODUCT_STRUCTURE, product.getProductStructure());
		this.addKeyValueToMap(fields, Parameters.PRODUCT_TRANSFER_STATUS, product.getTransferStatus());
		ProductType productType = product.getProductType();
		if (productType!=null) {
			this.addKeyValueToMap(fields, Parameters.PRODUCT_TYPE_NAME, productType.getName());
			this.addKeyValueToMap(fields, Parameters.PRODUCT_TYPE_ID, productType.getProductTypeId());
		}
		if (create) {
			// only insert date/time when product is first created
			Date productDateTime = new Date(); // current datetime
			this.addKeyValueToMap(fields, Parameters.PRODUCT_RECEIVED_TIME, Parameters.SOLR_DATE_TIME_FORMATTER.format(productDateTime));
		}

		// create new product: use Solr id == CAS id
		if (create) {
			docs.add( this.generateInsertDocuments(product.getProductId(), fields) );

			// update existing product
		} else {
			docs.addAll( this.generateUpdateDocuments(product.getProductId(), fields, true) ); // replace=true

		}

		return docs;

	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> serialize(String productId, Reference rootReference, List<Reference> references, boolean replace) {

		Map<String, List<String>> fields = new ConcurrentHashMap<String, List<String>>();

		// product root reference
		if (rootReference!=null) {

			addKeyValueToMap(fields, Parameters.ROOT_REFERENCE_ORIGINAL, StringEscapeUtils.escapeXml(rootReference.getOrigReference()));
			addKeyValueToMap(fields, Parameters.ROOT_REFERENCE_DATASTORE, StringEscapeUtils.escapeXml(rootReference.getDataStoreReference()));
			addKeyValueToMap(fields, Parameters.ROOT_REFERENCE_FILESIZE, ""+rootReference.getFileSize());
			addKeyValueToMap(fields, Parameters.ROOT_REFERENCE_MIMETYPE, StringEscapeUtils.escapeXml(rootReference.getMimeType().toString()));

		}

		// all other product references
		// note that Solr will preserve the indexing order.
		for (Reference reference : references) {

			addKeyValueToMap(fields, Parameters.REFERENCE_ORIGINAL, StringEscapeUtils.escapeXml(reference.getOrigReference()));
			addKeyValueToMap(fields, Parameters.REFERENCE_DATASTORE, StringEscapeUtils.escapeXml(reference.getDataStoreReference()));
			addKeyValueToMap(fields, Parameters.REFERENCE_FILESIZE, ""+reference.getFileSize());
			addKeyValueToMap(fields, Parameters.REFERENCE_MIMETYPE, StringEscapeUtils.escapeXml(reference.getMimeType().toString()));

		}

		return generateUpdateDocuments(productId, fields, replace);

	}

	/**
	 * {@inheritDoc}
	 */
	public QueryResponse deserialize(String xml) throws CatalogException {

		try {

			QueryResponse queryResponse = new QueryResponse();

			// parse XML into DOM
			Document document = parseXml(xml);

			// extract information from DOM to Product
			Element response = document.getDocumentElement();
			Node result = response.getElementsByTagName("result").item(0);
			queryResponse.setNumFound( Integer.parseInt( ((Element)result).getAttribute("numFound") ) );
			queryResponse.setStart( Integer.parseInt( ((Element)result).getAttribute("start") ) );
			NodeList docs = result.getChildNodes();
			for (int i=0; i< docs.getLength(); i++) {
				Node node = docs.item(i);
				if (node.getNodeName().equals("doc")) {
					Element doc = (Element)node;
					CompleteProduct cp = this.deserialize(doc);
					queryResponse.getCompleteProducts().add(cp);
				}
			}
			return queryResponse;

		} catch(Exception e) {
			LOG.log(Level.SEVERE, e.getMessage());
			throw new CatalogException(e.getMessage(), e);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> serialize(String productId, Metadata metadata, boolean replace) {

		Map<String, List<String>> fields = new ConcurrentHashMap<String, List<String>>();

		for (String key : metadata.getKeys()) {
			if (! (key.startsWith(Parameters.NS)              // skip metadata keys starting with reserved namespace
					//|| Parameters.PRODUCT_TYPE_NAME.contains(key)
					// skip 'ProductType' as already stored as 'CAS.ProductTypeName'
					|| Parameters.PRODUCT_STRUCTURE.contains(key))) { // skip 'ProductType' as already stored as 'CAS.ProductStructure'
				for (String value : metadata.getAllMetadata(key)) {
					this.addKeyValueToMap(fields, key, StringEscapeUtils.escapeXml(value));
				}
			}
		}

		return this.generateUpdateDocuments(productId, fields, replace);

	}

	/**
	 * Method to add a (key, value) to a multi-valued map with appropriate checks.
	 * @param map
	 * @param key
	 * @param value
	 */
	protected void addKeyValueToMap(Map<String, List<String>> map, String key, String value) {

		if (!map.containsKey(key)) {
			map.put(key, new ArrayList<String>());
		}
		if (value!=null) {
			map.get(key).add(value);
		} else {
			// use special value to trigger key removal
			map.get(key).add(Parameters.NULL);
		}
	}

	/**
	 * Utility method to generate a Solr insert document.
	 *
	 * @param productId
	 * @param fields
	 * @return
	 */
	protected String generateInsertDocuments(String productId, Map<String,List<String>> fields) {

		StringBuilder doc = new StringBuilder();
		doc.append("<doc>");

		// product Solr id field
		doc.append( encodeIndexField(Parameters.ID, productId) );

		// all other fields
		for (Map.Entry<String, List<String>> key : fields.entrySet()) {
			for (String value : key.getValue()) {
				doc.append( encodeIndexField(key.getKey(), StringEscapeUtils.escapeXml(value)) );
			}
		}

		doc.append("</doc>");
		return doc.toString();
	}

	/**
	 * Utility method to generate Solr update documents.
	 * Note that the requests for setting/adding/deleting fields must be sent as separate documents to Solr
	 * @param productId
	 * @param fields
	 * @param replace
	 * @return
	 */
	protected List<String> generateUpdateDocuments(String productId, Map<String,List<String>> fields, boolean replace) {

		// list for different instruction types
		List<String> setFields = new ArrayList<String>();
		List<String> addFields = new ArrayList<String>();
		List<String> delFields = new ArrayList<String>();

		// encode update instructions
		for (Map.Entry<String, List<String>> key : fields.entrySet()) {

			List<String> values = key.getValue();

			if (replace) {

				if (values.isEmpty()) {
					// use special value to flag removal
					delFields.add( this.encodeUpdateField(key.getKey(), Parameters.NULL, true) );

				} else {
					for (String value : values) {
						setFields.add( this.encodeUpdateField(key.getKey(), StringEscapeUtils.escapeXml(value), true) );
					}
				}

			} else {
				for (String value : values) {
					addFields.add( this.encodeUpdateField(key.getKey(), StringEscapeUtils.escapeXml(value), false) );
				}
			}

		}

		List<String> docs = new ArrayList<String>();
		if (!delFields.isEmpty()) {
			docs.add(toDoc(productId, delFields));
		}
		if (!setFields.isEmpty()) {
			docs.add(toDoc(productId, setFields));
		}
		if (!addFields.isEmpty()) {
			docs.add(toDoc(productId, addFields));
		}
		return docs;

	}

	/**
	 * Utility method to merge field update instructions into a single document.
	 * @param productId
	 * @param updates
	 * @return
	 */
	private String toDoc(String productId, List<String> updates) {

		StringBuilder doc = new StringBuilder();
		doc.append("<doc>");

		// reference product record id
		doc.append( encodeIndexField(Parameters.ID, productId) );

		// loop over field update instructions
		for (String update : updates) {
			doc.append(update);
		}

		doc.append("</doc>");

		return doc.toString();

	}

	/**
	 * Method to encode a Solr field indexing instruction.
	 * If the value is null, the empty string is returned.
	 * @param key
	 * @param value
	 * @return
	 */
	protected String encodeIndexField(String key, String value) {
		if (value==null || value.equals(Parameters.NULL)) {
			return "";
		} else {
			return "<field name=\""+key+"\">" + value + "</field>";
		}
	}

	/**
	 * Method to encode a field update instruction for the three possible cases:
	 * add new values to a key (1), replace current values for a key (2), remove all values for a key (3).
	 *
	 * @param key
	 * @param value
	 * @param replace
	 * @return
	 */
	protected String encodeUpdateField(String key, String value, boolean replace) {
		StringBuilder sb = new StringBuilder();
		sb.append("<field name=\"").append(key).append("\"");

		if (replace) {

			if (value==null || value.equals(Parameters.NULL)) {

				// (3) remove all values for given key
				sb.append(" update=\"set\" null=\"true\" />");

			} else {

				// (2) replace existing values with new values
				sb.append(" update=\"set\">").append(value).append("</field>");
			}

		} else {

			// (1) add new values to existing values
			sb.append(" update=\"add\">").append(value).append("</field>");

		}

		return sb.toString();
	}



	/**
	 * Method that parses a single Solr document snippet
	 * to extract Product and Metadata attributes.
	 *
	 * @param doc
	 * @return
	 */
	private CompleteProduct deserialize(Element doc) {

		CompleteProduct cp = new CompleteProduct();
		Product product = cp.getProduct();
		ProductType productType = product.getProductType();
		Metadata metadata = cp.getMetadata();
		List<Reference> references = product.getProductReferences();
		Reference rootReference = product.getRootRef();

		NodeList children = doc.getChildNodes();
		for (int j=0; j<children.getLength(); j++) {

			Node child = children.item(j);
			Element element = (Element)child;
			String name = element.getAttribute("name");

			/**
			 *<arr name="ScanPointingSource">
			 *	<str>G073.65+0.19</str>
			 *	<str>J2015+3410</str>
			 *  ..........
			 */
			if (child.getNodeName().equals("arr")) {

				NodeList values = element.getChildNodes();
				List<String> vals = new ArrayList<String>();
				for (int k=0; k<values.getLength(); k++) {
					String value = ((Element)values.item(k)).getTextContent();
					vals.add(StringEscapeUtils.unescapeXml(value));
				}
				// CAS.reference.... fields
				if (name.startsWith(Parameters.NS)) {
					for (int k=0; k<values.getLength(); k++) {
						// create this reference
						if (references.size()<=k) {
							references.add(new Reference());
						}
						if (name.equals(Parameters.REFERENCE_ORIGINAL)) {
							references.get(k).setOrigReference(vals.get(k));
						} else if (name.equals(Parameters.REFERENCE_DATASTORE)) {
							references.get(k).setDataStoreReference(vals.get(k));
						} else if (name.equals(Parameters.REFERENCE_FILESIZE)) {
							references.get(k).setFileSize(Long.parseLong(vals.get(k)));
						} else if (name.equals(Parameters.REFERENCE_MIMETYPE)) {
							references.get(k).setMimeType(vals.get(k));
						}
					}
					// all other multi-valued fields
				} else {
					this.deserializeMultiValueField(name, vals, metadata);
				}

				/**
				 * 	<str name="id">6684d79d-a011-4bc0-b3b3-4f11817091c8</str>
				 *  <str name="CAS.ProductId">6684d79d-a011-4bc0-b3b3-4f11817091c8</str>
				 *  <str name="CAS.ProductName">tns_br145x4_20</str>
				 *  <str name="FileLocation">/usr/local/ska-dc/data/archive</str>
				 *  ...........
				 */
			} else {

				String value = StringEscapeUtils.unescapeXml(element.getTextContent());

				// core CAS fields
				if (name.startsWith(Parameters.NS)) {
					if (name.equals(Parameters.PRODUCT_ID)) {
						product.setProductId(value);
						metadata.addMetadata(name, value);

					} else if (name.equals(Parameters.PRODUCT_NAME)) {
						product.setProductName(value);
						metadata.addMetadata(name, value);

					} else if (name.equals(Parameters.PRODUCT_STRUCTURE)) {
						product.setProductStructure(value);
						metadata.addMetadata(name, value);

					} else if (name.equals(Parameters.PRODUCT_TRANSFER_STATUS)) {
						product.setTransferStatus(value);
						metadata.addMetadata(name, value);

					} else if (name.equals(Parameters.PRODUCT_TYPE_NAME)) {
						productType.setName(value);
						metadata.addMetadata(name, value);

					} else if (name.equals(Parameters.PRODUCT_TYPE_ID)) {
						productType.setProductTypeId(value);
						metadata.addMetadata(name, value);

					} else if (name.equals(Parameters.PRODUCT_RECEIVED_TIME)) {
						product.setProductRecievedTime(value);
						metadata.addMetadata(name, value);
						// CAS root reference
					} else if (name.startsWith(Parameters.NS+Parameters.ROOT)) {
						if (rootReference==null) {
							rootReference = new Reference();
						}
						if (name.equals(Parameters.ROOT_REFERENCE_ORIGINAL)) {
							rootReference.setOrigReference(value);
						} else if (name.equals(Parameters.ROOT_REFERENCE_DATASTORE)) {
							rootReference.setDataStoreReference(value);
						} else if (name.equals(Parameters.ROOT_REFERENCE_FILESIZE)) {
							rootReference.setFileSize(Long.parseLong(value));
						} else if (name.equals(Parameters.ROOT_REFERENCE_MIMETYPE)) {
							rootReference.setMimeType(value);
						}

					}

					// non core single-valued fields
				} else {
					this.deserializeSingleValueField(name, value, metadata);
				} // "CAS".... or not

			} // "arr" or anything else

		} // loop over <doc> children

		return cp;

	}

	private Document parseXml(String xml) throws IOException, SAXException, ParserConfigurationException {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder parser = factory.newDocumentBuilder();
		return parser.parse( new InputSource(new StringReader(xml)) );

	}

	/**
	 * Method that deserializes a single-valued Solr field into a Metadata element.
	 * This method can be overridden by sub-classes to provide custom behavior.
	 *
	 * @param name : the Solr field name
	 * @param value : the Solr field single value
	 * @param metadata : the metadata container
	 */
	protected void deserializeSingleValueField(String name, String value, Metadata metadata) {
		// ignore Solr internal identifier (as it is duplicate information of CAS.ProductId)
		if (!name.equals(Parameters.ID)){
			metadata.addMetadata(name, value);
		}

	}

	/**
	 * Method that deserializes a multi-valued Solr field into a Metadata element.
	 * This method can be overridden by sub-classes to provide custom behavior.
	 *
	 * @param name : the Solr field name
	 * @param values : the Solr field multiple values
	 * @param metadata : the metadata container
	 */
	protected void deserializeMultiValueField(String name, List<String> values, Metadata metadata) {
		metadata.addMetadata(name, values);
	}


}
