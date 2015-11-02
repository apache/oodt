/**
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

package org.apache.oodt.cas.webcomponents.filemgr.browser.types;

//JDK imports
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.webcomponents.filemgr.FileManagerConn;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.util.GenericBaseModel;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports
//Wicket imports

/**
 * 
 * This page is mounted to:
 * 
 * <code>/browser/types</code> and shows a type plus 
 * its num product count.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class Types extends Panel {

  private static final long serialVersionUID = 6263193710066495653L;

  private static final Logger LOG = Logger.getLogger(Types.class.getName());
  
  private FileManagerConn fm;

  private static final String BLANK_SPACE = " ";

  public Types(String componentId, String fmUrlStr, final Class<? extends WebPage> typeBrowser) {
    super(componentId);
    this.fm = new FileManagerConn(fmUrlStr);
    List<ProductType> types = this.fm.safeGetProductTypes();
    Collections.sort(types, new Comparator<ProductType>() {
      public int compare(ProductType type1, ProductType type2) {
           return type1.getName().compareTo(type2.getName());
      }
    });
    List<TypeCountTuple> counts = this.toTypeCounts(types);
    add(new ListView<TypeCountTuple>("cas_fm_browser_ptype_table_rows", counts) {

      /*
       * (non-Javadoc)
       * 
       * @see
       * org.apache.wicket.markup.html.list.ListView#populateItem(org.apache
       * .wicket.markup.html.list.ListItem)
       */
      @Override
      protected void populateItem(ListItem<TypeCountTuple> type) {
        if (type.getModelObject().getTypes().size() == 1) {
          type.add(new Label("type_name", type.getModelObject().getTypes().get(
              0).getName()));
          Link<ProductType> typeCountLink = new Link<ProductType>(
              "type_count_link", new ProductTypeModel(type.getModelObject()
                  .getTypes().get(0))) {
            /*
             * (non-Javadoc)
             * 
             * @see org.apache.wicket.markup.html.link.Link#onClick()
             */
            @Override
            public void onClick() {
              PageParameters params = new PageParameters();
              params.add("name", this.getModelObject().getName());
              params.add("pageNum", "1");
              setResponsePage(typeBrowser, params);

            }
          };
          typeCountLink.add(new Label("type_count", String.valueOf(type
              .getModelObject().getCounts().get(0))));
          type.add(typeCountLink);
          Link<ProductType> type2Link = new Link<ProductType>(
              "type2_count_link") {
            /*
             * (non-Javadoc)
             * 
             * @see org.apache.wicket.Component#isVisible()
             */
            @Override
            public boolean isVisible() {
              return false;
            }

            @Override
            public void onClick() {
            }
          };
          type.add(new Label("type2_name", BLANK_SPACE));
          type2Link.add(new Label("type2_count", BLANK_SPACE));
          type.add(type2Link);

        } else {
          type.add(new Label("type_name", type.getModelObject().getTypes().get(
              0).getName()));
          Link<ProductType> typeCountLink = new Link<ProductType>(
              "type_count_link", new ProductTypeModel(type.getModelObject()
                  .getTypes().get(0))) {
            /*
             * (non-Javadoc)
             * 
             * @see org.apache.wicket.markup.html.link.Link#onClick()
             */
            @Override
            public void onClick() {
              PageParameters params = new PageParameters();
              params.add("name", this.getModelObject().getName());
              params.add("pageNum", "1");
              setResponsePage(typeBrowser, params);

            }
          };
          typeCountLink.add(new Label("type_count", String.valueOf(type
              .getModelObject().getCounts().get(0))));
          type.add(typeCountLink);
          type.add(new Label("type2_name", type.getModelObject().getTypes()
              .get(1).getName()));
          Link<ProductType> type2Link = new Link<ProductType>(
              "type2_count_link", new ProductTypeModel(type.getModelObject()
                  .getTypes().get(1))) {
            /*
             * (non-Javadoc)
             * 
             * @see org.apache.wicket.markup.html.link.Link#onClick()
             */
            @Override
            public void onClick() {
              PageParameters params = new PageParameters();
              params.add("name", this.getModelObject().getName());
              params.add("pageNum", "1");
              setResponsePage(typeBrowser, params);

            }
          };
          type2Link.add(new Label("type2_count", String.valueOf(type
              .getModelObject().getCounts().get(1))));
          type.add(type2Link);
        }

      }
    });
  }

  private List<TypeCountTuple> toTypeCounts(List<ProductType> types) {
    List<TypeCountTuple> counts = new Vector<TypeCountTuple>();
    TypeCountTuple tuple = new TypeCountTuple();
    for (int i = 0; i < types.size(); i++) {
      ProductType type = types.get(i);
      if (tuple.getTypes().size() == 2) {
        counts.add(tuple);
        tuple = new TypeCountTuple();
      }
      tuple.getTypes().add(type);
      try {
        tuple.getCounts().add(fm.getFm().getNumProducts(type));
      } catch (CatalogException e) {
        LOG.log(Level.SEVERE, e.getMessage());
        LOG.log(Level.WARNING, "Unable to set count for product type: ["
            + type.getName() + "]: Reason: [" + e.getMessage() + "]");
      }

      if (i == types.size() - 1) {
        counts.add(tuple);
      }

    }

    return counts;
  }

  class TypeCountTuple implements Serializable {

    private static final long serialVersionUID = 7908536266142876646L;

    private List<ProductType> types;

    private List<Integer> counts;

    public TypeCountTuple() {
      this.types = new Vector<ProductType>();
      this.counts = new Vector<Integer>();
    }

    /**
     * @return the types
     */
    public List<ProductType> getTypes() {
      return types;
    }

    /**
     * @param types
     *          the types to set
     */
    public void setTypes(List<ProductType> types) {
      this.types = types;
    }

    /**
     * @return the counts
     */
    public List<Integer> getCounts() {
      return counts;
    }

    /**
     * @param counts
     *          the counts to set
     */
    public void setCounts(List<Integer> counts) {
      this.counts = counts;
    }

  }

  class ProductTypeModel extends GenericBaseModel<ProductType> {

    private static final long serialVersionUID = 6528976699866791800L;

    public ProductTypeModel(ProductType type) {
      this.setObject(type);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.wicket.model.util.GenericBaseModel#createSerializableVersionOf
     * (java.lang.Object)
     */
    @Override
    protected ProductType createSerializableVersionOf(ProductType type) {
      SerializableProductType stype = new SerializableProductType();
      stype.setDescription(type.getDescription());
      stype.setExtractors(type.getExtractors());
      stype.setHandlers(type.getHandlers());
      stype.setName(type.getName());
      stype.setProductRepositoryPath(type.getProductRepositoryPath());
      stype.setProductTypeId(type.getProductTypeId());
      stype.setTypeMetadata(type.getTypeMetadata());
      stype.setVersioner(type.getVersioner());
      return stype;
    }

  }
  
  class SerializableProductType extends ProductType implements Serializable {

    private static final long serialVersionUID = 6900948355619420582L;
    
  }

}
