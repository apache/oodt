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


package gov.nasa.jpl.oodt.cas.filemgr.versioning;

// JDK imports
import java.io.File;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

//OODT imports
import gov.nasa.jpl.oodt.cas.filemgr.structs.Product;
import gov.nasa.jpl.oodt.cas.filemgr.structs.ProductType;
import gov.nasa.jpl.oodt.cas.filemgr.structs.Reference;
import gov.nasa.jpl.oodt.cas.filemgr.structs.exceptions.VersioningException;
import gov.nasa.jpl.oodt.cas.metadata.Metadata;

import jpl.eda.util.DateConvert;

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

	/**
	 * 
	 */
	public TestDateTimeVersioner() {
		super();
		// TODO Auto-generated constructor stub
		System.setProperty("gov.nasa.jpl.oodt.cas.filemgr.mime.type.repository", new File("./src/main/resources/mime-types.xml").getAbsolutePath());
	}

	/**
	 * @param arg0
	 */
	public TestDateTimeVersioner(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
		System.setProperty("gov.nasa.jpl.oodt.cas.filemgr.mime.type.repository", new File("./src/main/resources/mime-types.xml").getAbsolutePath());
	}

	protected void setUp() {

	}

	public void testFlat() {
		Metadata metadata = new Metadata();
		Product product = new Product();
		ProductType type = new ProductType();

		type.setProductRepositoryPath("file:///foo/bar");
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
			String refname = new File("src/testdata/test.txt").toURL().toExternalForm().toString();
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
						+ generatedRef + "], expected=[file:/foo/bar/test_product" +
								"/test.txt."+prodDateTimeNonIso+"]", 
								"file:/foo/bar/test_product/test.txt."
						+ prodDateTimeNonIso, generatedRef);

	}

}
