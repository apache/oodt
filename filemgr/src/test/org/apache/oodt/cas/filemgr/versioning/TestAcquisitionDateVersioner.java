/**
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
import java.text.SimpleDateFormat;
import java.util.Date;

//OODT imports
import org.apache.oodt.cas.filemgr.metadata.CoreMetKeys;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.metadata.Metadata;

//Junit imports
import junit.framework.TestCase;

/**
 * 
 * Test harness for the {@link AcquisitionDateVersioner}.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class TestAcquisitionDateVersioner extends TestCase {

  public TestAcquisitionDateVersioner() {
    System.setProperty("org.apache.oodt.cas.filemgr.mime.type.repository",
        new File("./src/main/resources/mime-types.xml").getAbsolutePath());
  }

  public void testVersionerWithNoStartDateTimeAndNoAcqDate() {
    AcquisitionDateVersioner versioner = new AcquisitionDateVersioner();
    Product p = Product.getDefaultFlatProduct("test", "urn:oodt:GenericFile");
    p.getProductType().setProductRepositoryPath("file:///home/files");
    Reference r = new Reference("file:///tmp/dir1/file1.txt", null, 4L);
    p.getProductReferences().add(r);
    Metadata met = new Metadata();
    met.addMetadata(CoreMetKeys.FILENAME, "file1.txt");
    String expectedDateTimeStr = new SimpleDateFormat(
        AcquisitionDateVersioner.ACQ_DATE_FORMAT).format(new Date());
    String expectedPath = "file:/home/files/" + expectedDateTimeStr
        + "/file1.txt";
    try {
      versioner.createDataStoreReferences(p, met);
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

    assertNotNull(p.getProductReferences());
    assertEquals(1, p.getProductReferences().size());
    assertEquals(expectedPath, p.getProductReferences().get(0)
        .getDataStoreReference());
  }

  public void testVersionerWithNoStartDateTimeAndAcqDate() {
    AcquisitionDateVersioner versioner = new AcquisitionDateVersioner();
    Product p = Product.getDefaultFlatProduct("test", "urn:oodt:GenericFile");
    p.getProductType().setProductRepositoryPath("file:///home/files");
    Reference r = new Reference("file:///tmp/dir1/file1.txt", null, 4L);
    p.getProductReferences().add(r);
    Metadata met = new Metadata();
    met.addMetadata(CoreMetKeys.FILENAME, "file1.txt");
    met.addMetadata("AcquisitionDate", "090910");
    String expectedPath = "file:/home/files/"
        + met.getMetadata("AcquisitionDate") + "/file1.txt";
    try {
      versioner.createDataStoreReferences(p, met);
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

    assertNotNull(p.getProductReferences());
    assertEquals(1, p.getProductReferences().size());
    assertEquals(expectedPath, p.getProductReferences().get(0)
        .getDataStoreReference());
  }

  public void testVersionerWithStartDateTime() {
    AcquisitionDateVersioner versioner = new AcquisitionDateVersioner();
    Product p = Product.getDefaultFlatProduct("test", "urn:oodt:GenericFile");
    p.getProductType().setProductRepositoryPath("file:///home/files");
    Reference r = new Reference("file:///tmp/dir1/file1.txt", null, 4L);
    p.getProductReferences().add(r);
    Metadata met = new Metadata();
    met.addMetadata(CoreMetKeys.FILENAME, "file1.txt");
    met.addMetadata("StartDateTime", "2006-09-10T00:00:01.000Z");
    String expectedPath = "file:/home/files/060910/file1.txt";
    try {
      versioner.createDataStoreReferences(p, met);
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

    assertNotNull(p.getProductReferences());
    assertEquals(1, p.getProductReferences().size());
    assertEquals(expectedPath, p.getProductReferences().get(0)
        .getDataStoreReference());
  }

}
