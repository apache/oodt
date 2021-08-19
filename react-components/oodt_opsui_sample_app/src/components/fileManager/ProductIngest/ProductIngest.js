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
import { OutlinedInput, Paper, withStyles } from "@material-ui/core";
import Typography from "@material-ui/core/Typography";
import PropTypes from "prop-types";
import Button from "@material-ui/core/Button";
import * as fmservice from "services/fmservice"
import ProgressBar from "components/ProgressBar"
import FormControl from "@material-ui/core/FormControl";
import MenuItem from "@material-ui/core/MenuItem";
import InputLabel from "@material-ui/core/InputLabel";
import Select from "@material-ui/core/Select";
import {shrinkString} from "utils/utils"
import { withSnackbar } from 'notistack';

const styles = theme => ({
  root: {
    flexGrow: 1,
    backgroundColor: theme.palette.background.paper,
    padding: 20
  },
  button: {
    marginLeft: 20
  },
  textField: {
    marginLeft: 2,
    marginRight: 2
  },
  layout: {
    display: "flex",
    flexDirection: "row",
    alignItems: "center",
    height: "15vh",
    justifyContent: "space-between",
    padding: 0
  },
  progress: {
    margin: 2
  },
  formControlContainer: {
    display: "flex",
    justifyContent: "space-between",
    width: "40%",
  },
});

class ProductIngest extends Component {
  constructor(props) {
    super(props);
    this.handleFile = this.handleFile.bind(this);
    this.handleProductStructure = this.handleProductStructure.bind(this);
    this.handleProductType = this.handleProductType.bind(this);
    this.ingestProduct = this.ingestProduct.bind(this);
  }

  state = {
    ingestedFile: null,
    ingestedFileName: "",
    productId: "",
    productType: "",
    productStructure: "Flat",
    isIngested: false,
    isIngestButtonClicked: false,
    ingestedPercentage: 0,
    productTypes: [],
  };

  componentDidMount() {
    fmservice
      .getAllProductTypes()
      .then((productTypes) =>
        this.setState({ productTypes, productType: productTypes[0].name })
      )
      .catch((err) => {
        console.error(err)
        this.props.enqueueSnackbar("Couldn't get the available product types",{
          variant: "warning"
        })
      });
  }

  handleFile(e) {
    let file = e.target.files[0];
    this.setState({ ingestedFile: file,ingestedFileName: shrinkString(file.name,25,6) });
  }

  handleProductStructure(e) {
    let productStructure = e.target.value;
    this.setState({ productStructure: productStructure });
  }

  handleProductType(e) {
    let productType = e.target.value;
    this.setState({ productType: productType });
  }

  keyPress(e) {
    if (e.keyCode === 13) {
      this.handleProductStructure();
    }
  }

  ingestProduct() {
    this.setState({ isIngested: false, isIngestButtonClicked: true });
    if(!this.state.ingestedFile){
      this.props.enqueueSnackbar("No product selected", { 
        variant: 'error',
    });
      this.setState({isIngestButtonClicked: false})
      return;
    }
    if(!this.state.productType){
      this.props.enqueueSnackbar("Product type is not selected",{
        variant: "error"
      })
      this.setState({isIngestButtonClicked: false})
      return;
    }
    let formData = new FormData();
    formData.append("productFile", this.state.ingestedFile);

    let onUploadProgress = (progressEvent) => {
      let percentCompleted = Math.round(
        (progressEvent.loaded * 100) / progressEvent.total
      );
      this.setState({ ingestedPercentage: percentCompleted });
    };

    fmservice
      .ingestProduct(
        formData,
        this.state.productType,
        this.state.productStructure,
        onUploadProgress
      )
      .then((res) => {
        this.setState({
          productId: res.productId,
          isIngestButtonClicked: false,
          isIngested: true,
          ingestedFile: null,
          ingestedFileName: "",
          ingestedPercentage: 0,
        });
        this.props.enqueueSnackbar("Successfully Ingested Product "+res.productId,{
          variant: "success"
        })
      })
      .catch((error) => {
        console.error(error);
        this.setState({
          isIngested: false,
          ingestedPercentage: 0,
          isIngestButtonClicked: false,
        });
        this.props.enqueueSnackbar("Product Ingestion Failed",{
          variant: "error"
        })
      });
  }

  render() {
    const { classes } = this.props;

    return (
      <Paper className={classes.root}>
        <div style={{display: "flex",justifyContent: "space-between"}}>
        <Typography variant="subtitle1">
          <strong>Product Ingest with Single File</strong>
        </Typography>
        {this.state.isIngestButtonClicked && (
            <ProgressBar value={this.state.ingestedPercentage} />
          )}
        </div>

        <br />

        <div className={classes.layout}>
          <div className={classes.formControlContainer}>
          <FormControl
            variant="outlined"
            style={{ width: "180px" }}
          >
            <InputLabel htmlFor="outlined-product-Type-simple">
              Product Type
            </InputLabel>
            <Select
              value={this.state.productType}
              onChange={this.handleProductType}
              input={
                <OutlinedInput
                  labelWidth={100}
                  name="Product Type"
                  id="outlined-product-Type-simple"
                />
              }
            >
              {this.state.productTypes.map((productType) => {
                return (
                  <MenuItem value={productType.name}>
                    {productType.name}
                  </MenuItem>
                );
              })}
            </Select>
          </FormControl>

          <FormControl
            variant="outlined"
            style={{ width: "180px" }}
          >
            <InputLabel htmlFor="outlined-product-structure-simple">
              Product Structure
            </InputLabel>
            <Select
              value={this.state.productStructure}
              onChange={this.handleProductStructure}
              input={
                <OutlinedInput
                  labelWidth={130}
                  name="Product Structure"
                  id="outlined-product-structure-simple"
                />
              }
            >
              <MenuItem selected={true} value={"Flat"}>
                Flat
              </MenuItem>
            </Select>
          </FormControl>
          </div>

          <Button variant="contained" component="label">
            Upload File
            <input
              className={classes.button}
              type="file"
              name="fileToUpload"
              id="fileToUpload"
              onChange={(e) => this.handleFile(e)}
              hidden
            />
          </Button>

          <div style={{width: "18%"}}>
          {this.state.ingestedFileName}
          </div>

          <Button
            variant="contained"
            disabled={this.state.isIngestButtonClicked}
            color="primary"
            onClick={this.ingestProduct}
          >
            Ingest Product
          </Button>
        </div>
      </Paper>
    );
  }
}

ProductIngest.propTypes = {
  classes: PropTypes.object.isRequired
};

export default withStyles(styles)(withSnackbar(ProductIngest));