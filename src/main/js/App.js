import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
import {withRouter} from 'react-router-dom';

import 'font-awesome/scss/font-awesome.scss';

import MainPage from './MainPage';
import LoginPage from './LoginPage';
import AboutInfoFooter from './AboutInfoFooter';
import {getConfig} from './store/actions/config';
import {verifyLogin} from './store/actions/session';
import {getCurrentSystemSetup} from './store/actions/system';

import '../css/main.scss';
import SetupPage from "./SetupPage";

class App extends Component {
    componentDidMount() {
        this.props.getConfig();
        this.props.verifyLogin();
        this.props.getCurrentSetup();
    }

    render() {
        if (this.props.initializing) {
            return (<div/>);
        } else {
            const contentPage = (this.props.systemSetupAttempted) ? ((this.props.loggedIn) ? <MainPage/> : <LoginPage/>) : <SetupPage/>;
            return (
                <div>
                    {contentPage}
                    <AboutInfoFooter/>
                </div>
            );
        }
    }
}

App.propTypes = {
    loggedIn: PropTypes.bool.isRequired,
    initializing: PropTypes.bool.isRequired,
    getConfig: PropTypes.func.isRequired,
    verifyLogin: PropTypes.func.isRequired,
    getCurrentSetup: PropTypes.func.isRequired,
    systemSetupAttempted: PropTypes.bool.isRequired
};

// Redux mappings to be used later....
const mapStateToProps = state => ({
    loggedIn: state.session.loggedIn,
    initializing: state.session.initializing,
    systemSetupAttempted: state.system.setupRedirect
});

const mapDispatchToProps = dispatch => ({
    getConfig: () => dispatch(getConfig()),
    verifyLogin: () => dispatch(verifyLogin()),
    getCurrentSetup: () => dispatch(getCurrentSystemSetup())
});

export default withRouter(connect(mapStateToProps, mapDispatchToProps)(App));
