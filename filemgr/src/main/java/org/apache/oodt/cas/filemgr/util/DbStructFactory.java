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

package org.apache.oodt.cas.filemgr.util;

//OODT imports
import org.apache.oodt.cas.filemgr.structs.Element;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.metadata.Metadata;

//JDK imports
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author mattmann
 * @version $Revsion$
 * 
 * <p>
 * Object creation utilities to create File Manager objects from SQL
 * {@link ResultSet}s.
 * </p>
 * 
 */
public final class DbStructFactory {

    public static ProductType getProductType(ResultSet rs) throws SQLException {
        ProductType type = new ProductType();
        type.setDescription(rs.getString("product_type_description"));
        type.setName(rs.getString("product_type_name"));
        type.setProductRepositoryPath(rs
                .getString("product_type_repository_path"));
        type.setProductTypeId(String.valueOf(rs.getInt("product_type_id")));
        type.setVersioner(rs.getString("product_type_versioner_class"));

        return type;
    }

    public static Product getProduct(ResultSet rs) throws SQLException {
        return getProduct(rs, true, false);
    }

    public static Product getProduct(ResultSet rs, boolean getType, boolean productIdString)
            throws SQLException {
        Product product = new Product();
        if (productIdString) {
        	product.setProductId(rs.getString("product_id"));
        } else {
        	product.setProductId(String.valueOf(rs.getInt("product_id")));
        }
        product.setProductName(rs.getString("product_name"));
        product.setProductStructure(rs.getString("product_structure"));
        product.setTransferStatus(rs.getString("product_transfer_status"));
        if (getType) {
            product.setProductType(getProductType(rs));
        } else {
            // still grab the ID
            ProductType type = new ProductType();
            type.setProductTypeId(rs.getString("product_type_id"));
            product.setProductType(type);
        }

        return product;
    }

    public static Reference getReference(ResultSet rs) throws SQLException {
        Reference r = new Reference();
        r.setOrigReference(rs.getString("product_orig_reference"));
        r.setDataStoreReference(rs.getString("product_datastore_reference"));
        r.setFileSize(rs.getLong("product_reference_filesize"));
        r.setMimeType(rs.getString("product_reference_mimetype"));
        return r;
    }

    public static Element getElement(ResultSet rs) throws SQLException {
        Element element = new Element();
        element.setElementId(String.valueOf(rs.getInt("element_id")));
        element.setElementName(rs.getString("element_name"));
        element.setDCElement(rs.getString("dc_element"));
        element.setDescription(rs.getString("element_description"));
        return element;

    }

    public static String getParent(ResultSet rs) throws SQLException {
      return rs.getString("parent_id");
    }
    
    public static Element toScienceDataElement(ResultSet rs) throws SQLException {
      Element element = new Element();
      element.setElementId(rs.getString("parameter_id"));
      element.setElementName(rs.getString("shortName"));
      element.setDescription(rs.getString("description"));
      return element;
    }

    public static Product toScienceDataProduct(ResultSet rs) throws SQLException {
      Product product = new Product();
      product.setProductId(rs.getString("granule_id"));
      product.setProductName(rs.getString("filename"));
      product.setProductStructure(Product.STRUCTURE_FLAT);
      product.setTransferStatus(Product.STATUS_RECEIVED);
      ProductType type = new ProductType();
      type.setProductTypeId(rs.getString("dataset_id"));
      product.setProductType(type);
      return product;
    }

    public static ProductType toScienceDataProductType(ResultSet rs) throws SQLException {
      ProductType type = new ProductType();
      type.setProductTypeId(rs.getString("dataset_id"));
      type.setDescription(rs.getString("description"));
      type.setName(rs.getString("shortName"));
      type.setVersioner("gov.nasa.jpl.oodt.cas.filemgr.versioning.BasicVersioner"); // use
                                                                                    // basic
                                                                                    // versioner
      type.setProductRepositoryPath("file:///tmp"); // not moving files anyways

      Metadata typeMet = new Metadata();
      typeMet.addMetadata("DatasetId", type.getProductTypeId());
      typeMet.addMetadata("DatasetShortName",
          type.getName() != null ? type.getName() : "");
      typeMet.addMetadata("DatasetLongName",
          rs.getString("longName") != null ? rs.getString("longName") : "");
      typeMet.addMetadata("Description",
          type.getDescription() != null ? type.getDescription() : "");
      typeMet.addMetadata("Source",
          rs.getString("source") != null ? rs.getString("source") : "");
      typeMet.addMetadata("ReferenceURL",
          rs.getString("referenceURL") != null ? rs.getString("referenceURL")
              : "");
      type.setTypeMetadata(typeMet);
      return type;
    }    

}
