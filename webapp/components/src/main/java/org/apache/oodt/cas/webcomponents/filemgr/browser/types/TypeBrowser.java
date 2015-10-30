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
import org.apache.oodt.cas.filemgr.structs.Element;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductPage;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.Query;
import org.apache.oodt.cas.filemgr.structs.TermQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.filemgr.structs.exceptions.DataTransferException;
import org.apache.oodt.cas.webcomponents.filemgr.FMBrowserSession;
import org.apache.oodt.cas.webcomponents.filemgr.FileManagerConn;
import org.apache.oodt.cas.webcomponents.filemgr.browser.pagination.ProductPaginator;
import org.apache.oodt.cas.webcomponents.filemgr.model.ProductModel;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.util.ListModel;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports
//Wicket imports

/**
 * 
 * This page is mounted to:
 * 
 * <code>/browser/type/[TypeName]</code>.
 * 
 * And provides a query-based, product browser, complete with pagination
 * provided by the attached {@link ProductPaginator} component.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class TypeBrowser extends Panel {

  private static final Logger LOG = Logger.getLogger(TypeBrowser.class
      .getName());

  private List<TermQueryCriteria> criteria;

  private ProductType type;

  private ProductPage productPage;

  private int pageNum;

  private int startIdx;

  private int endIdx;

  private int totalProducts;

  private FileManagerConn fm;

  private static final int PAGE_SIZE = 20;

  public TypeBrowser(String componentId, String fmUrlStr,
      String productTypeName, int pageNum,
      final Class<? extends WebPage> typeBrowserPage,
      final Class<? extends WebPage> produdctBrowser,
      final Class<? extends WebPage> prodRefsBrowser,
      final Class<? extends WebPage> prodMetBrowser) {

    super(componentId);
    this.fm = new FileManagerConn(fmUrlStr);
    this.type = fm.safeGetProductTypeByName(productTypeName);
    this.pageNum = pageNum;
    this.criteria = ((FMBrowserSession) getSession()).getCriteria();
    this.refreshProductPage();
    this.computeStartEndIdx();

    add(new ExistingCriteriaForm("existing_criteria_form"));
    add(new AddCriteriaForm("new_criteria_form"));

    add(new Label("ptype_name", type.getName()));
    add(new Label("start_idx", String.valueOf(this.startIdx)));
    add(new Label("end_idx", String.valueOf(this.endIdx)));
    add(new Label("num_products", String.valueOf(this.totalProducts)));

    add(new ListView<Product>("product_list", this.productPage
        .getPageProducts()) {
      /*
       * (non-Javadoc)
       * 
       * @see
       * org.apache.wicket.markup.html.list.ListView#populateItem(org.apache
       * .wicket.markup.html.list.ListItem)
       */
      @Override
      protected void populateItem(ListItem<Product> prodItem) {
        Link prodPageLink = new Link<Product>("product_page_link",
            new ProductModel(prodItem.getModelObject())) {

          /*
           * (non-Javadoc)
           * 
           * @see org.apache.wicket.markup.html.link.Link#onClick()
           */
          @Override
          public void onClick() {
            PageParameters params = new PageParameters();
            params.add("id", this.getModelObject().getProductId());
            setResponsePage(produdctBrowser, params);

          }
        };

        prodPageLink.add(new Label("product_name", prodItem.getModelObject()
            .getProductName()));
        prodItem.add(prodPageLink);

        prodItem.add(new Label("product_transfer_status", prodItem
            .getModelObject().getTransferStatus()));
        try {
          prodItem.add(new Label("product_pct_transferred", NumberFormat
              .getPercentInstance().format(
                  fm.getFm()
                      .getProductPctTransferred(prodItem.getModelObject()))));
        } catch (DataTransferException e) {
          LOG.log(Level.WARNING,
              "Unable to obtain transfer percentage for product: ["
                  + prodItem.getModelObject().getProductName() + "]: Reason: "
                  + e.getMessage());
        }

        String prodReceivedTime = fm.getProdReceivedTime(prodItem
            .getModelObject());
        prodItem.add(new Label("product_received_time", prodReceivedTime));
        PopupSettings refSettings = new PopupSettings();
        refSettings.setWidth(525).setHeight(450).setWindowName("_refWin");
        Link<String> refLink = new Link<String>("ref_page_link",
            new Model<String>(prodItem.getModelObject().getProductId())) {

          /*
           * (non-Javadoc)
           * 
           * @see org.apache.wicket.markup.html.link.Link#onClick()
           */
          @Override
          public void onClick() {
            PageParameters params = new PageParameters();
            params.add("id", getModelObject());
            setResponsePage(prodRefsBrowser, params);

          }
        };
        refLink.setPopupSettings(refSettings);
        prodItem.add(refLink);

        Link<String> metLink = new Link<String>("met_page_link", new Model(
            prodItem.getModelObject().getProductId())) {

          /*
           * (non-Javadoc)
           * 
           * @see org.apache.wicket.markup.html.link.Link#onClick()
           */
          @Override
          public void onClick() {
            PageParameters params = new PageParameters();
            params.add("id", getModelObject());
            setResponsePage(prodMetBrowser, params);

          }

        };

        PopupSettings metSettings = new PopupSettings();
        metSettings.setWidth(525).setHeight(450).setWindowName("_metWin");
        metLink.setPopupSettings(metSettings);
        prodItem.add(metLink);

      }

    });

    add(new ProductPaginator("paginator", this.productPage,
        this.type.getName(), typeBrowserPage));

  }

  private void refreshProductPage() {
    Query query = new Query();
    System.out.println("CALLING REFRESH PRODUCT PAGE, CRITERIA:");
    for (TermQueryCriteria crit : this.criteria) {
      System.out.println(crit);
    }
    query.getCriteria().addAll(this.criteria);
    try {
      this.productPage = fm.getFm().pagedQuery(query, type, this.pageNum);
    } catch (CatalogException e) {
      LOG.log(Level.SEVERE, "Unable to obtain page products: type: ["
          + type.getName() + "]: Reason: " + e.getMessage());
    }
  }

  private void computeStartEndIdx() {
    if (this.productPage.getTotalPages() == 1) {
      this.totalProducts = this.productPage.getPageProducts().size();
      this.pageNum = 1;
    } else if (productPage.getTotalPages() == 0) {
      this.totalProducts = 0;
      this.pageNum = 1;
    } else {
      this.totalProducts = (productPage.getTotalPages() - 1) * PAGE_SIZE;
      this.pageNum = this.productPage.getPageNum();

      // get the last page
      ProductPage lastPage;
      Query query = new Query();
      query.getCriteria().addAll(this.criteria);

      try {
        lastPage = fm.getFm().pagedQuery(query, this.type,
            this.productPage.getTotalPages());
        this.totalProducts += lastPage.getPageProducts().size();
      } catch (Exception ignore) {
      }
    }
    this.endIdx = this.totalProducts != 0 ? Math.min(this.totalProducts,
        (PAGE_SIZE) * (this.pageNum)) : 0;
    this.startIdx = this.totalProducts != 0 ? ((this.pageNum - 1) * PAGE_SIZE) + 1
        : 0;
  }

  public class ExistingCriteriaForm extends Form<List<TermQueryCriteria>> {

    /**
     * @param id
     *          The wicket:id identifier of the criteria form.
     */
    public ExistingCriteriaForm(String id) {
      super(id);
      ListView<TermQueryCriteria> criteriaView = new ListView<TermQueryCriteria>(
          "criteria_selected_row", criteria) {

        @Override
        protected void populateItem(ListItem<TermQueryCriteria> item) {
          item.add(new Label("criteria_elem_name", item.getModelObject()
              .getElementName()));
          item.add(new Label("criteria_elem_value", item.getModelObject()
              .getValue()));
          item.add(new TermQueryCriteriaRemoveButton("criteria_elem_remove",
              item.getModelObject()));
        }
      };
      criteriaView.setReuseItems(true);
      add(criteriaView);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.wicket.markup.html.form.Form#onSubmit()
     */
    @Override
    protected void onSubmit() {
    }

  }

  class AddCriteriaForm extends Form<ElementCrit> {

    private static final long serialVersionUID = -4543355158252106121L;

    /**
     * @param id
     *          The wicket:id component ID of this form.
     */
    public AddCriteriaForm(String id) {
      super(id, new CompoundPropertyModel<ElementCrit>(new ElementCrit()));
      List<Element> ptypeElements = fm.safeGetElementsForProductType(type);
      Collections.sort(ptypeElements, new Comparator<Element>() {
        public int compare(Element e1, Element e2) {
          return e1.getElementName().compareTo(e2.getElementName());
        }
      });

      add(new DropDownChoice<Element>("criteria_list", new PropertyModel(
          getDefaultModelObject(), "elem"), new ListModel<Element>(
          ptypeElements), new ChoiceRenderer<Element>("elementName",
          "elementId")));
      add(new TextField<TermQueryCriteria>(
          "criteria_form_add_element_value",
          new PropertyModel<TermQueryCriteria>(getDefaultModelObject(), "value")));
      add(new Button("criteria_elem_add"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.wicket.markup.html.form.Form#onSubmit()
     */
    @Override
    protected void onSubmit() {
      ElementCrit tc = (ElementCrit) getDefaultModelObject();
      for (TermQueryCriteria tqc : criteria) {
        if (tqc.getElementName().equals(tc.getElem().getElementName())) {
          return;
        }
      }
      criteria.add(new TermQueryCriteria(tc.getElem().getElementName(), tc
          .getValue()));
      ((FMBrowserSession) getSession()).setCriteria(criteria);
      refreshProductPage();
      computeStartEndIdx();
      PageParameters parameters = new PageParameters();
      parameters.add("name", type.getName());
      parameters.add("pageNum", String.valueOf(pageNum));
      setResponsePage(getPage().getClass(), parameters);
    }
  }

  class ElementCrit implements Serializable {

    private static final long serialVersionUID = -5864863564626117763L;

    private Element elem;

    private String value;

    public ElementCrit() {
      this.elem = null;
      this.value = null;
    }

    /**
     * @return the elem
     */
    public Element getElem() {
      return elem;
    }

    /**
     * @param elem
     *          the elem to set
     */
    public void setElem(Element elem) {
      this.elem = elem;
    }

    /**
     * @return the value
     */
    public String getValue() {
      return value;
    }

    /**
     * @param value
     *          the value to set
     */
    public void setValue(String value) {
      this.value = value;
    }
  }

  class TermQueryCriteriaRemoveButton extends Button {

    private TermQueryCriteria crit;

    public TermQueryCriteriaRemoveButton(String id, TermQueryCriteria crit) {
      super(id);
      this.crit = crit;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.wicket.markup.html.form.Button#onSubmit()
     */
    @Override
    public void onSubmit() {
      for (int i = 0; i < criteria.size(); i++) {
        TermQueryCriteria cr = criteria.get(i);
        if (cr.getElementName().equals(crit.getElementName())) {
          criteria.remove(i);
          ((FMBrowserSession) getSession()).setCriteria(criteria);
          refreshProductPage();
          computeStartEndIdx();
          PageParameters parameters = new PageParameters();
          parameters.add("name", type.getName());
          parameters.add("pageNum", String.valueOf(pageNum));
          setResponsePage(getPage().getClass(), parameters);
          break;
        }
      }
    }

  }

}
