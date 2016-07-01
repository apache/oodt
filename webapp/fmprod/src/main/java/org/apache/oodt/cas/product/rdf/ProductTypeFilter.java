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


package org.apache.oodt.cas.product.rdf;

//OODT imports
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.metadata.Metadata;

//JDK imports
import java.util.Properties;

/**
 * 
 * Filters a {@link ProductType}, based on a set of constraints.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class ProductTypeFilter {

  private Properties constraints;

  public ProductTypeFilter() {
    this(null);
  }

  public ProductTypeFilter(String filter) {
    this.constraints = new Properties();
    if (filter != null) {
      this.parse(filter);
    }
  }

  public void parse(String filter) {
    if(filter == null) {
      return;
    }
    String[] attrConstrs = filter.split(",");
    for (String attrConstr : attrConstrs) {
      String[] attrConstPair = attrConstr.split("\\:");
      this.constraints.put(attrConstPair[0], attrConstPair[1]);
    }
  }

  public boolean filter(ProductType type) {
    if(this.constraints == null) {
      return true;
    }
    if (type.getTypeMetadata() != null) {
      Metadata typeMet = type.getTypeMetadata();
      for (Object constraintObj : this.constraints.keySet()) {
        String constraintName = (String) constraintObj;
        String constraintValue = this.constraints.getProperty(constraintName);
        if (!typeMet.containsKey(constraintName)) {
          return false;
        }

        if (!typeMet.getMetadata(constraintName).equals(constraintValue)) {
          return false;
        }

      }

      return true;
    } else {
      return false;
    }

  }
}
