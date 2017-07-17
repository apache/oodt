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

package org.apache.oodt.product.handlers.ofsn;

import org.apache.oodt.commons.xml.XMLUtils;
import org.apache.oodt.product.LargeProductQueryHandler;
import org.apache.oodt.product.ProductException;
import org.apache.oodt.product.handlers.ofsn.metadata.OFSNMetKeys;
import org.apache.oodt.product.handlers.ofsn.metadata.OFSNXMLConfigMetKeys;
import org.apache.oodt.product.handlers.ofsn.metadata.OFSNXMLMetKeys;
import org.apache.oodt.product.handlers.ofsn.metadata.XMLQueryMetKeys;
import org.apache.oodt.product.handlers.ofsn.util.OFSNObjectFactory;
import org.apache.oodt.product.handlers.ofsn.util.OFSNUtils;
import org.apache.oodt.xmlquery.LargeResult;
import org.apache.oodt.xmlquery.Result;
import org.apache.oodt.xmlquery.XMLQuery;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeTypesFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * An extensible implementation of the PDS-inspired Online File Specification
 * Name (OFSN) style product server. See the ofsn-ps.xml file for a detailed
 * specification of the configuration and motivation behind this product
 * handler.
 * 
 * @author mattmann
 * @version $Revision$
 */
