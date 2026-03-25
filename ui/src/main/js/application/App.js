import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';

import MainPage from 'application/MainPage';
import LoginPage from 'application/auth/LoginPage';
import Footer from 'application/Footer';
import { verifyLogin, verifySaml } from 'store/actions/session';
import { getAboutInfo } from 'store/actions/about';
import * as IconUtility from 'common/util/iconUtility';
import LogoutPage from 'application/auth/LogoutPage';
import SessionUnauthorizedPage from 'application/auth/SessionUnauthorizedPage';
// These are needed for the react-bootstrap tables to show the ascending/descending icons
import '@fortawesome/fontawesome-free/scss/fontawesome.scss';
import '@fortawesome/fontawesome-free/js/all.js';
import '@fortawesome/fontawesome-free/scss/v4-shims.scss';
import '@fortawesome/fontawesome-free/js/v4-shims.js';

import '../../css/main.scss';

IconUtility.loadIconData();

class App extends Component {
    componentDidMount() {
        const { verifyLogin: verifyLoginAction, verifySaml: verifySamlAction, getAboutInfo: getAboutInfoAction } = this.props;

        verifyLoginAction();
        verifySamlAction();
        getAboutInfoAction();
    }

    render() {
        const {
            initializing,
            logoutPerformed,
            sessionUnauthorizationPerformed,
            loggedIn
        } = this.props;

        if (initializing) {
            return (<div />);
        }

        if (logoutPerformed) {
            return <LogoutPage />;
        }

        if (sessionUnauthorizationPerformed) {
            return <SessionUnauthorizedPage />;
        }

        const contentPage = loggedIn ? <MainPage /> : <LoginPage />;

        return (
            <div>
                <div style={{ height: '96.5vh' }}>
                    {contentPage}
                </div>
                <div style={{ height: '3.5vh' }}>
                    <Footer />
                </div>
            </div>
        );
    }
}

App.propTypes = {
    loggedIn: PropTypes.bool.isRequired,
    logoutPerformed: PropTypes.bool.isRequired,
    sessionUnauthorizationPerformed: PropTypes.bool.isRequired,
    initializing: PropTypes.bool.isRequired,
    verifyLogin: PropTypes.func.isRequired,
    verifySaml: PropTypes.func.isRequired,
    getAboutInfo: PropTypes.func.isRequired
};

// Redux mappings to be used later....
const mapStateToProps = (state) => ({
    loggedIn: state.session.loggedIn,
    logoutPerformed: state.session.logoutPerformed,
    sessionUnauthorizationPerformed: state.session.sessionUnauthorizationPerformed,
    initializing: state.session.initializing
});

const mapDispatchToProps = (dispatch) => ({
    verifyLogin: () => dispatch(verifyLogin()),
    verifySaml: () => dispatch(verifySaml()),
    getAboutInfo: () => dispatch(getAboutInfo())
});

export default withRouter(connect(mapStateToProps, mapDispatchToProps)(App));
