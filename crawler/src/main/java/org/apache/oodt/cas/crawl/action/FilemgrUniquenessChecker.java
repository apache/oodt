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

//OODT static imports
import static org.apache.oodt.cas.filemgr.metadata.CoreMetKeys.PRODUCT_NAME;

//JDK imports
import java.io.File;
import java.net.URL;

//Apache imports
import org.apache.commons.lang.Validate;

//OODT imports
import org.apache.oodt.cas.crawl.structs.exceptions.CrawlerActionException;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;
import org.apache.oodt.cas.filemgr.util.RpcCommunicationFactory;
import org.apache.oodt.cas.metadata.Metadata;

/**
 * Check whether a product exists in the database already
 * 
 * @author bfoster (Brian Foster)
 */
public class FilemgrUniquenessChecker extends CrawlerAction {

   private String filemgrUrl;

   @Override
   public boolean performAction(File product, Metadata productMetadata)
         throws CrawlerActionException {
      try {
         Validate.notNull(productMetadata.getMetadata(PRODUCT_NAME),
               PRODUCT_NAME + " was not found in metadata");

         FileManagerClient fmClient = RpcCommunicationFactory.createClient(new URL(this.filemgrUrl));
         return !fmClient.hasProduct(productMetadata.getMetadata(PRODUCT_NAME));
      } catch (Exception e) {
         throw new CrawlerActionException("Product failed uniqueness check : ["
               + product + "] : " + e.getMessage(), e);
      }
   }

   @Override
   public void validate() throws CrawlerActionException {
      super.validate();
      try {
         Validate.notNull(filemgrUrl, "Must specify filemgrUrl");
      } catch (Exception e) {
         throw new CrawlerActionException(e);
      }
   }

   public void setFilemgrUrl(String filemgrUrl) {
      this.filemgrUrl = filemgrUrl;
   }
}
