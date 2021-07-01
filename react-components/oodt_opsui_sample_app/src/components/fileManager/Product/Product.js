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
import { fmconnection } from "constants/connection";
import Grid from "@material-ui/core/Grid";
import CloudDownloadIcon from '@material-ui/icons/CloudDownload';
import DeleteIcon from '@material-ui/icons/Delete';

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
    this.loadProduct = this.loadProduct.bind(this);
    this.removeProduct = this.removeProduct.bind(this);
  }

  state = {
    productData: [],
    productMetaDataFileLocation: [],
    productMetaDataFileType: [],
    productMetaDataMimeType: [],
    productReferencesData: [],
    noResultsText: "No products"
  };

  componentDidUpdate(prevProps){
    if(this.props.productId !== prevProps.productId){
      this.loadProduct();
    }
  }

  getProdDataBySection = (sectionTitle) => {
    let productData = {
      "File info": {
        "File name": this.state.productData["name"],
        "Product ID": this.state.productData["id"],
        "Structure": this.state.productData["structure"],
        "Transfer Status": this.state.productData["transferStatus"]
      },
      "Metadata info": {
        "File location": this.state.productMetaDataFileLocation["val"],
        "Product type": this.state.productMetaDataFileType["val"],
        "MIME type": this.state.productMetaDataMimeType[0]
      },
      "Reference info": {
        "Data store ref. location": this.state.productReferencesData["dataStoreReference"],
        "File size": this.state.productReferencesData["fileSize"],
        "Original reference": this.state.productReferencesData["originalReference"]
      }
    }
    return Object.entries(productData[sectionTitle]).map(([key,val]) => (
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
  }

  render() {
    const { classes } = this.props;
    return (
      <div className={classes.root}>
        {this.state.productData.length !== 0 ? (
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

  removeProduct() {
    let result = window.confirm(
      "Are you Sure to Remove the Product ?" + this.props.productId
    )
    if (result) {
      fmconnection
        .delete("/removeProduct?productId=" + this.props.productId)
        .then(res => {
          alert(
            "Product sucessfully removed productID: " + this.props.productId
          );
        });
    } else {
    }
  }

  loadProduct() {
    this.setState({noResultsText: "Searching..."})
    fmconnection
      .get("/product?productId=" + this.props.productId)
      .then(res => {
        this.setState({
          productData: res.data.product,
          productMetaDataFileLocation: Object.values(
            res.data.product.metadata
          )[0][0],
          productMetaDataFileType: Object.values(
            res.data.product.metadata
          )[0][1],
          productMetaDataMimeType: Object.values(
            res.data.product.metadata
          )[0][7]["val"],
          productReferencesData: res.data.product.references.reference
        });
        console.log(this.state.productMetaDataFileLocation["val"]);
        console.log(this.state.productMetaDataFileType["val"]);
        console.log(this.state.productMetaDastaMimeType[0]);
      })
      .catch(err => {
        this.setState({noResultsText: "No products"})
        console.log(err);
      });
  }

  componentDidMount() {
    // Make a request for a product with a given ID
    fmconnection
      .get("/product?productId=" + this.props.productId)
      .then(res => {
        this.setState({
          productData: res.data.product,
          productMetaDataFileLocation: Object.values(
            res.data.product.metadata
          )[0][0],
          productMetaDataFileType: Object.values(
            res.data.product.metadata
          )[0][1],
          productMetaDataMimeType: Object.values(
            res.data.product.metadata
          )[0][7]["val"],
          productReferencesData: res.data.product.references.reference
        });
        console.log(this.state.productMetaDataFileLocation["val"]);
        console.log(this.state.productMetaDataFileType["val"]);
        console.log(this.state.productMetaDastaMimeType[0]);
      })
      .catch(err => {
        console.log(err);
      });
  }
}

Product.propTypes = {
  classes: PropTypes.object.isRequired
};

export default withStyles(styles)(Product);