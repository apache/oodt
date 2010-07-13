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


package gov.nasa.jpl.oodt.cas.filemgr.datatransfer;

//OODT imports
import gov.nasa.jpl.oodt.cas.filemgr.structs.Product;
import gov.nasa.jpl.oodt.cas.filemgr.structs.Reference;

//JDK imports
import java.io.File;

//Junit imports
import junit.framework.TestCase;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public class TestLocalDataTransferer extends TestCase {

    private LocalDataTransferer transfer;

    private String productOrigLoc;

    private String productExpectedLoc;

    public TestLocalDataTransferer() {
        transfer = (LocalDataTransferer) new LocalDataTransferFactory()
                .createDataTransfer();
        
        try {
            File tempFileSrc = new File("./src/testdata/test.txt");
            productOrigLoc = tempFileSrc.getCanonicalPath();
            File tempFileDest = File.createTempFile("foo", ".txt");
            productExpectedLoc = tempFileDest.getAbsolutePath();
            // now delete the file so that it can be created
            assertTrue(tempFileDest.delete());

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    public void testTransfer() {
        Product testProduct = Product.getDefaultFlatProduct("test",
                "urn:oodt:GenericFile");
        testProduct.getProductReferences().add(
                new Reference("file://" + productOrigLoc, "file://"
                        + productExpectedLoc, new File(productOrigLoc).length()));
        
        System.out.println("Added ref: ["+((Reference)testProduct.getProductReferences().get(0))+"]");

        try {
            transfer.transferProduct(testProduct);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        // assert that it didn't transfer the file anywhere
        assertTrue(new File(productExpectedLoc).exists());
        new File(productExpectedLoc).deleteOnExit();
    }

}
