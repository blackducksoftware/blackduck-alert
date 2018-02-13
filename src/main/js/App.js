import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';

import MainPage from './MainPage';
import LoginPage from './LoginPage';
import { getConfig } from './actions/config';
import { verifyLogin } from "./actions/session";

class App extends Component {

	componentDidMount() {
		this.props.getConfig();
        this.props.verifyLogin();
	}

	render() {
		return this.props.loggedIn ? <MainPage /> : <LoginPage />;
	}
}

App.propTypes = {
	loggedIn: PropTypes.bool.isRequired,
	getConfig: PropTypes.func.isRequired,
    verifyLogin: PropTypes.func.isRequired
};

// Redux mappings to be used later....
const mapStateToProps = state => ({
    loggedIn: state.session.loggedIn
});

const mapDispatchToProps = dispatch => ({
    getConfig: () => dispatch(getConfig()),
	verifyLogin: () => dispatch(verifyLogin())
});

export default withRouter(connect(mapStateToProps, mapDispatchToProps)(App));
