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
import {
  MenuItem,
  InputLabel,
  Button,
  FormControl,
  withStyles,
  Select,
} from "@material-ui/core";
import * as wmservice from "services/wmservice";
import {withSnackbar} from "notistack"

const styles = (theme) => ({
  root: {
    paddingBottom: "2%",
    paddingTop: "1%",
    minHeight: "10vh",
    display: "flex",
    alignItems: "center",
  },
  formControl: {
    minWidth: "30%",
  },
  selectEmpty: {
    marginTop: theme.spacing(2),
  },
  button: {
    marginLeft: theme.spacing(1),
    padding: theme.spacing(1),
  },
});

class NewWorkflow extends Component {
  state = {
    selectedEvent: "",
    workflowEvents: [],
    isSubmitted: false,
  };

  componentDidMount() {
    wmservice
      .getRegisteredEvents()
      .then((eventsArr) => {
        this.setState({
          selectedEvent: eventsArr[0],
          workflowEvents: eventsArr || [],
        });
      })
      .catch((err) => console.error(err));
  }

  handleEventChange = (e) => {
    this.setState({ selectedEvent: e.target.value });
  };

  triggerWorkflow = () => {
    this.setState({isSubmitted: true})
    wmservice
      .handleEvent(this.state.selectedEvent)
      .then((isOk) => {
        this.setState({isSubmitted: false})
        this.props.enqueueSnackbar("Event " + this.state.selectedEvent +" triggered successfully",{
          variant: "success"
        })
        this.setState({isSubmitted: false})
      })
      .catch((err) => {
        console.error(err)
        this.props.enqueueSnackbar("Error occured when triggering event "+this.state.selectedEvent,{
          variant: "error"
        })
        this.setState({isSubmitted: false})
      });
  };

  render() {
    const { classes } = this.props;

    return (
      <div className={classes.root}>
        <FormControl variant="outlined" className={classes.formControl}>
          <InputLabel>Event name</InputLabel>
          <Select
            value={this.state.selectedEvent}
            onChange={this.handleEventChange}
            label="Workflow event"
          >
            {this.state.workflowEvents.map((event) => {
              return <MenuItem value={event}>{event}</MenuItem>;
            })}
          </Select>
        </FormControl>

        <Button
          variant="contained"
          onClick={this.triggerWorkflow}
          color="primary"
          disabled={this.state.isSubmitted}
          className={classes.button}
        >
          Trigger workflow
        </Button>
      </div>
    );
  }
}

export default withStyles(styles)(withSnackbar(NewWorkflow));