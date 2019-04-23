import React, {Component} from "react";
import Card from '@material-ui/core/Card';
import CardContent from '@material-ui/core/CardContent';
import Button from '@material-ui/core/Button';
import Typography from '@material-ui/core/Typography';
import {withStyles} from "@material-ui/core";
import PropTypes from "prop-types";
import {fmconnection} from "../../constants/fmconnection";
import Grid from "@material-ui/core/Grid";
import Fab from "@material-ui/core/Fab";

const styles = theme => ({
    root: {
        flexGrow: 1,
        backgroundColor: theme.palette.background.paper,
    },
    card: {
        minWidth: 275,
        backgroundColor: "#dcdcdc",

    },
    bullet: {
        display: 'inline-block',
        margin: '0 2px',
        transform: 'scale(0.8)',
    },
    title: {
        fontSize: 14,

    },
    pos: {
        marginBottom: 12,
    },
});

class Product extends Component {


    constructor(props) {
        super(props);
    }

    state = {
        productData: [],
        productMetaDataFileLocation:[],
        productMetaDataFileType:[],
        productMetaDataMimeType:[],
        productReferencesData: [],
        productId : this.props.productId,
    };

    render() {

        const { classes } = this.props;
        return(
            <div className={classes.root}>
            <Card className={classes.card}>

            <Grid>
            <Grid container spacing={24}>
            <Grid item xs={3}>
            <CardContent>
            <Typography variant="h4" color="textPrimary" gutterBottom>
        FILE INFO
        </Typography>
        <Typography variant="h6" color="textPrimary" gutterBottom>
        FILE NAME : <strong>{this.state.productData["name"]}</strong>
        </Typography>
        <Typography variant="h6" color="textPrimary" gutterBottom>
        PRODUCT ID : <strong>{this.state.productData["id"]}</strong>
        </Typography>
        <Typography variant="h6" color="textPrimary" gutterBottom>
        STRUCTURE : <strong>{this.state.productData["structure"]}</strong>
        </Typography>
        <Typography variant="h6" color="textPrimary" gutterBottom>
        TRANSFER STATUS : <strong>{this.state.productData["transferStatus"]}</strong>
        </Typography>
        </CardContent>
        </Grid>

        <Grid item xs={3}>
            <CardContent>
            <Typography variant="h4" color="textPrimary" gutterBottom>
        <span style={{color:"black"}}>METADATA INFO</span>
        </Typography>
        <Typography variant="h6" color="textPrimary" gutterBottom>
        <span style={{color:"black"}}>FILE LOCATION</span> : <strong>{this.state.productMetaDataFileLocation["val"]}</strong>
        </Typography>
        <Typography variant="h6" color="textPrimary" gutterBottom>
        PRODUCT TYPE : <strong>{this.state.productMetaDataFileType["val"]}</strong>
        </Typography>
        <Typography variant="h6" color="textPrimary" gutterBottom>
        MIME TYPE : <strong>{this.state.productMetaDataMimeType[0]}</strong>
        </Typography>
        </CardContent>
        <Button style={{background:"green"}}><span style={{color:"white"}}>View All Metadata</span></Button>
        </Grid>

        <Grid item xs={3}>
            <CardContent>
            <Typography variant="h4" color="textPrimary" gutterBottom>
        REFERENCE INFO
        </Typography>
        <Typography variant="h6" color="textPrimary" gutterBottom>
        DATA STORE REF LOCATION : <strong>{this.state.productReferencesData["dataStoreReference"]}</strong>
        </Typography>
        <Typography variant="h6" color="textPrimary" gutterBottom>
        FILE SIZE : <strong>{this.state.productReferencesData["fileSize"]}</strong>
        </Typography>
        <Typography variant="h6" color="textPrimary" gutterBottom>
        ORIGINAL REFERENCE : <strong>{this.state.productReferencesData["originalReference"]}</strong>
        </Typography>
        </CardContent>
        <Button style={{background:"green"}}><span style={{color:"white"}}>View All References</span></Button>
        </Grid>

        <Grid item xs={3}>
            <CardContent>
            <Typography variant="h6" color="textPrimary" gutterBottom>
        <Fab  variant="extended" color="primary" style={{fontSize:20}}>Download File</Fab>
        </Typography>
        <Typography variant="h6" color="textPrimary" gutterBottom>
        <Fab variant="extended" color="primary" style={{fontSize:20 ,background:"green"}}>View in File Manager</Fab>
        </Typography>
        <Typography variant="h6" color="textPrimary" gutterBottom>
        <Fab variant="extended" color="secondary" style={{fontSize:20}}>Remove Record</Fab>
        </Typography>
        </CardContent>
        </Grid>
        </Grid>
        </Grid>


        </Card>
        </div>
    );
    }

    componentDidMount() {
        console.log(this.state.productId);
        // Make a request for a product with a given ID
        fmconnection.get("/product?productId=" +this.state.productId)

            .then(res => {
                this.setState({
                    productData:res.data.product,
                    productMetaDataFileLocation:Object.values(res.data.product.metadata)[0][0],
                    productMetaDataFileType:Object.values(res.data.product.metadata)[0][1],
                    productMetaDataMimeType:Object.values(res.data.product.metadata)[0][7]["val"],
                    productReferencesData:res.data.product.references.reference,
                });
                console.log(this.state.productMetaDataFileLocation["val"]);
                console.log(this.state.productMetaDataFileType["val"]);
                console.log(this.state.productMetaDataMimeType[0]);

            })
            .catch(err=>{
                console.log(err);
            })
    }
}

Product.propTypes = {
    classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(Product);
