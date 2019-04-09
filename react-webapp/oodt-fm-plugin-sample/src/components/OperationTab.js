import React , {Component} from 'react' ;
import PropTypes from 'prop-types';
import {withStyles} from '@material-ui/core/styles';
import AppBar from "@material-ui/core/AppBar";
import Typography from "@material-ui/core/Typography";
import Tab from "@material-ui/core/Tab";
import Tabs from "@material-ui/core/Tabs";
import PhoneIcon from '@material-ui/icons/Phone';
import FavoriteIcon from '@material-ui/icons/Favorite';
import PersonPinIcon from '@material-ui/icons/PersonPin';
import HelpIcon from '@material-ui/icons/Help';
import Product from "./product";

const styles = theme => ({
    root: {
        flexGrow: 1,
        backgroundColor: theme.palette.background.paper,
    },
});

function TabContainer(props) {
    return (
        <Typography component="div" style={{ padding: 8 * 3 }}>
    {props.children}
</Typography>
);
}

TabContainer.propTypes = {
    children: PropTypes.node.isRequired,
};

class OperationTab extends Component {

    state = {
        value: 0,
    };

    handleChange = (event, value) => {
    this.setState({ value });
};

render(){
    const {classes} = this.props;
    const { value } = this.state;

    return (
        <div className={classes.root}>
        <AppBar position="static" color="default">
        <Tabs
    value={value}
    onChange={this.handleChange}
    variant="scrollable"
    scrollButtons="on"
    indicatorColor="primary"
    textColor="primary"
        >
        <Tab label="File List" icon={<PhoneIcon />} />
    <Tab label="Ingest Files" icon={<FavoriteIcon />} />
    <Tab label="Query Files" icon={<PersonPinIcon />} />
    <Tab label="Remove Files" icon={<HelpIcon />} />
    </Tabs>
    </AppBar>
    {value === 0 && <TabContainer>
    <Product productId="ce4380c5-d0d2-11e8-89ca-971c29fc9f21    "/>
        </TabContainer>}
        {value === 1 && <TabContainer>Ingest Files</TabContainer>}
            {value === 2 && <TabContainer>Query Files</TabContainer>}
                {value === 3 && <TabContainer>Remove Files</TabContainer>}

                </div>
                );
}


}
OperationTab.propTypes = {
    classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(OperationTab);
