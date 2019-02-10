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


package org.apache.oodt.cas.filemgr.metadata.extractors;

//JDK imports
import java.io.File;
import java.io.IOException;

//OODT imports
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.exceptions.MetExtractionException;

//Junit imports
import junit.framework.TestCase;

/**
 * @author mattmann
 * @version $Revision$
 * @since OODT-256
 * 
 * <p>
 * Test suite for the {@link AbstractFilemgrMetExtractor}.
 * </p>.
 */
public class TestAbstractFilemgrMetExtractor extends TestCase {

    AbstractFilemgrMetExtractor absExtractor;

    static final String tmpDirName = "fooabsTest";

    static String tmpDirFullPath;

    static {
        try {
            tmpDirFullPath = File.createTempFile("foo", "bar1").getParent();
            tmpDirFullPath = !tmpDirFullPath.endsWith("/") ? tmpDirFullPath += "/"
                    : tmpDirFullPath;
            tmpDirFullPath += tmpDirName;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void setUp() throws Exception {
        // create temp dir
        new File(tmpDirFullPath).mkdirs();
    }

    @Override
    protected void tearDown() throws Exception {
        // delete tmp dir
        new File(tmpDirFullPath).delete();
    }

    public void testGetProdFileHierarchicalProd() {
        absExtractor = new AbstractFilemgrMetExtractor() {

            @Override
            public Metadata doExtract(Product product, Metadata met)
                    throws MetExtractionException {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public void doConfigure() {
                // TODO Auto-generated method stub

            }

        };
        Product prod = Product.getDefaultFlatProduct("footest",
                "urn:oodt:GenericFile");
        String refUri = null;
        try {
            refUri = new File(tmpDirFullPath).toURI().toURL().toExternalForm();
            prod.setProductStructure(Product.STRUCTURE_HIERARCHICAL);
            prod.getProductType()
                    .setProductRepositoryPath(
                            new File(tmpDirFullPath).getParentFile().toURI().toURL()
                                    .toString());
        } catch (Exception e) {
            fail(e.getMessage());
        }
        prod.getProductReferences().add(new Reference(refUri, refUri, 0L));

        try {
            File prodFile = absExtractor.getProductFile(prod);
            assertNotNull(prodFile);
            assertTrue(prodFile.isDirectory());
        } catch (MetExtractionException e) {
            fail(e.getMessage());
        }
    }

}
