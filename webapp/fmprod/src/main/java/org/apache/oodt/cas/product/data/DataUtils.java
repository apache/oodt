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

//JDK imports

import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.SerializableMetadata;
import org.apache.oodt.cas.product.exceptions.CasProductException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

//OODT imports

/**
 * 
 * Utility methods for delivering data using the filemgr servlet data API.
 * 
 * @author mattmann
 * @version $Revision$ .
 */
public final class DataUtils implements DataDeliveryKeys {

  /* our log stream */
  private static final Logger LOG = Logger.getLogger(DataUtils.class.getName());

  /* file filter to list zip files */
  private static final FileFilter ZIP_FILTER = new FileFilter() {
    public boolean accept(File file) {
      return file.isFile() && file.getName().endsWith(".zip");
    }

  };
  public static final int INT = 512;

  public static String createDatasetZipFile(ProductType type,
      String workingDirPath) throws IOException, CasProductException {
    String datasetZipFileName = type.getName() + ".zip";
    workingDirPath += workingDirPath.endsWith("/") ? "" : "/";
    String datasetZipFilePath = workingDirPath + datasetZipFileName;

    // try and remove it first
    if (!new File(datasetZipFilePath).delete()) {
      LOG.log(Level.WARNING, "Attempt to remove temp dataset zip file: ["
          + datasetZipFilePath + "] failed.");
    }

    // get a list of all the product zip files within the temp dir
    // assumption: the temp dir provided has a whole bunch of *.zip files
    // that are zips of the products (and their files and metadata)
    // belonging
    // to this dataset

    // NOTE: it is important that this step be done BEFORE creating the zip
    // output stream: else that will cause the generated datset zip to be
    // included as well!
    File[] productZipFiles = new File(workingDirPath).listFiles(ZIP_FILTER);
    if (productZipFiles == null || productZipFiles.length == 0)
    {
      throw new CasProductException("No product zip files to include in dataset: ["
          + type.getName() + "]");
    }

    // now get a reference to the zip file that we want to write
    ZipOutputStream out = new ZipOutputStream(new FileOutputStream(
        datasetZipFilePath));

    for (File productZipFile : productZipFiles) {
      String filename = productZipFile.getName();
      FileInputStream in = new FileInputStream(productZipFile
          .getAbsoluteFile());
      addZipEntryFromStream(in, out, filename);
      in.close();

      if (!productZipFile.delete()) {
        LOG.log(Level.WARNING, "Unable to remove tempoary product zip file: ["
                               + productZipFile.getAbsolutePath() + "]");
      } else {
        LOG.log(Level.INFO, "Deleting original product zip file: ["
                            + productZipFile.getAbsolutePath() + "]");
      }
    }

    // add met file
    addMetFileToProductZip(type.getTypeMetadata(), type.getName(), out);

    // Complete the ZIP file
    out.close();

    // return the zip file path
    return datasetZipFilePath;

  }

  public static String createProductZipFile(Product product, Metadata metadata,
      String workingDirPath) throws IOException {
    String productZipFileName = product.getProductName() + ".zip";
    workingDirPath += workingDirPath.endsWith("/") ? "" : "/";
    String productZipFilePath = workingDirPath + productZipFileName;

    // try and remove it first
    if (!new File(productZipFilePath).delete()) {
      LOG.log(Level.WARNING, "Attempt to remove temp zip file: ["
          + productZipFilePath + "] failed.");
    }

    // now get a reference to the zip file that we want to write
    ZipOutputStream out = new ZipOutputStream(new FileOutputStream(
        productZipFilePath));

    for (Reference r : product.getProductReferences()) {
      try {
        File prodFile = new File(new URI(r.getDataStoreReference()));
        if (prodFile.isDirectory()) {
          LOG.log(Level.WARNING, "Data store reference is a directory. Not adding directory to the zip file: ["
                                 + r.getDataStoreReference() + "]");
          continue;
        }
        String filename = prodFile.getName();
        FileInputStream in = new FileInputStream(prodFile.getAbsoluteFile());
        addZipEntryFromStream(in, out, filename);
        in.close();
      } catch (URISyntaxException e) {
        LOG.log(Level.WARNING, "Unable to get filename from uri: ["
                               + r.getDataStoreReference() + "]");
      }

    }

    // add met file
    addMetFileToProductZip(metadata, product.getProductName(), out);

    // Complete the ZIP file
    out.close();

    // return the zip file path
    return productZipFilePath;

  }

  /**
   * Guess what the content type is from the given url.
   * 
   * @param name
   *          URL to reference.
   * @return A guessed content type.
   */
  public static String guessTypeFromName(String name) {
    name = name.toLowerCase();
    if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
      return "image/jpeg";
    } else if (name.endsWith(".png")) {
      return "image/png";
    } else if (name.endsWith(".gif")) {
      return "image/gif";
    } else if (name.endsWith(".doc")) {
      return "application/msword";
    } else if (name.endsWith(".pdf")) {
      return "application/pdf";
    } else if (name.endsWith(".rtf")) {
      return "application/rtf";
    } else if (name.endsWith(".xls")) {
      return "application/vnd.ms-excel";
    } else if (name.endsWith(".ppt")) {
      return "application/vnd.ms-powerpoint";
    } else if (name.endsWith(".html") || name.endsWith(".htm")) {
      return "text/html";
    } else if (name.endsWith(".xml")) {
      return "text/xml";
    } else if (name.endsWith(".txt")) {
      return "text/plain";
    }
    return "application/octet-stream";
  }

  private static void addMetFileToProductZip(Metadata productMet,
      String metFileBaseName, ZipOutputStream out) throws IOException {

    // get the product metadata, and add its met file to the stream
    ByteArrayOutputStream metOut = new ByteArrayOutputStream();
    SerializableMetadata serialMet = new SerializableMetadata(productMet);
    serialMet.writeMetadataToXmlStream(metOut);
    ByteArrayInputStream metIn = new ByteArrayInputStream(metOut.toByteArray());
    String metFileName = metFileBaseName + ".met";
    addZipEntryFromStream(metIn, out, metFileName);
  }

  private static void addZipEntryFromStream(InputStream is, ZipOutputStream os,
      String filename) throws IOException {
    byte[] buf = new byte[INT];
    os.putNextEntry(new ZipEntry(filename));

    int len;
    while ((len = is.read(buf)) > 0) {
      os.write(buf, 0, len);
    }

    // Complete the entry
    os.closeEntry();
  }

}