public class OFSNFileHandler implements LargeProductQueryHandler,
    XMLQueryMetKeys, OFSNXMLMetKeys, OFSNMetKeys, OFSNXMLConfigMetKeys {

  private static final Logger LOG = Logger
      .getLogger(OFSNFileHandler.class.getName());

  private static final String CMD_SEPARATOR = ";";

  // by default return dir size on listing commands
  private boolean computeDirSize = true;

  // by default return file size on listing commands
  private boolean computeFileSize = true;

  private OFSNFileHandlerConfiguration conf;

  private Map<String, Object> HANDLER_CACHE;

  public OFSNFileHandler() throws InstantiationException {
    // init conf here
    String xmlConfigFilePath = System.getProperty(OFSN_XML_CONF_FILE_KEY);
    this.computeDirSize = Boolean.getBoolean(OFSN_COMPUTE_DIR_SIZE);
    this.computeFileSize = Boolean.getBoolean(OFSN_COMPUTE_FILE_SIZE);

    if (xmlConfigFilePath == null) {
      throw new InstantiationException(
          "Must define xml configuration file path via property : ["
              + OFSN_XML_CONF_FILE_KEY + "]");
    }

    try {
      this.conf = OFSNFileHandlerConfigurationReader
          .getConfig(xmlConfigFilePath);
    } catch (FileNotFoundException e) {
      throw new InstantiationException(
          "xml configuration file: [" + xmlConfigFilePath + "] not found!");
    }

    if (this.conf.getProductRoot() == null) {
      throw new InstantiationException(
          "Must define: [productRoot] attribute in XML configuration!");
    }

    // used to cache handlers -- map of RT type to Get/List handler instance
    HANDLER_CACHE = new ConcurrentHashMap<String, Object>();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.oodt.product.QueryHandler#query(org.apache.oodt.xmlquery.
   * XMLQuery)
   */
  public XMLQuery query(XMLQuery xmlQuery) throws ProductException {
    String ofsn = OFSNUtils.extractFieldFromQuery(xmlQuery, OFSN);
    String cmd = OFSNUtils.extractFieldFromQuery(xmlQuery, RETURN_TYPE);
    validate(ofsn, cmd);
    String cmdId = ofsn + CMD_SEPARATOR + cmd;
    OFSNHandlerConfig cfg = this.conf.getHandlerConfig(cmd);
    validateHandlerConfig(cfg, cmd);

    String realPath = this.conf.getProductRoot() + ofsn;

    if (isListingCmd(cmd)) {
      ByteArrayOutputStream outStream = new ByteArrayOutputStream();
      OFSNListHandler handler = getListHandler(cmd, cfg.getClassName());
      File[] fileList = handler.getListing(realPath);
      generateOFSNXml(fileList, cfg, outStream);
      xmlQuery.getResults().add(new Result(cmdId, XML_MIME_TYPE, null, cmdId,
          Collections.EMPTY_LIST, outStream.toString()));
    } else if (isGetCmd(cmd)) {
      OFSNGetHandler handler = getGetHandler(cmd, cfg.getClassName());
      String rtAndPath = cmd + CMD_SEPARATOR + realPath;
      String mimeType;

      // check for and use mimetype conf property if available
      if (cfg.getHandlerConf().containsKey(PROPERTY_MIMETYPE_ATTR)) {
        MediaType mediaType = MediaType
            .parse(cfg.getHandlerConf().getProperty(PROPERTY_MIMETYPE_ATTR));
        if (mediaType == null) {
          LOG.log(Level.WARNING,
              "MIME type ["
                  + cfg.getHandlerConf().getProperty(PROPERTY_MIMETYPE_ATTR)
                  + "] specified " + "for handler [" + cfg.getClassName()
                  + "] invalid. Defaulting to MIME type ["
                  + MediaType.OCTET_STREAM.toString() + "]");
          mediaType = MediaType.OCTET_STREAM;
        }
        mimeType = mediaType.toString();
      } else { // use default mimetype of product on disk
        try {
          mimeType = MimeTypesFactory.create().getMimeType(new File(realPath))
              .getName();
        } catch (Exception e) {
          mimeType = null;
        }
      }

      xmlQuery.getResults()
          .add(new LargeResult(/* id */rtAndPath, /* mimeType */ mimeType,
              /* profileID */null, /* resourceID */new File(realPath).getName(),
              Collections.EMPTY_LIST, handler.sizeOf(realPath)));
    } else {
      throw new ProductException("return type: [" + cmd + "] is unsupported!");
    }

    return xmlQuery;

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.product.LargeProductQueryHandler#close(java.lang.String)
   */
  public void close(String id) {
    // nothing to do
  }

  /*
   * Get the file handler configuration.
   *
   * @return The file handler configuration.
   */
  public OFSNFileHandlerConfiguration getOfsnFileHandlerConfiguration() {
    return this.conf;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.product.LargeProductQueryHandler#retrieveChunk(java.lang.
   * String, long, int)
   */
  public byte[] retrieveChunk(String id, long offset, int length)
      throws ProductException {
    // unmarshall the return type and path
    String[] rtTypeAndPathArr = id.split(CMD_SEPARATOR);
    String rtType = rtTypeAndPathArr[0];
    String filepath = rtTypeAndPathArr[1];

    OFSNGetHandler handler = getGetHandler(rtType,
        this.conf.getHandlerClass(rtType));

    return handler.retrieveChunk(filepath, offset, length);
  }

  private void generateOFSNXml(File[] mlsFileList, OFSNHandlerConfig cfg,
      OutputStream outStream) {
    XMLUtils.writeXmlToStream(OFSNUtils.getOFSNDoc(Arrays.asList(mlsFileList),
        cfg, this.conf.getProductRoot(), this.computeDirSize,
        this.computeFileSize), outStream);
  }

  private void validate(String ofsn, String cmd) throws ProductException {
    if (ofsn == null || cmd == null || (ofsn.equals("")) || (cmd.equals(""))) {
      throw new ProductException("must specify OFSN and RT parameters!");
    } else if (!OFSNUtils.validateOFSN(ofsn)) {
      throw new ProductException("OFSN is invalid");
    }
  }

  private void validateHandlerConfig(OFSNHandlerConfig cfg, String cmd)
      throws ProductException {
    if (cfg == null) {
      throw new ProductException("Unrecognized command: [" + cmd + "]!");
    }
  }

  private OFSNListHandler getListHandler(String rtType, String className) {
    if (HANDLER_CACHE.containsKey(rtType)) {
      return (OFSNListHandler) HANDLER_CACHE.get(rtType);
    } else {
      OFSNListHandler handler = OFSNObjectFactory.getListHandler(className);
      LOG.log(Level.INFO, "Getting handler config for RT: [" + rtType + "]");
      handler.configure(this.conf.getHandlerConfig(rtType).getHandlerConf());
      HANDLER_CACHE.put(rtType, handler);
      return handler;
    }
  }

  private OFSNGetHandler getGetHandler(String rtType, String className) {
    if (HANDLER_CACHE.containsKey(rtType)) {
      return (OFSNGetHandler) HANDLER_CACHE.get(rtType);
    } else {
      OFSNGetHandler handler = OFSNObjectFactory.getGetHandler(className);
      handler.configure(this.conf.getHandlerConfig(rtType).getHandlerConf());
      HANDLER_CACHE.put(rtType, handler);
      return handler;
    }
  }

  private boolean isListingCmd(String cmd) throws ProductException {
    OFSNHandlerConfig cfg = this.conf.getHandlerConfig(cmd);
    if (cfg == null) {
      throw new ProductException("Unrecognized command: [" + cmd + "]!");
    }

    return cfg.getType().equals(LISTING_CMD);
  }

  private boolean isGetCmd(String cmd) {
    OFSNHandlerConfig cfg = this.conf.getHandlerConfig(cmd);

    return cfg.getType().equals(GET_CMD);
  }

}
