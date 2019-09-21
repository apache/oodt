import React, { Component } from "react";
import { BrowserRouter, Switch, Route } from "react-router-dom";
import {
  ProductList,
  Product,
  ProductIngest,
  ProductIngestWithMetaFile
} from "oodt_fm_plugin_sample";
import OPSUIHome from "./components/OPSUIHome";
import SearchBar from "./components/SearchBar";
// import { WorkflowList } from "oodt_wm_plugin_sample";
import Paper from "@material-ui/core/Paper";
import Typography from "@material-ui/core/Typography";
import Switch1 from "@material-ui/core/Switch";
import axios from "axios";

class MyApp extends Component {
  constructor(props) {
    super(props);
    this.setSelectedProductId = this.setSelectedProductId.bind(this);
    this.handleChange = this.handleChange.bind(this);
    // this.timeOut = this.timeOut.bind(this);
  }

  state = {
    selectedProductId: "",
    checkedA: false
  };

  setSelectedProductId(productId) {
    this.setState({ selectedProductId: productId });
    console.log(this.state.selectedProductId);
  }

  componentDidMount() {
    this.handleChange();
    // this.timeOut()
  }

  handleChange(){
    axios
      .get("http://"+ window.location.hostname +":8080/cas_product_war/jaxrs/v2/fmprodstatus")
      .then(result => {
        console.log(result.data.FMStatus.serverUp);
        if (result.data.FMStatus.serverUp) {
          this.setState({ checkedA: true });
        }
      })
      .catch(error => {});
  };

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
                  <Paper
                    style={{
                      flexGrow: 1,
                      backgroundColor: "white",
                      padding: 20
                    }}
                  >
                    <Typography variant="h5" component="h3">
                      File Manager Status
                      <Switch1
                        disabled={true}
                        checked={this.state.checkedA}
                        onChange={this.handleChange}
                      />
                    </Typography>
                  </Paper>
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
                  {/*<WorkflowList />*/}
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
