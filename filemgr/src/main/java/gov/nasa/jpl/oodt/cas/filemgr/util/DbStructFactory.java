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

package gov.nasa.jpl.oodt.cas.filemgr.util;

//OODT imports
import gov.nasa.jpl.oodt.cas.filemgr.structs.Element;
import gov.nasa.jpl.oodt.cas.filemgr.structs.ProductType;
import gov.nasa.jpl.oodt.cas.filemgr.structs.Product;
import gov.nasa.jpl.oodt.cas.filemgr.structs.Reference;

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
        return getProduct(rs, true);
    }

    public static Product getProduct(ResultSet rs, boolean getType)
            throws SQLException {
        Product product = new Product();
        product.setProductId(String.valueOf(rs.getInt("product_id")));
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
        String parent = rs.getString("parent_id");
        return parent;
    }

}
