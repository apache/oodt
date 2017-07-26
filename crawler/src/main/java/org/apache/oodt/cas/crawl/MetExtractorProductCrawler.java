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

import org.apache.oodt.cas.crawl.structs.exceptions.CrawlerActionException;
import org.apache.oodt.cas.metadata.MetExtractor;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.exceptions.MetExtractionException;
import org.apache.oodt.cas.metadata.exceptions.NamingConventionException;
import org.apache.oodt.cas.metadata.filenaming.NamingConvention;
import org.apache.oodt.cas.metadata.preconditions.PreCondEvalUtils;
import org.apache.oodt.cas.metadata.preconditions.PreConditionComparator;
import org.springframework.beans.factory.annotation.Required;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;


/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * A variant of the Standard Product Crawler where .met files are generated on
 * the fly as product files are encountered.
 * </p>
 */
public class MetExtractorProductCrawler extends ProductCrawler {

    private MetExtractor metExtractor;

    private String metExtractorConfig;

    private List<String> preCondIds = new ArrayList<String>();

    private String namingConventionId;

    @Override
    protected Metadata getMetadataForProduct(File product) throws MetExtractionException {
        return metExtractor.extractMetadata(product);
    }

    @Override
    protected boolean passesPreconditions(File product) {
        if (this.getPreCondIds() != null) {
          PreCondEvalUtils evalUtils = new PreCondEvalUtils(
              this.getApplicationContext());
          if (!evalUtils.eval(this.getPreCondIds(), product)) {
            return false;
          }          
        }
        
        String preCondComparatorId = "ProductFileExistsCheck";
        String preCondComparatorIdLength = "ProductLengthGreaterZeroCheck";
        
        boolean existsFlag = true, lengthFlag = true;
        
        if (!product.exists()){
          existsFlag = false;
          logConditionMessage(existsFlag, preCondComparatorId, product);
        }
        
        if (product.length() == 0){
          lengthFlag = false;
          logConditionMessage(lengthFlag, preCondComparatorIdLength, product);
        }
        
        return existsFlag && lengthFlag;
    }

    @Override
    protected File renameProduct(File product, Metadata productMetadata)
        throws CrawlerActionException, NamingConventionException {
       if (getNamingConventionId() != null) {
          NamingConvention namingConvention = (NamingConvention)
                getApplicationContext().getBean(getNamingConventionId());
          if (namingConvention == null) {
             throw new CrawlerActionException("NamingConvention Id '" + getNamingConventionId()
                   + "' is not defined");
          }
          return namingConvention.rename(product, productMetadata);
       } else {
          return product;
       }
    }

    @Required
    public void setMetExtractor(String metExtractor)
            throws MetExtractionException, InstantiationException,
            IllegalAccessException, ClassNotFoundException {
        this.metExtractor = (MetExtractor) Class.forName(metExtractor)
                .newInstance();
        if (metExtractorConfig != null && !metExtractorConfig.equals("")) {
          this.metExtractor.setConfigFile(metExtractorConfig);
        }
    }

    @Required
    public void setMetExtractorConfig(String metExtractorConfig)
            throws MetExtractionException {
        this.metExtractorConfig = metExtractorConfig;
        if (this.metExtractor != null && metExtractorConfig != null
                && !metExtractorConfig.equals("")) {
          this.metExtractor.setConfigFile(metExtractorConfig);
        }
    }

    public List<String> getPreCondIds() {
        return preCondIds;
    }

    public void setPreCondIds(List<String> preCondIds) {
        this.preCondIds = preCondIds;
    }

    public void setNamingConventionId(String namingConventionId) {
        this.namingConventionId = namingConventionId;
    }

    public String getNamingConventionId() {
       return namingConventionId;
    }
    
    private void logConditionMessage(boolean flag, String preCondComparatorId, File product){
      String startMsg = flag ? "Passed":"Failed";
      LOG.log(Level.INFO, startMsg + " precondition comparator id "
          + preCondComparatorId+" file: "+product);      
    }
}
