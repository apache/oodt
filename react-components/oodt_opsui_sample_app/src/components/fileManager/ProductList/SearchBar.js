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
import {Button,TextField } from "@material-ui/core";
import SearchIcon from "@material-ui/icons/Search";
import { withStyles } from "@material-ui/core";
import { fmconnection } from "constants/connection";
import PropTypes from "prop-types";

const styles = theme => ({
  root: {
    padding: "2px 4px",
    display: "flex",
    alignItems: "center",
    width: 400
  },
  row: {
    width: "60%",
    display: "flex",
    flexDirection: "row"
  },
  formControl: {
    minWidth: 180
  },
  input: {
    marginLeft: 8,
    flex: 1
  },
  iconButton: {
    padding: 10
  },
  divider: {
    width: 1,
    height: 28,
    margin: 4
  },
  button: {
    marginLeft: theme.spacing(1)
  }
});

class SearchBar extends Component {

  state = {
    productName: "",
    isSearching: false
  };

  componentDidMount(){
    fmconnection.get("/productTypes").then(res => {
      const {productTypes} = res.data.productTypeList 
      this.setState({
        productTypes: productTypes,
        productTypeName: productTypes[0].name})
    }).catch(err => console.error(err))
  }


  handleChange = (e) => {
    this.setState({ [e.target.name]: e.target.value });
  }

  onSearch = () => {
    setTimeout(() => {
        this.props.onQueryTimeout({ isQueryTimedOut: true });
    },3000);
    this.setState({isSearching: true})
    this.props.onSearch(this.state.productName)
  }

  render() {
    const { classes } = this.props;
    return (
      <div className={classes.row}>
        <div style={{width: "60%"}}>
        <TextField
          id="outlined-basic"
          label="Product name"
          fullWidth
          className={classes.formControl}
          value={this.state.productName}
          onChange={this.handleChange}
          name="productName"
          variant="outlined"
        />
        </div>
      
        <Button
          startIcon={<SearchIcon />}
          variant="contained"
          size="large"
          className={classes.button}
          onClick={this.onSearch}
          color="primary"
        >
          Search products
        </Button>
      </div>
    );
  }
}

SearchBar.propTypes = {
  classes: PropTypes.object.isRequired,
  onSearch: PropTypes.func.isRequired,
  onQueryTimeout:PropTypes.func,
};

export default withStyles(styles)(SearchBar);
