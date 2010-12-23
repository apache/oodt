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
package org.apache.oodt.cas.filemgr.structs.query;

//JDK imports
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

//Spring imports
import org.springframework.util.StringUtils;

//OODT imports
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.metadata.Metadata;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 *          <p>
 *          A combination of a Product and its Metadata
 *          <p>
 */
public class QueryResult {

	private Product product;
	private Metadata metadata;

	public QueryResult(Product product, Metadata metadata) {
		this.metadata = metadata;
		this.product = product;
	}

	public Product getProduct() {
		return this.product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public Metadata getMetadata() {
		return this.metadata;
	}

	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}

	private static String convertMetadataToString(Metadata metadata,
			String format) {
		if (format == null)
			return concatMetadataIntoString(metadata);
		String outputString = format;
		for (String key : metadata.getAllKeys())
			outputString = outputString.replaceAll("\\$" + key, StringUtils
					.collectionToDelimitedString(metadata.getAllMetadata(key),
							","));
		return outputString;
	}

	private static String concatMetadataIntoString(Metadata metadata) {
		String outputString = "";
		for (String key : metadata.getAllKeys())
			outputString += StringUtils.collectionToDelimitedString(metadata
					.getAllMetadata(key), ",")
					+ ",";
		return outputString.substring(0, outputString.length() - 1);
	}

}
