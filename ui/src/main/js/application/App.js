import PropTypes from 'prop-types';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';

import MainPage from 'application/MainPage';
import LoginPage from 'application/auth/LoginPage';
import LogoutPage from 'application/auth/LogoutPage';
import * as IconUtility from 'common/util/iconUtility';
import AboutInfoFooter from 'page/about/AboutInfoFooter';
import { verifyLogin, verifySaml } from 'store/actions/session';
// These are needed for the react-bootstrap tables to show the ascending/descending icons
import '@fortawesome/fontawesome-free/js/all.js';
import '@fortawesome/fontawesome-free/js/v4-shims.js';
import '@fortawesome/fontawesome-free/scss/fontawesome.scss';
import '@fortawesome/fontawesome-free/scss/v4-shims.scss';

import '../../css/main.scss';

IconUtility.loadIconData();

class App extends Component {
    componentDidMount() {
        this.props.verifyLogin();
        this.props.verifySaml();
    }

    render() {
        if (this.props.initializing) {
            return (<div />);
        }

        if (this.props.logoutPerformed) {
            return <LogoutPage />;
        }

        const contentPage = (this.props.loggedIn) ? <MainPage /> : <LoginPage />;

        return (
            <div>
                {contentPage}
                <div>
                    <AboutInfoFooter />
                </div>
            </div>
        );
    }
}

App.propTypes = {
    loggedIn: PropTypes.bool.isRequired,
    logoutPerformed: PropTypes.bool.isRequired,
    initializing: PropTypes.bool.isRequired,
    verifyLogin: PropTypes.func.isRequired,
    verifySaml: PropTypes.func.isRequired,
};

// Redux mappings to be used later....
const mapStateToProps = (state) => ({
    loggedIn: state.session.loggedIn,
    logoutPerformed: state.session.logoutPerformed,
    initializing: state.session.initializing,
});

const mapDispatchToProps = (dispatch) => ({
    verifyLogin: () => dispatch(verifyLogin()),
    verifySaml: () => dispatch(verifySaml())
});

export default withRouter(connect(mapStateToProps, mapDispatchToProps)(App));
