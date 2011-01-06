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
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports
import org.apache.oodt.cas.catalog.exception.CatalogDictionaryException;
import org.apache.oodt.cas.catalog.query.CustomQueryExpression;
import org.apache.oodt.cas.catalog.query.CustomWrapperQueryExpression;
import org.apache.oodt.cas.catalog.query.QueryExpression;
import org.apache.oodt.cas.catalog.query.QueryLogicalGroup;
import org.apache.oodt.cas.catalog.query.StdQueryExpression;
import org.apache.oodt.cas.catalog.query.TermQueryExpression;
import org.apache.oodt.cas.catalog.struct.Dictionary;
import org.apache.oodt.cas.catalog.term.Term;
import org.apache.oodt.cas.catalog.term.TermBucket;
import org.apache.oodt.cas.filemgr.metadata.CoreMetKeys;
import org.apache.oodt.cas.filemgr.structs.Element;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.exceptions.ValidationLayerException;
import org.apache.oodt.cas.metadata.Metadata;

/**
 * @author bfoster
 * @version $Revision$
 * 
 * <p></p>
 * 
 */
public class ProductDictionary implements Dictionary {

//	private static final long serialVersionUID = 311618724382619649L;
	
	private static Logger LOG = Logger.getLogger(ProductDictionary.class.getName());
	
	protected ProductType productType;
	protected List<Element> productTypeElements;
	
	/*
	 * (non-Javadoc)
	 * @see org.apache.oodt.cas.catalog.struct.Dictionary#understands(org.apache.oodt.cas.catalog.query.QueryExpression)
	 */
	public boolean understands(QueryExpression queryExpression) throws CatalogDictionaryException {
		try {
			Set<String> productTypeNames = queryExpression.getBucketNames();
			if (productTypeNames == null || productTypeNames.contains(productType.getName())) {
				if (queryExpression instanceof TermQueryExpression) {
					return this.isUnderstoodProductTypeTerm(((TermQueryExpression) queryExpression).getTerm());
				}else if (queryExpression instanceof CustomQueryExpression) {
					CustomQueryExpression customQE = (CustomQueryExpression) queryExpression;
					if (customQE.getName().equals(CatalogServiceMetKeys.PRODUCT_NAME_QUERY_EXPRESSION) || customQE.getName().equals(CatalogServiceMetKeys.TOP_N_QUERY_EXPRESSION))
						return true;
				}else if (queryExpression instanceof StdQueryExpression || queryExpression instanceof QueryLogicalGroup) {
					return true;
				}else if (queryExpression instanceof CustomWrapperQueryExpression) {
					return ((CustomWrapperQueryExpression) queryExpression).getMeaning().equals(CatalogServiceMetKeys.FILEMGR_CATALOGS_ONLY);
				}
			}
			return false;
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to check if understood query : " + e.getMessage(), e);
			throw new CatalogDictionaryException("Failed to check if understood query : " + e.getMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.oodt.cas.catalog.struct.Dictionary#lookup(org.apache.oodt.cas.metadata.Metadata)
	 */
	public TermBucket lookup(Metadata metadata) throws CatalogDictionaryException {
		try {
			List<String> productTypeNames = metadata.getAllMetadata(CoreMetKeys.PRODUCT_TYPE);
			if (productTypeNames != null && productTypeNames.contains(this.productType.getName()))
				return CatalogServiceUtils.asTermBucket(metadata, this.productType, this.productTypeElements);
			else
				return null;
		}catch (Exception e) {
			throw new CatalogDictionaryException("", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.oodt.cas.catalog.struct.Dictionary#reverseLookup(org.apache.oodt.cas.catalog.term.TermBucket)
	 */
	public Metadata reverseLookup(TermBucket termBucket) throws CatalogDictionaryException {
		try {
			Metadata metadata = new Metadata();
			if (termBucket.getName().equals(this.productType.getName()))
				for (Term term : termBucket.getTerms()) 
					if (this.isUnderstoodTerm(term)) 
						metadata.addMetadata(term.getName(), term.getValues());
			return metadata;
		}catch (Exception e) {
			throw new CatalogDictionaryException("", e);
		}
	}
	
	protected boolean isUnderstoodTerm(Term term) throws ValidationLayerException {
		if (term.getName().equals(CoreMetKeys.PRODUCT_REFERENCE_MIME_TYPE) ||
				term.getName().equals(CoreMetKeys.PRODUCT_REFERENCE_ORIGINAL) ||
				term.getName().equals(CoreMetKeys.PRODUCT_REFERENCE_DATA_STORE) ||
				term.getName().equals(CoreMetKeys.PRODUCT_REFERENCE_FILE_SIZE) ||
				term.getName().equals(CoreMetKeys.PRODUCT_NAME) ||
				term.getName().equals(CoreMetKeys.PRODUCT_STRUCTURE) ||
				term.getName().equals(CoreMetKeys.PRODUCT_ROOT_REF_DATA_STORE) ||
				term.getName().equals(CoreMetKeys.PRODUCT_ROOT_REF_ORIG) ||
				term.getName().equals(CoreMetKeys.PRODUCT_ROOT_REF_FILE_SIZE) ||
				term.getName().equals(CoreMetKeys.PRODUCT_ROOT_REF_MIME_TYPE) ||
				term.getName().equals(CatalogActions.CATALOG_ACTION_KEY) ||
				term.getName().equals(CoreMetKeys.PRODUCT_STATUS) ||
				term.getName().equals(CoreMetKeys.PRODUCT_RECEVIED_TIME)) {
			return true;
		}else if (term.getName().equals(CoreMetKeys.PRODUCT_TYPE)) {
			return term.getValues().contains(productType.getName());
		}else {
			return this.isUnderstoodProductTypeTerm(term);
		}
	}
	
	protected boolean isUnderstoodProductTypeTerm(Term term) throws ValidationLayerException {
		for (Element element : this.productTypeElements)
			if (term.getName().equals(element.getElementName()) 
					|| term.getName().equals("urn:" + this.productType.getName() 
							+ ":" + element.getElementName())) 
				return true;
		return false;
	}
	
	public void setProductType(ProductType productType, List<Element> productTypeElements) {
		this.productType = productType;
		this.productTypeElements = productTypeElements;
	}
	
	public ProductType getProductType() {
		return this.productType;
	}
	
//	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
//		try {
//			Hashtable<String, Object> productTypeHash = XmlRpcStructFactory.getXmlRpcProductType(this.productType);
//			out.writeObject(productTypeHash);
//			Vector<Hashtable<String, Object>> productTypeElementsHash = XmlRpcStructFactory.getXmlRpcElementList(this.productTypeElements);
//			out.writeObject(productTypeElementsHash);
//		}catch (Exception e) {
//			LOG.log(Level.SEVERE, "Failed to serialize ProductDictionary : " + e.getMessage(), e);
//			throw new IOException("Failed to serialize ProductDictionary : " + e.getMessage());
//		}
//	}
//
//	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
//		try {
//			this.productType = XmlRpcStructFactory.getProductTypeFromXmlRpc((Hashtable<String, Object>) in.readObject());
//			this.productTypeElements = XmlRpcStructFactory.getElementListFromXmlRpc((Vector<Hashtable<String, Object>>) in.readObject());
//		}catch (Exception e) {
//			LOG.log(Level.SEVERE, "Failed to deserialize ProductDictionary : " + e.getMessage(), e);
//			throw new IOException("Failed to deserialize ProductDictionary : " + e.getMessage());
//		}
//	}


}
