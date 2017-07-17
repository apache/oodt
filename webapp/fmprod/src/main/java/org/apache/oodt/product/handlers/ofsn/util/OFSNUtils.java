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


package org.apache.oodt.product.handlers.ofsn.util;

//OODT imports

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.oodt.commons.xml.XMLUtils;
import org.apache.oodt.product.handlers.ofsn.OFSNHandlerConfig;
import org.apache.oodt.product.handlers.ofsn.metadata.OFSNMetKeys;
import org.apache.oodt.product.handlers.ofsn.metadata.OFSNXMLMetKeys;
import org.apache.oodt.product.handlers.ofsn.metadata.OODTMetKeys;
import org.apache.oodt.xmlquery.QueryElement;
import org.apache.oodt.xmlquery.XMLQuery;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

//JDK imports
//APACHE imports

/**
 * 
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public final class OFSNUtils implements OODTMetKeys, OFSNXMLMetKeys,
    OFSNMetKeys {
  public static final int INT = 1024;
  private static Logger LOG = Logger.getLogger(OFSNUtils.class.getName());
  public static String extractFieldFromQuery(XMLQuery query, String name) {
    for (Iterator<QueryElement> i = query.getWhereElementSet().iterator(); i
        .hasNext();) {
      QueryElement element = i.next();
      if (element.getRole().equals(XMLQUERY_QUERYELEM_ROLE_ELEM)
          && element.getValue().equalsIgnoreCase(name)) {
        // get the next element and ensure that it is a LITERAL, and
        // return that
        QueryElement litElement = i.next();
        return litElement.getValue();
      }
    }

    return null;
  }

  public static Document getOFSNDoc(List<File> fileList, OFSNHandlerConfig cfg,
      String productRoot, boolean showDirSize, boolean showFileSize) {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    Document document;

    try {
      DocumentBuilder builder = factory.newDocumentBuilder();
      document = builder.newDocument();

      Element root = (Element) document.createElement(DIR_RESULT_TAG);
      XMLUtils.addAttribute(document, root, "xmlns", DIR_LISTING_NS);
      document.appendChild(root);

      for (File file : fileList) {
        Element dirEntryElem = XMLUtils.addNode(document, root, DIR_ENTRY_TAG);
        String ofsn = toOFSN(file.getAbsolutePath(), productRoot);
        //This ensures that we get ofsn names with unix style separators.
        //On a Windows machine, the product server would return '\'
        //separators.
        String unixStyleOFSN = FilenameUtils.separatorsToUnix(ofsn);
        if (cfg.getType().equals(LISTING_CMD)) {
          if (!Boolean.valueOf(cfg.getHandlerConf().getProperty("isSizeCmd"))) {
            XMLUtils.addNode(document, dirEntryElem, OFSN_TAG, unixStyleOFSN);
          }
        }

        long size = Long.MIN_VALUE;

        if (file.isDirectory()) {
          if (showDirSize) {
            size = FileUtils.sizeOfDirectory(file);
          }
        } else {
          if (showFileSize) {
            size = file.length();
          }
        }

        if (size != Long.MIN_VALUE) {
          XMLUtils.addNode(document, dirEntryElem, FILE_SIZE_TAG, String
              .valueOf(size));
        }
      }

      return document;
    } catch (ParserConfigurationException e) {
      LOG.log(Level.SEVERE, e.getMessage());
      return null;
    }

  }

  public static String relativeize(String path, String productRoot) {
    return productRoot + (path.startsWith("/") ? path.substring(1) : path);
  }

  public static File buildZipFile(String zipFileFullPath, File[] files) {
    // Create a buffer for reading the files
    byte[] buf = new byte[INT];
    ZipOutputStream out = null;

    try {
      // Create the ZIP file
      out = new ZipOutputStream(new FileOutputStream(zipFileFullPath));

      for (File file : files) {
        FileInputStream in = new FileInputStream(file);

        // Add ZIP entry to output stream.
        out.putNextEntry(new ZipEntry(file.getName()));

        // Transfer bytes from the file to the ZIP file
        int len;
        while ((len = in.read(buf)) > 0) {
          out.write(buf, 0, len);
        }

        // Complete the entry
        out.closeEntry();
        in.close();
      }
    } catch (IOException e) {
      LOG.log(Level.SEVERE, e.getMessage());
    } finally {
      if (out != null) {
        try {
          out.close();
        } catch (Exception ignore) {
        }

      }
    }

    return new File(zipFileFullPath);

  }

  public static boolean validateOFSN(String ofsn) {
      if (ofsn == null) {
          return false;
      } else {
          return !ofsn.equals("") && !ofsn.matches(".*\\.\\..*");
      }
  }
  
  private static String toOFSN(String absolutePath, String productRootPath) {
    if (absolutePath.startsWith(productRootPath)) {
      return absolutePath.substring(productRootPath.length());
    } else {
      // must have been a *.zip file, generated in some cache dir
      // just return the file name
      return new File(absolutePath).getName();
    }
  }

}
