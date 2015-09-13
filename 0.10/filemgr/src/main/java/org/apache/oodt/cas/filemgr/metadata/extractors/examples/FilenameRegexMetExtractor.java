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

package org.apache.oodt.cas.filemgr.metadata.extractors.examples;

//JDK imports
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//OODT imports
import org.apache.oodt.cas.filemgr.metadata.extractors.AbstractFilemgrMetExtractor;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.exceptions.MetExtractionException;

/**
 * @author nchung
 * @version $Revision$
 * 
 * <p>
 * Extracts {@link Metadata} from a {@link Product} filename that matches 
 * a provided regular expression.
 * </p>.
 */
public class FilenameRegexMetExtractor extends AbstractFilemgrMetExtractor {

   private String filenamePattern;
   private List<String> metadataKeys;

   public void doConfigure() {
      if (this.configuration != null) {
         this.filenamePattern = this.configuration
               .getProperty("filenamePattern");
         this.metadataKeys = Arrays.asList(this.configuration.getProperty(
               "metadataKeys").split(","));
      }
   }

   public Metadata doExtract(Product product, Metadata met)
         throws MetExtractionException {
      Metadata extractMet = new Metadata();
      merge(met, extractMet);
      
      Pattern pattern = Pattern.compile(this.filenamePattern);
      Matcher matcher = pattern.matcher(getProductFile(product).getName());
      if (matcher.matches()) {
         for (int i = 0; i < this.metadataKeys.size(); i++) {
            String key = this.metadataKeys.get(i);
            String value = matcher.group(i + 1);
            extractMet.addMetadata(key, value);
         }
      } else {
         throw new MetExtractionException("Filename does not conform to the pattern "
               + this.filenamePattern);
      }
      return extractMet;
   }
}
