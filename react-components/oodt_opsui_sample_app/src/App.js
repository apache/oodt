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
    // this.timeOut = this.timeOut.bind(this);

    this.fmRestApiUrl = process.env.REACT_APP_FM_REST_API_URL;
    this.wmRestApiUrl = process.env.REACT_APP_WM_REST_API_URL;
  }

  state = {
    selectedProductId: "",
    fmAvailable: false,
    wmAvailable: false
  };

  setSelectedProductId(productId) {
    this.setState({ selectedProductId: productId });
    console.log(this.state.selectedProductId);
  }

  componentDidMount() {
    // this.timeOut()
  }

  // timeOut() {
  //   setInterval(function () {
  //     axios
  //     .get("http://localhost:8080/cas_product_war/jaxrs/v2/fmprodstatus")
  //     .then(result => {
  //       console.log(result.data.FMStatus.serverUp);
  //       if (result.data.FMStatus.serverUp) {
  //         this.setState({ checkedA: true });
  //       }
  //     })
  //     .catch(error => {});
  //
  //   }, 10000);
  // }
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
                  <ComponentStatus
                      fmRestApiUrl={this.fmRestApiUrl}
                      wmRestApiUrl={this.wmRestApiUrl}>
                  </ComponentStatus>
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
