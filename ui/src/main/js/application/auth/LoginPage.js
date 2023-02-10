import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { useDispatch, useSelector } from 'react-redux';
import PasswordInput from 'common/component/input/PasswordInput';
import TextInput from 'common/component/input/TextInput';
import SubmitButton from 'common/component/button/SubmitButton';
import Header from 'common/component/Header';
import { login } from 'store/actions/session';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import SamlLogin from './SamlLogin';

const LoginPage = () => {
    const dispatch = useDispatch();
    const { errorMessage, fetching, samlEnabled } = useSelector((state) => state.session);
    const [loginForm, setLoginForm] = useState({
        username: '',
        password: ''
    })

    function handleChange({ target }) {
        const { name, value } = target;
        setLoginForm({...loginForm, [name]: value});
    }

    function handleSubmit(evt) {
        evt.preventDefault();
        dispatch(login(loginForm.username,loginForm.password));
    }

    return (
        <div className="wrapper">
            <div className="loginContainer">
                <div className="loginBox">
                    <Header />
                    <form method="POST" className="form-horizontal loginForm" onSubmit={handleSubmit}>
                        { errorMessage && (
                            <div className="alert alert-danger">
                                <p name="configurationMessage">{errorMessage}</p>
                            </div>
                        )}

                        <TextInput
                            id="loginUsername"
                            label="Username"
                            name="username"
                            onChange={handleChange}
                            autoFocus
                            value={loginForm.username}
                            readOnly={false}
                        />

                        <PasswordInput
                            id="loginPassword"
                            label="Password"
                            name="password"
                            onChange={handleChange}
                            value={loginForm.password}
                        />
                        <div className="row">
                            <div className="col-sm-12 text-right">
                                <SubmitButton id="loginSubmit">Login</SubmitButton>
                                <div className="progressIcon">
                                    {fetching && (
                                        <FontAwesomeIcon icon="spinner" className="alert-icon" size="lg" spin />
                                    )}
                                    {!fetching && (
                                        <span>&nbsp;&nbsp;</span>
                                    )}
                                </div>
                            </div>
                        </div>
                    </form>
                    {samlEnabled && (
                        <SamlLogin />
                    )}
                </div>
            </div>
        </div>
    );
}

export default LoginPage;
