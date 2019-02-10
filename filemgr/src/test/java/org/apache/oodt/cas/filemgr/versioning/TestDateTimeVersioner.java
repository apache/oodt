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


package org.apache.oodt.cas.filemgr.versioning;

// JDK imports
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

//OODT imports
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.exceptions.VersioningException;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.commons.util.DateConvert;

//Junit imports
import junit.framework.TestCase;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Unit tests for the DateTimeVersioner class.
 * </p>
 * 
 */
public class TestDateTimeVersioner extends TestCase {

	/* the versioner that we're going to test out */
	private DateTimeVersioner dateTimeVersioner = new DateTimeVersioner();

  private Properties initialProperties = new Properties(System.getProperties());

  public void setUp() throws Exception {
    Properties properties = new Properties(System.getProperties());
    URL url = this.getClass().getResource("/mime-types.xml");
    properties.setProperty("org.apache.oodt.cas.filemgr.mime.type.repository",
        new File(url.getFile()).getAbsolutePath());
    System.setProperties(properties);
  }

  public void tearDown() throws Exception {
    System.setProperties(initialProperties);
  }

	public void testFlat() {
		Metadata metadata = new Metadata();
		Product product = new Product();
		ProductType type = new ProductType();

		type.setProductRepositoryPath("file:///foo/space%20bar");
		product.setProductName("test_product");
		product.setProductStructure(Product.STRUCTURE_FLAT);
		product.setProductType(type);

	    Date prodDateTime = new Date();
		String prodDateTimeStr = DateConvert.isoFormat(prodDateTime);
		metadata.addMetadata("CAS.ProductReceivedTime", prodDateTimeStr);
		

        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyddMM.HHmmss");
        String prodDateTimeNonIso = dateFormatter.format(prodDateTime);

		List refs = new Vector();
		try {
      URL url = this.getClass().getResource("/test.txt");
      String refname = new File(url.getFile()).toURI().toURL().toExternalForm();
			refs.add(refname);
		} catch (MalformedURLException e) {
			fail(e.getMessage());
		}
		
		VersioningUtils.addRefsFromUris(product, refs);

		try {
			dateTimeVersioner.createDataStoreReferences(product, metadata);
		} catch (VersioningException e) {
			fail(e.getMessage());
		}

		String generatedRef = ((Reference) product.getProductReferences()
				.get(0)).getDataStoreReference();
		assertNotNull(generatedRef);
		assertEquals(
				"Generated ref does not equal expected ref: generatedRef=["
						+ generatedRef + "], expected=[file:/foo/space%20bar/test_product" +
								"/test.txt."+prodDateTimeNonIso+"]", 
								"file:/foo/space%20bar/test_product/test.txt."
						+ prodDateTimeNonIso, generatedRef);

	}

}
