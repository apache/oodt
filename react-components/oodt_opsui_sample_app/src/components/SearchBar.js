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

import React, {Component} from 'react';
import Paper from '@material-ui/core/Paper';
import InputBase from '@material-ui/core/InputBase';
import IconButton from '@material-ui/core/IconButton';
import SearchIcon from '@material-ui/icons/Search';
import {withStyles} from "@material-ui/core";
import {PropTypes} from "prop-types";


const styles = theme => ({
    root: {
        padding: '2px 4px',
        display: 'flex',
        alignItems: 'center',
        width: 400,
    },
    input: {
        marginLeft: 8,
        flex: 1,
    },
    iconButton: {
        padding: 10,
    },
    divider: {
        width: 1,
        height: 28,
        margin: 4,
    },
});

class SearchBar extends Component {

    constructor(props) {
        super(props);
        this.handleChange = this.handleChange.bind(this);
        this.keyPress = this.keyPress.bind(this);
        // this.click = this.click.bind(this);
    }

    state = {
        selectedProductId: '',
    };

    handleChange(e) {
        this.setState({ selectedProductId: e.target.value });
    }

    keyPress(e){
        if(e.keyCode === 13){
            console.log(e.target.value);
            this.props.setSelectedProductId(this.state.selectedProductId);
            // this.click();
        }
    }

    // click() {
    //     this.props.loadProducts();
    // }

    render(){
        const { classes } = this.props;
        return (
            <Paper className={classes.root}>
                <InputBase
                    className={classes.input}
                    placeholder="Search Products by Product Id"
                    inputProps={{ 'aria-label': 'Search Products' }}
                    onKeyDown={this.keyPress} onChange={this.handleChange}
                />
                <IconButton className={classes.iconButton} aria-label="Search" >
                    <SearchIcon />
                </IconButton>
            </Paper>
        );
    }


}


SearchBar.propTypes = {
    classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(SearchBar);
