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

package org.apache.oodt.cas.filemgr.structs.query;

//JDK imports
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.metadata.Metadata;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

//OODT imports

/**
 * 
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * A combination of a Product and its Metadata
 * <p>
 */
public class QueryResult {

    private Product product;
    private Metadata metadata;
    private String toStringFormat;
    
    public QueryResult(Product product, Metadata metadata) {
        this.metadata = metadata;
        this.product = product;
    }
    
    public Product getProduct() {
        return this.product;
    }
    
    public void setProduct(Product product) {
        this.product = product;
    }
    
    public Metadata getMetadata() {
        return this.metadata;
    }
    
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }
        
    public String getToStringFormat() {
        return this.toStringFormat;
    }

    public void setToStringFormat(String toStringFormat) {
        this.toStringFormat = toStringFormat;
    }

    public String toString() {
        return convertMetadataToString(this.metadata, this.toStringFormat);
    }
    
    private static String convertMetadataToString(Metadata metadata, String format) {
        if (format == null) {
          return concatMetadataIntoString(metadata);
        }
        String outputString = format;
        for (String key : metadata.getAllKeys()) {
          outputString = outputString.replaceAll("\\$" + key,
              StringUtils.collectionToCommaDelimitedString(metadata.getAllMetadata(key)));
        }
        return outputString;
    }
    
    private static String concatMetadataIntoString(Metadata metadata) {
        List<String> outputString = new ArrayList<String>();
        for (String key : metadata.getAllKeys()) {
          outputString.add(StringUtils.collectionToCommaDelimitedString(metadata.getAllMetadata(key)));
        }
        return StringUtils.collectionToCommaDelimitedString(outputString);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      QueryResult other = (QueryResult) obj;
      if (metadata == null) {
        if (other.metadata != null) {
          return false;
        }
      } else if (!metadata.equals(other.metadata)) {
        return false;
      }
      if (product == null) {
        if (other.product != null) {
          return false;
        }
      } else if (!product.equals(other.product)) {
        return false;
      }
      if (toStringFormat == null) {
        if (other.toStringFormat != null) {
          return false;
        }
      } else if (!toStringFormat.equals(other.toStringFormat)) {
        return false;
      }
      return true;
    }

  @Override
  public int hashCode() {
    int result = product != null ? product.hashCode() : 0;
    result = 31 * result + (metadata != null ? metadata.hashCode() : 0);
    result = 31 * result + (toStringFormat != null ? toStringFormat.hashCode() : 0);
    return result;
  }
}
