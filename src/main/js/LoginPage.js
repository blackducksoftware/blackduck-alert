import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import PasswordInput from './field/input/PasswordInput';
import TextInput from './field/input/TextInput';
import SubmitButton from './field/input/SubmitButton';
import Header from './component/common/Header';
import { login } from './store/actions/session';

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
        const { hubUsername, hubPassword } = this.state;
        this.props.login(hubUsername, hubPassword);
    }

    render() {
        return (
            <div className="wrapper">
                <div className="loginContainer">
                    <div className="loginBox">
                        <Header />
                        <form method="POST" className="form-horizontal loginForm" onSubmit={this.handleSubmit}>
                            { this.props.errorMessage &&
                                <div className="alert alert-danger">
                                    <p name="configurationMessage">{this.props.errorMessage}</p>
                                </div>
                            }

                            <TextInput
                                id="login_username"
                                label="Username"
                                name="hubUsername"
                                onChange={this.handleChange}
                                errorName="usernameError"
                                autoFocus
                            />

                            <PasswordInput
                                id="login_password"
                                label="Password"
                                name="hubPassword"
                                onChange={this.handleChange}
                                errorName="passwordError"
                            />

                            <div className="row">
                                <div className="col-sm-11 text-right">
                                    { this.props.loggingIn &&
                                        <div className="progressIcon">
                                            <span className="fa fa-spinner fa-pulse" aria-hidden="true" />
                                        </div>
                                    }
                                    <SubmitButton id="login-loginButton">Login</SubmitButton>
                                </div>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        );
    }
}

LoginPage.propTypes = {
    loggingIn: PropTypes.bool.isRequired,
    errorMessage: PropTypes.string,
    login: PropTypes.func.isRequired
};

LoginPage.defaultProps = {
    errorMessage: ''
};

// Redux mappings to be used later....
const mapStateToProps = state => ({
    loggingIn: state.session.fetching,
    errorMessage: state.session.errorMessage
});

const mapDispatchToProps = dispatch => ({
    login: (username, password) => dispatch(login(username, password))
});

export default connect(mapStateToProps, mapDispatchToProps)(LoginPage);
