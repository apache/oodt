import React from "react";
import Snackbar from "@material-ui/core/Snackbar";
import SnackbarContent from "@material-ui/core/SnackbarContent";
import Typography from "@material-ui/core/Typography";
import InfoIcon from "@material-ui/icons/Info"
import WarningIcon from "@material-ui/icons/Warning"
import SuccessIcon from "@material-ui/icons/CheckCircleOutline"
import IconButton from "@material-ui/core/IconButton";
import CloseIcon from "@material-ui/icons/Close";
import PropTypes from "prop-types";
import { withStyles } from "@material-ui/core";

const styles = theme => ({
  close: {
    padding: 10
  },
  info: {
    backgroundColor: "#0F8EF7",
  },
  warning: {
    backgroundColor: "#F0AD11"
  },
  success: {
    backgroundColor: "#41BE41"
  },
  error: {
    backgroundColor: "red"
  },
  snackContent: {
    display: "flex",
    height: "2vh",
    alignItems: "center",
  },
  icon: {
    paddingRight: "2%",
    fontSize: "1.5em"
  }
});

class SimpleSnackBar extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      open: false,
      message: "",
      severity: "" // one of "error","warning","info","success"
    };
    this.handleClose = this.handleClose.bind(this);
  }

  handleOpen = (message,severity) => {
    this.setState({ open: true, message: message, severity: severity });
  }

  handleClose(event, reason) {
    if (reason === "clickaway") {
      return;
    }
    this.setState({ open: false });
  }

  render() {
    const { classes } = this.props;
    
    const severityIconMap = {
      "info": <InfoIcon className={classes.icon}/>,
      "error": <InfoIcon className={classes.icon}/>,
      "warning": <WarningIcon className={classes.icon}/>,
      "success": <SuccessIcon className={classes.icon}/>,
    }

    return (
      <div>
        <Snackbar
          open={this.state.open}
          autoHideDuration={5000}
          onClose={this.handleClose}
          anchorOrigin={{
            vertical: "top",
            horizontal: "center"
          }}
        >
          <SnackbarContent
            className={classes[this.state.severity]}
           message={<div className={classes.snackContent}>
             {severityIconMap[this.state.severity]}
             <Typography variant="body1">
             {this.state.message}
        </Typography></div>}
           action={<IconButton
            key="close"
            aria-label="Close"
            color="inherit"
            className={classes.close}
            onClick={this.handleClose}
          >
            <CloseIcon />
          </IconButton>}
          />
        </Snackbar>
        {/* <Snackbar
          anchorOrigin={{
            vertical: "top",
            horizontal: "center"
          }}
          severity={this.state.severity}
          open={this.state.open}
          autoHideDuration={6000}
          onClose={this.handleClose}
          ContentProps={{
            "aria-describedby": "message-id"
          }}
          message={<span id="message-id">{this.state.message}</span>}
          action={[
            <IconButton
              key="close"
              aria-label="Close"
              color="inherit"
              className={classes.close}
              onClick={this.handleClose}
            >
              <CloseIcon />
            </IconButton>
          ]}
        /> */}
      </div>
    );
  }
}

SimpleSnackBar.propTypes = {
  classes: PropTypes.object.isRequired
};

export default withStyles(styles)(SimpleSnackBar);
