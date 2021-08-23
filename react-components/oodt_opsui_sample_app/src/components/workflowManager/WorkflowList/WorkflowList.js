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
import { Button, withStyles, TablePagination, Typography, Grid } from "@material-ui/core";
import Table from "@material-ui/core/Table";
import TableBody from "@material-ui/core/TableBody";
import TableCell from "@material-ui/core/TableCell";
import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import Paper from "@material-ui/core/Paper";
import PropTypes from "prop-types";
import * as wmservice from "services/wmservice"
import {NewWorkflow} from "components/workflowManager/NewWorkflow"
import CircularProgress from "@material-ui/core/CircularProgress";

const styles = theme => ({
  root: {
    width: "100%",
    marginTop: 0,
    padding: 20
  },
  table: {
    minWidth: 650
  },
  progress: {
    margin: 2
  },
  loading: {
    display: "flex",
    width: "100%",
    justifyContent: "center"
  }
});

class WorkflowList extends Component {
  state = {
    rows: [],
    currentPage: 1,
    totalPages: 0,
    totalCount: 0,
    workflowState: [],
    noWorkflowsText: "Loading..."
  };

  componentDidMount() {
    this.loadNextWorkflows()
  }

  loadNextWorkflows = () => {
    wmservice.getWorkflowList(this.state.currentPage).then(
      workflowData => {
        let workflowArr = workflowData.pageWorkflows
        if (typeof(workflowArr) === "undefined"){
          this.setState({noWorkflowsText: "No workflows found"})
          return;
        }
        // Backend returns a product object when the object count is 1
        // and returns an array of products when the object count is more than 1.
        // This check converts object to array to avoid this problem
        // Need to fix this in the backend
        if (!Array.isArray(workflowArr)) {
          workflowArr = [workflowArr]
        }
        this.setState({
          rows: workflowArr || [],
          totalPages: workflowData.totalPages,
          totalCount: workflowData.totalCount
        })
      }
    ).catch(err => {
      console.error(err)
    })
  }

  updateWorkflowStatus = (workflowInstanceId, state) => {
    let result = window.confirm("Are you sure to change workflow " + workflowInstanceId + " state to " + state);
    if(result){
      wmservice.updateWorkflowStatus(workflowInstanceId, state)
      .then((result) => {
        wmservice.getWorkflowList().then(
          workflows => {
            this.setState({rows: workflows})
          }
        ).catch(err => {
          console.error(err)
        })
      })
      .catch((err) => console.error(err));
    }
  }

  handleChangePage = (event,newPageNo) => {
    this.setState({currentPage: newPageNo+1},() => this.loadNextWorkflows())
  }

  render() {
    const { classes } = this.props;
    return (
      <Paper className={classes.root}>
        <Typography variant="h6">New workflow</Typography>
        <NewWorkflow />
        
        <Typography variant="h6">Past workflows</Typography>
        <Table className={classes.table}>
          <TableHead>
            <TableRow>
              <TableCell><strong>Workflow Instance Id</strong></TableCell>
              <TableCell align="center"><strong>Workflow Name</strong></TableCell>
              <TableCell align="center"><strong>Task Id</strong></TableCell>
              <TableCell align="center"><strong>Workflow State</strong></TableCell>
              <TableCell align="center"><strong>Actions</strong></TableCell>
            </TableRow>
          </TableHead>

            <TableBody>
              {this.state.rows.length > 0 && this.state.rows.map(row => (
                <TableRow key={row.workflowInstanceId}>
                  <TableCell component="th" scope="row">
                    {row.workflowInstanceId}
                  </TableCell>
                  <TableCell align="right">
                    {row.sharedContext.keyval[0].val}
                  </TableCell>
                  <TableCell align="right">
                    {row.sharedContext.keyval[1].val}
                  </TableCell>
                  <TableCell align="right">{row.workflowState.name}</TableCell>
                  <TableCell align="center">
                    <div>
                      {row.workflowState.name === "Running" && (
                        <Button
                          color={"primary"}
                          onClick={() =>
                            this.updateWorkflowStatus(
                              row.workflowInstanceId,
                              "PAUSED"
                            )
                          }
                        >
                          Pause
                        </Button>
                      )}
                      {row.workflowState.name === "Running" && (
                        <Button
                          color={"secondary"}
                          onClick={() =>
                            this.updateWorkflowStatus(
                              row.workflowInstanceId,
                              "FINISHED"
                            )
                          }
                        >
                          Stop
                        </Button>
                      )}
                      {row.workflowState.name === "PAUSED" && (
                        <Button
                          color={"secondary"}
                          onClick={() =>
                            this.updateWorkflowStatus(
                              row.workflowInstanceId,
                              "Running"
                            )
                          }
                        >
                          Resume
                        </Button>
                      )}
                    </div>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
        </Table>
        {this.state.rows.length === 0 && (
                <div className={classes.loading}>
                  <CircularProgress className={classes.progress} />
                </div>                  
          )}
                  <Grid
            style={{ width: "100%" }}
            container
            spacing={1}
            direction="column"
            alignItems="center"
          >
            <Grid item>
              <TablePagination
                component="div"
                count={this.state.totalCount }
                page={this.state.currentPage - 1}
                onChangePage={this.handleChangePage}
                rowsPerPage={20}
                rowsPerPageOptions={[]}
              />
            </Grid>
          </Grid>
      </Paper>
    );
  }
}

WorkflowList.propTypes = {
  classes: PropTypes.object.isRequired
};

export default withStyles(styles)(WorkflowList);
