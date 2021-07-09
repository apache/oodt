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
import { Button, withStyles } from "@material-ui/core";
import Table from "@material-ui/core/Table";
import TableBody from "@material-ui/core/TableBody";
import TableCell from "@material-ui/core/TableCell";
import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import Paper from "@material-ui/core/Paper";
import PropTypes from "prop-types";
import { wmconnection } from "constants/connection";
import CircularProgress from "@material-ui/core/CircularProgress";

const styles = theme => ({
  root: {
    width: "100%",
    marginTop: 0,
    overflowX: "auto"
  },
  table: {
    minWidth: 650
  },
  progress: {
    margin: 2
  },
  loading: {
    textAlign: "center",
    display: "block",
    position: "relative"
  }
});

class WorkflowList extends Component {
  constructor(props) {
    super(props);
    this.updateWorkflowStatus = this.updateWorkflowStatus.bind(this);
    this.reloadWorkflow = this.reloadWorkflow.bind(this);
  }

  componentWillMount() {
    wmconnection
      .get("/workflows/firstpage")
      .then(result => {
        this.setState({
          rows: result.data.workflowPageInstance.pageWorkflows
        });
        // console.log(this.state.rows)
        console.log(this.state.rows[0].sharedContext.keyval[0].val);
      })
      .catch(error => {
        console.log(error);
      });
  }

  updateWorkflowStatus(workflowInstanceId, state) {
    let result = window.confirm(
      "Are you sure to change workflow " +
        workflowInstanceId +
        " state to " +
        state
    );
    if (result) {
      wmconnection
        .post(
          "/updatestatus/workflow?workflowInstanceId=" +
            workflowInstanceId +
            "&status=" +
            state
        )
        .then(result => {
          console.log("Success");
          this.reloadWorkflow();
        })
        .catch(error => {
          console.log(error);
        });
    } else {
    }
  }

  reloadWorkflow() {
    wmconnection
      .get("/workflows/firstpage")
      .then(result => {
        this.setState({
          rows: result.data.workflowPageInstance.pageWorkflows
        });
        // console.log(this.state.rows)
        console.log(this.state.rows[0].sharedContext.keyval[0].val);
      })
      .catch(error => {
        console.log(error);
      });
  }

  state = {
    rows: [],
    workflowState: []
  };

  render() {
    const { classes } = this.props;
    return (
      <Paper className={classes.root}>
        <Table className={classes.table}>
          <TableHead>
            <TableRow>
              <TableCell>Workflow Instance Id</TableCell>
              <TableCell align="center">Workflow Name</TableCell>
              <TableCell align="center">Task Id</TableCell>
              <TableCell align="center">Workflow State</TableCell>
              <TableCell align="center">Actions</TableCell>
            </TableRow>
          </TableHead>

          {this.state.rows.length > 0 && (
            <TableBody>
              {this.state.rows.map(row => (
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
          )}
          {this.state.rows.length === 0 && (
            <TableBody className={classes.loading}>
              <CircularProgress className={classes.progress} />
            </TableBody>
          )}
        </Table>
      </Paper>
    );
  }
}

WorkflowList.propTypes = {
  classes: PropTypes.object.isRequired
};

export default withStyles(styles)(WorkflowList);
