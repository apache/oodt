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

//JDK imports
import java.io.File;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Vector;

//OODT imports
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.exceptions.VersioningException;

//Junit imports
import junit.framework.TestCase;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Unit tests for the BasicVersioner class.
 * </p>
 * 
 */
public class TestBasicVersioner extends TestCase {

	/* the BasicVersioner we're going to test out */
	private BasicVersioner basicVersioner = new BasicVersioner();

	public TestBasicVersioner() {
		System.setProperty("org.apache.oodt.cas.filemgr.mime.type.repository", new File("./src/main/resources/mime-types.xml").getAbsolutePath());
	}

	protected void setUp() {
	}

	public void testVersionFlat() {
		Product p = new Product();
		p.setProductName("test_product");
		p.setProductStructure(Product.STRUCTURE_FLAT);
		ProductType type = new ProductType();
		type.setProductRepositoryPath("file:///foo/bar");
		p.setProductType(type);

		List refs = new Vector();
		try {
			String refname = new File("src/testdata/test.txt").toURL().toExternalForm().toString();
			refs.add(refname);
		} catch (MalformedURLException e) {
			fail(e.getMessage());
		}
		
		VersioningUtils.addRefsFromUris(p, refs);

		try {
			basicVersioner.createDataStoreReferences(p, null);
		} catch (VersioningException e) {
			fail(e.getMessage());
		}

		String generatedRef = ((Reference) p.getProductReferences().get(0))
				.getDataStoreReference();
		assertNotNull(generatedRef);

		assertEquals("Versioned refs not equal: ref=[" + generatedRef + "]",
				"file:/foo/bar/test_product/test.txt", generatedRef);

	}

}
