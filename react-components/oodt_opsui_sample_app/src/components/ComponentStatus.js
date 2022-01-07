/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import React, {Component} from "react";
import Grid from "@material-ui/core/Grid";
import Chip from "@material-ui/core/Chip";
import Typography from "@material-ui/core/Typography";
import {Paper, withStyles} from "@material-ui/core";
import {fmconnection,wmconnection} from "constants/connection"

const styles = theme => ({
  root: {
    flexGrow: 1,
  },
  paper: {
    padding: theme.spacing(2),
    textAlign: 'left'
  },
  grid: {
    padding: theme.spacing(2)
  },
  chip: {
    margin: theme.spacing(1),
    textAlign: 'center'
  },
  online: {
    color: 'white',
    backgroundColor: 'green'
  },
  offline: {
    color: 'white',
    backgroundColor: 'red'
  }
});

class ComponentStatus extends Component {

  state = {
    fmAvailable: false,
    wmAvailable: false
  };

  constructor(props) {
    super(props);
    this.handleChange = this.handleChange.bind(this);
  }

  componentDidMount() {
    this.handleChange();
  }

  handleChange() {
    fmconnection.get("/fmprodstatus")
    .then(result => {
      if (result.data.FMStatus.serverUp) {
        this.setState({fmAvailable: true});
      } else {
        this.setState({fmAvailable: false});
      }
    })
    .catch(error => {
      console.error("Unable to get file manager status", error);
    });

    wmconnection.get("/workflow/status")
    .then(result => {
      if (result.data.WorkflowManagerStatus.serverUp) {
        this.setState({wmAvailable: true});
      } else {
        this.setState({wmAvailable: false});
      }
    })
    .catch(error => {
      console.error("Unable to get workflow manager status", error);
    });
  };

  render() {
    const {classes} = this.props;

    return (
        <div className={classes.root}>
          <Grid container spacing={2}>
            <Grid item xs={2}/>
            <Grid item xs={8}>
              <Paper className={classes.paper}>
                <Grid container>
                  <Grid item xs={8}>
                    <Typography variant="h6">File Manager Status</Typography>
                  </Grid>
                  <Grid item xs={4} style={{textAlign: 'center'}}>
                    <Chip label={this.state.fmAvailable ? 'Online' : 'Offline'}
                          className={this.state.fmAvailable ? classes.online : classes.offline}/>
                  </Grid>
                </Grid>
              </Paper>
            </Grid>
            <Grid item xs={2}/>

            <Grid item xs={2}/>
            <Grid item xs={8}>
              <Paper className={classes.paper}>
                <Grid container>
                  <Grid item xs={8}>
                    <Typography variant="h6">Workflow Manager Status</Typography>
                  </Grid>
                  <Grid item xs={4} style={{textAlign: 'center'}}>
                    <Chip label={this.state.wmAvailable ? 'Online' : 'Offline'}
                          className={this.state.wmAvailable ? classes.online : classes.offline}/>
                  </Grid>
                </Grid>
              </Paper>
            </Grid>
            <Grid item xs={2}/>
          </Grid>
        </div>
    )
  }
}

export default withStyles(styles)(ComponentStatus);