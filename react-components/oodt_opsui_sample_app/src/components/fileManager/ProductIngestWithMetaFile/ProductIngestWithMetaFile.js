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
import { fmconnection } from "constants/connection";
import ProgressBar from "components/ProgressBar"

const styles = theme => ({
  root: {
    flexGrow: 1,
    backgroundColor: theme.palette.background.paper,
    padding: 20
  },
  button: {
    padding: 10,
    margin: 5
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
    isIngestButtonClicked: false,
    ingestedPercentage: 0
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
    this.setState({ isIngested: false,isIngestButtonClicked: true });

    let formData = new FormData();
    formData.append("productFile", this.state.ingestedFile);
    formData.append("metadataFile", this.state.metaFile);

    let config = {
      onUploadProgress: (progressEvent) => {
        let percentCompleted = Math.round( (progressEvent.loaded * 100) / progressEvent.total );
        this.setState({ingestedPercentage: percentCompleted})
      }
    };

    fmconnection
      .post("productWithMeta", formData, config)
      .then((result) => {
        this.setState({
          isIngested: true,
          productId: result.data,
          ingestedFile: null,
          metaFile: null,
          isIngestButtonClicked: false,
        });
        alert("Sucessfully Ingested :ProductId " + result.data);
      })
      .catch((error) => {
        console.error(error);
        this.setState({ isIngested: false, isIngestButtonClicked: false });
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
        <br />
        <div className={classes.layout}>
          
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


          <Button
            variant="contained"
            color="primary"
            className={classes.button}
            onClick={this.ingestProduct}
          >
            Ingest Product
          </Button>
          {this.state.isIngestButtonClicked && <ProgressBar value={this.state.ingestedPercentage} />}
        </div>
      </Paper>
    );
  }
}

ProductIngestWithMetaFile.propTypes = {
  classes: PropTypes.object.isRequired
};

export default withStyles(styles)(ProductIngestWithMetaFile);
