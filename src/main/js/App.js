import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';

import 'font-awesome/scss/font-awesome.scss';

import MainPage from './MainPage';
import LoginPage from './LoginPage';
import { getConfig } from './store/actions/config';
import { verifyLogin } from './store/actions/session';

import '../css/main.scss';

class App extends Component {
    componentDidMount() {
        this.props.getConfig();
        this.props.verifyLogin();
    }

    render() {
        if (this.props.initializing) {
            return (<div />);
        } else if (this.props.loggedIn) {
            return <MainPage />;
        }
        return <LoginPage />;
    }
}

App.propTypes = {
    loggedIn: PropTypes.bool.isRequired,
    initializing: PropTypes.bool.isRequired,
    getConfig: PropTypes.func.isRequired,
    verifyLogin: PropTypes.func.isRequired
};

// Redux mappings to be used later....
const mapStateToProps = state => ({
    loggedIn: state.session.loggedIn,
    initializing: state.session.initializing
});

const mapDispatchToProps = dispatch => ({
    getConfig: () => dispatch(getConfig()),
    verifyLogin: () => dispatch(verifyLogin())
});

export default withRouter(connect(mapStateToProps, mapDispatchToProps)(App));
