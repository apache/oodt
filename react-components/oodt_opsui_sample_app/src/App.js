import React, {Component} from "react";
import {BrowserRouter, Route, Switch} from "react-router-dom";
import { SnackbarProvider } from 'notistack';
import OPSUIHome from "./components/OPSUIHome";
import ComponentStatus from "./components/ComponentStatus";
import {WorkflowList} from "./components/workflowManager";
import {ProductIngest, ProductIngestWithMetaFile, ProductList} from "./components/fileManager";

class MyApp extends Component {
  render() {
    return (
      <BrowserRouter>
        <SnackbarProvider
          maxSnack={3}
          autoHideDuration={3000}
          anchorOrigin={{
            vertical: "top",
            horizontal: "center",
          }}
        >
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
                  <ProductList
                    selectedProductId={this.setSelectedProductId}
                    {...props}
                  />
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
                path={"/workflows"}
                render={() => (
                  <div>
                    <WorkflowList />
                  </div>
                )}
              />
            </Switch>
          </OPSUIHome>
        </SnackbarProvider>
      </BrowserRouter>
    );
  }
}

export default MyApp;
