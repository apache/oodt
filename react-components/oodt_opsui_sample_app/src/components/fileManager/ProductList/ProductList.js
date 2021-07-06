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

import React, { Component } from "react";
import { fmconnection } from "constants/connection";
import PropTypes from "prop-types";
import { withStyles } from "@material-ui/core";
import clsx from "clsx";
import Button from "@material-ui/core/Button";
import Grid from "@material-ui/core/Grid";
import Paper from "@material-ui/core/Paper";
import SimpleSnackBar from "./SimpleSnackBar";
import CircularProgress from "@material-ui/core/CircularProgress";
import Typography from "@material-ui/core/Typography";
import SearchBar from "./SearchBar"

const styles = theme => ({
  root: {
    flexGrow: 1,
    backgroundColor: theme.palette.background.paper
  },
  card: {
    minWidth: 275,
    backgroundColor: "#dcdcdc"
  },
  bullet: {
    display: "inline-block",
    margin: "0 2px",
    transform: "scale(0.8)"
  },
  title: {
    fontSize: 14
  },
  pos: {
    marginBottom: 12
  },
  productcontainer: {
    display: "flex",
    flexDirection: "row",
    padding: 0
  },
  paper: {
    padding: 12,
    display: "flex",
    overflow: "auto",
    flexDirection: "column"
  },
  fixedHeight: {
    height: "auto"
  },
  formControl: {
    margin: 1,
    minWidth: 120
  },
  button: {
    padding: 10,
    margin: 20
  }
});

class Product extends Component {
  constructor(props) {
    super(props);
    this.loadProducts = this.loadProducts.bind(this);
    this.loadNextProducts = this.loadNextProducts.bind(this);
    this.loadPrevProducts = this.loadPrevProducts.bind(this);
    this.onClick = this.onClick.bind(this);
  }

  state = {
    productData: [],
    productDetailsArray: [],
    productFilesArray: [],
    productFoldersArray: [],
    selectedProductId: "",
    currentProductPage: 1,
    productTypeArray: [],
    isQueryTimedOut: true
  };

  componentDidMount() {
    this.loadProducts();
  }

  onClick(message) {
    this.child.handleClick(message); // do stuff
  }

  loadProducts() {
    this.setState({ productData: [] });
    this.setState({ productDetailsArray: [] });
    this.setState({ productFilesArray: [] });
    this.setState({ productFoldersArray: [] });
    fmconnection
      .get("/products?productTypeName=GenericFile")

      .then(res => {
        this.setState({
          productData: res.data.productPage
        });
        this.setState({
          productDetailsArray: this.state.productData.products.product
        });

        this.getFiles();
        this.getFolders();

        // Snackbar for Request Success
        this.onClick("Request Successful !!");
      })
      .catch(error => {
        if (error.response) {
          console.log(error.response.data);
          this.onClick("404 - Couldn't Find Resource");
        }
      });
  }

  loadNextProducts() {
    this.setState({ currentProductPage: this.state.currentProductPage + 1 });
    this.setState({ productData: [] });
    this.setState({ productDetailsArray: [] });
    this.setState({ productFilesArray: [] });
    this.setState({ productFoldersArray: [] });
    fmconnection
      .get(
        "/products?productTypeName=GenericFile" +
          "&currentProductPage=" +
          this.state.currentProductPage
      )

      .then(res => {
        this.setState({
          productData: res.data.productPage
        });
        this.setState({
          productDetailsArray: this.state.productData.products.product
        });

        this.getFiles();
        this.getFolders();
        // Snackbar for Request Success
        this.onClick("Request Successful !!");
      })
      .catch(error => {
        if (error.response) {
          console.log(error.response.data);
          this.onClick("404 - Couldn't Find Resource");
        }
      });
  }

  // Have to Implement
  loadPrevProducts() {
    this.setState({ currentProductPage: this.state.currentProductPage - 1 });
    // this.setState({productData: []});
    // this.setState({productDetailsArray: []});
    // this.setState({productFilesArray: []});
    // this.setState({productFoldersArray: []});
    // fmconnection.get("/products?productTypeName=" + this.state.productTypeName+"&currentProductPage="+this.state.currentProductPage)
    //
    //     .then(res => {
    //         this.setState({
    //             productData: res.data.productPage,
    //         });
    //         this.setState({productDetailsArray: this.state.productData.products.product});
    //
    //         this.getFiles();
    //         this.getFolders();
    //         console.log(this.state.productDetailsArray);
    //     })
    //     .catch(err => {
    //         console.log(err);
    //     });
  }

