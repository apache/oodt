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
import * as fmservice  from "services/fmservice";
import PropTypes from "prop-types";
import { withStyles,TablePagination } from "@material-ui/core";
import clsx from "clsx";
import Grid from "@material-ui/core/Grid";
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableContainer from '@material-ui/core/TableContainer';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Paper from "@material-ui/core/Paper";
import CircularProgress from "@material-ui/core/CircularProgress";
import SearchBar from "./SearchBar"
import {shrinkString} from "utils/utils"
import { NavLink } from "react-router-dom";
import { withSnackbar } from 'notistack';
import { Product as ProductDrawer } from "components/fileManager/Product"

const styles = (theme) => ({
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
    padding: 5,
    margin: 20
  },
  progress: {
    width: "inherit",
    display: "flex",
    justifyContent: "center",
    minHeight: "10vh",
    alignItems: "center",
  },
  nameCell: {
    cursor: "pointer",
    textDecoration: "underline",
  },
});

class ProductBrowser extends Component {
  constructor(props) {
    super(props);
    this.snackBarRef = React.createRef();
  }

  state = {
    productData: [],
    productDetailsArray: [],
    selectedProductId: "",
    currentProductPage: 0,
    totalProductCount: 0,
    productTypeArray: [],
    isQueryTimedOut: true,
    noProductsText: "Loading..."
  };

  componentDidMount() {
    const params = new URLSearchParams(window.location.search);
    this.setState({selectedProductId: params.get("id")})
    this.loadNextProducts();
  }

  componentDidUpdate(prevProps){
    if(this.props !== prevProps) {
      const params = new URLSearchParams(window.location.search);
      this.setState({selectedProductId: params.get("id")})
    }
  }

  onProductDrawerClose = () => {
    this.props.history.push("/products")
    this.loadNextProducts()
  }

  onProductSearch = (productName) => {
    fmservice.getProductPage({
      productName: productName
    }).then(productPage => {
        let productsArr = productPage.products.product
        if (typeof(productsArr) === "undefined"){
          this.setState({noProductsText: "No products"})
          return;
        }
        if (!Array.isArray(productsArr)) {
          productsArr = [productsArr]
        }
        
        this.setState({
          productData: productPage,
          totalProductCount: productPage.totalProducts,
          productDetailsArray: productsArr || [],
        });
    }).catch(err => {
      this.props.enqueueSnackbar("Product search failed",{
        variant: "error"
      })
    })
  } 

  loadNextProducts = () => {
    this.setState({
      productFoldersArray: [],
      productData: [],
      productFilesArray: [],
      productDetailsArray: [],
    });
 
    fmservice
      .getProductPage({
        productType: "GenericFile",
        productPageNo: this.state.currentProductPage + 1
      })
      .then((productPage) => {
        let productsArr = productPage.products.product
        // Backend returns a product object when the object count is 1
        // and returns an array of products when the object count is more than 1.
        // This check converts object to array to avoid this problem
        // Need to fix this in the backend
        if (typeof(productsArr) === "undefined"){
          this.setState({noProductsText: "No products"})
          return;
        }
        if (!Array.isArray(productsArr)) {
          productsArr = [productsArr]
        }
        this.setState({
          productData: productPage,
          totalProductCount: productPage.totalProducts,
          productDetailsArray: productsArr || [],
        });
      })
      .catch(error => {
        if (error.response) {
          console.error("err: ",error.response.data);
          this.props.enqueueSnackbar("Couldn't fetch products",{
            variant: "error"
          })
        }
      });
  }

  handleChangePage = (event,newPageNo) => {
    this.setState({currentProductPage: newPageNo},() => this.loadNextProducts())
  }

  render() {
    const { classes } = this.props;

    return (
      <div className={classes.root}>
        <Paper className={clsx(classes.paper, classes.fixedHeight)}>
          <SearchBar
            onSearch={this.onProductSearch}
            onQueryTimeout={({ isQueryTimedOut }) =>
              this.setState({ isQueryTimedOut })
            }
          />

          <TableContainer style={{ marginTop: "2%",width: "100%" }}>
            <Table className={classes.productTable} aria-label="a dense table">
              <TableHead>
                <TableRow>
                  <TableCell align="left">
                    <strong>Product ID</strong>
                  </TableCell>
                  <TableCell align="left">
                    <strong>Name</strong>
                  </TableCell>
                  <TableCell align="left">
                    <strong>Received at</strong>
                  </TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {this.state.productDetailsArray.length > 0 && 
                  this.state.productDetailsArray.map((product) => {
                    const productReceivedTime = product.metadata?.keyval.find(
                      (meta) => meta.key === "CAS.ProductReceivedTime"
                    ).val;
                    return (
                      <TableRow key={product.id}>
                        <TableCell>{product.id}</TableCell>
                        <TableCell className={classes.nameCell}>
                        <NavLink to={"/products?id=" + product.id}>
                            {shrinkString(product.name, 40, 10)}
                        </NavLink>
                        </TableCell>
                        <TableCell>{productReceivedTime}</TableCell>
                      </TableRow>
                    );
                  } 
              )}
                {this.state.productDetailsArray.length === 0 && (
                   <TableRow>
                   <TableCell></TableCell>
                   <TableCell>
                   <div className={classes.progress}>
                   <p>{this.state.noProductsText}</p>
                  </div>
                   </TableCell>
                   <TableCell></TableCell>
                 </TableRow>
                )}
              </TableBody>
            </Table>
          </TableContainer>

          <Grid
            style={{ width: "100%" }}
            container
            spacing={1}
            direction="column"
            alignItems="center"
          >
            <Grid item>
              <TablePagination
                component="div"
                count={this.state.totalProductCount}
                page={this.state.currentProductPage}
                onChangePage={this.handleChangePage}
                rowsPerPage={20}
                rowsPerPageOptions={[]}
              />
            </Grid>
          </Grid>
        </Paper>
        <ProductDrawer 
          productId={this.state.selectedProductId}
          onClose={this.onProductDrawerClose}
        />
      </div>
    );
  }
}

ProductBrowser.propTypes = {
  classes: PropTypes.object.isRequired
};

export default withStyles(styles)(withSnackbar(ProductBrowser));