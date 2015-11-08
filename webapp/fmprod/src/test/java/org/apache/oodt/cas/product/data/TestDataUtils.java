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

package org.apache.oodt.cas.product.data;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.metadata.Metadata;

import com.google.common.collect.Lists;
import com.google.common.io.Files;

import junit.framework.TestCase;

/**
 * Tests DataUtils module methods
 *
 * @author thomas (Thomas Bennett)
 */

public class TestDataUtils extends TestCase {
    private File workingDir;
	@Override
	public void tearDown() throws Exception {
		FileUtils.forceDelete(workingDir);
	}

	public void testCreateProductZipFileFromHierarchicalProduct(){
		//Data store reference needs absolute path to test data
		String cwd=System.getProperty("user.dir");

		Product product = new Product();
		product.setProductId("TestProductId");
		product.setProductName("TestProductName");
		product.setProductReferences(Lists.newArrayList(
			new Reference("file:///orig/data/", "file://" + cwd + "/src/test/resources/", 4096),
			new Reference("file:///orig/data/test-file-1.txt", "file://" + cwd + "/src/test/resources/test-file-1.txt", 20),
			new Reference("file:///orig/data/test-file-2.txt", "file://" + cwd + "/src/test/resources/test-file-2.txt", 20),
			new Reference("file:///orig/data/test-file-3.txt", "file://" + cwd + "/src/test/resources/test-file-3.txt", 20)));
		product.setProductStructure(Product.STRUCTURE_HIERARCHICAL);
		product.setTransferStatus(Product.STATUS_RECEIVED);
		ProductType pt = new ProductType();
		pt.setName("TestProductType");

		Metadata metadata = new Metadata();

		String workingDirPath;
		workingDir = Files.createTempDir();
		workingDirPath = workingDir.getAbsolutePath();
		workingDir.deleteOnExit();

		String productZipFilePath = null;
		try {
			productZipFilePath = DataUtils.createProductZipFile(product, metadata, workingDirPath);
		} catch (Exception e) {
			fail(e.getMessage());
		}
		String zipFileName = product.getProductName() + ".zip";
		assertEquals(productZipFilePath, workingDirPath + "/" + zipFileName);
	}

}
