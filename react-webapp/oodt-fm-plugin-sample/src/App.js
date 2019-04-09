import React, { Component } from 'react';
import AppBar from "@material-ui/core/AppBar";
import Toolbar from "@material-ui/core/Toolbar";
import Typography from "@material-ui/core/Typography";
import Grid from "@material-ui/core/Grid";
import OperationTab from "./components/OperationTab";

class App extends Component {
  render() {
    return (
      <div className="App">
          <AppBar position="static" color="default">
          <Toolbar>
          <img src="/Images/oodt_asf_logo_med.png" width={150} height={70}  alt={"OODT_Logo"} style={{padding:22}}/>
      <Typography variant="h3" color="inherit" style={{marginRight:20}}>
      OPS UI - FILE MANAGER </Typography>
      </Toolbar>
      </AppBar>

      <Grid>
      <Grid container spacing={24}>
          <Grid item xs={12}>
          <OperationTab/>
          </Grid>
          </Grid>
          </Grid>
      </div>
    );
  }
}

export default App;
