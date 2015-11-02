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
package org.apache.oodt.cas.crawl.action;

//JDK imports
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;

//OODT imports
import org.apache.oodt.cas.crawl.structs.exceptions.CrawlerActionException;
import org.apache.oodt.cas.filemgr.ingest.Ingester;
import org.apache.oodt.cas.filemgr.ingest.StdIngester;
import org.apache.oodt.cas.filemgr.metadata.CoreMetKeys;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.util.PathUtils;

//Spring imports
import org.springframework.beans.factory.annotation.Required;

/**
 * This action allows the crawler to ingest an ancillary file while crawling
 * other files. For example, if the crawler is configured to pick up images then
 * this action could be used to pick up the thumbnails. The ancillary file will
 * be submitted to the file manager given the product type specified and an
 * identifier placed in the metadata of the file being ingested. One will have
 * to set up the product type in the file manager policy to support this action.
 * This action does not perform metadata extraction and if this is required one
 * should consider using a server side metadata extractor configured with the
 * file manager.
 * 
 * @author pramirez (Paul Ramirez)
 * @author mattmann (Chris Mattmann)
 */
public class IngestAncillary extends FileBasedAction {
  private String fileManagerUrl;
  private String dataTransferService;
  private String relatedKey;
  private String productType;
  private Map<String, List<String>> ancillaryMetadata;
  private List<String> copyKeys;
  private List<String> replaceDynamicKeys;
  private List<String> writeBackKeys;
  private String writeBackKeyPrefix;

  public IngestAncillary() {
    super();
  }

  @Override
  public boolean performFileAction(File actionFile, Metadata metadata) {
    Metadata ingestMetadata = new Metadata();
    if (ancillaryMetadata != null) {
      for (Map.Entry<String, List<String>> entry : this.ancillaryMetadata.entrySet()) {
        for (String value : entry.getValue()) {
          ingestMetadata.addMetadata(entry.getKey(), PathUtils.replaceEnvVariables(value));
        }
      }
    }

    if (copyKeys != null) {
      for (String copyKey : copyKeys) {
        if (metadata.containsKey(copyKey)) {
          ingestMetadata.addMetadata(copyKey, metadata.getAllMetadata(copyKey));
        }
      }
    }

    if (ingestMetadata.getMetadata(CoreMetKeys.FILE_LOCATION) == null) {
      ingestMetadata.addMetadata(CoreMetKeys.FILE_LOCATION,
          actionFile.getParent());
    }
    if (ingestMetadata.getMetadata(CoreMetKeys.FILENAME) == null) {
      ingestMetadata.addMetadata(CoreMetKeys.FILENAME, actionFile.getName());
    }
    if (ingestMetadata.getMetadata(CoreMetKeys.PRODUCT_NAME) == null) {
      ingestMetadata.addMetadata(CoreMetKeys.PRODUCT_NAME, actionFile.getName()
          .substring(0, actionFile.getName().indexOf(".")));
    }
    if (ingestMetadata.getMetadata(CoreMetKeys.PRODUCT_TYPE) == null) {
      ingestMetadata.addMetadata(CoreMetKeys.PRODUCT_TYPE, productType);
    }

    try {
      Ingester ingester = new StdIngester(dataTransferService);
      String identifier = ingester.ingest(new URL(this.fileManagerUrl),
          actionFile, ingestMetadata);
      if (identifier != null) {
        LOG.info("Succesfully ingested ancillary file "
            + actionFile.getAbsolutePath() + " with identifier " + identifier);
      } else {
        LOG.severe("Failed to ingest ancillary file "
            + actionFile.getAbsolutePath()
            + " identifer was not returned from file manager");
      }

      if (relatedKey != null) {
        metadata.addMetadata(relatedKey, identifier);
      }

      if (writeBackKeys != null) {
        if (writeBackKeyPrefix == null) {
          writeBackKeyPrefix = "";
        }
        ingestMetadata.addMetadata(writeBackKeyPrefix + CoreMetKeys.PRODUCT_ID,
            identifier);
        for (String writeBackKey : writeBackKeys) {
          metadata.addMetadata(writeBackKeyPrefix + writeBackKey,
              ingestMetadata.getAllMetadata(writeBackKey));
        }
      }

      if (replaceDynamicKeys != null) {
        for (String replaceDynamicKey : replaceDynamicKeys) {
          String value = PathUtils.replaceEnvVariables(
              metadata.getMetadata(replaceDynamicKey), metadata);
          metadata.removeMetadata(replaceDynamicKey);
          metadata.addMetadata(replaceDynamicKey, value);
        }
      }

    } catch (Exception ex) {
      LOG.severe("Failed to ingest ancillary file "
          + actionFile.getAbsolutePath());
      LOG.severe(ex.getMessage());
      return false;
    }

    return true;
  }

  public void setReplaceDynamicKeys(List<String> replaceDynamicKeys) {
    this.replaceDynamicKeys = replaceDynamicKeys;
  }

  @Required
  public void setFileManagerUrl(String fileManagerUrl) {
    this.fileManagerUrl = fileManagerUrl;
  }

  @Required
  public void setDataTransferService(String dataTransferService) {
    this.dataTransferService = dataTransferService;
  }

  public void setRelatedKey(String relatedKey) {
    this.relatedKey = relatedKey;
  }

  public void setAncillaryMetadata(Map<String, List<String>> ancillaryMetadata) {
    this.ancillaryMetadata = ancillaryMetadata;
  }

  @Required
  public void setProductType(String productType) {
    this.productType = productType;
  }

  public void setCopyKeys(List<String> copyKeys) {
    this.copyKeys = copyKeys;
  }

  public void setWriteBackKeys(List<String> writeBackKeys) {
    this.writeBackKeys = writeBackKeys;
  }

  public void setWriteBackKeyPrefix(String writeBackKeyPrefix) {
    this.writeBackKeyPrefix = writeBackKeyPrefix;
  }

  public static void main(String[] args) throws CrawlerActionException {
    if (args.length != 6) {
      System.out
          .println("Usage: java "
              + IngestAncillary.class.getName()
              + " <serviceUrl> <transferServiceClass> <productFile> <fileSuffix> <relatedKey> <productType");
      System.exit(-1);
    }
    IngestAncillary ingest = new IngestAncillary();
    ingest.setFileManagerUrl(args[0]);
    ingest.setDataTransferService(args[1]);
    File product = new File(args[2]);
    ingest.setFileSuffix(args[3]);
    ingest.setRelatedKey(args[4]);
    ingest.setProductType(args[5]);
    Metadata metadata = new Metadata();
    ingest.performAction(product, metadata);
    System.out.println("Ingested " + args[5] + ": "
        + metadata.getMetadata(args[4]));
  }
}
