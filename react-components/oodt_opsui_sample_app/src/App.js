import React, {Component} from "react";
import {BrowserRouter, Route, Switch} from "react-router-dom";
import OPSUIHome from "./components/OPSUIHome";
import SearchBar from "./components/SearchBar";
import ComponentStatus from "./components/ComponentStatus";
import {WorkflowList} from "./components/workflowManager";
import {Product, ProductIngest, ProductIngestWithMetaFile, ProductList} from "./components/fileManager";

class MyApp extends Component {

  constructor(props) {
    super(props);
    this.setSelectedProductId = this.setSelectedProductId.bind(this);
  }

  state = {
    selectedProductId: ""
  };

  setSelectedProductId(productId) {
    this.setState({ selectedProductId: productId });
  }

  render() {
    return (
      <BrowserRouter>
        <OPSUIHome>
          <Switch>
            <Route
              exact
              path={"/"}
              render={() => (
                <div>
                  <h1>Dashboard</h1>
                  <ComponentStatus />
                </div>
              )}
            />

            <Route
              path={"/products"}
              render={() => (
                <ProductList selectedProductId={this.setSelectedProductId} />
              )}
            />

            <Route
              path={"/productIngest"}
              render={() => (
                <div>
                  <ProductIngest />
                  <br />
                  <ProductIngestWithMetaFile />
                </div>
              )}
            />

            <Route
              path={"/product"}
              render={() => (
                <div>
                  <SearchBar setSelectedProductId={this.setSelectedProductId} />
                  <br />
                  <Product productId={this.state.selectedProductId} />
                </div>
              )}
            />

            <Route
              path={"/workflows"}
              render={() => (
                <div>
                  <WorkflowList/>
                </div>
              )}
            />
          </Switch>
        </OPSUIHome>
      </BrowserRouter>
    );
  }
}

export default MyApp;