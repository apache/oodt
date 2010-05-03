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

//JDK imports
import java.io.File;

//OODT imports
import gov.nasa.jpl.oodt.cas.filemgr.structs.Product;
import gov.nasa.jpl.oodt.cas.filemgr.structs.ProductType;
import gov.nasa.jpl.oodt.cas.filemgr.structs.Reference;
import gov.nasa.jpl.oodt.cas.filemgr.structs.exceptions.VersioningException;
import gov.nasa.jpl.oodt.cas.metadata.Metadata;

//Junit imports
import junit.framework.TestCase;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A Test Case for the MetadataBasedFileVersioner.
 * </p>.
 */
public class TestMetadataBasedFileVersioner extends TestCase {

	private String productTypePath = "file:/foo/bar";

	/**
	 * 
	 */
	public TestMetadataBasedFileVersioner() {
		System.setProperty("gov.nasa.jpl.oodt.cas.filemgr.mime.type.repository", new File("./src/main/resources/mime-types.xml").getAbsolutePath());
	}

	protected void setUp() {

	}

	public void testVersionerNoStatic() {
		String filePathSpec = "/[ProductType]/[ProductionDate]/[Filename]";
		Product product = new Product();
		product.setProductStructure(Product.STRUCTURE_FLAT);
		ProductType type = new ProductType();
		type.setProductRepositoryPath(productTypePath);
		product.setProductType(type);

		Metadata metadata = new Metadata();
		metadata.addMetadata("ProductType", "FooFile");
		metadata.addMetadata("ProductionDate", "060804");
		metadata.addMetadata("Filename", "foo.txt");

		Reference r = new Reference();
		product.getProductReferences().add(r);

		MetadataBasedFileVersioner versioner = new MetadataBasedFileVersioner(
				filePathSpec);
		try {
			versioner.createDataStoreReferences(product, metadata);
		} catch (VersioningException e) {
			fail(e.getMessage());
		}

		String expected = "file:/foo/bar/FooFile/060804/foo.txt";
		assertEquals("The reference: [" + r.getDataStoreReference()
				+ "] is not equal to: [" + expected + "]", expected, r
				.getDataStoreReference());
	}

	public void testVersionerWithStatic() {
		String filePathSpec = "/[ProductType]/some/other/path[ProductionDate]/[Filename]";
		Product product = new Product();
		product.setProductStructure(Product.STRUCTURE_FLAT);
		ProductType type = new ProductType();
		type.setProductRepositoryPath(productTypePath);
		product.setProductType(type);

		Metadata metadata = new Metadata();
		metadata.addMetadata("ProductType", "FooFile");
		metadata.addMetadata("ProductionDate", "060804");
		metadata.addMetadata("Filename", "foo.txt");

		Reference r = new Reference();
		product.getProductReferences().add(r);

		MetadataBasedFileVersioner versioner = new MetadataBasedFileVersioner(
				filePathSpec);
		try {
			versioner.createDataStoreReferences(product, metadata);
		} catch (VersioningException e) {
			fail(e.getMessage());
		}

		String expected = "file:/foo/bar/FooFile/some/other/path060804/foo.txt";
		assertEquals("The reference: [" + r.getDataStoreReference()
				+ "] is not equal to: [" + expected + "]", expected, r
				.getDataStoreReference());
	}
}
