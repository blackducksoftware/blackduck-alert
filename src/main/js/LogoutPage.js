import React, { Component } from "react";
import Header from "./LoginPage";
import PropTypes from "prop-types";
import { logout } from "./store/actions/session";
import { connect } from "react-redux";

class LogoutPage extends Component {
    constructor(props) {
        super(props);
    }

    componentDidUpdate(prevProps) {
        this.props.logout();
    }

    render() {
        return (
            <div className="wrapper">
                <div className="loginContainer">
                    <div className="loginBox">
                        <Header />
                        <div>
                            {this.props.errorMessage &&
                            <div className="alert alert-danger">
                                <p name="configurationMessage">{this.props.errorMessage}</p>
                            </div>
                            }
                            <div className="row">
                                <div className="col-sm-12 text-right">
                                    <p>You've successfully logged out of Alert! Please close your browser to complete logout</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}

LogoutPage.propTypes = {
    logout: PropTypes.func.isRequired,
    errorMessage: PropTypes.string
};

LogoutPage.defaultProps = {
    errorMessage: ''
};

// Redux mappings to be used later....
const mapStateToProps = state => ({});

const mapDispatchToProps = dispatch => ({
    logout: () => dispatch(logout())
});

export default connect(mapStateToProps, mapDispatchToProps)(LogoutPage);
