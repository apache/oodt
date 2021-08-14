import React, {Component} from "react";
import {BrowserRouter, Route, Switch} from "react-router-dom";
import OPSUIHome from "./components/OPSUIHome";
import ComponentStatus from "./components/ComponentStatus";
import {WorkflowList} from "./components/workflowManager";
import {Product, ProductIngest, ProductIngestWithMetaFile, ProductList} from "./components/fileManager";

class MyApp extends Component {
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
              render={(props) => (
                <ProductList selectedProductId={this.setSelectedProductId} {...props}/>
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
                  <Product />
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
