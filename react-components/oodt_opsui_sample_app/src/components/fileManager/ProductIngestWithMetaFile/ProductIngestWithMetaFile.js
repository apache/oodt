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
import * as fmservice from "services/fmservice"
import ProgressBar from "components/ProgressBar"
import {shrinkString} from "utils/utils"
import { withSnackbar } from 'notistack';

const styles = theme => ({
  root: {
    flexGrow: 1,
    backgroundColor: theme.palette.background.paper,
    padding: 20
  },
  filename: {
    width: "20%",
    whiteSpace: "nowrap",
    overflow: "hidden",
    textAlign: "left",
    textOverflow: "ellipsis",
  },
  textField: {
    marginLeft: 2,
    marginRight: 2
  },
  layout: {
    display: "flex",
    flexDirection: "row",
    padding: 0,
    width: "100%",
    justifyContent: "space-between",
    alignItems: "flex-end",
  },
  titleContainer: {
    display: "flex",
    flexDirection: "row",
    justifyContent: "space-between",
  },
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
    ingestedFileName: "",
    metaFile: null,
    metaFileName: "",
    productId: "",
    isIngested: false,
    isIngestButtonClicked: false,
    ingestedPercentage: 0
  };

  handleFile(e) {
    let file = e.target.files[0];
    this.setState({ ingestedFile: file,ingestedFileName: shrinkString(file.name,25,6) });
  }

  handleMetaFile(e) {
    let metaFile = e.target.files[0];
    this.setState({ metaFile: metaFile,metaFileName: shrinkString(metaFile.name,25,6) });
  }

  ingestProduct() {
    this.setState({ isIngested: false,isIngestButtonClicked: true });
    if (!this.state.ingestedFile) {
      this.props.enqueueSnackbar("No product selected",{
        variant: "error"
      })
      this.setState({isIngestButtonClicked: false})
      return;
    }
    if(!this.state.metaFile){
      this.props.enqueueSnackbar("No metadata file selected",{
        variant: "error"
      })
      this.setState({isIngestButtonClicked: false})
      return;
    }
    let formData = new FormData();
    formData.append("productFile", this.state.ingestedFile);
    formData.append("metadataFile", this.state.metaFile);

    let onUploadProgress = (progressEvent) => {
      let percentCompleted = Math.round(
        (progressEvent.loaded * 100) / progressEvent.total
      );
      this.setState({ ingestedPercentage: percentCompleted });
    };

    fmservice
      .ingestProductWithMetaFile(formData, onUploadProgress)
      .then((res) => {
        const { productId } = res;
        this.setState({
          isIngested: true,
          productId: productId,
          ingestedFile: null,
          metaFile: null,
          ingestedFileName: "",
          metaFileName: "",
          isIngestButtonClicked: false,
        });
        this.props.enqueueSnackbar("Sucessfully Ingested product "+productId,{
          variant: "success"
        })
      })
      .catch((error) => {
        console.error(error);
        this.setState({ isIngested: false, isIngestButtonClicked: false });
        this.props.enqueueSnackbar("Error while ingesting product with metadata",{
          variant: "error"
        })
      });
  }

  render() {
    const { classes } = this.props;

    return (
      <Paper className={classes.root}>
        <div className={classes.titleContainer}>
          <Typography variant="subtitle1">
            <strong>Product Ingest with Meta File</strong>
          </Typography>
          {this.state.isIngestButtonClicked && (
            <ProgressBar value={this.state.ingestedPercentage} />
          )}
        </div>
        <br />
        <div className={classes.layout}>
          <div>
            <Typography variant="body1">File</Typography>
            <Button variant="contained" component="label">
              Upload File
              <input
                type="file"
                name={"fileToUpload"}
                id={"fileToUpload"}
                onChange={(e) => this.handleFile(e)}
                hidden
              />
            </Button>
          </div>
          <Typography variant="body1" className={classes.filename}>
            {this.state.ingestedFileName}
          </Typography>

          <div>
            <Typography variant="body1">Metadata File</Typography>
            <Button variant="contained" component="label">
              Select Metadata File
              <input
                type="file"
                name={"metaFileToUpload"}
                id={"metaFileToUpload"}
                onChange={(e) => this.handleMetaFile(e)}
                hidden
              />
            </Button>
          </div>

          <Typography variant="body1" className={classes.filename}>
            {this.state.metaFileName}
          </Typography>

          <div>
            <Button
              disabled={this.state.isIngestButtonClicked}
              variant="contained"
              color="primary"
              onClick={this.ingestProduct}
            >
              Ingest Product with metadata
            </Button>
          </div>
        </div>
      </Paper>
    );
  }
}

ProductIngestWithMetaFile.propTypes = {
  classes: PropTypes.object.isRequired
};

export default withStyles(styles)(withSnackbar(ProductIngestWithMetaFile));
