import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import PasswordInput from 'field/input/PasswordInput';
import TextInput from 'field/input/TextInput';
import SubmitButton from 'field/input/SubmitButton';
import Header from 'component/common/Header';
import { login } from 'store/actions/session';
import { hideResetModal, sendPasswordResetEmail, showResetModal } from "store/actions/system";
import ResetPasswordModal from "./component/common/ResetPasswordModal";

class LoginPage extends Component {
    constructor(props) {
        super(props);

        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    handleChange({ target }) {
        const value = target.type === 'checkbox' ? target.checked : target.value;
        this.setState({
            [target.name]: value
        });
    }

    handleSubmit(evt) {
        evt.preventDefault();
        const { blackDuckUsername, blackDuckPassword } = this.state;
        this.props.login(blackDuckUsername, blackDuckPassword);
    }

    render() {
        return (
            <div className="wrapper">
                <div className="loginContainer">
                    <div className="loginBox">
                        <Header />
                        <form method="POST" className="form-horizontal loginForm" onSubmit={this.handleSubmit}>
                            {this.props.errorMessage &&
                            <div className="alert alert-danger">
                                <p name="configurationMessage">{this.props.errorMessage}</p>
                            </div>
                            }

                            <TextInput
                                id="loginUsername"
                                label="Username"
                                name="blackDuckUsername"
                                onChange={this.handleChange}
                                errorName="usernameError"
                                autoFocus
                            />

                            <PasswordInput
                                id="loginPassword"
                                label="Password"
                                name="blackDuckPassword"
                                onChange={this.handleChange}
                                errorName="passwordError"
                            />

                            <div className="row">
                                <div className="col-sm-12 text-right">
                                    <a href="#"
                                       onClick={(evt) => {
                                           evt.preventDefault();
                                           this.props.showResetModal();
                                       }}
                                    >Reset Password</a>
                                    <span>&nbsp;&nbsp;&nbsp;</span>
                                    <SubmitButton id="loginSubmit">Login</SubmitButton>
                                    <div className="progressIcon">
                                        {this.props.loggingIn &&
                                        <span className="fa fa-spinner fa-pulse" aria-hidden="true" />
                                        }
                                        {!this.props.loggingIn &&
                                        <span>&nbsp;&nbsp;</span>
                                        }
                                    </div>
                                </div>
                            </div>
                            <div>
                                <ResetPasswordModal
                                    showResetModal={this.props.showPasswordResetModal}
                                    cancelResetModal={this.props.hideResetModal}
                                    resetPassword={(resetUsername) => {
                                        this.props.resetPassword(resetUsername);
                                    }}
                                    resettingPassword={this.props.resettingPassword}
                                />
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        );
    }
}

LoginPage.propTypes = {
    login: PropTypes.func.isRequired,
    loggingIn: PropTypes.bool.isRequired,
    errorMessage: PropTypes.string,
    showResetModal: PropTypes.func.isRequired,
    showPasswordResetModal: PropTypes.bool.isRequired,
    hideResetModal: PropTypes.func.isRequired,
    resetPassword: PropTypes.func.isRequired,
    resettingPassword: PropTypes.bool.isRequired
};

LoginPage.defaultProps = {
    errorMessage: ''
};

// Redux mappings to be used later....
const mapStateToProps = state => ({
    loggingIn: state.session.fetching,
    errorMessage: state.session.errorMessage,
    resettingPassword: state.system.resettingPassword,
    showPasswordResetModal: state.system.showPasswordResetModal
});

const mapDispatchToProps = dispatch => ({
    login: (username, password) => dispatch(login(username, password)),
    resetPassword: resetUsername => dispatch(sendPasswordResetEmail(resetUsername)),
    showResetModal: () => dispatch(showResetModal()),
    hideResetModal: () => dispatch(hideResetModal())
});

export default connect(mapStateToProps, mapDispatchToProps)(LoginPage);
