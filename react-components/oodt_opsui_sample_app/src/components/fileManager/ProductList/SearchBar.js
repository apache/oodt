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
import { OutlinedInput,Button } from "@material-ui/core";
import Select from "@material-ui/core/Select";
import FormControl from "@material-ui/core/FormControl";
import MenuItem from "@material-ui/core/MenuItem";
import InputLabel from "@material-ui/core/InputLabel";
import SearchIcon from "@material-ui/icons/Search";
import { withStyles } from "@material-ui/core";
import PropTypes from "prop-types";

const styles = theme => ({
  root: {
    padding: "2px 4px",
    display: "flex",
    alignItems: "center",
    width: 400
  },
  row: {
    width: "100%",
    display: "flex",
    flexDirection: "row",
    justifyContent: "space-between"
  },
  formControl: {
    margin: 1,
    minWidth: 120
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
  }
});

class SearchBar extends Component {
  constructor(props) {
    super(props);
  }

  state = {
    selectedProductType: "",
    selectedProductStructure: "FLAT",
    selectedMIMEType: "",
    selectedTransferStatus: "RECEIVED",
    isSearching: false
  };

  componentDidMount(){
    if(this.props) {
      this.setState({selectedProductType: this.props.availableProductTypes[0]})
    }
  }

  componentDidUpdate(prevProps){
    if(prevProps !== this.props){
      this.setState({selectedProductType: this.props.availableProductTypes[0]})
    }
  }

  handleChange = (e) => {
    this.setState({ [e.target.name]: e.target.value });
  }

  onSearch = () => {
    setTimeout(() => {
        this.props.onQueryTimeout({ isQueryTimedOut: true });
    },3000);
    this.setState({isSearching: true})
    this.props.onSearch()
  }

  render() {
    const { classes } = this.props;
    return (
      <div className={classes.row}>
        <FormControl
          variant="outlined"
          className={classes.formControl}
          style={{ width: "200px" }}
        >
          <InputLabel htmlFor="outlined-product-Type-simple">
            Product Type
          </InputLabel>
          <Select
            value={this.state.selectedProductType}
            onChange={this.handleChange}
            input={
              <OutlinedInput
                labelWidth={100}
                name="selectedProductType"
                id="outlined-product-Type-simple"
              />
            }
          >
            {
              this.props.availableProductTypes.map((productType,index) => (
                <MenuItem selected={index === 0} value={productType}>
                  {productType}
                </MenuItem>
              ))
            } 
          </Select>
        </FormControl>

        
        <FormControl
          variant="outlined"
          className={classes.formControl}
          style={{ width: "200px" }}
        >
          <InputLabel htmlFor="outlined-product-Type-simple">
            Structure
          </InputLabel>
          <Select
           value={this.state.selectedProductStructure}
            onChange={this.handleChange}
            input={
              <OutlinedInput
                labelWidth={100}
                name="selectedProductStructure"
                id="outlined-product-structure-simple"
              />
            }
          >
            <MenuItem value="FLAT">
            Flat
          </MenuItem>
          <MenuItem value="HIERARCHICAL">
            Hierarchical
          </MenuItem>
          </Select>
        </FormControl>

        <FormControl
          variant="outlined"
          className={classes.formControl}
          style={{ width: "200px" }}
        >
          <InputLabel htmlFor="outlined-product-Type-simple">
            MIME type
          </InputLabel>
          <Select
            value={this.state.selectedMIMEType}
            onChange={this.handleChange}
            input={
              <OutlinedInput
                labelWidth={100}
                name="selectedMIMEType"
                id="outlined-mime-type-simple"
              />
            }
          >
            {this.props && this.props.availableMIMETypes.map(MIMEType => (
            <MenuItem selected={true} value={MIMEType}>
            {MIMEType}
          </MenuItem>
            ))}
          </Select>
        </FormControl>

        <FormControl
          variant="outlined"
          className={classes.formControl}
          style={{ width: "200px" }}
        >
          <InputLabel htmlFor="outlined-product-Type-simple">
            Transfer status
          </InputLabel>
          <Select
            value={this.state.selectedTransferStatus}
            onChange={this.handleChange}
            input={
              <OutlinedInput
                labelWidth={100}
                name="selectedTransferStatus"
                id="outlined-transfer-status-simple"
              />
            }
          >
            <MenuItem value="TRANSFERING">
            Tranfering
            </MenuItem>
            <MenuItem selected={true} value="RECEIVED">
              Received
            </MenuItem>
          </Select>
        </FormControl>


        <Button
          variant="contained"
          size="medium"
          color="primary"
          onClick={this.onSearch}
        >
          <SearchIcon />
        </Button>
      </div>
    );
  }
}

SearchBar.propTypes = {
  classes: PropTypes.object.isRequired,
  onSearch: PropTypes.func.isRequired,
  onQueryTimeout:PropTypes.func,
  availableProductTypes: PropTypes.array.isRequired,
  availableMIMETypes: PropTypes.array.isRequired,
};

export default withStyles(styles)(SearchBar);
