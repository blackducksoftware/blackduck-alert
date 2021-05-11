import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import PasswordInput from 'common/input/PasswordInput';
import TextInput from 'common/input/TextInput';
import SubmitButton from 'common/button/SubmitButton';
import Header from 'common/Header';
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
        this.props.login(username, password);
    }

    render() {
        return (
            <div className="wrapper">
                <div className="loginContainer">
                    <div className="loginBox">
                        <Header />
                        <form method="POST" className="form-horizontal loginForm" onSubmit={this.handleSubmit}>
                            {this.props.errorMessage
                            && (
                                <div className="alert alert-danger">
                                    <p name="configurationMessage">{this.props.errorMessage}</p>
                                </div>
                            )}

                            <TextInput
                                id="loginUsername"
                                label="Username"
                                name="username"
                                onChange={this.handleChange}
                                autoFocus
                                value={this.state.username}
                            />

                            <PasswordInput
                                id="loginPassword"
                                label="Password"
                                name="password"
                                onChange={this.handleChange}
                                value={this.state.password}
                            />
                            <div className="row">
                                <div className="col-sm-12 text-right">
                                    <SubmitButton id="loginSubmit">Login</SubmitButton>
                                    <div className="progressIcon">
                                        {this.props.loggingIn
                                        && <FontAwesomeIcon icon="spinner" className="alert-icon" size="lg" spin />}
                                        {!this.props.loggingIn
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
