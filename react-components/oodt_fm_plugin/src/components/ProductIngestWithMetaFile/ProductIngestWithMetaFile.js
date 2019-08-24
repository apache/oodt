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
import { Paper, withStyles } from "@material-ui/core";
import Typography from "@material-ui/core/Typography";
import PropTypes from "prop-types";
import Button from "@material-ui/core/Button";
import { fmconnection } from "../../constants/fmconnection";
import CircularProgress from "@material-ui/core/CircularProgress";

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
  }
});

class ProductIngestWithMetaFile extends Component {
  constructor(props) {
    super(props);
    this.handleFile = this.handleFile.bind(this);
    this.handleMetaFile = this.handleMetaFile.bind(this);
    this.ingestProduct = this.ingestProduct.bind(this);
  }

  state = {
    ingestedFile: null,
    metaFile: null,
    productId: "",
    isIngested: false,
    isIngestButtonClicked: false
  };

  handleFile(e) {
    let file = e.target.files[0];
    this.setState({ ingestedFile: file });
  }

  handleMetaFile(e) {
    let metaFile = e.target.files[0];
    this.setState({ metaFile: metaFile });
  }

  ingestProduct() {
    this.setState({ isIngested: false });
    this.setState({ isIngestButtonClicked: true });

    let product = this.state.ingestedFile;
    let metaProduct = this.state.metaFile;
    let formData = new FormData();
    formData.append("productFile", product);
    formData.append("metadataFile", metaProduct);
    fmconnection
      .post("productWithMeta", formData)
      .then(result => {
        console.log(result);
        this.setState({ productId: result.data });
        this.setState({ isIngested: true });
        alert("Sucessfully Ingested :ProductId " + result.data);
      })
      .catch(error => {
        console.log(error);
        this.setState({ isIngested: false });
        alert("Exception Thrown " + error);
      });
  }

  render() {
    const { classes } = this.props;

    return (
      <Paper className={classes.root}>
        <Typography variant="h5" component="h3">
          Product Ingest with Meta File
        </Typography>

        <div className={classes.layout}>
          <div>
            <div>
              <div>
                <label>File</label>
                <input
                  className={classes.button}
                  type={"file"}
                  name={"fileToUpload"}
                  id={"fileToUpload"}
                  onChange={e => this.handleFile(e)}
                />
                <label>MetadataFile</label>
                <input
                  className={classes.button}
                  type={"file"}
                  name={"metaFileToUpload"}
                  id={"metaFileToUpload"}
                  onChange={e => this.handleMetaFile(e)}
                />
              </div>
            </div>
          </div>

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
            alert("Sucessfully Ingested :ProductId  :" + this.state.productId)}
        </div>
      </Paper>
    );
  }
}

ProductIngestWithMetaFile.propTypes = {
  classes: PropTypes.object.isRequired
};

export default withStyles(styles)(ProductIngestWithMetaFile);
