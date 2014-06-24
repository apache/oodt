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

//OODT imports
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.exceptions.VersioningException;
import org.apache.oodt.cas.metadata.Metadata;

//Junit imports
import junit.framework.TestCase;

/**
 * @author mattmann
 * @version $Revision$
 *
 * <p>Test Case for the Single File Basic Versioner.</p>
 *
 */
public class TestSingleFileBasicVersioner extends TestCase {

	private String origFileRef = "file:/foo/bar/testfile.txt";
	
	private String expectedDestFileRef = "file:/foo/bar2/testfile.txt";
	
	private String productRepoPath = "file:/foo/bar2/";
	
	/**
	 * 
	 */
	public TestSingleFileBasicVersioner() {
		System.setProperty("org.apache.oodt.cas.filemgr.mime.type.repository", new File("./src/main/resources/mime-types.xml").getAbsolutePath());		
	}
	
	public void testVersioning(){
		Product product = new Product();
		ProductType type = new ProductType();
		type.setProductRepositoryPath(productRepoPath);
		product.setProductStructure(Product.STRUCTURE_FLAT);
		product.setProductType(type);
		
		Metadata metadata = new Metadata();
		metadata.addMetadata(SingleFileBasicVersioner.FILENAME_FIELD, "testfile.txt");
		
		Reference r = new Reference();
		r.setOrigReference(origFileRef);
		
		product.getProductReferences().add(r);
		
		SingleFileBasicVersioner versioner = new SingleFileBasicVersioner();
		try{
			versioner.createDataStoreReferences(product, metadata);
		}
		catch(VersioningException e){
			fail(e.getMessage());
		}
		
		assertEquals("The generated ref: ["+r.getDataStoreReference()+"] is not equal to the expected ref: ["+expectedDestFileRef+"]", expectedDestFileRef, r.getDataStoreReference());
		
		
	}

}
