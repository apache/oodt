//Copyright (c) 2010, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.product.rdf;

//OODT imports
import gov.nasa.jpl.oodt.cas.filemgr.structs.ProductType;
import gov.nasa.jpl.oodt.cas.metadata.Metadata;

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
    if (filter != null)
      this.parse(filter);
  }

  public void parse(String filter) {
    if(filter == null) return;
    String[] attrConstrs = filter.split(",");
    for (String attrConstr : attrConstrs) {
      String[] attrConstPair = attrConstr.split("\\:");
      this.constraints.put(attrConstPair[0], attrConstPair[1]);
    }
  }

  public boolean filter(ProductType type) {
    if(this.constraints == null) return true;
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
