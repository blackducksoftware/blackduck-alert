import React, { Component } from 'react';
import Header from 'common/Header';
import { connect } from 'react-redux';

class LogoutPage extends Component {
    constructor(props) {
        super(props);
    }

    render() {
        return (
            <div className="wrapper">
                <div className="loginContainer">
                    <div className="loginBox">
                        <Header />
                        <div className="form-horizontal loginForm">
                            <div className="row">
                                <div className="col-sm-12 text-center">
                                    <div className="d-inline-flex flex-column p-2">You've successfully logged out of Alert!</div>
                                    <div className="d-inline-flex flex-column p-2">Please close your browser to complete logout.</div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}

LogoutPage.propTypes = {};

LogoutPage.defaultProps = {};

// Redux mappings to be used later....
const mapStateToProps = () => ({});

const mapDispatchToProps = () => ({});

export default connect(mapStateToProps, mapDispatchToProps)(LogoutPage);
