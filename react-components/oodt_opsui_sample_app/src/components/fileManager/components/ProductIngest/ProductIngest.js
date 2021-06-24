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
import TextField from "@material-ui/core/TextField";
import Button from "@material-ui/core/Button";
import { fmconnection } from "../../constants/fmconnection";
import CircularProgress from "@material-ui/core/CircularProgress";

import FormControl from "@material-ui/core/FormControl";
import MenuItem from "@material-ui/core/MenuItem";
import InputLabel from "@material-ui/core/InputLabel";
import Select from "@material-ui/core/Select";
import FormHelperText from "@material-ui/core/FormHelperText";
import Input from "@material-ui/core/Input";

const styles = theme => ({
  root: {
    flexGrow: 1,
    backgroundColor: theme.palette.background.paper,
    padding: 20
  },
  button: {
    padding: 10,
    margin: 20
  },
  textField: {
    marginLeft: 2,
    marginRight: 2
  },
  layout: {
    display: "flex",
    flexDirection: "row",
    padding: 0
  },
  progress: {
    margin: 2
  },
  formControl: {
    margin: 1,
    minWidth: 120
  }
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
    productId: "",
    productType: "GenericFile",
    productStructure: "Flat",
    isIngested: false,
    isIngestButtonClicked: false
  };

  handleFile(e) {
    let file = e.target.files[0];
    this.setState({ ingestedFile: file });
  }

  handleProductStructure(e) {
    let productStructure = e.target.value;
    this.setState({ productStructure: productStructure });
  }

  handleProductType(e) {
    let productType = e.target.value;
    this.setState({ productType: productType });
    console.log(this.state.productType);
  }

  keyPress(e) {
    if (e.keyCode === 13) {
      console.log(e.target.value);
      this.handleProductStructure();
    }
  }

  ingestProduct() {
    this.setState({ isIngested: false });
    this.setState({ isIngestButtonClicked: true });

    let product = this.state.ingestedFile;
    let formData = new FormData();
    formData.append("productFile", product);
    fmconnection
      .post(
        "productWithFile?productType=" +
          this.state.productType +
          "&productStructure=" +
          this.state.productStructure,
        formData
      )
      .then(result => {
        console.log(result);
        this.setState({ productId: result.data });
        this.setState({ isIngested: true });
      })
      .catch(error => {
        console.log(error);
        this.setState({ isIngested: false });
        alert("Product Ingestion Failed : " + error);
      });
  }

  render() {
    const { classes } = this.props;

    return (
      <Paper className={classes.root}>
        <Typography variant="h5" component="h3">
          Product Ingest with Single File
        </Typography>

        <br />

        <div className={classes.layout}>
          <form>
            <FormControl
              variant="outlined"
              className={classes.formControl}
              style={{ width: "200px" }}
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
                <MenuItem selected={true} value={"GenericFile"}>
                  GenericFile
                </MenuItem>
                <MenuItem value={"LocationAwareProduct"}>
                  LocationAwareProduct
                </MenuItem>
              </Select>
            </FormControl>

            <FormControl
              variant="outlined"
              className={classes.formControl}
              style={{ width: "200px" }}
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
          </form>

          <input
            className={classes.button}
            type={"file"}
            name={"fileToUpload"}
            id={"fileToUpload"}
            onChange={e => this.handleFile(e)}
          />

          <Button
            variant="contained"
            color="primary"
            className={classes.button}
            onClick={this.ingestProduct}
          >
            Ingest Product
          </Button>

          {this.state.isIngested === false &&
            this.state.isIngestButtonClicked == true && <CircularProgress />}
          {this.state.isIngested == true &&
            alert("Successfully Ingested Product ID :" + this.state.productId)}
        </div>
      </Paper>
    );
  }
}

ProductIngest.propTypes = {
  classes: PropTypes.object.isRequired
};

export default withStyles(styles)(ProductIngest);
