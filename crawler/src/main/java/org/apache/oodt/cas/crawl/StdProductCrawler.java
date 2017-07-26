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
package org.apache.oodt.cas.crawl;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.exceptions.MetExtractionException;
import org.apache.oodt.cas.metadata.extractors.MetReaderExtractor;

//JDK imports
import java.io.File;
import java.util.logging.Level;

/**
 * A generic Product Crawler for Products. The Crawler is given a root Product
 * Path and it searches through all directories and sub-directories for .met
 * files, which it uses to determine products to ingest into the file manger.
 * The important .met file fields that this crawler requires are:
 * 
 * <ul>
 * <li><code>FileLocation</code>: directory absolute path to location of product
 * file</li>
 * <li><code>Filename</code>: name of the product file to ingest</li>
 * <li><code>ProductType</code>: the ProductType that will be sent to the file
 * manager for the product file described by the .met file.</li>
 * </ul>
 * 
 * @author mattmann (Chris Mattmann)
 * @author bfoster (Brian Foster)
 */
public class StdProductCrawler extends ProductCrawler {

   String metFileExtension;

   public StdProductCrawler() {
      this.metFileExtension = "met";
   }

   @Override
   protected Metadata getMetadataForProduct(File product) throws MetExtractionException {
      MetReaderExtractor extractor = new MetReaderExtractor(
            this.metFileExtension);
      return extractor.extractMetadata(product);
   }

   @Override
   protected boolean passesPreconditions(File product) {
      String metFilePath = product.getAbsolutePath() + "." + this.metFileExtension;
      boolean flag = new File(metFilePath).exists();
      String preCondComparatorId = "MetFileExistsCheck";

      if (!flag){
        LOG.log(Level.INFO, "Failed precondition comparator id "
            + preCondComparatorId+" file: "+metFilePath+" does not exist.");
      }
      else{
        LOG.log(Level.INFO, "Passed precondition comparator id "
            + preCondComparatorId+" file: "+metFilePath+" exists.");        
      }
      return flag;
   }

   @Override
   protected File renameProduct(File product, Metadata productMetadata) {
      return product;
   }

   public void setMetFileExtension(String metFileExtension) {
      this.metFileExtension = metFileExtension;
   }
}
