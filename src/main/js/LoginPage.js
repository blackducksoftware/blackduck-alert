import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import PasswordInput from 'field/input/PasswordInput';
import TextInput from 'field/input/TextInput';
import SubmitButton from 'field/input/SubmitButton';
import Header from 'component/common/Header';
import { login } from 'store/actions/session';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

class LoginPage extends Component {
    constructor(props) {
        super(props);

        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);

        this.state = {
            username: '',
            password: ''
        };
    }

    handleChange({ target }) {
        const value = target.type === 'checkbox' ? target.checked : target.value;
        this.setState({
            [target.name]: value
        });
    }

    handleSubmit(evt) {
        evt.preventDefault();
        const { username, password } = this.state;
        const { login: loginAction } = this.props;
        loginAction(username, password);
    }

    render() {
        const {
            errorMessage, loggingIn
        } = this.props;
        const {
            username, password
        } = this.state;
        return (
            <div className="wrapper">
                <div className="loginContainer">
                    <div className="loginBox">
                        <Header />
                        <form method="POST" className="form-horizontal loginForm" onSubmit={this.handleSubmit}>
                            {errorMessage
                            && (
                                <div className="alert alert-danger">
                                    <p name="configurationMessage">{errorMessage}</p>
                                </div>
                            )}

                            <TextInput
                                id="loginUsername"
                                label="Username"
                                name="username"
                                onChange={this.handleChange}
                                autoFocus
                                value={username}
                            />

                            <PasswordInput
                                id="loginPassword"
                                label="Password"
                                name="password"
                                onChange={this.handleChange}
                                value={password}
                            />
                            <div className="row">
                                <div className="col-sm-12 text-right">
                                    <SubmitButton id="loginSubmit">Login</SubmitButton>
                                    <div className="progressIcon">
                                        {loggingIn
                                        && <FontAwesomeIcon icon="spinner" className="alert-icon" size="lg" spin />}
                                        {!loggingIn
                                        && <span>&nbsp;&nbsp;</span>}
                                    </div>
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
    login: PropTypes.func.isRequired,
    loggingIn: PropTypes.bool.isRequired,
    errorMessage: PropTypes.string
};

LoginPage.defaultProps = {
    errorMessage: ''
};

// Redux mappings to be used later....
const mapStateToProps = (state) => ({
    loggingIn: state.session.fetching,
    errorMessage: state.session.errorMessage
});

const mapDispatchToProps = (dispatch) => ({
    login: (username, password) => dispatch(login(username, password))
});

export default connect(mapStateToProps, mapDispatchToProps)(LoginPage);