  getFiles() {
    let fileArray = [];
    let fileTypesArray = [];
    for (let i = 0; i < this.state.productDetailsArray.length; i++) {
      if (this.state.productDetailsArray[i].structure === "Flat") {
        fileArray.push(this.state.productDetailsArray[i]);
        fileTypesArray.push(this.state.productDetailsArray[i].type);
      }
    }
    this.setState({ productFilesArray: fileArray });
    let uniqueFileTypes = [...new Set(fileTypesArray)];
    this.setState({ productTypeArray: uniqueFileTypes });
  }

  getFolders() {
    let folderArray = [];
    for (let i = 0; i < this.state.productDetailsArray.length; i++) {
      if (this.state.productDetailsArray[i].structure === "Hierarchical") {
        folderArray.push(this.state.productDetailsArray[i]);
      }
    }
    this.setState({ productFoldersArray: folderArray });
  }

  selectedItem(selectedProductId) {
    this.setState({ selectedProductId: selectedProductId });
    this.props.selectedProductId(this.state.selectedProductId);
  }

  render() {
    const { classes } = this.props;

    // Mapping Folder-products
    let listFolderItems = this.state.productFoldersArray.map(product => (
      <div className={classes.root}>
        <Grid item xs={12} style={{ padding: 3 }}>
          <Button variant={"outlined"} color={"inherit"}>
            {product.name}
          </Button>
        </Grid>
      </div>
    ));

    // Mapping File-products
    let listFileItems = this.state.productFilesArray.map(product => (
      <div className={classes.root}>
        <Grid item xs={6} style={{ padding: 3 }}>
          <Button
            variant={"outlined"}
            color={"inherit"}
            onClick={() => {
              this.selectedItem(product.id);
            }}
          >
            {product.name}
          </Button>
        </Grid>
      </div>
    ));

    return (
      <div className={classes.root}>
        <Paper className={clsx(classes.paper, classes.fixedHeight)}>
          {/*Success/Error Snackbar*/}
          <SimpleSnackBar onRef={(ref) => (this.child = ref)} />
          <SearchBar
            availableProductTypes={this.state.productTypeArray}
            availableMIMETypes={[]}
            onSearch={() => {}}
            onQueryTimeout={({isQueryTimedOut}) => this.setState({isQueryTimedOut})}
          />
          <h4>Folders</h4>

          {this.state.productFoldersArray.length === 0 &&
            this.state.isQueryTimedOut === false && (
              <div align={"center"}>
                <CircularProgress />
              </div>
            )}

          {this.state.productFoldersArray.length === 0 &&
            this.state.isQueryTimedOut === true && (
              <div align={"center"}>
                <Typography variant="subtitle1">No Product Folders Found</Typography>
              </div>
            )}

          {this.state.productFoldersArray.length > 0 && (
            <Grid container spacing={1}>
              {listFolderItems}
            </Grid>
          )}

          {/*Load File Products*/}
          <h4>Files</h4>

          {this.state.productFilesArray.length === 0 &&
            this.state.isQueryTimedOut === false && (
              <div align={"center"}>
                <CircularProgress />
              </div>
            )}

          {this.state.productFilesArray.length === 0 &&
            this.state.isQueryTimedOut === true && (
              <div align={"center"}>
                <Typography variant="subtitle1">No Product Files Found</Typography>
              </div>
            )}

          {this.state.productFilesArray.length > 0 && (
            <Grid container spacing={1}>
              {listFileItems}
            </Grid>
          )}

          <Grid item xs={12} md={6}>
            <Grid container spacing={1} direction="column" alignItems="center">
              <Grid item>
                <Button onClick={this.loadPrevProducts}>{"<<"}</Button>
                <Button>{this.state.currentProductPage}</Button>
                <Button onClick={this.loadNextProducts}>{">>"}</Button>
              </Grid>
            </Grid>
          </Grid>
        </Paper>
      </div>
    );
  }
}

Product.propTypes = {
  classes: PropTypes.object.isRequired
};

export default withStyles(styles)(Product);
