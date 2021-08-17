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
import Card from "@material-ui/core/Card";
import CardContent from "@material-ui/core/CardContent";
import Button from "@material-ui/core/Button";
import Typography from "@material-ui/core/Typography";
import { withStyles } from "@material-ui/core";
import PropTypes from "prop-types";
import * as fmservice from "services/fmservice"
import Grid from "@material-ui/core/Grid";
import CloudDownloadIcon from '@material-ui/icons/CloudDownload';
import SimpleSnackBar from "components/SimpleSnackBar"
import DeleteIcon from '@material-ui/icons/Delete';
import SingleProductSearchBar from "./SearchBar"

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
  button: {
    margin: theme.spacing(1),
  },
  noResultsText: {
    width: "100%",
    display: "flex",
    justifyContent: "center"
  }
});

class Product extends Component {
  constructor(props) {
    super(props);
    this.snackBarRef = React.createRef();
    this.loadProduct = this.loadProduct.bind(this);
    this.removeProduct = this.removeProduct.bind(this);
  }

  state = {
    selectedProductId: null,
    productData: {},
    productMetaData: {},
    productRefData: {},
    noResultsText: "No products"
  };

  componentDidMount() {
    const productIdFromParams = new URLSearchParams(window.location.search).get("id")
    if (this.state.selectedProductId) {
      this.props.history.push("/product")
      this.loadProduct(this.state.selectedProductId);
    } else if (productIdFromParams) {
      this.setState({selectedProductId: productIdFromParams}) 
      this.loadProduct(productIdFromParams)
    }
  }

  openSnackBar = (message,severity) => {
    this.snackBarRef.current.handleOpen(message,severity);
  }

  removeProduct() {
    let result = window.confirm("Are you Sure to Remove the Product " +this.state.selectedProductId + "?")
    if (result) {
      fmservice
        .removeProductById(this.state.selectedProductId)
        .then((isDeleted) => {
          this.openSnackBar("Sucessfully removed productID: " + this.state.selectedProductId,"success")
          this.props.history.push("/product")
          this.setState({
            selectedProductId: "",
            productData: {},
            productMetaData: {},
            productRefData: {},
          });
        })
        .catch((err) => {
          console.error(err);
        });
    }
  }

  loadProduct(productId) {
    this.setState({ noResultsText: "Searching..." });
    fmservice
      .getProductById(productId)
      .then((productData) => {
        this.setState({
          productData: productData,
          productMetaData: productData.metadata,
          productRefData: productData.references,
        });
      })
      .catch((err) => {
        this.setState({ noResultsText: "No products" });
        console.error(err);
      });
  }

  setSelectedProductId = (productId) => {
    this.props.history.push("/product")
    this.setState({selectedProductId: productId})
    this.loadProduct(productId)
  }

  getProdDataBySection = (sectionTitle) => {
    let productData = {
      "File info": {
        "File name": this.state.productData?.name,
        "Product ID": this.state.productData?.id,
        "Structure": this.state.productData?.structure,
        "Transfer Status": this.state.productData?.transferStatus
      },
      "Metadata info": {
        "File location": this.state.productMetaData?.FileLocation,
        "Product type": this.state.productMetaData?.ProductType,
        "MIME type": this.state.productMetaData?.MimeType[0],
      },
      "Reference info": {
        "Data store ref. location": this.state.productRefData?.dataStoreReference,
        "File size": this.state.productRefData?.fileSize,
        "Original reference": this.state.productRefData?.originalReference,
      },
    };

    return Object.entries(productData[sectionTitle]).map(([key, val]) => (
      <React.Fragment key={key}>
      <Typography
        variant="subtitle1"
        color="textPrimary"
        gutterBottom
      >
        {key}
      </Typography>
      <Typography
        variant="subtitle2"
        color="textPrimary"
        style={{wordWrap: "break-word"}}
        gutterBottom
      >
        <strong>{val}</strong>
      </Typography>
    </React.Fragment>
    ))
  };

  isObjEmpty = (obj) => {
    return Object.keys(obj).length === 0
  }

  render() {
    const { classes } = this.props;
    return (
      <div className={classes.root}>
        <SimpleSnackBar ref={this.snackBarRef} />
        <SingleProductSearchBar setSelectedProductId={this.setSelectedProductId} productId={this.state.selectedProductId}/>
        <br />
        {!this.isObjEmpty(this.state.productData) ? (
          <Card className={classes.card}>
            <Grid>
              <div
                style={{
                  display: "flex",
                  flexDirection: "row",
                  justifyContent: "flex-end",
                }}
              >
                <Button
                  startIcon={<CloudDownloadIcon />}
                  variant="contained"
                  size="large"
                  className={classes.button}
                  color="primary"
                >
                  Download File
                </Button>

                <Button
                  startIcon={<DeleteIcon />}
                  onClick={this.removeProduct}
                  variant="contained"
                  size="large"
                  className={classes.button}
                  color="secondary"
                >
                  Remove Record
                </Button>
              </div>

              <Grid container spacing={10}>
                <Grid item lg="4">
                  <CardContent>
                    <Typography variant="h6" color="textPrimary" gutterBottom>
                      <strong>FILE INFO</strong>
                    </Typography>
                    {this.getProdDataBySection("File info")}
                  </CardContent>
                </Grid>

                <Grid item lg="4">
                  <CardContent>
                    <Typography variant="h6" color="textPrimary" gutterBottom>
                      <strong>METADATA INFO</strong>
                    </Typography>
                    {this.getProdDataBySection("Metadata info")}
                  </CardContent>
                </Grid>

                <Grid item lg="4">
                  <CardContent>
                    <Typography variant="h6" color="textPrimary" gutterBottom>
                      <strong>REFERENCE INFO</strong>
                    </Typography>
                    {this.getProdDataBySection("Reference info")}
                  </CardContent>
                </Grid>
              </Grid>
            </Grid>
          </Card>
        ) : (
          <div className={classes.noResultsText}>
            <Typography variant="h6" gutterBottom>
              {this.state.noResultsText}
            </Typography>
          </div>
        )}
      </div>
    );
  }
}

Product.propTypes = {
  classes: PropTypes.object.isRequired
};

export default withStyles(styles)(Product);